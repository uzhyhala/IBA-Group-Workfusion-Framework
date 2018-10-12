package com.ibagroup.workfusion.rpa.systems.invoiceplane.clients;

import java.util.concurrent.TimeUnit;

import com.ibagroup.workfusion.rpa.core.clients.RobotDriverWrapper;
import com.ibagroup.workfusion.rpa.core.config.ConfigurationManager;
import com.ibagroup.workfusion.rpa.systems.invoiceplane.pages.LoginPage;

public class InvoicePlaneClient extends RobotDriverWrapper {

	public InvoicePlaneClient(ConfigurationManager cmn) {
		super(cmn);
		initDriver();
	}

    private void initDriver() {
        getDriver().manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS).pageLoadTimeout(90, TimeUnit.SECONDS);
        getDriver().manage().deleteAllCookies();
    }

    public LoginPage getLoginPage() {
        getDriver().get(getCfg().getConfigItem("invoicePlane_site_url"));
        return new LoginPage(getCfg());
    }

}
