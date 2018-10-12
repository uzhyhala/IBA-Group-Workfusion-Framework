package com.ibagroup.workfusion.rpa.core.robots.factory;

import java.lang.reflect.Method;

public abstract class MethodWrapper implements Wrapper {
	
	abstract void before(Method thisMethod);
	
	abstract void after(Method thisMethod);
	
	abstract void exceptionHandling(Throwable exception);

}
