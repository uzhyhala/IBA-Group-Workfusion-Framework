package com.ibagroup.workfusion.rpa.core.robots.factory;

import com.ibagroup.workfusion.rpa.core.mis.IRobotLogger;
import groovy.lang.Binding;
import com.ibagroup.workfusion.rpa.core.exceptions.ExceptionHandler;
import com.ibagroup.workfusion.rpa.core.metadata.MetadataManager;
import com.ibagroup.workfusion.rpa.core.metadata.storage.MetadataPermanentStorage;

public class MethodAdapterBuilder {
	
	private final Binding binding;
    private final boolean uploadAfterEachPerform;
	private final boolean uploadAfterFailure;
    private MetadataPermanentStorage metadataPermanentStorage;
    private ExceptionHandler exceptionHandler;
    private MetadataManager metadataManager;
    private IRobotLogger robotLogger;
    private final boolean doNotThrowException;
    
    public MethodAdapterBuilder(Binding binding, boolean uploadAfterEachPerform, boolean uploadAfterFailure, boolean doNotThrowException, MetadataPermanentStorage metadataPermanentStorage, ExceptionHandler exceptionHandler, MetadataManager metadataManager, IRobotLogger robotLogger) {
        this.binding = binding;
        this.doNotThrowException = doNotThrowException;
        this.uploadAfterEachPerform = uploadAfterEachPerform;
        this.uploadAfterFailure = uploadAfterFailure;
        this.metadataPermanentStorage = metadataPermanentStorage;
        this.exceptionHandler = exceptionHandler;
        this.metadataManager = metadataManager;
        this.robotLogger = robotLogger;
    }
	
	MethodAdapter build(Class<? extends MethodAdapter> adapterClass) throws Exception{
		if (adapterClass.equals(PerformMethodAdapter.class) ){
			MethodAdapter adapter = adapterClass.getConstructor(boolean.class, boolean.class, boolean.class, Binding.class, MetadataPermanentStorage.class, ExceptionHandler.class, MetadataManager.class, IRobotLogger.class).
					newInstance(uploadAfterEachPerform, uploadAfterFailure, doNotThrowException, binding, metadataPermanentStorage, exceptionHandler, metadataManager, robotLogger);
    		return adapter;
		}
		if (adapterClass.equals(LoggerMethodAdapter.class) ){
			MethodAdapter adapter = adapterClass.getConstructor(IRobotLogger.class).
					newInstance(robotLogger);
    		return adapter;
		}
		return null;
	}
}
