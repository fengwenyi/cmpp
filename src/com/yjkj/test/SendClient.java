package com.yjkj.test;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.yjkj.msg.Msg_Active_Test_Resp;
import com.yjkj.msg.Msg_Command;
import com.yjkj.msg.Msg_Connect;
import com.yjkj.msg.Msg_Connect_Resp;
import com.yjkj.msg.Msg_Deliver_Resp;
import com.yjkj.msg.Msg_Submit;

/**
 * 
 * @author Administrator
 *
 */
public class SendClient {
	protected static final Log log = LogFactory.getLog(SendClient.class);

	private String ip; // 服务端ip
	private int port; // 服务端口
	private String spid;// 鉴权账号
	private String password;// 鉴权密码
	private String msgsrc;// 企业代码
	private String serviceId;// 服务Id
	private String servicenumber;// 显示到接收手机端的主叫号码
	private String msgContent;// 发送的短信内容
	private String phone; //
	private CMPPService cmppService;
	private CMPPSocket cmppSocket;

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
	public SendClient(String ip, int port, String spid, String password, String msgsrc, String serviceId) {
		this.ip = ip;
		this.port = port;
		this.spid = spid;
		this.password = password;
		this.msgsrc = msgsrc;
		this.serviceId = serviceId;
	}

	// 开启系统发送功能
	public boolean startSocket() {
		boolean result = false;
		try {
			// 新建CMPP封装的SOCKET
			cmppSocket = new CMPPSocket(this.ip, this.port);
			// 初始化SOCKET
			cmppSocket.initialSock();
			cmppService = new CMPPService(cmppSocket);
			this.din = new DataInputStream(cmppSocket.getInputStream());
			this.dout = new DataOutputStream(cmppSocket.getOs());
			// 登陆连接到网关
			int connStatus = login(this.spid, this.password, (byte) 3);
			if (connStatus == 0) {
				result = true;
			}
		} catch (IOException e) {
			log.error("开启系统发送功能出错：" + e.getMessage());
		}
		return result;
	}
	
	//关闭系统
	public void closeSys() {
		try {
			cmppSocket.closeSock();
		} catch (IOException e) {
			log.info("关闭系统出错：" + e.getMessage());
		}
	}

	//发送短信
	public boolean sendNotifySms(String servicenumber, String msgContent, String phone) {
		boolean result = false;
		if (cmppService != null) {
			try {
				int testStatus;
				testStatus = cmppService.cmppActiveTest();
				// 链路测试成功
				if (testStatus == 0) {
					isrunning = true;
					this.servicenumber = servicenumber;
					this.msgContent = msgContent;
					this.phone = phone;
					// 开始发送短信
					this.sendStart();
					result = true;
				}
				
			} catch (IOException e) {
				log.error("发送短信出错，数据[" + msgContent + "/" + phone + "]");
			}
		}
		return result;
	}

	private Msg_Submit test_submit;

	/**
	 * 开始发送短信内容给终端设备
	 */
	public void sendStart() {
		try {
			// 初始化短信提交消息
			test_submit = intoSubmit();
			// 打包短信内容到字节包
			byte[] b = MsgUtils.packMsg(test_submit);
			// 发送字节包到网关
			sendMsg(b);
		} catch (Exception e) {
			log.error("发送短信异常" + e.getMessage());
		}
	}

	// 字节输入输出流
	private DataInputStream din;
	private DataOutputStream dout;
	// 链路检查
	private Msg_Active_Test_Resp activeTest_resp;
	// 短信下发相应
	//private Msg_Deliver_Resp deliver_resp;
	// 记录是否在和网关保持通讯
	public static boolean isrunning = false;

	// 接收消息时,肯定是要先读消息头,先取四个字节
	// 从输入流上接收消息
	public byte[] recvMsg() {
		try {
			synchronized (this.din) {
				// 读取数据
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
			}
		} catch (Exception e) {
			e.printStackTrace();
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
	 */
	public void sendMsg(byte[] data) {
		try {
			synchronized (this.dout) {
				if (null != data) {
					this.dout.write(data);
					this.dout.flush();
				}
			}
		} catch (IOException e) {

		}
	}


	/**
	 * 初始化短信提交到网关信息
	 * 
	 * @return
	 */
	public Msg_Submit intoSubmit() {
		Msg_Submit submit = new Msg_Submit();
		int SEQUENCE_ID = Util.getSequence();
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
		byte MSG_FMT = 8;
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

}
