package com.bdp.schedule.service.impl;

import java.util.List;

import javax.annotation.PostConstruct;

import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.ListenerManager;
import org.quartz.Scheduler;
import org.quartz.TriggerKey;
import org.quartz.spi.MutableTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bdp.schedule.dto.ScheduleInfo;
import com.bdp.schedule.dto.TriggerInfo;
import com.bdp.schedule.impl.ScheduleJob;
import com.bdp.schedule.listener.GlobalJobListener;
import com.bdp.schedule.listener.GlobalSchedulerListener;
import com.bdp.schedule.listener.GlobalTriggerListener;
import com.bdp.schedule.service.ScheduleService;
import com.bdp.schedule.trigger.TriggerCreator;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ScheduleServiceImpl implements ScheduleService {

	@Autowired
	private List<TriggerCreator> triggerCreators;

	// Spring MVC中内置的，可直接使用
	@Autowired
	private ObjectMapper oMapper;

	/**
	 * 直接注入，这是由SchedulerFactoryBean自动创建的。
	 * 因为SchedulerFactoryBean实现了spring的FactoryBean接口
	 */
	@Autowired
	private Scheduler scheduler;

	@Autowired
	private GlobalJobListener jobListener;

	@Autowired
	private GlobalTriggerListener triggerListener;

	@Autowired
	private GlobalSchedulerListener schedulerListener;

	@PostConstruct
	public void init() throws Exception {
		ListenerManager listenerMgr = scheduler.getListenerManager();
		listenerMgr.addJobListener(jobListener);
		listenerMgr.addTriggerListener(triggerListener);
		listenerMgr.addSchedulerListener(schedulerListener);
	}

	@Override
	public void create(ScheduleInfo scheduleInfo) throws Exception {
		TriggerInfo triggerInfo = scheduleInfo.getTriggerInfo();
		CronTrigger trigger = null;
		for (TriggerCreator creator : triggerCreators) {
			if (creator.isSupport(triggerInfo)) {
				trigger = creator.createTrigger(triggerInfo);
				TriggerKey triggerKey = createTriggerKey(scheduleInfo.getAppId(), scheduleInfo.getGroup(),
						scheduleInfo.getName());
				((MutableTrigger) trigger).setKey(triggerKey);
				break;
			}
		}
		if (trigger != null) {
			throw new Exception("未找到对应的触发器生成类,triggerInfo:" + oMapper.writeValueAsString(triggerInfo));
		}
		JobKey jobKey = createJobKey(scheduleInfo.getAppId(), scheduleInfo.getGroup(), scheduleInfo.getName());
		JobDetail job = JobBuilder.newJob(ScheduleJob.class).withIdentity(jobKey).build();
		job.getJobDataMap().put(ScheduleInfo.SCHEDULEINFO_KEY_IN_JOBDETAIL, scheduleInfo);
		scheduler.scheduleJob(job, trigger);
	}

	@Override
	public void update(ScheduleInfo scheduleInfo) throws Exception {
		TriggerInfo triggerInfo = scheduleInfo.getTriggerInfo();
		CronTrigger trigger = null;
		TriggerKey triggerKey = createTriggerKey(scheduleInfo.getAppId(), scheduleInfo.getGroup(),
				scheduleInfo.getName());
		for (TriggerCreator creator : triggerCreators) {
			if (creator.isSupport(triggerInfo)) {
				trigger = creator.createTrigger(triggerInfo);
				((MutableTrigger) trigger).setKey(triggerKey);
				break;
			}
		}
		if (trigger != null) {
			throw new Exception("未找到对应的触发器生成类,triggerInfo:" + oMapper.writeValueAsString(triggerInfo));
		}
		scheduler.rescheduleJob(triggerKey, trigger);
	}

	@Override
	public ScheduleInfo get(String appId, String group, String name) throws Exception {
		return null;
	}

	@Override
	public List<ScheduleInfo> list(String appId) throws Exception {
		return null;
	}

	@Override
	public void delete(String appId, String group, String name) throws Exception {

	}

	private JobKey createJobKey(String appId, String group, String name) {
		return new JobKey(name + "@" + appId, group + "@" + appId);
	}

	private TriggerKey createTriggerKey(String appId, String group, String name) {
		return new TriggerKey(name + "@" + appId, group + "@" + appId);
	}

}
