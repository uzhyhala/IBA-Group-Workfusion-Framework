package com.ibagroup.workfusion.rpa.systems;

public interface SameRequestToService<REQ> extends RequestPreparer<REQ, REQ> {

    default REQ prepareRequest(REQ response) {
        return response;
    }

}
