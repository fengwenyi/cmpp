package com.yjkj.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import org.apache.log4j.Logger;

import com.xfsy.util.DBUtil;
import com.yjkj.msg.Msg_Active_Test;
import com.yjkj.msg.Msg_Active_Test_Resp;
import com.yjkj.msg.Msg_Cancel;
import com.yjkj.msg.Msg_Cancel_Resp;
import com.yjkj.msg.Msg_Command;
import com.yjkj.msg.Msg_Connect;
import com.yjkj.msg.Msg_Connect_Resp;
import com.yjkj.msg.Msg_Deliver;
import com.yjkj.msg.Msg_Deliver_Resp;
import com.yjkj.msg.Msg_Head;
import com.yjkj.msg.Msg_Query;
import com.yjkj.msg.Msg_Query_Resp;
import com.yjkj.msg.Msg_Submit;
import com.yjkj.msg.Msg_Submit_Resp;

/**
 * 1：打包解包工
 * 
 */
public class MsgUtils {
	private static Logger log = Logger.getLogger(DBUtil.class);

	/**
	 * 1:打包
	 * 
	 * @param msg
	 *            打包的消息类
	 * @return
	 */
	public static byte[] packMsg(Msg_Head msg) throws Exception {
		// 生成一段内存字节流
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		byte[] data = null;
		// 得到该信息的命令字
		int command = msg.getMsg_command();
		switch (command) {
		// 接收到的为.CMPP_CONNECT消息类型时 处理如下
		case Msg_Command.CMPP_CONNECT:
			StringBuffer sb = new StringBuffer();
			Msg_Connect connect = (Msg_Connect) msg;
			byte[] con_byte = Util.writeInt(connect.getMsg_length());
			dos.write(con_byte);
			sb.append("登陆消息 length:" + connect.getMsg_length());
			byte[] cmd_byte = Util.writeInt(connect.getMsg_command());
			dos.write(cmd_byte);
			sb.append(" command:" + connect.getMsg_command());
			byte[] seq_byte = Util.writeInt(connect.getMsg_squence());
			dos.write(seq_byte);
			sb.append(" sequence:" + connect.getMsg_squence());
			Util.writeFully(dos, 6, connect.getSource_Addr());
			sb.append(" spid:" + connect.getSource_Addr());
			dos.write(connect.getAuthenticatorSource());
			sb.append(" version:" + connect.getVersion());
			dos.writeByte(connect.getVersion());
			byte[] tim_byte = Util.writeInt(connect.getTimestamp());
			dos.write(tim_byte);
			sb.append(" timestamp:" + connect.getTimestamp());
			dos.flush();
			data = bos.toByteArray();
			// Client.setText(sb.toString());
			break;
		// 发起终止连接消息data
		case Msg_Command.CMPP_TERMINATE:
			byte[] len = Util.writeInt(12);
			dos.write(len);
			byte[] cmd = Util.writeInt(Msg_Command.CMPP_TERMINATE);
			dos.write(cmd);
			byte[] seq = Util.writeInt(Util.getSequence());
			dos.write(seq);
			dos.flush();
			data = bos.toByteArray();
			// 返回ISMG的终止响应data
		case Msg_Command.CMPP_TERMINATE_RESP:
			byte[] len1 = Util.writeInt(12);
			dos.write(len1);
			byte[] cmd1 = Util.writeInt(Msg_Command.CMPP_TERMINATE_RESP);
			dos.write(cmd1);
			byte[] seq1 = Util.writeInt(Util.getSequence());
			dos.write(seq1);
			dos.flush();
			data = bos.toByteArray();
			// sp向ismg提交信息data
		case Msg_Command.CMPP_SUBMIT:
			Msg_Submit submit = (Msg_Submit) msg;
			// -------------------写入长度
			byte[] sub_len = Util.writeInt(submit.getMsg_length());
			dos.write(sub_len);
			// -------------------写入协议类型
			byte[] cmd2 = Util.writeInt(submit.getMsg_command());
			dos.write(cmd2);
			// -------------------写入流水号
			byte[] seq2 = Util.writeInt(submit.getMsg_squence());
			dos.write(seq2);
			// -------------------写入消息id
			byte[] msgid = Util.writeLong(submit.getMsg_Id());
			dos.write(msgid);// [0]
			dos.writeByte(submit.getPk_total());// [1]
			dos.writeByte(submit.getPk_number());// [2]
			dos.writeByte(submit.getRegistered_Delivery());// [3]
			dos.writeByte(submit.getMsg_level());// [4]
			Util.writeFully(dos, 10, submit.getService_Id());//// [5]
			dos.writeByte(submit.getFee_UserType());// 一个长度s[6]
			Util.writeFullyTest(dos, 32, submit.getFee_terminal_Id());// [7]
			dos.writeByte(submit.getFee_terminal_type());// [8]
			dos.writeByte(submit.getTP_pId());// [9]
			dos.writeByte(submit.getTP_udhi());// [10]
			dos.writeByte(submit.getMsg_Fmt());// [11]
			Util.writeFully(dos, 6, submit.getMsg_src());// [12]
			Util.writeFully(dos, 2, submit.getFeeType());// [13]
			Util.writeFully(dos, 6, submit.getFeeCode());// [14]
			Util.writeFully(dos, 17, submit.getValId_Time());// [15]
			Util.writeFully(dos, 17, submit.getAt_Time());// [16]
			Util.writeFully(dos, 21, submit.getSrc_Id());// [17]
			dos.writeByte(submit.getDestUsr_tl());// [18]
			Util.writeFully(dos, 32, submit.getDest_terminal_Id());
			dos.writeByte(submit.getDest_terminal_type());
			dos.writeByte(submit.getMsg_Length());
			if (submit.getMsg_Fmt() == 0) {
				Util.writeFully(dos, submit.getMsg_Length(), submit.getMsg_Content());
			} else {
				Util.writeFullyZH(dos, submit.getMsg_Length(), submit.getMsg_Content());
			}
			Util.writeFully(dos, 20, submit.getLinkID());
			// 提交数据
			dos.flush();
			data = bos.toByteArray();
			break;
		// 查询业务情况消息 打包data
		case Msg_Command.CMPP_QUERY:
			Msg_Query query = (Msg_Query) msg;
			byte[] len3 = Util.writeInt(query.getMsg_length());
			dos.write(len3);
			byte[] cmd3 = Util.writeInt(query.getMsg_command());
			dos.write(cmd3);
			byte[] seq3 = Util.writeInt(query.getMsg_squence());
			dos.write(seq3);
			Util.writeFully(dos, 8, query.getTime());
			dos.writeByte(query.getQuery_Type());
			Util.writeFully(dos, 10, query.getQuery_Code());
			Util.writeFully(dos, 8, query.getReserve());
			dos.flush();
			data = bos.toByteArray();
			break;
		// 打包Deliver_Resp消息 data
		case Msg_Command.CMPP_DELIVER_RESP:
			// log.info("deliver_resp");
			Msg_Deliver_Resp deliver_resp = (Msg_Deliver_Resp) msg;

			byte[] len4 = Util.writeInt(deliver_resp.getMsg_length());
			dos.write(len4);

			byte[] cmd4 = Util.writeInt(deliver_resp.getMsg_command());
			dos.write(cmd4);

			byte[] seq4 = Util.writeInt(deliver_resp.getMsg_squence());
			dos.write(seq4);

			dos.write(Util.writeLong(deliver_resp.getMsg_Id()));

			byte[] res = Util.writeInt(deliver_resp.getResult());
			dos.write(res);

			dos.flush();
			data = bos.toByteArray();
			break;

		case Msg_Command.CMPP_CANCEL:
			Msg_Cancel cancel = (Msg_Cancel) msg;

			byte[] len5 = Util.writeInt(cancel.getMsg_length());
			dos.write(len5);
			byte[] cmd5 = Util.writeInt(cancel.getMsg_command());
			dos.write(cmd5);
			byte[] seq5 = Util.writeInt(cancel.getMsg_squence());
			dos.write(seq5);
			dos.write(Util.writeLong(cancel.getMsg_Id()));
			dos.flush();
			data = bos.toByteArray();
			break;
		// 链路检测
		case Msg_Command.CMPP_ACTIVE_TEST:
			Msg_Active_Test act = (Msg_Active_Test) msg;
			byte[] len6 = Util.writeInt(act.getMsg_length());
			dos.write(len6);
			byte[] cmd6 = Util.writeInt(Msg_Command.CMPP_ACTIVE_TEST);
			dos.write(cmd6);
			byte[] seq6 = Util.writeInt(act.getMsg_squence());
			dos.write(seq6);
			dos.flush();
			data = bos.toByteArray();
			break;
		// 链路检测应答 13字节
		case Msg_Command.CMPP_ACTIVE_TEST_RESP:
			Msg_Active_Test_Resp act_resp = (Msg_Active_Test_Resp) msg;
			byte[] len7 = Util.writeInt(act_resp.getMsg_length());
			dos.write(len7);
			byte[] cmd7 = Util.writeInt(Msg_Command.CMPP_ACTIVE_TEST_RESP);
			dos.write(cmd7);
			byte[] seq7 = Util.writeInt(act_resp.getMsg_squence());
			dos.write(seq7);
			dos.writeByte(1); // 不知道写什么值
			dos.flush();
			data = bos.toByteArray();
			break;

		}
		return data;
	}

	/**
	 * 1:解包，解析成对应的相应消息
	 * 
	 * @param msg
	 *            数据包
	 * @return 对应的消息头
	 */
	public static Msg_Head praseMsg(byte[] msg) throws Exception {
		// 将字节流封装成内存流
		ByteArrayInputStream bais = new ByteArrayInputStream(msg);
		DataInputStream dis = new DataInputStream(bais);
		/**
		 * 在上一个方法中已经读出了长度 并把剩余的数据封装在data里
		 */
		int command_Id = Util.readInt(dis);
		int sequenceId = Util.readInt(dis);
		switch (command_Id) {
		// 当接收到得消息类型为CMPP_CONNECT_RESP 时
		case Msg_Command.CMPP_CONNECT_RESP:
			Msg_Connect_Resp msg_resp = new Msg_Connect_Resp();
			msg_resp.setMsg_length(msg.length + 6);
			msg_resp.setMsg_command(command_Id);
			msg_resp.setMsg_squence(sequenceId);
			msg_resp.setStatus(Util.readInt(dis));
			byte[] auth = new byte[16];
			dis.read(auth);
			msg_resp.setAuthenticatorISMG(auth);
			msg_resp.setVersion(dis.readByte());
			return msg_resp;
		// 断开连接 和 应答
		case Msg_Command.CMPP_TERMINATE:
			return null;
		case Msg_Command.CMPP_TERMINATE_RESP:
			return null;
		case Msg_Command.CMPP_SUBMIT_RESP:// 要解包Submit应答消息
			Msg_Submit_Resp msr = new Msg_Submit_Resp();
			msr.setMsg_length(msg.length + 4);
			msr.setMsg_command(command_Id);
			msr.setMsg_squence(sequenceId);
			msr.setMsg_Id(Util.readLong(dis));
			msr.setResult(Util.readInt(dis));
			return msr;
		case Msg_Command.CMPP_QUERY_RESP:// 查询消息应答
			Msg_Query_Resp query = new Msg_Query_Resp();
			query.setMsg_length(msg.length + 4);
			query.setMsg_command(command_Id);
			query.setMsg_squence(sequenceId);
			query.setTime(Util.readString(dis, 8));
			query.setQuery_Type(dis.readByte());
			query.setQuery_Code(Util.readString(dis, 10));
			query.setMT_TLMsg(Util.readInt(dis));
			query.setMT_Tlusr(Util.readInt(dis));
			query.setMT_Scs(Util.readInt(dis));
			query.setMT_WT(Util.readInt(dis));
			query.setMT_FL(Util.readInt(dis));
			query.setMO_Scs(Util.readInt(dis));
			query.setMO_WT(Util.readInt(dis));
			query.setMO_FL(Util.readInt(dis));
			return query;
		case Msg_Command.CMPP_DELIVER:// 如果是deliver消息
			Msg_Deliver msd = new Msg_Deliver();
			msd.setMsg_length(msg.length + 4);// 总长
			msd.setMsg_command(command_Id);
			msd.setMsg_squence(sequenceId);
			msd.setMsg_Id(Util.readLong(dis));
			msd.setDest_Id(Util.readString(dis, 21));
			msd.setService_Id(Util.readString(dis, 10));// SP的服务代码
			msd.setTP_pid(dis.readByte());
			msd.setTP_udhi(dis.readByte());
			msd.setMsg_Fmt(dis.readByte());
			msd.setSrc_terminal_Id(Util.readString(dis, 32));
			msd.setSrc_terminal_type(dis.readByte());
			msd.setRegistered_Delivery(dis.readByte());
			int msl = dis.readByte();//78
			if (msl < 0) {
				msl = msl + 128 * 2;
			}
			msd.setMsgCntLength(msl); //78
			// 如果是状态报报告,那就不一样! 此处如果是报告就不用读msg_length了么？？？？？？？？？？？？？？？？？？？？
			if (msd.getRegistered_Delivery() == 1) {
				msd.setMsg_Id_report(Util.readLong(dis));
				msd.setStat(Util.readString(dis, 7));
				msd.setSubmit_time(Util.readString(dis, 10));
				msd.setDone_time(Util.readString(dis, 10));
				msd.setDest_terminal_Id(Util.readString(dis, 32));
				msd.setSMSC_sequence(Util.readInt(dis));
			} else {// 才是一般消息,即手机发送上来的内容
				// System.out.println("解析数据");
				// 读取
				if (msd.getMsg_Fmt() == 8) {
					if (msd.getTP_udhi() == 0x40) {
						msd.setMsg_length(msg.length + 6);
						byte bt_hl = dis.readByte(); //05, 表示剩余协议头的长度
						byte bt_h2 = dis.readByte(); //00, 这个值在GSM 03.40规范9.2.3.24.1中规定，表示随后的这批超长短信的标识位长度为1（格式中的XX值）。
						byte bt_h3 = dis.readByte(); //03, 这个值表示剩下短信标识的长度
						byte bt_h4 = dis.readByte(); //?????   XX，这批短信的唯一标志，事实上，SME(手机或者SP)把消息合并完之后，就重新记录，所以这个标志是否唯一并不是很 重要。
						byte bt_h5 = dis.readByte(); //MM, 这批短信的数量。如果一个超长短信总共5条，这里的值就是5。
						byte bt_h6 = dis.readByte(); //NN, 这批短信的数量。如果当前短信是这批短信中的第一条的值是1，第二条的值是2
						String cnt;
						if (msd.getMsgCntLength() > 126) {
							String str1 = Util.readStringZH(dis, 126);
							int num = msd.getMsgCntLength() - 126;
							String str2 = Util.readStringZH(dis, num);
							cnt = str1 + str2;
//							cnt = new String((str1 + str2).getBytes(), "UTF-8");
//							msd.setMsg_Content(cnt);// 消息内容
						} else {
							cnt = Util.readStringZH(dis, msd.getMsgCntLength());
//							cnt = new String(str.getBytes(), "UTF-8");
//							msd.setMsg_Content(cnt);// 消息内容
						}
						//去掉莫名其妙的尾巴
						cnt = cnt.substring(0, cnt.length()-3);
						
						//接收完成，处理
						/*if (condition) {
							String phone = msd.getSrc_terminal_Id();
							boolean rs_num = DBUtil.delinerSMS(phone, );
							if (!rs_num) {
								log.info("将接收到的数据写入数据库库失败，数据[from:" + deliver.getSrc_terminal_Id() + ",消息内容："
										+ cnt + "]");
							}
						}*/
						if (bt_h5 != bt_h6) {
							cnt = "\"" + bt_h6 + "\":\"" + cnt + "\",";
						} else {
							cnt = "\"" + bt_h6 + "\":\"" + cnt + "\"";
						}
						DBUtil.delinerLongSMS(msd.getSrc_terminal_Id(), cnt, bt_h5, bt_h6, bt_h4);
					}
					String str = Util.readStringZH(dis, msd.getMsgCntLength());
					String cnt = new String(str.getBytes(), "UTF-8");
					msd.setMsg_Content(cnt);// 消息内容
				} else {
					msd.setMsg_Content(Util.readString(dis, msd.getMsgCntLength()));// 消息内容
				}
			}
			msd.setLinkID(Util.readString(dis, 20));// 读取linkID
			return msd;

		// 删除消息
		case Msg_Command.CMPP_CANCEL:
			Msg_Cancel_Resp can_resp = new Msg_Cancel_Resp();
			can_resp.setMsg_length(msg.length + 4);
			can_resp.setMsg_command(command_Id);
			can_resp.setMsg_squence(sequenceId);
			can_resp.setSuccess_Id(Util.readInt(dis));
			return can_resp;

		case Msg_Command.CMPP_ACTIVE_TEST:// 链路检测
			Msg_Active_Test act = new Msg_Active_Test();
			act.setMsg_length(12);
			act.setMsg_command(command_Id);
			act.setMsg_squence(sequenceId);
			return act;
		case Msg_Command.CMPP_ACTIVE_TEST_RESP:// 链路检测应答 13字节
			Msg_Active_Test_Resp act_resp = new Msg_Active_Test_Resp();
			act_resp.setMsg_length(13);
			act_resp.setMsg_command(command_Id);
			act_resp.setMsg_squence(sequenceId);
			act_resp.setReserved(dis.readByte());
			return act_resp;
		}

		return null;

	}

	public static byte[] packMsgLong(Msg_Submit submit) throws Exception {
		// 生成一段内存字节流
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		StringBuffer sb = new StringBuffer();
		// -------------------写入长度
		byte[] sub_len = Util.writeInt(submit.getMsg_length());
		dos.write(sub_len);
		// -------------------写入协议类型
		byte[] cmd2 = Util.writeInt(submit.getMsg_command());
		dos.write(cmd2);
		// -------------------写入流水号
		byte[] seq2 = Util.writeInt(submit.getMsg_squence());
		dos.write(seq2);
		// -------------------写入消息id
		byte[] msgid = Util.writeLong(submit.getMsg_Id());
		dos.write(msgid);// [0]
		dos.writeByte(submit.getPk_total());// [1]
		dos.writeByte(submit.getPk_number());// [2]
		dos.writeByte(submit.getRegistered_Delivery());// [3]
		dos.writeByte(submit.getMsg_level());// [4]
		Util.writeFully(dos, 10, submit.getService_Id());//// [5]
		dos.writeByte(submit.getFee_UserType());// 一个长度s[6]
		Util.writeFullyTest(dos, 32, submit.getFee_terminal_Id());// [7]
		dos.writeByte(submit.getFee_terminal_type());// [8]
		dos.writeByte(submit.getTP_pId());// [9]
		dos.writeByte(submit.getTP_udhi());// [10]
		dos.writeByte(submit.getMsg_Fmt());// [11]
		Util.writeFully(dos, 6, submit.getMsg_src());// [12]
		Util.writeFully(dos, 2, submit.getFeeType());// [13]
		Util.writeFully(dos, 6, submit.getFeeCode());// [14]
		Util.writeFully(dos, 17, submit.getValId_Time());// [15]
		Util.writeFully(dos, 17, submit.getAt_Time());// [16]
		Util.writeFully(dos, 21, submit.getSrc_Id());// [17]
		dos.writeByte(submit.getDestUsr_tl());// [18]
		Util.writeFully(dos, 32, submit.getDest_terminal_Id());
		dos.writeByte(submit.getDest_terminal_type());
		dos.writeByte(submit.getMsg_Length());
		Util.writeFullyZHLong(dos, submit.getMsg_Length(), submit.getMsgContent());
		Util.writeFully(dos, 20, submit.getLinkID());
		// 提交数据
		dos.flush();
		return bos.toByteArray();

	}
}
