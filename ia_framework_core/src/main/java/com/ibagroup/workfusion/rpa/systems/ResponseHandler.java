package com.ibagroup.workfusion.rpa.systems;

public interface ResponseHandler<RESP, RESP_SRV> {

    RESP handleResponse(RESP_SRV response);

}
