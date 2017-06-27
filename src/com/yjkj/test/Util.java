package com.yjkj.test;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.xfsy.util.ConfigUtil;

public class Util {

	
	public static String readString(DataInputStream din, int len) throws IOException {
		byte[] b = new byte[len];
		din.read(b);
		String s = new String(b, "UTF-8");
		return s.trim();
	}

	public static String readStringZH(DataInputStream ins, int len) throws IOException {
		byte[] b = new byte[len];
		ins.read(b);
		String s = new String(b, "UTF-16BE");
		return s;
	}

	/**
	 * UCS2解码
	 * 
	 * @param src
	 *            UCS2 源串
	 * @return 解码后的UTF-16BE字符串
	 */
	public static String DecodeUCS2(String src) {
		byte[] bytes = new byte[src.length() / 2];
		for (int i = 0; i < src.length(); i += 2) {
			bytes[i / 2] = (byte) (Integer.parseInt(src.substring(i, i + 2), 16));
		}
		String reValue = "";
		try {
			reValue = new String(bytes, "UTF-16BE");
		} catch (UnsupportedEncodingException e) {
			reValue = "";
		}
		return reValue;

	}

	/**
	 * 1:
	 * 
	 * @param status
	 *            ״̬
	 * @param authsource
	 * @param sercet
	 * @return
	 */
	public static byte[] getMd5AuthIsmg(int status, byte[] authsource, String sercet) {
		try {
			java.security.MessageDigest md5 = MessageDigest.getInstance("MD5");
			String auth = new String(authsource);
			String authMd5 = status + auth + sercet;
			byte[] data = authMd5.getBytes();
			byte[] md5_result = md5.digest(data);
			return md5_result;
		} catch (Exception ex) {
			// ex.printStackTrace();
			return null;
		}
	}

	/**
	 * 1:�ж��ֽ������Ƿ����
	 * 
	 * @param auth1
	 *            �ֽ�����1
	 * @param auth2
	 *            �ֽ�����2
	 * @return
	 */
	public static boolean byteEquals(byte[] auth1, byte[] auth2) {
		debugData("SP-MD5\n", auth1);
		debugData("IMSG-MD5\n", auth2);
		int length1 = auth1.length;
		int length2 = auth2.length;
		if (length1 != length2) {
			return false;
		}
		for (int i = 0; i < length1; i++) {
			if (auth1[i] != auth2[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * SP���յ�����֤��Ϣ��ҪSPȷ��
	 * 
	 * @return
	 */
	public static byte[] getLoginMD5(String pid, String pwd, String timeStamp) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			String authMd5 = pid + "\0\0\0\0\0\0\0\0\0" + pwd + timeStamp;
			byte[] data = authMd5.getBytes();
			byte[] md5_result = md5.digest(data);
			return md5_result;
		} catch (NoSuchAlgorithmException e) {
			// Client.setText("Util:��ɵ�½�������쳣\n");
		}
		return null;
	}

	/**
	 * 
	 */
	public static int sequenceId = 1;

	public static int getSequence() {
		++sequenceId;
		if (sequenceId > Integer.MAX_VALUE) {
			sequenceId = 0;
		}
		return sequenceId;
	}

	public static long getMsg_Id() {
		return new Date().getTime();
	}

	/**
	 * 获取时间
	 * 
	 * @return
	 */
	public static String getMMDDHHMMSS() {
		SimpleDateFormat fomart = new SimpleDateFormat("MMddhhmmss");
		Date date = new Date();
		return fomart.format(date);
	}

	/**
	 * 
	 * 
	 * @param dos
	 *            输出流
	 * @param length
	 *            所占协议中长度
	 * @param input
	 *            待转化的字符串字符串
	 */
	public static void writeFully(DataOutputStream dos, int length, String input) throws Exception {
		byte[] bb = new byte[length];
		byte[] in = input.getBytes("GBK");
		int len = in.length;
		if (len > bb.length) {
			throw new Exception("传入的数据过长\n");
		}
		dos.write(in);
		while (len < bb.length) {
			dos.writeByte(0);
			len++;
		}

	}

	public static void writeFullyZH(DataOutputStream dos, int length, String input) throws Exception {
		byte[] bb = new byte[length];
		byte[] in = input.getBytes("UnicodeBigUnmarked");
		int len = in.length;
		if (len > bb.length) {
			throw new Exception("传入的数据过长\n");
		}
		dos.write(in);
		while (len < bb.length) {
			dos.writeByte(0);
			len++;
		}

	}

	public static void writeFullyZHLong(DataOutputStream dos, int length, byte[] input) throws Exception {
		System.out.println(length);
		byte[] bb = new byte[length];
		byte[] in = input;
		int len = in.length;
		System.out.println(len + "/" + bb.length);
		if (len > bb.length) {
			throw new Exception("传入的数据过长\n");
		}
		dos.write(in);
		while (len < bb.length) {
			dos.writeByte(0);
			len++;
		}
	}

	/**
	 * 用于查看转换的字节数据，真实情况请使用writeFully
	 * 
	 * @param dos
	 * @param length
	 * @param input
	 * @throws Exception
	 */
	public static void writeFullyTest(DataOutputStream dos, int length, String input) throws Exception {

		byte[] bb = new byte[length];
		byte[] in = input.getBytes("GBK");
		int len = in.length;
		if (len > bb.length) {
			throw new Exception("传入的数据过长\n");
		}
		try {
			// System.out.print("----->");
			for (byte c : in) {
				System.out.print(c + " ");
			}
			// System.out.println();

			dos.write(in);
			while (len < bb.length) {
				dos.writeByte(0);
				len++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * ������Ϣԭʼ���
	 * 
	 * @param dir
	 *            :��Ϣ���ͷ���˵��
	 * @param data
	 *            :��Ϣ���
	 */
	public static void debugData(String dir, byte[] data) {
		StringBuffer sb = new StringBuffer();
		sb.append(dir);
		int count = 0;
		for (int i = 0; i < data.length; i++) {
			int b = data[i];
			if (b < 0) {
				b += 256;
			}
			String hexString = Integer.toHexString(b);
			hexString = (hexString.length() == 1) ? "0" + hexString : hexString;
			sb.append(hexString);
			sb.append("  ");
			count++;
			if (count % 4 == 0) {
				sb.append(" ");
			}
			if (count % 16 == 0) {

				sb.append("\r\n");
			}
		}
		sb.append("\r\n");
		// Client.setText("Util:"+sb.toString());
	}

	/**
	 * ���з���������ǰʱ���ʽ��,��ʽ��Ϊ12/12 06:50
	 * 
	 * @return String
	 */
	public static String getFormatTime() {
		Calendar now = Calendar.getInstance();
		String mon = Integer.toString(now.get(Calendar.MONTH) + 1);
		String day = Integer.toString(now.get(Calendar.DAY_OF_MONTH));
		String hour = Integer.toString(now.get(Calendar.HOUR_OF_DAY));
		String min = Integer.toString(now.get(Calendar.MINUTE));
		String sec = Integer.toString(now.get(Calendar.SECOND));
		mon = (mon.length() == 1) ? "0" + mon : mon;
		day = (day.length() == 1) ? "0" + day : day;
		hour = (hour.length() == 1) ? "0" + hour : hour;
		min = (min.length() == 1) ? "0" + min : min;
		sec = (sec.length() == 1) ? "0" + sec : sec;
		return (mon + "-" + day + " " + hour + ":" + min + ":" + sec);
	}

	/////////////////////////////////////////////////////////
	/**
	 * byte long ת�� ����
	 * 
	 * @throws IOException
	 */
	public static long readLong(DataInputStream din) throws IOException {
		byte[] b = new byte[8];
		din.read(b);
		ByteBuffer buf = ByteBuffer.allocate(8);
		buf.put(b);
		buf.flip();
		long l = Long.reverseBytes(buf.getLong());
		return l;
	}

	public static byte[] writeLong(long l) {
		byte[] b = new byte[8];
		putReverseBytesLong(b, l, 0);
		return b;
	}

	public static byte[] writeInt(int i) {
		byte[] b = new byte[4];
		putReverseBytesInt(b, i, 0);
		putInt(b, i, 0);
		debugData("", b);
		return b;
	}

	public static String getProperties(String key) {
		InputStream in = PropHelper.guessPropFile(PropHelper.class, "cmpppara.properties");
		String values = null;
		if (in != null) {
			Properties pro = new Properties();
			try {
				pro.load(in);
				in.close();
				values = pro.getProperty(key);
			} catch (IOException e) {
			}
		}
		return values;
	}

	public static String[] getPhones(String str, String split) {
		return str.split(split);
	}

	// 短信序列号读写
	public static void writeSequence(String str) {
		try {
			String path = ConfigUtil.getSequence();
			File file = new File(path);
			if (!file.exists())
				file.createNewFile();
			FileOutputStream out = new FileOutputStream(file, false); // 如果追加方式用true
			StringBuffer sb = new StringBuffer();
			sb.append(str);
			out.write(sb.toString().getBytes("utf-8"));// 注意需要转换对应的字符集
			out.close();
		} catch (IOException ex) {
		}
	}

	public static String readSequence() {
		StringBuffer sb = new StringBuffer();
		String tempstr = null;
		try {
			String path = ConfigUtil.getSequence();
			File file = new File(path);
			if (!file.exists())
				throw new FileNotFoundException();
			// BufferedReader br=new BufferedReader(new FileReader(file));
			// while((tempstr=br.readLine())!=null)
			// sb.append(tempstr);
			// 另一种读取方式
			FileInputStream fis = new FileInputStream(file);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			while ((tempstr = br.readLine()) != null)
				sb.append(tempstr);
		} catch (IOException ex) {
		}
		return sb.toString();
	}

	public static void putLong(byte[] bb, long x, int index) {
		bb[index + 0] = (byte) (x >> 56);
		bb[index + 1] = (byte) (x >> 48);
		bb[index + 2] = (byte) (x >> 40);
		bb[index + 3] = (byte) (x >> 32);
		bb[index + 4] = (byte) (x >> 24);
		bb[index + 5] = (byte) (x >> 16);
		bb[index + 6] = (byte) (x >> 8);
		bb[index + 7] = (byte) (x >> 0);
	}

	public static void putReverseBytesLong(byte[] bb, long x, int index) {
		bb[index + 7] = (byte) (x >> 56);
		bb[index + 6] = (byte) (x >> 48);
		bb[index + 5] = (byte) (x >> 40);
		bb[index + 4] = (byte) (x >> 32);
		bb[index + 3] = (byte) (x >> 24);
		bb[index + 2] = (byte) (x >> 16);
		bb[index + 1] = (byte) (x >> 8);
		bb[index + 0] = (byte) (x >> 0);
	}

	public static long getLong(byte[] bb, int index) {
		return ((((long) bb[index + 0] & 0xff) << 56) | (((long) bb[index + 1] & 0xff) << 48)
				| (((long) bb[index + 2] & 0xff) << 40) | (((long) bb[index + 3] & 0xff) << 32)
				| (((long) bb[index + 4] & 0xff) << 24) | (((long) bb[index + 5] & 0xff) << 16)
				| (((long) bb[index + 6] & 0xff) << 8) | (((long) bb[index + 7] & 0xff) << 0));
	}

	public static long getReverseBytesLong(byte[] bb, int index) {
		return ((((long) bb[index + 7] & 0xff) << 56) | (((long) bb[index + 6] & 0xff) << 48)
				| (((long) bb[index + 5] & 0xff) << 40) | (((long) bb[index + 4] & 0xff) << 32)
				| (((long) bb[index + 3] & 0xff) << 24) | (((long) bb[index + 2] & 0xff) << 16)
				| (((long) bb[index + 1] & 0xff) << 8) | (((long) bb[index + 0] & 0xff) << 0));
	}
	//////////////////////////////////////////////////////////////

	public static void putInt(byte[] bb, int x, int index) {
		bb[index + 0] = (byte) (x >> 24);
		bb[index + 1] = (byte) (x >> 16);
		bb[index + 2] = (byte) (x >> 8);
		bb[index + 3] = (byte) (x >> 0);
	}

	public static int getInt(byte[] bb, int index) {
		return (int) ((((bb[index + 0] & 0xff) << 24) | ((bb[index + 1] & 0xff) << 16) | ((bb[index + 2] & 0xff) << 8)
				| ((bb[index + 3] & 0xff) << 0)));
	}

	public static int readInt(DataInputStream din) throws IOException {
		byte[] b = new byte[4];
		din.read(b);
		ByteBuffer buf = ByteBuffer.allocate(4);
		buf.put(b);
		buf.flip();
		int bs = getInt(b, 0);
		return bs;
	}

	public static void putReverseBytesInt(byte[] bb, int x, int index) {
		bb[index + 3] = (byte) (x >> 24);
		bb[index + 2] = (byte) (x >> 16);
		bb[index + 1] = (byte) (x >> 8);
		bb[index + 0] = (byte) (x >> 0);
	}

	public static boolean isContainChinese(String str) {

		Pattern p = Pattern.compile("[\u4e00-\u9fa5]");
		Matcher m = p.matcher(str);
		if (m.find()) {
			return true;
		}
		return false;
	}

	/**
	 * 截取字节
	 * 
	 * @param msg
	 * @param start
	 * @param end
	 * @return
	 */
	public static byte[] getMsgBytes(byte[] msg, int start, int end) {
		byte[] msgByte = new byte[end - start];
		int j = 0;
		for (int i = start; i < end; i++) {
			msgByte[j] = msg[i];
			j++;
		}
		return msgByte;
	}
}
