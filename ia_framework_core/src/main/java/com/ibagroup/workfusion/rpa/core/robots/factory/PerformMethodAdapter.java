package com.ibagroup.workfusion.rpa.core.robots.factory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import com.ibagroup.workfusion.rpa.core.mis.IRobotLogger;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.freedomoss.crowdcontrol.webharvest.web.dto.SecureEntryDTO;
import groovy.lang.Binding;
import com.ibagroup.workfusion.rpa.core.CommonConstants;
import com.ibagroup.workfusion.rpa.core.annotations.PrePerform;
import com.ibagroup.workfusion.rpa.core.exceptions.ExceptionHandler;
import com.ibagroup.workfusion.rpa.core.metadata.MetadataManager;
import com.ibagroup.workfusion.rpa.core.metadata.storage.MetadataPermanentStorage;
import com.ibagroup.workfusion.rpa.core.metadata.types.LoggingMetadata;
import com.ibagroup.workfusion.rpa.core.robots.RobotProtocol;
import com.ibagroup.workfusion.rpa.core.security.SecurityUtils;

public class PerformMethodAdapter extends MethodAdapter {

	private static final Logger logger = LoggerFactory.getLogger(PerformMethodAdapter.class);

	private static final List<String> PERFORM = Arrays.asList("perform");

	private final boolean uploadAfterEachPerform;
    private final boolean uploadAfterFailure;
	private final boolean doNotReThrowException;
	private final MetadataPermanentStorage metadataPermanentStorage;
	private final ExceptionHandler exceptionHandler;
	private final MetadataManager metadataManager;
	private final Binding binding;
	private final IRobotLogger robotLogger;
	private final LoggerMethodWrapper loggerMethodWrapper;
	private final LoggerDetailsWrapper loggerDetailsWrapper;

	public PerformMethodAdapter(boolean uploadAfterEachPerform, boolean uploadAfterFailure, boolean doNotReThrowException, Binding binding,
			MetadataPermanentStorage metadataPermanentStorage, ExceptionHandler exceptionHandler,
			MetadataManager metadataManager, IRobotLogger robotLogger) {
		super();
		this.uploadAfterEachPerform = uploadAfterEachPerform;
        this.uploadAfterFailure = uploadAfterFailure;
		this.doNotReThrowException = doNotReThrowException;
		this.metadataPermanentStorage = metadataPermanentStorage;
		this.exceptionHandler = exceptionHandler;
		this.metadataManager = metadataManager;
		this.binding = binding;
		this.robotLogger = robotLogger;
		this.loggerMethodWrapper = new LoggerMethodWrapper(robotLogger);
		this.loggerDetailsWrapper = new LoggerDetailsWrapper(robotLogger);

	}

	public static List<Class<?>> getIfs() {
		return null;
	}

	public static boolean isHandled(Method m) {
		return PERFORM.stream().anyMatch(name -> m.getName().matches(name));
	}

	@Override
	public ReturnResult invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
		// invoking perform.* method
		loggerDetailsWrapper.onstart(self);
		loggerMethodWrapper.before(thisMethod);

		MethodUtils.getMethodsListWithAnnotation(self.getClass(), PrePerform.class).stream().forEach(method -> {
			try {
				method.invoke(self);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		});

		SecureEntryDTO secureEntryDTO = extractSecureEntry(args);
		SecurityUtils rpaRssSecurityUtils = null;
		try {
			if (secureEntryDTO != null) {
				rpaRssSecurityUtils = new SecurityUtils(binding);
				rpaRssSecurityUtils.updateUserAliasesPerApplication(secureEntryDTO.getAlias(), secureEntryDTO.getKey(),
						CommonConstants.ACTIVE);
			}

			Object result = proceed.invoke(self, args);
			RunnerContext.setRecordUuid(CommonConstants.DUMMY_UUID);
			loggerMethodWrapper.after(thisMethod);
			// === Workaround solution for BULK mechanism ===
			// All records exceptions are catch by Robot itself and add metadata with name "stacktrace".
			// If such metadata exists once all records were processed then we need to upload methadata to S3.
			// ==============================================
			boolean isFailureFound = false;
			if (metadataManager != null) {
			    isFailureFound = metadataManager.getMetadataList().stream().anyMatch(metadata -> {
			        return metadata.getName().contains("stacktrace");
			    });
			}
			if (metadataPermanentStorage != null && (uploadAfterEachPerform || (uploadAfterFailure && isFailureFound))) {
				metadataPermanentStorage.storeAllMetadata(getUploadUuid(self, thisMethod, proceed, args));
			}
			loggerDetailsWrapper.oncompletion(self);
			robotLogger.storeLogs();
			return new ReturnResult(true, result);
		} finally {
			if (secureEntryDTO != null) {
				rpaRssSecurityUtils.updateUserAliasesPerApplication(secureEntryDTO.getAlias(), secureEntryDTO.getKey(),
						CommonConstants.INACTIVE);
			}
		}
	}

	protected SecureEntryDTO extractSecureEntry(Object[] args) {
		if (null != args) {
			for (int i = 0; i < args.length; i++) {
				if (null != args[i] && SecureEntryDTO.class.isAssignableFrom(args[i].getClass())) {
					return (SecureEntryDTO) args[i];
				}
			}
		}

		return null;
	}

	protected String getUploadUuid(Object self, Method thisMethod, Method proceed, Object[] args) {
		return null;
	}

	public ReturnResult handlerError(Object self, Method thisMethod, Method proceed, Object[] args,
			Throwable throwable) {
		RobotProtocol runner = (RobotProtocol) self;
		String err_message = ExceptionUtils.getMessage(throwable) + ": " + ExceptionUtils.getStackTrace(throwable);
		loggerMethodWrapper.exceptionHandling(throwable);
		logger.info("Exception " + err_message + " occured");
		if (null != exceptionHandler) {
			exceptionHandler.logCurrentException(throwable);
		}
		if (null != metadataManager) {
			metadataManager.addMetadata(new LoggingMetadata("stacktrace", err_message));
		}
		runner.storeCurrentMetadata();
		logger.info("Activity stored after exception: " + throwable.getMessage());

		ReturnResult handleResult = super.handlerError(self, thisMethod, proceed, args, throwable);

		if (uploadAfterFailure && null != metadataPermanentStorage) {
			metadataPermanentStorage.storeAllMetadata(getUploadUuid(self, thisMethod, proceed, args));
		}
		robotLogger.storeLogs();
		if (!doNotReThrowException) {
			throw new RuntimeException(throwable);
		}
		return handleResult;
	}

}