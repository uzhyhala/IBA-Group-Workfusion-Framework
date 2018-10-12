package com.ibagroup.workfusion.rpa.core.robots.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.ibagroup.workfusion.rpa.core.CommonConstants;
import com.ibagroup.workfusion.rpa.core.config.ConfigurationManager;
import com.ibagroup.workfusion.rpa.core.exceptions.ExceptionHandler;
import com.ibagroup.workfusion.rpa.core.metadata.MetadataManager;
import com.ibagroup.workfusion.rpa.core.metadata.storage.MetadataPermanentStorage;
import com.ibagroup.workfusion.rpa.core.mis.IRobotLogger;
import groovy.lang.Binding;

public class RobotsFactoryBuilder {

    private boolean handlersInsideBinding = false;

    private Binding binding;
    private ExceptionHandler exceptionHandler;
    private ConfigurationManager configurationManager;
    private MetadataManager metadataManager;
    private MetadataPermanentStorage metadataPermanentStorage;
    private IRobotLogger robotLogger;

    private List<Class<? extends MethodAdapter>> methodAdapters = new ArrayList<Class<? extends MethodAdapter>>();

    private List<Object> wiringObjs = new ArrayList<>();

    private boolean uploadAfterEachPerform = false;
    private boolean uploadAfterFailure = true;

    private boolean doNotReThrowException = false;
    private boolean doNotUsePerformAdapter = false;

    private MethodAdapterBuilder methodAdapterBuilder;

    public RobotsFactory build() {
        preBuild();
        return new RobotsFactory(getMapOfWiredObjects(), getMethodAdapters(), doNotReThrowException, methodAdapterBuilder);
    }

    @SuppressWarnings("unchecked")
    protected void preBuild() {
        if (!doNotUsePerformAdapter) {
            // add default performMethodAdapter
            addMethodAdapter(PerformMethodAdapter.class);
            addMethodAdapter(LoggerMethodAdapter.class);
        }

        boolean debugMode = configurationManager.getConfigItem(CommonConstants.DEBUG_MODE_ON, false, ConfigurationManager.Formatter.BOOLEAN);
        setUploadAfterEachPerform(debugMode);
        // if debugMode is ON then upload metadata to S3 in any case
        if (!debugMode) {
            setUploadAfterFailure(configurationManager.getConfigItem(CommonConstants.UPLOAD_AFTER_FAILURE, true, ConfigurationManager.Formatter.BOOLEAN));
        }

        if (handlersInsideBinding && null != binding) {
            binding.getVariables().forEach((key, value) -> {
                if (null == exceptionHandler && value instanceof ExceptionHandler) {
                    exceptionHandler = (ExceptionHandler) value;
                }
                if (null == configurationManager && value instanceof ConfigurationManager) {
                    configurationManager = (ConfigurationManager) value;
                }
                if (null == metadataManager && value instanceof MetadataManager) {
                    metadataManager = (MetadataManager) value;
                }
            });
        }
        
        methodAdapterBuilder = new MethodAdapterBuilder(binding, uploadAfterEachPerform, uploadAfterFailure, doNotReThrowException, metadataPermanentStorage, exceptionHandler, metadataManager, robotLogger);
    }

    public Map<Class<?>, Object> getMapOfWiredObjects() {
        return Arrays.asList(exceptionHandler, configurationManager, metadataManager, robotLogger, binding).stream()
                .filter(item -> item != null).filter(item -> null != item)
                .collect(Collectors.toMap(value -> value.getClass(), value -> value, (value1, value2) -> value1));
    }

    public <T extends ConfigurationManager & ExceptionHandler & MetadataManager> RobotsFactoryBuilder initHandlers(T init) {
        exceptionHandler = init;
        configurationManager = init;
        metadataManager = init;
        return this;
    }

    public RobotsFactoryBuilder useBingingToFetchHandlers() {
        this.handlersInsideBinding = true;
        return this;
    }
    
    private RobotsFactoryBuilder setUploadAfterFailure(Boolean upload) {
        this.uploadAfterFailure = upload;
        return this;
    }

    public RobotsFactoryBuilder setUploadAfterFailure() {
        this.uploadAfterFailure = true;
        return this;
    }

    public boolean isUploadAfterFailure() {
        return uploadAfterFailure;
    }

    private RobotsFactoryBuilder setUploadAfterEachPerform(Boolean upload) {
        this.uploadAfterEachPerform = upload;
        return this;
    }

    public RobotsFactoryBuilder uploadAfterEachPerform() {
        this.uploadAfterEachPerform = true;
        return this;
    }

    public boolean isUploadAfterEachPerform() {
        return uploadAfterEachPerform;
    }

    public RobotsFactoryBuilder doNotReThrowException() {
        this.doNotReThrowException = true;
        return this;
    }

    public boolean isDoNotReThrowException() {
        return doNotReThrowException;
    }

    public boolean isDoNotUsePerformAdapter() {
        return doNotUsePerformAdapter;
    }

    public RobotsFactoryBuilder doNotUsePerformAdapter() {
        this.doNotUsePerformAdapter = true;
        return this;
    }

    public RobotsFactoryBuilder setBinding(Binding binding) {
        this.binding = binding;
        return this;
    }

    public RobotsFactoryBuilder setExHandler(ExceptionHandler exHandler) {
        this.exceptionHandler = exHandler;
        return this;
    }

    public ExceptionHandler getExHandler() {
        return exceptionHandler;
    }

    public RobotsFactoryBuilder setCfg(ConfigurationManager cfg) {
        this.configurationManager = cfg;
        return this;
    }

    public RobotsFactoryBuilder setActivityMgr(MetadataManager activityMgr) {
        this.metadataManager = activityMgr;
        return this;
    }

    public MetadataManager getActivityMgr() {
        return metadataManager;
    }

    public RobotsFactoryBuilder addWiringObjs(Object obj) {
        this.wiringObjs.add(obj);
        return this;
    }

    public List<Object> getWiringObjs() {
        return wiringObjs;
    }

    public RobotsFactoryBuilder setActivitiesStorage(MetadataPermanentStorage activitiesStorage) {
        this.metadataPermanentStorage = activitiesStorage;
        return this;
    }

    public MetadataPermanentStorage getActivitiesStorage() {
        return metadataPermanentStorage;
    }

    public RobotsFactoryBuilder addMethodAdapter(Class<? extends MethodAdapter> methodAdapterClass) {
        this.methodAdapters.add(methodAdapterClass);
        return this;
    }

    public RobotsFactoryBuilder addMethodAdapters(List<Class<Class<? extends MethodAdapter>>> methodAdapterClasses) {
        this.methodAdapters.addAll(methodAdapters);
        return this;
    }

    public List<Class<? extends MethodAdapter>> getMethodAdapters() {
        return methodAdapters;
    }

	public IRobotLogger getRobotLogger() {
		return robotLogger;
	}

	public RobotsFactoryBuilder setRobotLogger(IRobotLogger robotLogger) {
		this.robotLogger = robotLogger;
		return this;
	}

}
