package com.ibagroup.workfusion.rpa.core.robots.factory;

import java.lang.reflect.Method;
import java.util.Date;

import com.ibagroup.workfusion.rpa.core.mis.IRobotLogger;
import com.ibagroup.workfusion.rpa.core.mis.LoggableMethod;
import com.ibagroup.workfusion.rpa.core.mis.LoggablePerform;
import com.ibagroup.workfusion.rpa.core.mis.TaskAction;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerMethodWrapper extends MethodWrapper {

	private static final Logger logger = LoggerFactory.getLogger(LoggerMethodWrapper.class);

	private static final String PERFORM = "perform";

	private IRobotLogger robotLogger;
	Long startTime;
	String module;
	String operation;
	boolean transactional;

	public LoggerMethodWrapper(IRobotLogger robotLogger) {
		super();
		this.robotLogger = robotLogger;
	}

	@Override
	void before(Method thisMethod) {
		startTime = new Date().getTime();
		if (thisMethod.isAnnotationPresent(LoggablePerform.class)) {
			operation = PERFORM;
			module = thisMethod.getAnnotation(LoggablePerform.class).module();
		} else if (thisMethod.isAnnotationPresent(LoggableMethod.class)) {
			operation = thisMethod.getAnnotation(LoggableMethod.class).operation();
			module = thisMethod.getAnnotation(LoggableMethod.class).module();
			transactional = thisMethod.getAnnotation(LoggableMethod.class).transactional();
		}
		RunnerContext.cleanActionData();
	}

	@Override
	void after(Method thisMethod) {
		if (!thisMethod.isAnnotationPresent(LoggablePerform.class)
				&& !thisMethod.isAnnotationPresent(LoggableMethod.class)) {
			return;
		}

		if (CollectionUtils.isNotEmpty(RunnerContext.getLastDescriptions())) {
			String des1 = "";
			try {
				des1 = RunnerContext.getLastDescriptions().get(0);
			} catch (Exception e) {
				logger.debug("empty description for TaskAction in RunnerContext");
			}
			String des2 = "";
			try {
				des2 = RunnerContext.getLastDescriptions().get(1);
			} catch (Exception e) {
				logger.debug("empty description for TaskAction in RunnerContext");
			}
			String des3 = "";
			try {
				des3 = RunnerContext.getLastDescriptions().get(2);
			} catch (Exception e) {
				logger.debug("empty description for TaskAction in RunnerContext");
			}

			TaskAction action = new TaskAction(module, operation, RunnerContext.getLastResult(), startTime, des1, des2,
					des3, transactional);
			robotLogger.addAction(action);
		} else {
			TaskAction action = new TaskAction(module, operation, RunnerContext.getLastResult(), startTime,
					transactional);
			robotLogger.addAction(action);
		}
		RunnerContext.cleanActionData();
	}

	@Override
	void exceptionHandling(Throwable exception) {
		TaskAction action = new TaskAction(module, operation, TaskAction.Result.EXCEPTION, startTime,
				ExceptionUtils.getStackTrace(exception), null, null, transactional);
		robotLogger.addAction(action);

	}

}
