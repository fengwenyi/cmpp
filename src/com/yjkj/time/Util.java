package com.yjkj.time;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

public class Util {

	public static List<String> timeTaskAll() throws SchedulerException {
		List<String> list = new ArrayList<>();
		Scheduler scheduler = new StdSchedulerFactory().getScheduler();
		for (String groupName : scheduler.getJobGroupNames()) {
			for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
				String jobName = jobKey.getName();
				String jobGroup = jobKey.getGroup();
				// get job's trigger
				@SuppressWarnings("unchecked")
				List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
				Date nextFireTime = triggers.get(0).getNextFireTime();
				//"[jobName] : " + jobName + " [groupName] : " + jobGroup + " - " + nextFireTime
				String timeTask = "[jobName] : " + jobName + " [groupName] : " + jobGroup + " - " + nextFireTime;
				list.add(timeTask);
			}
		}
		return list;
	}
}
