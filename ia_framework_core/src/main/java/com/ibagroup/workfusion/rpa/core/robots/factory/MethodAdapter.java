package com.ibagroup.workfusion.rpa.core.robots.factory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import com.ibagroup.workfusion.rpa.core.annotations.OnError;

public abstract class MethodAdapter {

	static List<Class<?>> getIfs() {
		return null;
	}

	/**
	 *
	 * @see MethodFilter#isHandled(Method)
	 *
	 * @param m
	 *            - method to check from base robot
	 * @return true if handled an no further process required
	 */
	static boolean isHandled(Method m) {
		return false;
	}

	/**
	 *
	 * @see MethodHandler#invoke(Object, Method, Method, Object[])
	 *
	 * @param self
	 *            - robot
	 * @param thisMethod
	 *            - overloaded method
	 * @param proceed
	 *            - robots method
	 * @param args
	 *            - arguments
	 * @return Return result with true if no further process required
	 * @throws Throwable
	 *             - throws any exception that can occur
	 */
	abstract ReturnResult invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable;

	public ReturnResult handlerError(Object self, Method thisMethod, Method proceed, Object[] args,
			Throwable throwable) {
		// invoking GenerateUuid method
		try {
			Optional<Method> any = MethodUtils.getMethodsListWithAnnotation(self.getClass(), OnError.class).stream()
					.findAny();
			if (any.isPresent()) {
				Method method = any.get();

				Object invoke;
				if (method.getParameterCount() > 1) {
					Object[] newArgs = ArrayUtils.add(new Object[] { self, thisMethod, proceed, throwable }, args);
					invoke = method.invoke(self, newArgs);
				} else if (method.getParameterCount() == 1) {
					invoke = method.invoke(self, throwable);
				} else {
					invoke = method.invoke(self);
				}

				return new ReturnResult(true, invoke);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return new ReturnResult(false, null);
	}

	static class ReturnResult {
		private final boolean handled;
		private final Object result;

		public ReturnResult(boolean handled, Object result) {
			super();
			this.handled = handled;
			this.result = result;
		}

		public boolean isHandled() {
			return handled;
		}

		public Object getResult() {
			return result;
		}

	}
}
