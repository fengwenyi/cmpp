package com.xfsy.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.xfsy.entity.Config;
import com.xfsy.entity.SMS;

/**
 * 数据库： cmp
 * 
 * @author Administrator
 *
 */
public class DBUtil {
	private static Logger logger = Logger.getLogger(DBUtil.class);

	/**
	 * getDBConn 链接数据库
	 * 
	 * @return
	 */
	public static Connection getDBConn() {
		Connection conn = null;
		String driver = "com.mysql.jdbc.Driver";
		String url = "jdbc:mysql://" + ConfigUtil.getHost() + "/" + ConfigUtil.getDBName()
				+ "?useUnicode=true&characterEncoding=UTF-8";
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, ConfigUtil.getUsername(), ConfigUtil.getPassword());
		} catch (ClassNotFoundException e) {
			logger.error("mysql 驱动错误，错误：" + e.getMessage());
		} catch (SQLException e) {
			logger.error("mysql 连接错误，请检查MySQL配置信息，错误：" + e.getMessage());
		}
		return conn;
	}

	/**
	 * 查询即时发送任务数据
	 * 
	 * @return
	 */
	public static List<SMS> getTask() {
		List<SMS> smsList = new ArrayList<>();
		Connection conn = getDBConn();
		// 1. 指定用户发送
		String sql = "select st_id, st_number, st_content,st_way, st_time_send, st_to_customer, st_to_company from sms_task where st_type = 0 and st_status = 0 and st_way = 0";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String st_id = rs.getString("st_id");
				String st_number = rs.getString("st_number");
				String st_content = rs.getString("st_content");
				String st_to_customer = rs.getString("st_to_customer");
				String st_to_company = rs.getString("st_to_company");
				SMS sms = new SMS(st_id, st_number, st_content, st_to_customer, st_to_company);
				smsList.add(sms);
				// 任务完成
				sql = "update sms_task set st_time_task = ?, st_status = ? where st_id = " + st_id;
				ps = conn.prepareStatement(sql);
				ps.setString(1, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
				ps.setString(2, "1");
				int rs_num = ps.executeUpdate();
				if (rs_num < 0 || rs_num == 0) {
					logger.info("任务完成写入失败，时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
							+ ", 任务编号：" + st_id);
				}
			}
		} catch (SQLException e) {
			logger.error("getTask()->1->" + e.getMessage());
		}

		// 2. 分组发送
		sql = "select st_id, st_to_sim_group, st_content, st_way, st_time_send, st_to_customer, st_to_company from sms_task where st_type = 1 and st_status = 0 and st_way = 0";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String st_id = rs.getString("st_id");
				String st_to_sim_group = rs.getString("st_to_sim_group");
				String st_to_customer = rs.getString("st_to_customer");
				String st_to_company = rs.getString("st_to_company");
				String st_content = rs.getString("st_content");
				sql = "select sb_number from sim_base where sb_is_blacklist = 0 and sb_to_sim_group = "
						+ st_to_sim_group;
				ps = conn.prepareStatement(sql);
				ResultSet rs_group = ps.executeQuery();
				while (rs_group.next()) {
					String st_number = rs_group.getString("sb_number");
					SMS sms = new SMS(st_id, st_number, st_content, st_to_customer, st_to_company);
					smsList.add(sms);
				}

				// 任务完成
				sql = "update sms_task set st_time_task = ?, st_status = ? where st_id = " + st_id;
				ps = conn.prepareStatement(sql);
				ps.setString(1, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
				ps.setString(2, "1");
				int rs_num = ps.executeUpdate();
				if (rs_num < 0 || rs_num == 0) {
					logger.info("任务完成写入失败，时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
							+ ", 任务编号：" + st_id);
				}
			}
		} catch (SQLException e) {
			logger.error("getTask()->1->" + e.getMessage());
		}

		// 3.全部号码
		sql = "select st_id, st_content, st_way, st_time_send, st_to_customer, st_to_company from sms_task where st_type = 3 and st_status = 0 and st_way = 0";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String st_id = rs.getString("st_id");
				String st_to_customer = rs.getString("st_to_customer");
				String st_to_company = rs.getString("st_to_company");
				String st_content = rs.getString("st_content");
				// 立即发送
				sql = "selecr sb_number where sb_is_blacklist = 0 and st_to_customer = " + st_to_customer;
				ps = conn.prepareStatement(sql);
				ResultSet rs_group = ps.executeQuery();
				while (rs_group.next()) {
					String st_number = rs_group.getString("sb_number");
					SMS sms = new SMS(st_id, st_number, st_content, st_to_customer, st_to_company);
					smsList.add(sms);
				}
				// 任务完成
				sql = "update sms_task set st_time_task = ?, st_status = ? where st_id = " + st_id;
				ps = conn.prepareStatement(sql);
				ps.setString(1, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
				ps.setString(2, "1");
				int rs_num = ps.executeUpdate();
				if (rs_num < 0 || rs_num == 0) {
					logger.info("任务完成写入失败，时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
							+ ", 任务编号：" + st_id);
				}
			}
		} catch (SQLException e) {
			logger.error("getTask()->1->" + e.getMessage());
		}
		try {
			conn.close();
		} catch (SQLException e) {
		}
		return smsList;
	}

	/*
	 * id， 号码 内容 发送方式 发送时间 select st_id, st_number, st_content,st_way,
	 * st_time_send from sms_task where st_type = 0 and st_status = 0;
	 * 
	 */

	// 写入发送号码
	/*
	 * String[] data; //电话号码phone, 信息内容msg, 发送短些序列号sequence, 发送状态status,
	 * 对应客户customer, 对应任务编号task data[0] phone data[1] msg data[2] sequence
	 * data[3] status = 0 data[4] customer data[5] task data[6] error
	 */
	/*public static boolean sendNumber(List<String[]> lists) {
		Connection conn = getDBConn();
		StringBuffer sb = new StringBuffer();
		String sql = "insert into sms_send(ss_number, ss_content, ss_sequence, ss_status, ss_to_customer, ss_to_company, ss_to_sms_task, ss_error) values";
		sb.append(sql);
		boolean result = false;
		try {
			for (int i = 0; i < lists.size(); i++) {
				String[] strings = lists.get(i);
				String str = "(" + strings[0] + ", " + strings[1] + ", " + strings[2] + ", 0, " + strings[4] + ", " + strings[5] + ", " + strings[6] + ", " + strings[7] + ")";
				sb.append(str);
				if (i < (lists.size() - 1)) {
					sb.append(", ");
				}
			}
			PreparedStatement ps = conn.prepareStatement(sb.toString());
			int rs_num = ps.executeUpdate();
			if (rs_num > 0) {
				result = true;
			} else {
				logger.info("发送数据写入失败，时间：" + sb.toString());
			}
		} catch (SQLException e) {
		}
		try {
			conn.close();
		} catch (SQLException e) {
		}
		return result;
	}*/
	public static boolean sendNumber(String[] data) {
		Connection conn = getDBConn();
		String sql = "insert into sms_send(ss_number, ss_content, ss_sequence, ss_status, ss_to_customer, ss_to_company, ss_to_sms_task, ss_error) values(?, ?, ?, ?, ?, ?, ?, ?)";
		boolean result = false;
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, data[0]);
			ps.setString(2, data[1]);
			ps.setString(3, data[2]);
			ps.setInt(4, 0);
			ps.setString(5, data[4]);
			ps.setString(6, data[5]);
			ps.setString(7, data[6]);
			ps.setString(8, data[7]);
			int rs_num = ps.executeUpdate();
			if (rs_num > 0) {
				result = true;
			} else {
				logger.info("发送数据写入失败，时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + ", 数据：");
				logger.info(data[0]);
				logger.info(data[1]);
				logger.info(data[2]);
				logger.info(data[3]);
				logger.info(data[4]);
				logger.info(data[5]);
				logger.info(
						"----------------------------------------------------------------------------------------------");
			}
		} catch (SQLException e) {
		}
		try {
			conn.close();
		} catch (SQLException e) {
		}
		return result;
	}

	// 完成任务，修改完成任务时间

	// sp发送完：
	// 修改发送状态
	// 自己定义序列号，存放到本地本件中，来区别
	/*
	 * String[] data; data[0] ss_status data[1] ss_error data[2] ss_sequence
	 */
	public static boolean updateNumber(String[] data) {
		Connection conn = getDBConn();
		String sql;
		boolean result = false;
		if (data[0].equals("0")) {
			sql = "update sms_send set ss_status = 1, ss_sequence=null where ss_status = 0 and ss_sequence = "
					+ data[2];
		} else {
			sql = "update sms_send set ss_status = 9, ss_sequence=null, ss_error = '" + data[1]
					+ "' where ss_status = 0 and ss_sequence = " + data[2];
		}
		try {
			conn.setAutoCommit(false);
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.executeUpdate();
			conn.commit();
			result = true;
			/*if (rs_num > 0) {
				result = true;
			} else {
				logger.info("短信发送应答写入失败:" + ps);
			}*/
		} catch (SQLException e) {
			logger.error("updateNumber:" + e.getMessage());
			try {
				conn.rollback();
			} catch (Exception e2) {
				// TODO: handle exception
			}
			
		}
		try {
			conn.close();
		} catch (SQLException e) {
		}
		return result;
	}

	// 读取config-all
	public static List<Config> getAllConfig() {
		String sql = "select * from sms_config";
		Connection conn = getDBConn();
		List<Config> configList = new ArrayList<>();
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String sc_to_customer = rs.getString("sc_to_customer"); // 客户编号
				String sc_name = rs.getString("sc_name"); // 网关登录用户名
				String sc_passwd = rs.getString("sc_passwd"); // 网关登录密码
				String sc_access_code = rs.getString("sc_access_code"); // 接入码
				String sc_company_code = rs.getString("sc_company_code"); // 企业代码
				String sc_service_code = rs.getString("sc_service_code"); // 业务代码
				Config config = new Config(sc_to_customer, sc_name, sc_passwd, sc_access_code, sc_company_code,
						sc_service_code);
				configList.add(config);
			}
		} catch (SQLException e) {
			logger.error("读取 Config-all 出错：" + e.getMessage());
		}
		try {
			conn.close();
		} catch (SQLException e) {
		}
		return configList;
	}

	// 通过用户编号读取配置信息
	public static List<Config> getConfigByCustomer(String st_to_customer) {
		String sql = "select * from sms_config where sc_to_customer = ?";
		Connection conn = getDBConn();
		List<Config> configList = new ArrayList<>();
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, st_to_customer);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				// String sc_to_customer = rs.getString("sc_to_customer"); //
				// 客户编号
				String sc_name = rs.getString("sc_name"); // 网关登录用户名
				String sc_passwd = rs.getString("sc_passwd"); // 网关登录密码
				String sc_access_code = rs.getString("sc_access_code"); // 接入码
				String sc_company_code = rs.getString("sc_company_code"); // 企业代码
				String sc_service_code = rs.getString("sc_service_code"); // 业务代码
				Config config = new Config(st_to_customer, sc_name, sc_passwd, sc_access_code, sc_company_code,
						sc_service_code);
				configList.add(config);
			}
		} catch (SQLException e) {
			logger.error("读取 Config 出错：" + e.getMessage());
		}
		try {
			conn.close();
		} catch (SQLException e) {
		}
		return configList;
	}

	// 接收短信入库
	public static boolean delinerSMS(String phone, String msg) {
		boolean result = false;
		// 查询号码对应客户编号
		String sql = "select sb_to_customer, sb_to_company from sim_base where sb_number = ?";
		Connection conn = getDBConn();
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, phone);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String sb_to_customer = rs.getString("sb_to_customer");
				String sb_to_company = rs.getString("sb_to_company");
				sql = "insert into sms_receive(sr_number, sr_content, sr_to_customer, sr_to_company) values(?, ?, ?, ?)";
				ps = conn.prepareStatement(sql);
				ps.setString(1, phone);
				ps.setString(2, msg);
				ps.setString(3, sb_to_customer);
				ps.setString(4, sb_to_company);
				int rs_num = ps.executeUpdate();
				if (rs_num < 0 || rs_num == 0) {
					logger.error("接收用户短信入库出错，数据：" + phone + "," + msg + "," + sb_to_customer);
				} else {
					return true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
		}
		return result;
	}
	
	// 接收长短信入库
	public static boolean delinerLongSMS(String phone, String msg, byte total, byte num, byte sequence) {
		boolean result = false;
		// 查询号码对应客户编号
		String sql = "select sb_to_customer, sb_to_company from sim_base where sb_number = ?";
		Connection conn = getDBConn();
		PreparedStatement ps;
		try {
			ps = conn.prepareStatement(sql);
			ps.setString(1, phone);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String sb_to_customer = rs.getString("sb_to_customer");
				String sb_to_company = rs.getString("sb_to_company");
				if (num == 1) {
					sql = "insert into sms_receive(sr_number, sr_content, sr_to_customer, sr_to_company, sr_long, sr_sequence) values(?, ?, ?, ?, ?, ?)";
					ps = conn.prepareStatement(sql);
					ps.setString(1, phone);
					ps.setString(2, msg);
					ps.setString(3, sb_to_customer);
					ps.setString(4, sb_to_company);
					ps.setString(5, "1");
					ps.setString(6, sequence + "");
					int rs_num = ps.executeUpdate();
					if (rs_num < 0 || rs_num == 0) {
						logger.error("接收用户短信入库出错，数据：" + phone + "," + msg + "," + sb_to_customer);
					} else {
						return true;
					}
				} else {
					sql = "select * from sms_receive where sr_sequence = ?";
					ps = conn.prepareStatement(sql);
					ps.setString(1, sequence + "");
					ResultSet rs1 = ps.executeQuery();
					while (rs1.next()) {
						String content = rs1.getString("sr_content");
						if (num == total) {
							//查询
							//拼接
							content = "{" + content + msg + "}";
							int error = 1;
							try {
								JsonObject object = new JsonParser().parse(content).getAsJsonObject();
								String msgContent = "";
								for (int i = 1; i <= total; i++) {
									msgContent += object.get(i + "").getAsString();
								}
								//入库
								sql = "update sms_receive set sr_content = ?, sr_sequence = null where sr_sequence = ?";
								ps = conn.prepareStatement(sql);
								ps.setString(1, msgContent);
								ps.setString(2, sequence + "");
								ps.executeUpdate();
								error = 0;
							} catch (Exception e) {
								// TODO: handle exception
							}
							//出错
							if (error == 1) {
								sql = "update sms_receive set sr_error = ?, sr_sequence = null where sr_sequence = ?";
								ps = conn.prepareStatement(sql);
								ps.setString(1, error + "");
								ps.setString(2, sequence + "");
								ps.executeUpdate();
							}
						} else {
							//2~(total-1)
							sql = "update sms_receive set sr_content = ? where sr_sequence = ?";
							ps = conn.prepareStatement(sql);
							ps.setString(1, content + msg);
							ps.setString(2, sequence + "");
							ps.executeUpdate();
						}
					}
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {
			conn.close();
		} catch (SQLException e) {
		}
		return result;
	}

	// 判断号码是否存在该号码
	public static boolean isNum(String phone) {
		boolean result = false;
		Connection conn = getDBConn();
		String sql = "select count(*) from sim_base where sb_number = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, phone);
			ResultSet rs = ps.executeQuery();
			if (rs != null) {
				result = true;
			}
		} catch (SQLException e) {
			logger.error("isNum:" + e.getMessage());
		}
		try {
			conn.close();
		} catch (SQLException e) {
		}
		return result;
	}

	// 判断该客户编号的网关是否配置
	public static boolean isCustomer(String customer_Id) {
		boolean result = false;
		Connection conn = getDBConn();
		String sql = "select count(*) from sms_config where sc_to_customer = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, customer_Id);
			ResultSet rs = ps.executeQuery();
			if (rs != null) {
				result = true;
			}
		} catch (SQLException e) {
			logger.error("isCustomer:" + e.getMessage());
		}
		try {
			conn.close();
		} catch (SQLException e) {
		}
		return result;
	}

	/**
	 * 链路检测时间(初始化)
	 * 
	 * @param spId
	 * @param time
	 */
	public static void insertActiverTest(String spId, long time) {
		Connection conn = getDBConn();
		// 先查询一次，如果不存在就写入（初始化）
		String sql = "select count(*) from check_line where cl_spid = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, spId);
			ResultSet rs = ps.executeQuery();
			if (rs == null) {
				sql = "insert into check_line(cl_spid, cl_time_active) values(?, ?)";
				ps = conn.prepareStatement(sql);
				ps.setString(1, spId);
				ps.setLong(2, time);
				int rs_num = ps.executeUpdate();
				if (rs_num > 0) {

				} else {
					logger.info("insertActiverTest in sql fail(" + spId);
				}
			}
		} catch (SQLException e) {
			logger.error(spId + ")insertActiverTest in sql error: " + e.getMessage());
		}
		try {
			conn.close();
		} catch (SQLException e) {
		}
	}

	/**
	 * 链路检测时间(修改)
	 * 
	 * @param time
	 * @param spId
	 */
	public static void updateActiverTest(long time, String spId) {
		insertActiverTest(spId, time);
		Connection conn = getDBConn();
		String sql = "update check_line set cl_time_active = ? where cl_spid = ?";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setLong(1, time);
			ps.setString(2, spId);
			int rs_num = ps.executeUpdate();
			if (rs_num > 0) {

			} else {
				logger.info("updateActiverTest in sql fail(" + spId);
			}
		} catch (SQLException e) {
			logger.error(spId + ")updateActiverTest in sql error: " + e.getMessage());
		}
		try {
			conn.close();
		} catch (SQLException e) {
		}
	}

	/**
	 * 链路检测时间(查询)
	 * 
	 * @param spId
	 * @return
	 */
	public static long getActiverTest(String spId) {
		Connection conn = getDBConn();
		String sql = "select * from check_line where cl_spid = ?";
		long time = 0;
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setString(1, spId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				time = rs.getLong("cl_time_active");
			}
		} catch (SQLException e) {
			logger.error(spId + ")getActiverTest error: " + e.getMessage());
		}
		try {
			conn.close();
		} catch (SQLException e) {
		}
		return time;
	}

	/**
	 * 获取定时发送任务数据
	 * 
	 * @return
	 */
	public static List<SMS> getTimeTask() {
		List<SMS> smsList = new ArrayList<>();
		Connection conn = getDBConn();
		// 指定号码
		String sql = "select st_id, st_number, st_content,st_way, st_time_send, st_to_customer, st_to_company from sms_task where st_type = 0 and st_status = 0 and st_way = 1";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String st_id = rs.getString("st_id");
				String st_number = rs.getString("st_number");
				String st_content = rs.getString("st_content");
				String st_to_customer = rs.getString("st_to_customer");
				String st_to_company = rs.getString("st_to_company");
				String st_time = rs.getString("st_time_send");
				SMS sms = new SMS(st_id, st_number, st_content, st_to_customer, st_to_company);
				sms.setTime(st_time);
				smsList.add(sms);
				// 任务完成
				sql = "update sms_task set st_time_task = ?, st_status = ? where st_id = " + st_id;
				ps = conn.prepareStatement(sql);
				ps.setString(1, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
				ps.setString(2, "2"); // 任务状态
				int rs_num = ps.executeUpdate();
				if (rs_num < 0 || rs_num == 0) {
					logger.info("任务完成写入失败，时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
							+ ", 任务编号：" + st_id);
				}
			}
		} catch (SQLException e) {
			logger.error("getTimeTask()->1->" + e.getMessage());
		}

		// 2. 分组发送
		sql = "select st_id, st_to_sim_group, st_content, st_way, st_time_send, st_to_customer, st_to_company from sms_task where st_type = 1 and st_status = 0 and st_way = 1";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String st_id = rs.getString("st_id");
				String st_to_sim_group = rs.getString("st_to_sim_group");
				String st_to_customer = rs.getString("st_to_customer");
				String st_to_company = rs.getString("st_to_company");
				String st_content = rs.getString("st_content");
				String st_time = rs.getString("st_time_send");
				sql = "select sb_number from sim_base where sb_is_blacklist = 0 and sb_to_sim_group = "
						+ st_to_sim_group;
				ps = conn.prepareStatement(sql);
				ResultSet rs_group = ps.executeQuery();
				while (rs_group.next()) {
					String st_number = rs_group.getString("sb_number");
					SMS sms = new SMS(st_id, st_number, st_content, st_to_customer, st_to_company);
					sms.setTime(st_time);
					smsList.add(sms);
				}

				// 任务完成
				sql = "update sms_task set st_time_task = ?, st_status = ? where st_id = " + st_id;
				ps = conn.prepareStatement(sql);
				ps.setString(1, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
				ps.setString(2, "2"); // 任务状态
				int rs_num = ps.executeUpdate();
				if (rs_num < 0 || rs_num == 0) {
					logger.info("任务完成写入失败，时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
							+ ", 任务编号：" + st_id);
				}
			}
		} catch (SQLException e) {
			logger.error("getTimeTask()->2->" + e.getMessage());
		}

		// 3.全部号码
		sql = "select st_id, st_content, st_way, st_time_send, st_to_customer, st_to_company from sms_task where st_type = 3 and st_status = 0 and st_way = 1";
		try {
			PreparedStatement ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String st_id = rs.getString("st_id");
				String st_to_customer = rs.getString("st_to_customer");
				String st_to_company = rs.getString("st_to_company");
				String st_content = rs.getString("st_content");
				String st_time = rs.getString("st_time_send");
				sql = "selecr sb_number where sb_is_blacklist = 0 and st_to_customer = " + st_to_customer;
				ps = conn.prepareStatement(sql);
				ResultSet rs_group = ps.executeQuery();
				while (rs_group.next()) {
					String st_number = rs_group.getString("sb_number");
					SMS sms = new SMS(st_id, st_number, st_content, st_to_customer, st_to_company);
					sms.setTime(st_time);
					smsList.add(sms);
				}
				// 任务完成
				sql = "update sms_task set st_time_task = ?, st_status = ? where st_id = " + st_id;
				ps = conn.prepareStatement(sql);
				ps.setString(1, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
				ps.setString(2, "2"); // 任务状态
				int rs_num = ps.executeUpdate();
				if (rs_num < 0 || rs_num == 0) {
					logger.info("任务完成写入失败，时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())
							+ ", 任务编号：" + st_id);
				}
			}
		} catch (SQLException e) {
			logger.error("getTimeTask()->3->" + e.getMessage());
		}
		try {
			conn.close();
		} catch (SQLException e) {
		}
		return smsList;
	}

	// 完成定时任务
	public static void updateTimeTask(String st_id) throws SQLException {
		Connection conn = getDBConn();
		String sql = "update sms_task set st_time_task = ?, st_status = ? where st_id = " + st_id;
		PreparedStatement ps = conn.prepareStatement(sql);
		ps.setString(1, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		ps.setString(2, "1");
		int rs_num = ps.executeUpdate();
		if (rs_num < 0 || rs_num == 0) {
			logger.info("任务完成写入失败，时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()) + ", 任务编号："
					+ st_id);
		}
		try {
			conn.close();
		} catch (SQLException e) {
		}
	}

	/*************************************************** 以下是测试代码 *************************************************************************/

	public static void main(String[] args) {
		// 测试连接链路检测时间
		// insertActiverTest("123456", System.currentTimeMillis() / 1000);
		// updateActiverTest(System.currentTimeMillis() / 1000, "123456");
		// System.out.println(getActiverTest("123456"));
		// getTimeTask();
	}
}
