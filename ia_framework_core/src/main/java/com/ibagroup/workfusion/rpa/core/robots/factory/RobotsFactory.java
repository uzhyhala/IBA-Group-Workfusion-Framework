package com.ibagroup.workfusion.rpa.core.robots.factory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import groovy.lang.Binding;
import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import com.ibagroup.workfusion.rpa.core.annotations.AfterInit;
import com.ibagroup.workfusion.rpa.core.config.ConfigurationManager;
import com.ibagroup.workfusion.rpa.core.exceptions.ExceptionHandler;
import com.ibagroup.workfusion.rpa.core.metadata.MetadataManager;
import com.ibagroup.workfusion.rpa.core.robots.factory.MethodAdapter.ReturnResult;

public class RobotsFactory {
	private static final Logger logger = LoggerFactory.getLogger(RobotsFactory.class);
	private final List<Class<? extends MethodAdapter>> methodAdapters = new ArrayList<Class<? extends MethodAdapter>>();

	private final Binding binding;

	private final Map<Class<?>, Object> wiringObjects;

	private MethodFilter methodFilter = null;
	private MethodHandler methodHandler = null;
	private MethodAdapterBuilder methodAdapterBuilder;
	private final boolean doNotThrowException;

	public RobotsFactory(Map<Class<?>, Object> objMap, List<Class<? extends MethodAdapter>> methodAdapters,
			boolean doNotThrowException, MethodAdapterBuilder methodAdapterBuilder) {
		this.methodAdapters.addAll(methodAdapters);
		this.doNotThrowException = doNotThrowException;
		this.wiringObjects = objMap;
		this.methodAdapterBuilder = methodAdapterBuilder;
		this.binding = (Binding) this.wiringObjects.get(Binding.class);
	}

	@SuppressWarnings("unchecked")
	public <T> T newInstance(Class<T> clazz) {
		ProxyFactory factory = new ProxyFactory();
		factory.setSuperclass(clazz);
		List<Class<?>> ifs = getIfs();
		if (!CollectionUtils.isEmpty(ifs)) {
			factory.setInterfaces(ifs.toArray(new Class<?>[0]));
		}

		factory.setFilter(getMethodFilter());

		try {
			T robot = (T) factory.create(new Class<?>[0], new Object[0], getMethodHandler());
			RobotsFactoryHelper.wireObject(robot, wiringObjects, binding);

			MethodUtils.getMethodsListWithAnnotation(clazz, AfterInit.class).stream().forEach(method -> {
				try {
					method.invoke(robot);
				} catch (Throwable e) {
					throw new RuntimeException(e);
				}
			});

			return robot;
		} catch (Throwable cause) {
			throw new RuntimeException(cause);
		}
	}

	private List<Class<?>> getIfs() {
		List<Class<?>> result = new ArrayList<>();
		methodAdapters.forEach(adapter -> {
			if (null != adapter) {
				List<Class<?>> ifs = null;
				try {
					ifs = (List<Class<?>>) adapter.getMethod("getIfs").invoke(null);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					logger.error("[RobotsFactory] PLEASE double check your MethodAdapter" + adapter.toString(), e);
					throw new RuntimeException(e);
				}
				// List<Class<?>> ifs = adapter.getIfs();
				if (!CollectionUtils.isEmpty(ifs)) {
					result.addAll(ifs);
				}
			}
		});
		return result;
	}

	private MethodFilter getMethodFilter() {
		if (null == methodFilter) {
			methodFilter = new MethodFilter() {

				@Override
				public boolean isHandled(Method m) {
					for (Iterator<Class<? extends MethodAdapter>> iterator = methodAdapters.iterator(); iterator
							.hasNext();) {
						Class<? extends MethodAdapter> methodAdapter = iterator.next();
						try {
							if ((boolean) methodAdapter.getMethod("isHandled", Method.class).invoke(null, m)) {
								return true;
							}
						} catch (Exception e) {
							logger.error(
									"[RobotsFactory] PLEASE double check your MethodAdapter" + methodAdapter.toString(),
									e);
							throw new RuntimeException(e);
						}
					}
					return false;
				}
			};
		}
		return methodFilter;
	}

	private MethodHandler getMethodHandler() {
		if (null == methodHandler) {
			methodHandler = new MethodHandler() {

				@Override
				public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
					try {

						for (Iterator<Class<? extends MethodAdapter>> iterator = methodAdapters.iterator(); iterator
								.hasNext();) {
							Class<? extends MethodAdapter> adapterClass = iterator.next();
							boolean isHandled = (boolean) adapterClass.getMethod("isHandled", Method.class).invoke(null,
									thisMethod);
							if (isHandled) {
								MethodAdapter methodAdapter = methodAdapterBuilder.build(adapterClass);
								try {
									ReturnResult result = methodAdapter.invoke(self, thisMethod, proceed, args);
									if (result.isHandled()) {
										Object resObj = result.getResult();
										return resObj != null ? resObj
												: RobotsFactoryHelper.defaultReturnValue(thisMethod);
									}
								} catch (Throwable throwable) {
									ReturnResult handleResult = methodAdapter.handlerError(self, thisMethod, proceed,
											args, throwable);
									if (handleResult.isHandled()) {
										Object resObj = handleResult.getResult();
										return resObj != null ? resObj
												: RobotsFactoryHelper.defaultReturnValue(thisMethod);
									} else {
										throw new RuntimeException(throwable);
									}
								}
							}
						}

						return proceed.invoke(self, args);
					} catch (Throwable e) {
						if (doNotThrowException) {
							logger.error(e.getMessage(), e);
							return RobotsFactoryHelper.defaultReturnValue(thisMethod);
						}
						throw e;
					}
				}
			};
		}
		return methodHandler;
	}

	void checkVars() {
		Validate.isTrue(Arrays.asList(ConfigurationManager.class, ExceptionHandler.class, MetadataManager.class)
				.stream().allMatch(clz -> wiringObjects.containsKey(clz)));
	}
}
