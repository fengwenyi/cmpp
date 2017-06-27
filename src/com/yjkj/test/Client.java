package com.yjkj.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.xfsy.util.DBUtil;
import com.yjkj.msg.Msg_Active_Test_Resp;
import com.yjkj.msg.Msg_Command;
import com.yjkj.msg.Msg_Connect;
import com.yjkj.msg.Msg_Connect_Resp;
import com.yjkj.msg.Msg_Deliver;
import com.yjkj.msg.Msg_Deliver_Resp;
import com.yjkj.msg.Msg_Head;
import com.yjkj.msg.Msg_Submit;
import com.yjkj.msg.Msg_Submit_Resp;

public class Client implements Runnable {
	protected static final Log log = LogFactory.getLog(Client.class);

	private String ip; // 服务端ip
	private int port; // 服务端口
	private String spid;// 鉴权账号
	private String password;// 鉴权密码
	private String msgsrc;// 企业代码
	private String serviceId;// 服务Id
	private String servicenumber;// 显示到接收手机端的主叫号码
	private String msgContent;// 发送的短信内容
	private String phone; //
	private CMPPSocket cmppSocket; //
	private CMPPService cmppService; //
	// 字节输入输出流
	private DataInputStream din;
	private DataOutputStream dout;
	// 链路检查
	private Msg_Active_Test_Resp activeTest_resp;
	// 短信下发相应
	private Msg_Deliver_Resp deliver_resp = new Msg_Deliver_Resp();
	// 记录是否在和网关保持通讯
	public volatile boolean isrunning = true;
	//
	private Msg_Submit test_submit;
	//
	private int sequence;
	

	public String getSpid() {
		return spid;
	}

	public void setSpid(String spid) {
		this.spid = spid;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getMsgsrc() {
		return msgsrc;
	}

	public void setMsgsrc(String msgsrc) {
		this.msgsrc = msgsrc;
	}

	public String getServiceId() {
		return serviceId;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public String getServicenumber() {
		return servicenumber;
	}

	public void setServicenumber(String servicenumber) {
		this.servicenumber = servicenumber;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * 初始化短信发送器
	 * 
	 * @param ip
	 *            服务端ip
	 * @param port
	 *            服务端口
	 * @param spid
	 *            鉴权账号
	 * @param password
	 *            鉴权密码
	 * @param msgsrc
	 *            企业代码
	 * @param serviceId
	 *            服务Id
	 */
	public Client(String ip, int port, String spid, String password, String msgsrc, String serviceId,
			String servicenumber) {
		this.ip = ip;
		this.port = port;
		this.spid = spid;
		this.password = password; 
		this.msgsrc = msgsrc; 
		this.serviceId = serviceId; 
		this.servicenumber = servicenumber; 
	}

	// 连接
	public boolean conn() {
		// 新建CMPP封装的SOCKET
		cmppSocket = new CMPPSocket(this.ip, this.port);
		// 初始化SOCKET
		boolean result = cmppSocket.connSocket();
		if (result) {
			cmppService = new CMPPService(cmppSocket);
			this.din = new DataInputStream(cmppSocket.getInputStream());
			this.dout = new DataOutputStream(cmppSocket.getOs());
		}
		return result;
	}

	// 登录
	public boolean login() {
		boolean result = false;
		int connStatus = login(spid, password, (byte) 3);
		if (connStatus == 0) {
			result = true;
		}
		return result;
	}

	// 测试
	public boolean test() {
		boolean result = false;
		int testStatus;
		try {
			testStatus = cmppService.cmppActiveTest();
			// 链路测试成功
			if (testStatus == 0) {
				result = true;
			}
		} catch (IOException e) {
		}
		return result;
	}

	// 关闭
	public void closeSocket() {
		try {
			if (cmppSocket != null) {
				cmppSocket.closeSock();
			}
		} catch (IOException e) {
		}
	}

	// socket状态
	public boolean isConned() {
		boolean result = false;
		if (cmppSocket != null) {
			result = cmppSocket.isConned();
		}
		return result;
	}
	
	/**
	 * 停止
	 */
	public void stop() {
		isrunning = false;
	}
	
	public void start() {
		isrunning = true;
	}

	/**
	 * 开始发送短信内容给终端设备
	 * 
	 * 启动系统，开始接收短信
	 */
	public void run() {
		//初始化链路检测时间
		//Util.writeTime(System.currentTimeMillis() / 1000 + "");
		DBUtil.insertActiverTest(spid, System.currentTimeMillis() / 1000);
		if (cmppSocket == null) {
			startSocket(ip, port, spid, password);
		}
		try {
			while (isrunning) {
				// 链路测试(每3分钟检测一次)
				//long start_time = Long.parseLong(Util.readTime());
				long start_time = DBUtil.getActiverTest(spid);
				long current_time = System.currentTimeMillis() / 1000;
				long time = current_time - start_time;
				if (time > (3 * 60)) {
					if (time > (60 * 24 *60)) {
						log.info("执行任务：" + spid); //每天记录一次，连接是否正常
					}
					//Util.writeTime(current_time + "");
					DBUtil.updateActiverTest(current_time, spid);
					int testStatus;
					boolean result_task = false;
					try {
						testStatus = cmppService.cmppActiveTest(); // 链路测试
						int count = 0;
						while (testStatus != 0) {
							count++;
							testStatus = cmppService.cmppActiveTest();
							if (count == 3) { // 连续检测三次
								break;
							}
						}
						if (testStatus == 0) {
							result_task = true;
						}
					} catch (IOException e) {
					}
					if (!result_task) {
						// 关闭任务
						isrunning = false;
						// do someThing ···
						// 关闭socket，等等，并通知系统，服务器需要
						log.info("ActiveTest shutdown···");
					}
				}
				// 获取从网关获取的响应
				byte[] bb = recvMsg();
				// 只要有消息接收 就代表连接是正常的 计时清0
				Msg_Head msg = null;
				if (bb != null) {
					// 将字节转化为协议内容
					msg = MsgUtils.praseMsg(bb);
				}
				if (null != msg) {
					if (msg.getMsg_command() == Msg_Command.CMPP_ACTIVE_TEST_RESP) {
					} else if (msg.getMsg_command() == Msg_Command.CMPP_ACTIVE_TEST) {
						/*
						 * activeTest_resp.setMsg_squence(msg.getMsg_squence());
						 * byte[] tb = MsgUtils.packMsg(activeTest_resp);
						 * sendMsg(tb);
						 */
					} else {
						// deliver消息
						if (msg.getMsg_command() == Msg_Command.CMPP_DELIVER) {
							// 正常deliver消息
							Msg_Deliver deliver = (Msg_Deliver) msg;
							if (deliver.getRegistered_Delivery() == 0) {
								if (deliver.getTP_udhi() == 0) { //短短信
									String phone = deliver.getSrc_terminal_Id();
									String msgContent = deliver.getMsg_Content();
									boolean rs_num = DBUtil.delinerSMS(phone, msgContent);
									if (!rs_num) {
										log.info("将接收到的数据写入数据库库失败，数据[from:" + deliver.getSrc_terminal_Id() + ",消息内容："
												+ deliver.getMsg_Content() + "]");
									}
								}
								// 回复应答
								// log.info("回复应答");
								deliver_resp.setMsg_length(24);
								deliver_resp.setMsg_command(Msg_Command.CMPP_DELIVER_RESP);
								deliver_resp.setMsg_squence(deliver.getMsg_squence());
								deliver_resp.setMsg_Id(deliver.getMsg_Id());
								deliver_resp.setResult(0);
								sendMsg(MsgUtils.packMsg(deliver_resp));
								// 消息报告
							} else {
								// 将报告处理结果存库
							}
						} else if (msg.getMsg_command() == Msg_Command.CMPP_SUBMIT_RESP) {
							// 通知已发送成功
							Msg_Submit_Resp sub_resp = (Msg_Submit_Resp) msg;
							// 短信发送结果
							int result = sub_resp.getResult();
							if (result != 0) {
								String error = null;
								switch (result) {
								case 1:
									error = "1:消息结构错";
									break;
								case 2:
									error = "2:命令字错";
									break;
								case 3:
									error = "3:消息序号重复";
									break;
								case 4:
									error = "4:消息长度错";
									break;
								case 5:
									error = "5:资费代码错";
									break;
								case 6:
									error = "6:超过最大信息长";
									break;
								case 7:
									error = "7:业务代码错";
									break;
								case 8:
									error = "8:流量控制错";
									break;
								case 9:
									error = "9:本网关不负责服务此计费号码";
									break;
								case 10:
									error = "10:短信接入码错误";
									break;
								case 11:
									error = "11:信息内容来源（SP_Id）错误";
									break;
								case 12:
									error = "12:被计费用户的号码错误";
									break;
								case 13:
									error = "13:接收短信的MSISDN号码错误";
									break;
								default:
									error = result + ":未知错误";
									break;
								}
								String[] data = { "9", error, sub_resp.getMsg_squence() + "" };
								DBUtil.updateNumber(data);
							} else {
								String[] data = { "0", null, sub_resp.getMsg_squence() + "" };
								DBUtil.updateNumber(data);
							}
						} else {
						}
					}
				}

				Thread.sleep(100);
			}
			cmppSocket.closeSock();
		} catch (Exception e) {
			log.error("run exception: " + e.getMessage());
		}

	}

	// 发送 短短信
	public boolean send(String phone, String msg, int sequence) {
		boolean result = false;
		try {
			this.msgContent = msg;
			this.phone = phone;
			this.sequence = sequence;
			// 初始化短信提交消息
			test_submit = intoSubmit();
			// 打包短信内容到字节包
			byte[] b = null;
			b = MsgUtils.packMsg(test_submit);
			sendMsg(b);
			result = true;
		} catch (UnsupportedEncodingException e) {
			log.error("send UnsupportedEncodingException:" + e.getMessage());
		} catch (Exception e) {
			log.error("send Exception:" + e.getMessage());
		}
		return result;
	}

	// 发送 长短信
	public boolean sendLongMsg(String phone, String msg, int sequence) {
		boolean result = false;
		try {
			this.msgContent = msg;
			this.phone = phone;
			this.sequence = sequence; // 打包短信内容到字节包
			List<byte[]> dataList = initLongMsg();
			for (byte[] data : dataList) {
				sendMsg(data);
			}
			result = true;
		} catch (Exception e) {
			log.error("send long sms error: " + e.getMessage());
		}
		return result;
	}

	// startSocket
	public boolean startSocket(String ip, int port, String spid, String password) {
		boolean result = false;
		try {
			// 新建CMPP封装的SOCKET
			cmppSocket = new CMPPSocket(ip, port);
			// 初始化SOCKET
			cmppSocket.initialSock();
			cmppService = new CMPPService(cmppSocket);
			this.din = new DataInputStream(cmppSocket.getInputStream());
			this.dout = new DataOutputStream(cmppSocket.getOs());
			// 登陆连接到网关
			int connStatus = login(spid, password, (byte) 3);
			if (connStatus == 0) {
				result = true;
			}
		} catch (IOException e) {
			log.error("startSocket:" + e.getMessage());
		}
		return result;
	}

	// 接收消息时,肯定是要先读消息头,先取四个字节
	// 从输入流上接收消息
	public byte[] recvMsg() {
		try {
			/*
			 * synchronized (this.din) { // 读取数据 int len = this.din.readInt();
			 * if (len == 0) { return null; } else if (len > 0 && len < 500) {
			 * byte[] data = new byte[len - 4]; this.din.readFully(data); return
			 * data; } else { log.info("recvMsg();SPConnection:不是正常的消息数据"); } }
			 */
			int len = this.din.readInt();
			if (len == 0) {
				return null;
			} else if (len > 0 && len < 500) {
				byte[] data = new byte[len - 4];
				this.din.readFully(data);
				return data;
			} else {
				log.info("recvMsg();SPConnection:不是正常的消息数据");
			}
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * 与网关建立联系
	 * 
	 * @param spid
	 *            登录的账号
	 * @param pwd
	 *            登录的密码
	 * @param version
	 *            版本
	 * @return
	 */
	public int login(String spid, String pwd, byte version) {
		try {
			Msg_Connect msg = getCmpp_Connect(spid, pwd, version);
			byte[] connect_data = MsgUtils.packMsg(msg);
			// 发送消息
			this.sendMsg(connect_data);
			// 获取网关返回的消息
			byte[] connect_resp_data = this.recvMsg();
			// 将返回的连接网关的消息转化为连接相应报文
			Msg_Connect_Resp connect_resp = (Msg_Connect_Resp) MsgUtils.praseMsg(connect_resp_data);
			if (null != connect_resp) {
				// 登陆成功 启动其他线程
				if (0 == connect_resp.getStatus()) {
					return 0;
				} else {
					return -1;
				}
			}
			// 做出其他处理
		} catch (Exception e) {
		}
		return -1;
	}

	/**
	 * 构造CMPP_CONNECT 协议数据包
	 * 
	 * @param id
	 * @param pwd
	 * @return
	 */
	public Msg_Connect getCmpp_Connect(String spid, String pwd, byte version) {
		Msg_Connect cmpp_connect = new Msg_Connect();
		cmpp_connect.setMsg_length(4 + 4 + 4 + 6 + 16 + 1 + 4);
		cmpp_connect.setMsg_command(Msg_Command.CMPP_CONNECT);
		cmpp_connect.setMsg_squence(Util.getSequence());
		cmpp_connect.setSource_Addr(spid);
		String timeStamp = Util.getMMDDHHMMSS();
		byte[] b = Util.getLoginMD5(spid, pwd, timeStamp);
		cmpp_connect.setAuthenticatorSource(b);
		cmpp_connect.setVersion(version);
		cmpp_connect.setTimestamp(Integer.parseInt(timeStamp));
		return cmpp_connect;
	}

	/**
	 * 发送字节消息到网关
	 * 
	 * @param data
	 *            发送的字节数据
	 * @throws IOException
	 */
	public void sendMsg(byte[] data) throws IOException {
		/*
		 * synchronized (this.dout) { if (null != data) { this.dout.write(data);
		 * this.dout.flush(); } }
		 */
		if (null != data) {
			this.dout.write(data);
			this.dout.flush();
		}
	}

	/**
	 * 初始化短信提交到网关信息
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public Msg_Submit intoSubmit() throws UnsupportedEncodingException {
		Msg_Submit submit = new Msg_Submit();
		int SEQUENCE_ID = sequence;
		long MSG_ID = 0;
		byte PK_TOTAL = 1;
		byte PK_NUMBER = 1;
		byte REGISTERED_DELIVERY = 0;
		byte MSG_LEVEL = 1;
		String SERVICE_ID = serviceId;
		byte FEE_USERTYPE = 0x00;
		String FEE_TERMINAL_ID = "";
		byte FEE_TERMINAL_TYPE = 0;
		byte TP_PID = 0;
		byte TP_UDHI = 0;
		byte MSG_FMT = 0;

		String MSG_SRC = msgsrc;
		String FEETYPE = "01";
		String FEECODE = "00030";
		String VALID_TIME = "";
		String AT_TIME = "";
		String SRC_ID = servicenumber;
		byte DESTUSR_TL = 1;
		String DEST_TERMINAL_ID = phone;
		byte DEST_TERMINAL_TYPE = 0;
		// 信息内容
		String MSG_CONTENT = msgContent;
		int msgLen = MSG_CONTENT.getBytes().length;

		boolean charResult = Util.isContainChinese(msgContent);
		if (charResult) {
			MSG_FMT = 8;
			msgLen = MSG_CONTENT.getBytes("UnicodeBigUnmarked").length;
		}
		String LINKID = "1";
		int totalLen = 12 + 8 + 1 + 1 + 1 + 1 + 10 + 1 + 32 + 1 + 1 + 1 + 1 + 6 + 2 + 6 + 17 + 17 + 21 + 1 + 32 + 1 + 1
				+ msgLen + 20;

		submit.setMsg_length(totalLen);
		submit.setMsg_squence(SEQUENCE_ID);
		submit.setMsg_command(Msg_Command.CMPP_SUBMIT);
		submit.setMsg_Id(MSG_ID);
		submit.setPk_total(PK_TOTAL);
		submit.setPk_number(PK_NUMBER);
		submit.setRegistered_Delivery(REGISTERED_DELIVERY);
		submit.setMsg_level(MSG_LEVEL);
		submit.setService_Id(SERVICE_ID);
		submit.setFee_UserType(FEE_USERTYPE);
		submit.setFee_terminal_Id(FEE_TERMINAL_ID);
		submit.setFee_terminal_type(FEE_TERMINAL_TYPE);
		submit.setTP_pId(TP_PID);
		submit.setTP_udhi(TP_UDHI);
		submit.setMsg_Fmt(MSG_FMT);
		submit.setMsg_src(MSG_SRC);
		submit.setFeeType(FEETYPE);
		submit.setFeeCode(FEECODE);
		submit.setValId_Time(VALID_TIME);
		submit.setAt_Time(AT_TIME);
		submit.setSrc_Id(SRC_ID);
		submit.setDestUsr_tl(DESTUSR_TL);
		submit.setDest_terminal_Id(DEST_TERMINAL_ID);
		submit.setDest_terminal_type(DEST_TERMINAL_TYPE);
		submit.setMsg_Length((byte) msgLen);
		submit.setMsg_Content(MSG_CONTENT);
		submit.setLinkID(LINKID);

		return submit;
	}

	public List<byte[]> initLongMsg() throws UnsupportedEncodingException {

		int SEQUENCE_ID = sequence;
		long MSG_ID = 0;
		//byte PK_TOTAL = 1;
		//byte PK_NUMBER = 1;
		byte REGISTERED_DELIVERY = 0;
		byte MSG_LEVEL = 1;
		String SERVICE_ID = serviceId;
		byte FEE_USERTYPE = 0x00;
		String FEE_TERMINAL_ID = "";
		byte FEE_TERMINAL_TYPE = 0;
		byte TP_PID = 0;
		byte TP_UDHI = 1;
		byte MSG_FMT = 8;
		String MSG_SRC = msgsrc;
		String FEETYPE = "01";
		String FEECODE = "00030";
		String VALID_TIME = "";
		String AT_TIME = "";
		String SRC_ID = servicenumber;
		byte DESTUSR_TL = 1;
		String DEST_TERMINAL_ID = phone;
		byte DEST_TERMINAL_TYPE = 0; // String MSG_CONTENT = msgContent;
		String LINKID = "1";

		byte[] allByte = null;
		allByte = msgContent.getBytes();
		List<byte[]> dataList = new ArrayList<byte[]>();
		int maxMessageLen = 126;
		byte[] messageUCS2;
		messageUCS2 = msgContent.getBytes("UnicodeBigUnmarked");
		int messageUCS2Len = messageUCS2.length;
		// 长短信发送
		// int tpUdhi = 1;
		// int msgFmt = 0x08;
		int messageUCS2Count = messageUCS2Len / (maxMessageLen - 6) + 1; // 长短信分为多少条发送
		byte[] tp_udhiHead = new byte[6];
		tp_udhiHead[0] = 0x05;
		tp_udhiHead[1] = 0x00;
		tp_udhiHead[2] = 0x03;
		tp_udhiHead[3] = 0x0A;
		tp_udhiHead[4] = (byte) messageUCS2Count;
		tp_udhiHead[5] = 0x01;

		for (int i = 0; i < messageUCS2Count; i++) {

			tp_udhiHead[5] = (byte) (i + 1);
			byte[] msgContent;
			if (i != messageUCS2Count - 1) {
				// 不为最后一条
				msgContent = byteAdd(tp_udhiHead, messageUCS2, i * (maxMessageLen - 6), (i + 1) * (maxMessageLen - 6));
			} else {
				msgContent = byteAdd(tp_udhiHead, messageUCS2, i * (maxMessageLen - 6), messageUCS2Len);
			}

			// 短信数据
			Msg_Submit submit = new Msg_Submit();
			submit.setMsg_length(12 + 8 + 1 + 1 + 1 + 1 + 10 + 1 + 32 + 1 + 1 + 1 + 1 + 6 + 2 + 6 + 17 + 17 + 21 + 1
					+ 32 + 1 + 1 + msgContent.length + 20);
			submit.setMsg_squence(SEQUENCE_ID);
			submit.setMsg_command(Msg_Command.CMPP_SUBMIT);
			submit.setMsg_Id(MSG_ID);
			submit.setPk_total((byte) messageUCS2Count);
			submit.setPk_number((byte) (i + 1));
			submit.setRegistered_Delivery(REGISTERED_DELIVERY);
			submit.setMsg_level(MSG_LEVEL);
			submit.setService_Id(SERVICE_ID);
			submit.setFee_UserType(FEE_USERTYPE);
			submit.setFee_terminal_Id(FEE_TERMINAL_ID);
			submit.setFee_terminal_type(FEE_TERMINAL_TYPE);
			submit.setTP_pId(TP_PID);
			submit.setTP_udhi(TP_UDHI);
			submit.setMsg_Fmt(MSG_FMT);
			submit.setMsg_src(MSG_SRC);
			submit.setFeeType(FEETYPE);
			submit.setFeeCode(FEECODE);
			submit.setValId_Time(VALID_TIME);
			submit.setAt_Time(AT_TIME);
			submit.setSrc_Id(SRC_ID);
			submit.setDestUsr_tl(DESTUSR_TL);
			submit.setDest_terminal_Id(DEST_TERMINAL_ID);
			submit.setDest_terminal_type(DEST_TERMINAL_TYPE);
			submit.setMsg_Length((byte) msgContent.length);
			submit.setMsgContent(msgContent);
			submit.setLinkID(LINKID);

			try {
				sendMsg(MsgUtils.packMsgLong(submit));
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (Exception e1) { // TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				dataList.add(MsgUtils.packMsg(submit));
			} catch (Exception e) {
			}
		}
		return dataList;
	}

	/***
	 * * 功能： 将src里的字节与add里的从start开始到end（不包括第end个位置)的字节串连在一起返回
	 * 
	 * @param src
	 * @param add
	 * @param start
	 *            add 开始位置
	 * @param end
	 *            add 的结束位置(不包括end位置)
	 * @return 也即实现类似String类型的src+add.subString(start,end)功能
	 */
	public static byte[] byteAdd(byte[] src, byte[] add, int start, int end) {
		byte[] dst = new byte[src.length + end - start];
		for (int i = 0; i < src.length; i++) {
			dst[i] = src[i];
		}
		for (int i = 0; i < end - start; i++) {
			dst[src.length + i] = add[start + i];
		}
		return dst;
	}
}
