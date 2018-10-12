package com.ibagroup.workfusion.rpa.core.robots.factory;

import java.lang.reflect.Method;
import java.util.List;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import com.ibagroup.workfusion.rpa.core.mis.IRobotLogger;
import com.ibagroup.workfusion.rpa.core.mis.LoggableMethod;

public class LoggerMethodAdapter extends MethodAdapter {

//	private static final Logger logger = LoggerFactory.getLogger(PerformMethodAdapter.class);

	

	private final LoggerMethodWrapper loggerMethodWrapper;

	public LoggerMethodAdapter(IRobotLogger robotLogger) {
		super();
		this.loggerMethodWrapper = new LoggerMethodWrapper(robotLogger);
	}

	public static List<Class<?>> getIfs() {
		return null;
	}

	public static boolean isHandled(Method m) {
		return m.isAnnotationPresent(LoggableMethod.class);
	}

	@Override
	public ReturnResult invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
		loggerMethodWrapper.before(thisMethod);
		Object result = proceed.invoke(self, args);
		loggerMethodWrapper.after(thisMethod);
		return new ReturnResult(true, result);
	}

	public ReturnResult handlerError(Object self, Method thisMethod, Method proceed, Object[] args,
			Throwable throwable) {
		loggerMethodWrapper.exceptionHandling(throwable);
		throw new RuntimeException(throwable);

	}

}
