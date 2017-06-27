package com.yjkj.test;

import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.Endpoint;

import org.quartz.SchedulerException;

import com.xfsy.entity.Config;
import com.xfsy.entity.Return;
import com.xfsy.entity.SMS;
import com.xfsy.util.ConfigUtil;
import com.xfsy.util.Constant;
import com.xfsy.util.DBUtil;
import com.xfsy.util.Utils;
import com.yjkj.time.TimeTask;

@WebService
public class API {
	Map<String, Client> map = new HashMap<>();
	Map<String, Thread> mapThread = new HashMap<>();
	// List<Config> configList = DBUtil.getAllConfig();
	List<Config> configList = new ArrayList<>();
	List<Config> configCustomer = new ArrayList<>();
	Client client;
	Thread thread;
	static API api = new API();

	// 查询所有短信网关配置
	public API() {
		configList = DBUtil.getAllConfig();
		for (int i = 0; i < configList.size(); i++) {
			Config config = configList.get(i);
			String spid = config.getSpId();
			String password = config.getSharedSecret();
			String msgsrc = config.getMsgSrc();
			String serviceId = config.getServiceId();
			String customer = config.getSc_to_customer();
			String spCode = config.getSpCode();
			client = map.put("client" + customer,
					new Client(Constant.ISMP_IP, Constant.ISMP_POST, spid, password, msgsrc, serviceId, spCode));
			mapThread.put("thread" + customer, new Thread(client));
		}
	}

	/**
	 * 开启指定客户的短信服务
	 * 
	 * @param customer_Id
	 * @return
	 */
	@WebMethod
	@WebResult(name = "return")
	public Return startCustomer(@WebParam(name = "customer_Id") String customer_Id) {
		Return rtn = new Return();
		boolean status = false; // 请求状态
		boolean data = false; // 服务数据
		String error = null; // 请求错误
		String fail = null; // 服务错误
		if (customer_Id != null && !customer_Id.equals("")) {
			// 查询是否存在
			boolean isCustomer = DBUtil.isCustomer(customer_Id);
			if (isCustomer) {
				if (!statusJA(customer_Id)) {
					status = true;
					// 获取配置信息
					configCustomer = DBUtil.getConfigByCustomer(customer_Id);
					Config config = configCustomer.get(0);
					String spid = config.getSpId();
					String password = config.getSharedSecret();
					String msgsrc = config.getMsgSrc();
					String serviceId = config.getServiceId();
					String spCode = config.getSpCode();
					// 测试连接，如果不成功则返回
					boolean test_conn = testConnJA(client.getSpid(), client.getPassword(), client.getMsgsrc(),
							client.getServiceId(), client.getServicenumber());
					if (test_conn) {
						map.put("client" + customer_Id, new Client(Constant.ISMP_IP, Constant.ISMP_POST, spid, password,
								msgsrc, serviceId, spCode));
						client = map.get("client" + customer_Id);
						// client.isrunning = true; //
						mapThread.put("thread" + customer_Id, new Thread(client));
						thread = mapThread.get("thread" + customer_Id);
						try {
							client.start();
							thread.start();
							data = true;
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						//
					}
				} else {
					error = "服务正在运行中···";
				}

			} else {
				error = Constant.ERROR_NOFONT;
			}
		} else {
			error = Constant.ERROR_NULL;
		}
		rtn.setStatus(status);
		rtn.setData(data);
		rtn.setError(error);
		return rtn;
	}

	/**
	 * 关闭指定客户的短信服务
	 * 
	 * @param customer_Id
	 * @return
	 */
	@WebMethod
	@WebResult(name = "return")
	public Return stopCustomer(@WebParam(name = "customer_Id") String customer_Id) {
		Return rtn = new Return();
		boolean status = false; // 请求状态
		boolean data = false; // 服务数据
		String error = null; // 请求错误
		String fail = null; // 服务错误
		if (customer_Id != null && !customer_Id.equals("")) {
			// 查询是否存在
			boolean isCustomer = DBUtil.isCustomer(customer_Id);
			if (isCustomer) {
				status = true;
				thread = mapThread.get("thread" + customer_Id);
				// 关闭线程
				client = map.get("client" + customer_Id);
				client.stop();
				try {
					thread.join(5 * 1000); // 等候5s
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				data = true;
			} else {
				error = Constant.ERROR_NOFONT;
			}
		} else {
			error = Constant.ERROR_NULL;
		}
		rtn.setStatus(status);
		rtn.setData(data);
		rtn.setError(error);
		return rtn;
	}

	@WebMethod
	@WebResult(name = "return")
	public Return testConn(@WebParam(name = "customer_Id") String customer_Id) {
		Return rtn = new Return();
		boolean status = false; // 请求状态
		boolean data = false; // 服务数据
		String error = null; // 请求错误
		String fail = null; // 服务错误
		if (customer_Id != null && !customer_Id.equals("")) {
			// 查询是否存在
			boolean isCustomer = DBUtil.isCustomer(customer_Id);
			if (isCustomer) {
				status = true;
				int errorCode = 0;
				configCustomer = DBUtil.getConfigByCustomer(customer_Id);
				Config config = configCustomer.get(0);
				String spid = config.getSpId();
				String password = config.getSharedSecret();
				String msgsrc = config.getMsgSrc();
				String serviceId = config.getServiceId();
				String spCode = config.getSpCode();
				client = new Client(Constant.ISMP_IP, Constant.ISMP_POST, spid, password, msgsrc, serviceId, spCode);
				// 连接
				boolean isConn = client.conn();
				if (isConn) {
					// 登录
					boolean isLogin = client.login();
					if (isLogin) {
						// 测试
						boolean isTest = client.test();
						if (isTest) {
							data = true;
						} else {
							errorCode = 3; // 测试失败
						}
					} else {
						errorCode = 2; // 网关登录失败
					}
				} else {
					errorCode = 1; // 服务器连接失败
				}
				// 关闭
				client.closeSocket();
				// 结果
				if (errorCode != 0) {
					switch (errorCode) {
					case 1:
						fail = "(" + errorCode + ")服务器连接失败，请仔细核对服务器ip和端口号";
						break;

					case 2:
						fail = "(" + errorCode + ")网关登录失败，可能出现错误的原因：业务网关登录用户名、密码，企业代码，业务代码，短信接入码";
						break;

					case 3:
						fail = "(" + errorCode + ")测试链路失败";
						break;

					default:
						fail = "(" + errorCode + ")其他错误~";
						break;
					}
				}
			} else {
				error = Constant.ERROR_NOFONT;
			}
		} else {
			error = Constant.ERROR_NULL;
		}
		rtn.setStatus(status);
		rtn.setData(data);
		rtn.setError(error);
		return rtn;
	}

	/**
	 * 测试发送短信
	 * 
	 * @param customer_Id
	 *            客户编号
	 * @param phone
	 *            手机号
	 * @param msg
	 *            短信内容
	 * @param sequence
	 *            序列号
	 * @return
	 */
	@WebMethod
	@WebResult(name = "return")
	public Return sendMsgTest(@WebParam(name = "customer_Id") String customer_Id,
			@WebParam(name = "phone") String phone, @WebParam(name = "msg") String msg,
			@WebParam(name = "sequence") int sequence) {
		Return rtn = new Return();
		boolean status = false; // 请求状态
		boolean data = false; // 服务数据
		String error = null; // 请求错误
		String fail = null; // 服务错误
		if (customer_Id != null && !customer_Id.equals("")) {
			// 查询是否存在
			boolean isCustomer = DBUtil.isCustomer(customer_Id);
			if (isCustomer) {
				status = true;
				Client client = map.get("client" + customer_Id);
				try {
					boolean rs_status = false;
					int msgLen = 0;
					boolean charResult = Util.isContainChinese(msg);
					if (charResult) {
						msgLen = msg.getBytes("UnicodeBigUnmarked").length;
					} else {
						msgLen = msg.getBytes("UTF-8").length;
					}
					if (msgLen < 127) {
						rs_status = client.send(phone, msg, sequence);
						if (rs_status) {
							data = true;
						}
					} else {
						rs_status = client.sendLongMsg(phone, msg, sequence);
						if (rs_status) {
							data = true;
						}
					}
				} catch (UnsupportedEncodingException e) {
					fail = e.getMessage();
				}
			} else {
				error = Constant.ERROR_NOFONT;
			}
		} else {
			error = Constant.ERROR_NULL;
		}
		rtn.setStatus(status);
		rtn.setData(data);
		rtn.setError(error);
		return rtn;
	}

	/**
	 * 发送任务
	 * 
	 * @return
	 */
	@WebMethod
	@WebResult(name = "return")
	public Return sendTask() {
		Return rtn = new Return();
		boolean status = false; // 请求状态
		boolean data = false; // 服务数据
		String error = null; // 请求错误
		String fail = null; // 服务错误
		// ---------------------------------------
		status = true;
		boolean rs_status = doTask();
		if (rs_status) {
			data = true;
		}
		// --------------------------------------
		rtn.setStatus(status);
		rtn.setData(data);
		rtn.setError(error);
		return rtn;
	}

	/**
	 * 检测指定客户短信服务状态
	 * 
	 * @param customer_Id
	 * @return
	 */
	@WebMethod
	@WebResult(name = "return")
	public Return status(@WebParam(name = "customer_Id") String customer_Id) {
		Return rtn = new Return();
		boolean status = false; // 请求状态
		boolean data = false; // 服务数据
		String error = null; // 请求错误
		String fail = null; // 服务错误
		if (customer_Id != null && !customer_Id.equals("")) {
			// 查询是否存在
			boolean isCustomer = DBUtil.isCustomer(customer_Id);
			if (isCustomer) {
				status = true;
				client = map.get("client" + customer_Id);
				// socket
				thread = mapThread.get("thread" + customer_Id);
				// boolean rs_status = client.isConned();
				try {
					boolean rs_status = thread.isAlive();
					if (rs_status) {
						data = true;
					}
				} catch (Exception e) {

				}
			} else {
				error = Constant.ERROR_NOFONT;
			}
		} else {
			error = Constant.ERROR_NULL;
		}
		rtn.setStatus(status);
		rtn.setData(data);
		rtn.setError(error);
		return rtn;
	}

	/**
	 * 开启全部客户短信服务
	 * 
	 * @return
	 */
	@WebMethod
	@WebResult(name = "return")
	public Return start() {
		Return rtn = new Return();
		boolean status = false; // 请求状态
		boolean data = false; // 服务数据
		String error = null; // 请求错误
		String fail = ""; // 服务错误

		// ----------------------------------------------
		status = true;
		for (int i = 0; i < map.size(); i++) {
			Config config = configList.get(i);
			String customer_Id = config.getSc_to_customer();
			client = map.get("client" + customer_Id);
			// 测试连接，如果不成功则返回
			boolean test_conn = testConnJA(client.getSpid(), client.getPassword(), client.getMsgsrc(),
					client.getServiceId(), client.getServicenumber());
			if (test_conn) {
				thread = mapThread.get("thread" + customer_Id);
				client.start();
				thread.start();
			} else {
				fail += customer_Id + ",";
			}
		}
		System.out.println("[ " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + " ]" + fail);
		data = true;
		// ------------------------------------------------
		rtn.setStatus(status);
		rtn.setData(data);
		rtn.setError(error);
		return rtn;
	}

	/**
	 * 关闭全部客户短信服务
	 * 
	 * @return
	 */
	@WebMethod
	@WebResult(name = "return")
	public Return close() {
		Return rtn = new Return();
		boolean status = false; // 请求状态
		boolean data = false; // 服务数据
		String error = null; // 请求错误
		String fail = null; // 服务错误
		// -----------------------------------------------
		status = true;
		for (int i = 0; i < configList.size(); i++) {
			Config config = configList.get(i);
			String customer_Id = config.getSc_to_customer();
			thread = mapThread.get("thread" + customer_Id);
			// 关闭线程
			client = map.get("client" + customer_Id);
			client.stop();
			try {
				thread.join(10 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		data = true;
		// ---------------------------------------------------
		rtn.setStatus(status);
		rtn.setData(data);
		rtn.setError(error);
		return rtn;
	}

	/**
	 * 发送定时短信
	 * 
	 * @return
	 */
	@WebMethod
	@WebResult(name = "return")
	public Return sendTimeTask() {
		Return rtn = new Return();
		boolean status = false; // 请求状态
		boolean data = false; // 服务数据
		String error = null; // 请求错误
		String fail = null; // 服务错误
		// ---------------------------------------
		status = true;
		boolean rs_status = doTimeTask();
		if (rs_status) {
			data = true;
		}
		// --------------------------------------
		rtn.setStatus(status);
		rtn.setData(data);
		rtn.setError(error);
		return rtn;
	}
	
	/**************************以下方法仅开发者使用*******************************/
	/**
	 * 查询当前所有定时任务
	 * @return
	 * @throws SchedulerException
	 */
	@WebMethod
	@WebResult(name = "return")
	public List<String> timeTaskAll() throws SchedulerException {
		
		return com.yjkj.time.Util.timeTaskAll();
	}
	
	
	
	
	/**************************以下方法仅内部使用*******************************/

	/**
	 * 测试连接短信服务器接口
	 * 
	 * @param ip
	 *            服务器ip
	 * @param port
	 *            端口号
	 * @param spid
	 *            业务网关登录用户名
	 * @param password
	 *            业务网关登录密码
	 * @param msgsrc
	 *            企业代码
	 * @param serviceId
	 *            业务代码
	 * @param servicenumber
	 *            短信接入码
	 * 
	 * @return result[] result[0] status 测试结果，成功/失败 result[1] errorCode 错误代码
	 *         result[2] error 错误原因
	 */
	public boolean testConnJA(String spid, String password, String msgSrc, String serviceId, String spCode) {
		boolean status = false;
		client = new Client(Constant.ISMP_IP, Constant.ISMP_POST, spid, password, msgSrc, serviceId, spCode);
		// 连接
		boolean isConn = client.conn();
		if (isConn) {
			// 登录
			boolean isLogin = client.login();
			if (isLogin) {
				// 测试
				boolean isTest = client.test();
				if (isTest) {
					status = true;
				}
			}
		}
		// 关闭
		client.closeSocket();
		return status;
	}

	// 做任务
	public static boolean doTask() {
		String customer;
		String company;
		String phone;
		String msg;
		String st_id;
		// 查询任务
		List<SMS> smsList = DBUtil.getTask();

		// 处理数据
		for (SMS sms : smsList) {
			customer = sms.getCustomer();
			company = sms.getCompany();
			phone = sms.getPhone();
			msg = sms.getMsg();
			st_id = sms.getSt_id();

			// do something
			String[] phones = Utils.getPhones(phone, ",");
			for (int i = 0; i < phones.length; i++) {
				// 判断该号码是否存在
				boolean isNum = DBUtil.isNum(phones[i]);
				if (isNum) {
					// 发送短信
					int sequence = Integer.parseInt(Util.readSequence());
					if (sequence > Integer.MAX_VALUE) {
						Util.writeSequence("1");
						sequence = 1;
					}
					boolean send_status = api.send(customer, phones[i], msg, sequence);
					// 发送结果处理
					if (send_status) {
						String[] send_success = { phones[i], msg, sequence + "", "1", customer, company, st_id, null };
						DBUtil.sendNumber(send_success);
					} else {
						String[] send_fail = { phones[i], msg, sequence + "", "0", customer, company,
								st_id, null };
						DBUtil.sendNumber(send_fail);
					}
					// ···
					Util.writeSequence(++sequence + "");
				} else {
					String[] send_fail = { phones[i], msg, "", "9", customer, company, st_id, "号码不正确" };
					DBUtil.sendNumber(send_fail);
				}
			}
		}
		// 返回
		return true;
	}

	// 做任务
	public static boolean doTimeTask() {
		boolean result = false;
		String customer;
		String company;
		String phone;
		String msg;
		String st_id;
		String time;
		// 查询定时任务
		List<SMS> smsList = DBUtil.getTimeTask();

		// 处理数据
		for (SMS sms : smsList) {
			customer = sms.getCustomer();
			company = sms.getCompany();
			phone = sms.getPhone();
			msg = sms.getMsg();
			st_id = sms.getSt_id();
			time = sms.getTime();
			// 对时间的处理
			SimpleDateFormat format =  new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
			long timeSpamp;
			try {
				timeSpamp = format.parse(time).getTime();
				// 年
				String year = new SimpleDateFormat("yyyy").format(timeSpamp);
				String month = new SimpleDateFormat("MM").format(timeSpamp);
				String day = new SimpleDateFormat("dd").format(timeSpamp);
				String hour = new SimpleDateFormat("HH").format(timeSpamp);
				String minute = new SimpleDateFormat("mm").format(timeSpamp);
				String second = new SimpleDateFormat("ss").format(timeSpamp);
				String cron = second + " " + minute + " " + hour + " " + day + " " + month + " ? " + year;
				System.out.println(cron);
				TimeTask timeTask = new TimeTask();
				timeTask.start(company, customer, cron, phone, msg, st_id);
				result = true;
			} catch (ParseException e1) {
			} catch (SchedulerException e) {
			}
		}
		// 返回
		return result;
	}

	/**
	 * 定时发送数据
	 * 
	 * @param phone
	 * @param customer
	 * @param msg
	 * @param company
	 * @param st_id
	 */
	public static void sendTimeData(String phone, String customer, String msg, String company, String st_id) {
		// do something
		String[] phones = Utils.getPhones(phone, ",");
		for (int i = 0; i < phones.length; i++) {
			// 判断该号码是否存在
			boolean isNum = DBUtil.isNum(phones[i]);
			if (isNum) {
				// 发送短信
				int sequence = Integer.parseInt(Util.readSequence());
				if (sequence > Integer.MAX_VALUE) {
					Util.writeSequence("1");
					sequence = 1;
				}
				boolean send_status = api.send(customer, phones[i], msg, sequence);
				// 发送结果处理
				if (send_status) {
					String[] send_success = { phones[i], msg, sequence + "", "1", customer, company, st_id, null };
					DBUtil.sendNumber(send_success);
				} else {
					String[] send_fail = { phones[i], msg, sequence + "", "0", customer, company, st_id,
							null };
					DBUtil.sendNumber(send_fail);
				}
				// ···
				Util.writeSequence(++sequence + "");
			} else {
				String[] send_fail = { phones[i], msg, "", "9", customer, company, st_id, "号码不正确" };
				DBUtil.sendNumber(send_fail);
			}
		}
		// 定时任务完成
		try {
			DBUtil.updateTimeTask(st_id);
		} catch (SQLException e) {

		}
	}

	//发送
	public boolean send(String customer, String phone, String msg, int sequence) {
		Client client = map.get("client" + customer);
		boolean result = false;
		int msgLen = 0;
		boolean charResult = Util.isContainChinese(msg);
		if (charResult) {
			try {
				msgLen = msg.getBytes("UnicodeBigUnmarked").length;
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				msgLen = msg.getBytes("UTF-8").length;
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (msgLen < 126) {
			result = client.send(phone, msg, sequence);
		} else {
			result = client.sendLongMsg(phone, msg, sequence);
		}
		return result;
	}

	public boolean statusJA(String customer_Id) {
		boolean status = false;
		if (customer_Id != null && !customer_Id.equals("")) {
			// 查询是否存在
			boolean isCustomer = DBUtil.isCustomer(customer_Id);
			if (isCustomer) {
				client = map.get("client" + customer_Id);
				// socket
				thread = mapThread.get("thread" + customer_Id);
				// boolean rs_status = client.isConned();
				try {
					boolean rs_status = thread.isAlive();
					if (rs_status) {
						status = true;
					}
				} catch (Exception e) {

				}
			} else {

			}
		} else {

		}
		return status;
	}
	
	// 开启webservice
	public static void main(String[] args) {
		System.out.println("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + "] 程序正在启动中···");
		String address = "http://" + ConfigUtil.getURL() + "/cmpp/v2";
		Endpoint.publish(address, api);
		// api.start();
	}
}
