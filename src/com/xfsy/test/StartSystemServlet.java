package com.xfsy.test;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.xfsy.entity.Config;
import com.xfsy.util.Constant;
import com.xfsy.util.DBUtil;
import com.yjkj.test.ReceiverClient;

/**
 * Servlet implementation class StartSystemServlet
 */
@WebServlet("/start")
public class StartSystemServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public StartSystemServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#service(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//开启系统，接收短信
		/*ReceiverClient cMPPClient=new ReceiverClient("183.230.96.94",17890, "230160", "230160","230160","YJKJ");
		cMPPClient.sendNotifySms("1064899230160", "123@yangjuntech.com", "1064885137473");*/
		response.setCharacterEncoding("UTF-8");
		PrintWriter pw = response.getWriter();
		List<Config> configList = DBUtil.getAllConfig();
		for (int i = 0; i < 1; i++) {
			Config config = configList.get(i);
			String spid = config.getSpId();
			String password = config.getSharedSecret();
			String msgsrc = config.getMsgSrc();
			String serviceId = config.getServiceId();
			ReceiverClient client=new ReceiverClient(Constant.ISMP_IP, Constant.ISMP_POST, spid, password, msgsrc, serviceId);
			client.start();
		}
		
	}

}
