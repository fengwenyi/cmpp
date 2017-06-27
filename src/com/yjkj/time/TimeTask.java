package com.yjkj.time;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

public class TimeTask {
	
	/**
	 * 启动一个线程来执行定时任务
	 * @param company
	 * @param customer
	 * @param cron
	 * @throws SchedulerException
	 */
	public void start(String company, String customer, String cron, String phone, String msg, String st_id) throws SchedulerException {
		//job name = sendJob + 当前时间戳
		//trigger name = sendTrigger + 当前时间戳
		//group name = company + customer
		JobDetail job = JobBuilder.newJob(SendJob.class)
				.withIdentity("sendJob" + System.currentTimeMillis(), company + company).build();
		job.getJobDataMap().put("company", company);
		job.getJobDataMap().put("customer", customer);
		job.getJobDataMap().put("phone", phone);
		job.getJobDataMap().put("msg", msg);
		job.getJobDataMap().put("st_id", st_id);
		Trigger trigger = TriggerBuilder
				.newTrigger()
				.withIdentity("sendTrigger" + System.currentTimeMillis(), company + customer)
				.withSchedule(CronScheduleBuilder.cronSchedule(cron))
				.build();
		Scheduler scheduler = new StdSchedulerFactory().getScheduler();
		scheduler.start();
		scheduler.scheduleJob(job, trigger);
	}
}
