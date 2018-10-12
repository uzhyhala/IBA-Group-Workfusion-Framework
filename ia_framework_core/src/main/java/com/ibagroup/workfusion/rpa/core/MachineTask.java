package com.ibagroup.workfusion.rpa.core;

import com.freedomoss.crowdcontrol.webharvest.CampaignDto;
import com.freedomoss.crowdcontrol.webharvest.RunDto;
import com.freedomoss.crowdcontrol.webharvest.WebHarvestTaskItem;
import com.ibagroup.workfusion.rpa.core.mis.IRobotLogger;
import groovy.lang.Binding;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webharvest.utils.SystemUtilities;
import com.ibagroup.workfusion.rpa.core.config.ConfigurationManager;
import com.ibagroup.workfusion.rpa.core.config.DataStoreConfiguration;
import com.ibagroup.workfusion.rpa.core.exceptions.ExceptionHandler;
import com.ibagroup.workfusion.rpa.core.metadata.MetadataListManager;
import com.ibagroup.workfusion.rpa.core.metadata.MetadataManager;
import com.ibagroup.workfusion.rpa.core.metadata.storage.MetadataStorage;
import com.ibagroup.workfusion.rpa.core.metadata.storage.MetadataPermanentStorage;
import com.ibagroup.workfusion.rpa.core.metadata.types.Metadata;
import com.ibagroup.workfusion.rpa.core.mis.RobotLogger;
import com.ibagroup.workfusion.rpa.core.mis.TaskAction;
import com.ibagroup.workfusion.rpa.core.mis.TaskDetail;
import com.ibagroup.workfusion.rpa.core.robots.factory.RunnerContext;
import com.ibagroup.workfusion.rpa.core.storage.S3Manager;
import com.ibagroup.workfusion.rpa.core.storage.StorageManager;

public class MachineTask implements ConfigurationManager, ExceptionHandler, MetadataManager, MetadataPermanentStorage, IRobotLogger {

    private static final Logger logger = LoggerFactory.getLogger(MachineTask.class);

    // name for Item UUID
    public static final String PROCESS_UUID = "processguid"; // column
    // name for Process (run) UUID

    private String lastErrorMessage = "";
    private final SystemUtilities sys;
    private final Binding binding;
    private final MetadataManager metadataManagerManager;
    private final DataStoreConfiguration cfg;
    private final MetadataPermanentStorage metadataPermanentStorage;
    private final IRobotLogger robotLogger;

    public MachineTask(Binding binding) {
       this(binding, null);
    }
    
	public MachineTask(Binding binding, String dsName) {
		this.binding = binding;
		this.sys = BindingUtils.getSys(binding);
		this.cfg = new DataStoreConfiguration(binding, dsName);
		if(!cfg.isLocal()){
        	RunnerContext.setCampaignUuid(BindingUtils.getWebHarvestTaskItem(binding).getCampaignDto().getCampaignMapDto().getParentCampaignUuid());
        }
        RunnerContext.setRunUuid(BindingUtils.getWebHarvestTaskItem(binding).getRun().getRootRunUuid());
        this.robotLogger = new RobotLogger(binding, getConfigItem("bp_actions"),getConfigItem("bp_details"));
        StorageManager storageMgr = new S3Manager(binding, getConfigItem("screenshots_bucket"), getConfigItem("screenshots_folder"));
        this.metadataManagerManager = new MetadataListManager(binding);
        
      //  robotLogger.addAction(actions);

        metadataPermanentStorage = new MetadataStorage(binding, storageMgr, metadataManagerManager, () -> {
            WebHarvestTaskItem item = BindingUtils.getWebHarvestTaskItem(binding);
            RunDto runDto = item.getRun();
            CampaignDto campaignDto = item.getCampaignDto();
            String runname = runDto.getCampaignName();
            String taskName = campaignDto.getTitle().equals("WWI TODO: Config name should be here") ? "local-task" : campaignDto.getTitle();

            String submissionId = BindingUtils.getPropertyValue(binding, CommonConstants.ITEM_UUID_CLM);
            //String processGuid = BindingUtils.getPropertyValue(binding, PROCESS_UUID);
            String processGuid =  BindingUtils.getWebHarvestTaskItem(getBinding()).getRun().getRootRunUuid();
            String taskPath = "";

            if (StringUtils.isNotBlank(processGuid)) {
                taskPath += processGuid + "/";
            }

            if (StringUtils.isNotBlank(submissionId)) {
                taskPath += submissionId + "/";
            }

            taskPath += runname + "_" + new Date().toString().replaceAll("\\s", "_") + "/" + taskName;
            return taskPath;
        });
	}

    @Override
    public String getLastErrorMessage() {
        return lastErrorMessage;
    }

    @Override
    public void setLastErrorMessage(String lastErrorMessage) {
        setLastErrorMessage(lastErrorMessage, null);
    }

    @Override
    public void setLastErrorMessage(String shortMsg, Throwable exception) {
        logger.error(shortMsg, exception);
        Logger log = BindingUtils.getTypedPropertyValue(binding, "log");
        log.error(shortMsg, exception);
        this.lastErrorMessage = shortMsg;
        if (null != exception) {
            this.lastErrorMessage += " : " + ExceptionUtils.getStackTrace(exception).toString();
        }
        sys.defineVariable(CommonConstants.ERR_MESSAGE_FIELD, lastErrorMessage, true);
    }

    @Override
    public void logCurrentException() {
        setLastErrorMessage(BindingUtils.getPropertyValue(binding, "_exception_message"), BindingUtils.getTypedPropertyValue(binding, "_exception"));
    }

    @Override
    public void logCurrentException(Throwable throwable) {
        setLastErrorMessage(ExceptionUtils.getMessage(throwable), throwable);
    }

    @Override
    public String getConfigItem(String keyParam) {
        return cfg.getConfigItem(keyParam);
    }

    @Override
    public boolean isLocal() {
        return cfg.isLocal();
    }

    @Override
    public void addMetadata(Metadata... activities) {
        metadataManagerManager.addMetadata(activities);
    }

    @Override
    public List<Metadata> getMetadataList() {
        return metadataManagerManager.getMetadataList();
    }

    @Override
    public boolean storeAllMetadata() {
        return metadataPermanentStorage.storeAllMetadata();
    }

    @Override
    public void clearMetadata() {
        metadataManagerManager.clearMetadata();
    }

    public Binding getBinding() {
        return binding;
    }

    @Override
    public boolean storeAllMetadata(String uploadUid) {
        return metadataPermanentStorage.storeAllMetadata(uploadUid);
    }
    
    @Override
	public void addAction(TaskAction... actions) {
		robotLogger.addAction(actions);
	}

	@Override
	public List<TaskAction> getActions() {
		return robotLogger.getActions();
	}

	@Override
	public void clearActions() {
		robotLogger.clearActions();
	}

	@Override
	public void addDetails(TaskDetail... details) {
		robotLogger.addDetails(details);
		
	}

	@Override
	public List<TaskDetail> getDetails() {
		return robotLogger.getDetails();
	}

	@Override
	public void clearDetails() {
		robotLogger.clearDetails();
		
	}

	@Override
	public boolean storeLogs() {
		return robotLogger.storeLogs();
		
	}

}