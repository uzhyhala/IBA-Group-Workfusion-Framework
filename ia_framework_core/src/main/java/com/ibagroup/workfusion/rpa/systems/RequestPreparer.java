package com.ibagroup.workfusion.rpa.systems;

public interface RequestPreparer<REQ, REQ_SRV> {

    REQ_SRV prepareRequest(REQ requestStr);

}
