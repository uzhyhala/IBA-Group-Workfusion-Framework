package com.ibagroup.workfusion.rpa.robots.frameworktraining;

import com.ibagroup.workfusion.rpa.core.BindingUtils;
import com.ibagroup.workfusion.rpa.core.MachineTask;
import com.ibagroup.workfusion.rpa.core.datastore.DataStoreQuery;
import com.ibagroup.workfusion.rpa.core.robots.RobotCapabilities;

public class WaitForAllRecords extends RobotCapabilities {

	public int perform() {
        String processGuid = BindingUtils.getWebHarvestTaskItem(getBinding()).getRun().getRootRunUuid();

        int rowsCount = new DataStoreQuery(getBinding()).executeQuery(TrainingConstants.INVOICEPLANE_RECORDS_DS,
                "select * from @this where " + MachineTask.PROCESS_UUID + "='" + processGuid + "'").getNumberOfRowsAffected();

        return rowsCount;
	}

}