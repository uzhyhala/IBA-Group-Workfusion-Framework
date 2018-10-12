package com.ibagroup.workfusion.rpa.core.robots;

import com.ibagroup.workfusion.rpa.core.mis.IRobotLogger;
import com.ibagroup.workfusion.rpa.core.mis.TaskAction;
import com.ibagroup.workfusion.rpa.core.robots.factory.RunnerContext;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import groovy.lang.Binding;
import com.ibagroup.workfusion.rpa.core.annotations.Wire;
import com.ibagroup.workfusion.rpa.core.config.ConfigurationManager;
import com.ibagroup.workfusion.rpa.core.exceptions.ExceptionHandler;
import com.ibagroup.workfusion.rpa.core.metadata.MetadataManager;
import com.ibagroup.workfusion.rpa.core.metadata.types.LoggingMetadata;

public abstract class RobotCapabilities implements RobotProtocol {

	@Wire
	private Binding binding;
	@Wire
	private ExceptionHandler exceptionHandler;
	@Wire
	private ConfigurationManager cfg;
	@Wire
	private MetadataManager metadataManager;
	@Wire
	private IRobotLogger robotLogger;

	@Override
	public void storeCurrentActionResult(TaskAction.Result result, String... description) {
		RunnerContext.setLastResult(result, description);
	}

	public RobotCapabilities() {
		super();
	}

	public Binding getBinding() {
		return binding;
	}

	public MetadataManager getMetadataManager() {
		return metadataManager;
	}

	public ConfigurationManager getCfg() {
		return cfg;
	}

	public ExceptionHandler getExceptionHandler() {
		return exceptionHandler;
	}

	public IRobotLogger getRobotLogger() {
		return robotLogger;
	}

	@Override
	public boolean storeCurrentMetadata() {
		String toString = ReflectionToStringBuilder.toString(this, ToStringStyle.MULTI_LINE_STYLE).toString();
		getMetadataManager().addMetadata(new LoggingMetadata("toString", toString));
		return true;
	}

}
