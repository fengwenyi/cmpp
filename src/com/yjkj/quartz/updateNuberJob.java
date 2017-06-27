package com.yjkj.quartz;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.xfsy.util.ConfigUtil;
import com.yjkj.test.API;

public class updateNuberJob implements Job {

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		/*String string = read();
		String[] strings = getPhones(string, ";");
		for(int i = 0; i < strings.length; i++) {
			//System.out.println(strings[i]);
			String sql = strings[i].substring(49, strings[i].length());
			System.out.println(sql);
		}*/
	}
	

	
		

		public static String read() {
			StringBuffer sb = new StringBuffer();
			String tempstr = null;
			try {
				String path = ConfigUtil.getUpdateNumberSql();
				File file = new File(path);
				if (!file.exists())
					throw new FileNotFoundException();
				FileInputStream fis = new FileInputStream(file);
				BufferedReader br = new BufferedReader(new InputStreamReader(fis));
				while ((tempstr = br.readLine()) != null)
					sb.append(tempstr);
			} catch (IOException ex) {
			}
			return sb.toString();
		}

}
