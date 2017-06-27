package com.yjkj.time;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.yjkj.test.API;

public class SendJob implements Job {

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		JobDataMap jobDataMap = arg0.getJobDetail().getJobDataMap();
		String company = jobDataMap.getString("company");
		String customer = jobDataMap.getString("customer");
		String phone = jobDataMap.getString("phone");
		String msg = jobDataMap.getString("msg");
		String st_id = jobDataMap.getString("st_id");
		API.sendTimeData(phone, customer, msg, company, st_id);
	}

}
