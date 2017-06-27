package com.yjkj.quartz;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

public class updateNumberTask {
	
	/**
	 * 
	 * @throws SchedulerException
	 */
	public void start() throws SchedulerException {
		JobDetail job = JobBuilder.newJob(updateNuberJob.class)
				.withIdentity("updateNumberJob", "dbUtilJob").build();
		Trigger trigger = TriggerBuilder
				.newTrigger()
				.withIdentity("updateNumberTrigger", "dbUtilTrigger")
				.withSchedule(CronScheduleBuilder.cronSchedule("0 0 23 * * ? *"))
				.build();
		Scheduler scheduler = new StdSchedulerFactory().getScheduler();
		scheduler.start();
		scheduler.scheduleJob(job, trigger);
	}
}
