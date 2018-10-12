package com.ibagroup.workfusion.rpa.robots.frameworktraining;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.freedomoss.workfusion.utils.gson.GsonUtils;
import com.ibagroup.workfusion.rpa.core.BindingUtils;
import com.ibagroup.workfusion.rpa.core.MachineTask;
import com.ibagroup.workfusion.rpa.core.annotations.Wire;
import com.ibagroup.workfusion.rpa.core.datastore.DataStoreInsert;
import com.ibagroup.workfusion.rpa.core.robots.RobotCapabilities;
import com.ibagroup.workfusion.rpa.systems.invoiceplane.to.ProductTO;

public class SaveRecordToDS extends RobotCapabilities {

	private static final Logger logger = LoggerFactory.getLogger(SaveRecordToDS.class);

    @Wire(name = TrainingConstants.PRODUCT_JSON)
    private String productJson;

	private Map<String, String> inputAsMap;

	public Map<String, String> perform() {
        String processGuid = BindingUtils.getWebHarvestTaskItem(getBinding()).getRun().getRootRunUuid();
        logger.info("processGuid: " + processGuid);

	    inputAsMap = BindingUtils.getInputAsMap(getBinding(), null);

        ProductTO productTO = GsonUtils.GSON.<ProductTO>fromJson(productJson, ProductTO.class);

        Map<String, String> rowsMap = new HashMap<>();
        rowsMap.put(MachineTask.PROCESS_UUID, processGuid);
        rowsMap.put(TrainingConstants.GOOGLESEARCH_RPA_DURATION, inputAsMap.get(TrainingConstants.GOOGLESEARCH_RPA_DURATION));
        rowsMap.put(TrainingConstants.INVOICEPLANE_RPA_DURATION, inputAsMap.get(TrainingConstants.INVOICEPLANE_RPA_DURATION));
        rowsMap.put(TrainingConstants.PRODUCT_RESULT_JSON, inputAsMap.get(TrainingConstants.PRODUCT_RESULT_JSON));
        rowsMap.put(TrainingConstants.PRODUCT_NAME, productTO.getProductName());

		DataStoreInsert dsInsert = new DataStoreInsert(getBinding());
		dsInsert.insertRow(TrainingConstants.INVOICEPLANE_RECORDS_DS, rowsMap);

        return rowsMap;
	}
}