package com.alibaba.otter.manager.biz.monitor.impl;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.alibaba.otter.manager.biz.common.alarm.DragoonAlarmService;
import com.alibaba.otter.manager.biz.config.record.LogRecordService;
import com.alibaba.otter.manager.biz.monitor.AlarmController;
import com.alibaba.otter.manager.biz.monitor.Monitor;
import com.alibaba.otter.manager.biz.monitor.PassiveMonitor;
import com.alibaba.otter.shared.common.model.config.alarm.AlarmRule;
import com.alibaba.otter.shared.common.model.config.alarm.MonitorName;
import com.alibaba.otter.shared.common.model.config.pipeline.Pipeline;
import com.alibaba.otter.shared.common.model.config.record.LogRecord;

/**
 * 一类报警规则的匹配规则和报警
 * 
 * @author zebin.xuzb @ 2012-8-29
 * @version 4.1.0
 */
public abstract class AbstractRuleMonitor implements Monitor, PassiveMonitor {

    protected static final Logger log = LoggerFactory.getLogger("monitorInfo");

    @Resource(name = "dragoonAlarmService")
    private DragoonAlarmService   dragoonAlarmService;

    @Resource(name = "logRecordService")
    private LogRecordService      logRecordService;

    @Resource(name = "alarmController")
    private AlarmController       alarmController;

    @Override
    public void explore() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void explore(Long... pipelineIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void feed(Object data, Long pipelineId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void feed(Object data, List<AlarmRule> rules) {
        throw new UnsupportedOperationException();
    }

    protected void sendAlarm(AlarmRule rule, String message) {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put(rule.getReceiverKey(), "otter");
        data.put("message", message);

        data = alarmController.control(rule, message, data);
        postProcessAlarmData(data);

        if (CollectionUtils.isEmpty(data)) {
            log.info("has suppressed alarm : " + message);
            return;
        }

        String[] receiverKeys = StringUtils.split(rule.getReceiverKey(), ',');
        for (String receiverKey : receiverKeys) {
            Map<String, Object> sendData = new HashMap<String, Object>(data);
            sendData.put("receiveKey", receiverKey);

            dragoonAlarmService.sendAlarm(sendData);
            log.info("has send alarm : " + message + "; rule is " + sendData);
        }
    }

    protected void logRecordAlarm(Long pipelineId, MonitorName monitorName, String message) {
        logRecordAlarm(pipelineId, -1l, monitorName, message);
    }

    protected void logRecordAlarm(Long pipelineId, Long nodeId, MonitorName monitorName, String message) {

        Pipeline pipeline = new Pipeline();
        pipeline.setId(pipelineId);
        LogRecord logRecord = new LogRecord();
        logRecord.setTitle(monitorName.toString());
        logRecord.setNid(nodeId);
        logRecord.setPipeline(pipeline);
        logRecord.setMessage(message);
        logRecordService.create(logRecord);
    }

    protected void postProcessAlarmData(Map<String, Object> data) {
        // do nothing by default
    }

    // 规则一般是 xxxx@16:00-24:00,00:00-06:00
    // 目前仅仅是局限在某一天的时间段中
    protected boolean inPeriod(AlarmRule alarmRule) {
        String rule = alarmRule.getMatchValue();
        if (StringUtils.isEmpty(rule)) {
            log.info("rule is empty " + alarmRule);
            return false;
        }

        String periods = StringUtils.substringAfterLast(rule, "@");
        if (StringUtils.isEmpty(periods)) {
            // 没有时间要求，则任务在报警时间段内
            return isInPeriodWhenNoPeriod();
        }

        Calendar calendar = currentCalendar();
        periods = StringUtils.trim(periods);
        for (String period : StringUtils.split(periods, ",")) {
            String[] startAndEnd = StringUtils.split(period, "-");
            if (startAndEnd == null || startAndEnd.length != 2) {
                log.error("error period time format in rule : " + alarmRule);
                return isInPeriodWhenErrorFormat();
            }

            String start = startAndEnd[0];
            String end = startAndEnd[1];
            if (checkInPeriod(calendar, start, end)) {
                log.info("rule is in period : " + alarmRule);
                return true;
            }
        }

        log.info("rule is not in period : " + alarmRule);
        return false;
    }

    protected boolean checkInPeriod(Calendar now, String start, String end) {
        return isAfter(now, start) && !isAfter(now, end);
    }

    protected boolean isAfter(Calendar now, String time) {
        String[] hourAndMin = StringUtils.split(time, ":");
        if (hourAndMin == null || hourAndMin.length != 2) {
            log.error("error period time format in rule : " + time);
            return isInPeriodWhenErrorFormat();
        }

        int hour;
        int min;
        try {
            hour = Integer.parseInt(hourAndMin[0]);
            min = Integer.parseInt(hourAndMin[1]);
        } catch (NumberFormatException e) {
            log.error("error period time format in rule : " + time, e);
            return isInPeriodWhenErrorFormat();
        }

        if (hour > 24 || min > 60) {
            log.error("error period time format in rule : " + time);
            return isInPeriodWhenErrorFormat();
        }

        Calendar when = (Calendar) now.clone();
        when.set(Calendar.HOUR_OF_DAY, hour);
        when.set(Calendar.MINUTE, min);

        return !now.before(when);
    }

    /**
     * 如果时间段格式有问题，则默认是在时间段内。子类可以修改此策略
     */
    protected boolean isInPeriodWhenErrorFormat() {
        return true;
    }

    /**
     * 如果没有时间段格式，则默认是在时间段内。子类可以修改此策略
     */
    protected boolean isInPeriodWhenNoPeriod() {
        return true;
    }

    /**
     * 返回当前的时间，方便测试
     */
    protected Calendar currentCalendar() {
        Calendar calendar = Calendar.getInstance();
        return calendar;
    }

}
