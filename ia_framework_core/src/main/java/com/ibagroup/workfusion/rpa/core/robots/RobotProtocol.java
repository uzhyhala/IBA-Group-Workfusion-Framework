package com.ibagroup.workfusion.rpa.core.robots;

import com.ibagroup.workfusion.rpa.core.mis.TaskAction;

public interface RobotProtocol {

    boolean storeCurrentMetadata();
    
    //void storeCurrentActionResult(Result result);
    
    void storeCurrentActionResult(TaskAction.Result result, String... description);
    //void storeCurrentActionDescription(Result result);

}
