package com.xfsy.test;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xfsy.util.DBUtil;
import com.yjkj.test.ReceiverClient;
import com.yjkj.test.SendClient;

/**
 * Servlet implementation class TestServlet
 */
@WebServlet("/send")
public class TestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    
    public TestServlet() {
        super();
    }

	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		/*//接收网关短信的手机号码集合
		String recP[]={"1064885137473"};
		//初始化短信发送
		CMPPClient cMPPClient=new CMPPClient("183.230.96.94",17890, "230160", "230160","230160","YJKJ");
		//发送短信
		cMPPClient.sendNotifySms("1064899230160", "123@yangjuntech.com", recP);*/
		/*SendClient cMPPClient=new SendClient("183.230.96.94", 17890, "230160", "230160","230160","YJKJ");
		cMPPClient.startSocket();
		//发送短信
		cMPPClient.sendNotifySms("1064899230160", "中文测试", "1064885137473");*/
		DBUtil.getTask();
	}

}
