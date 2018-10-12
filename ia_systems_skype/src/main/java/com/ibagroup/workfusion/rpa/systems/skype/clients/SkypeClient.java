package com.ibagroup.workfusion.rpa.systems.skype.clients;

import java.util.concurrent.TimeUnit;

import com.ibagroup.workfusion.rpa.core.clients.RobotDriverWrapper;
import com.ibagroup.workfusion.rpa.core.config.ConfigurationManager;
import com.ibagroup.workfusion.rpa.systems.skype.pages.LoginPage;

public class SkypeClient extends RobotDriverWrapper {

    public static final String SEARCH_SKYPE_LOGIN_WINDOW = "[CLASS:TLoginForm;REGEXPTITLE:(.*)Skype(.*)]";
    public static final String SEARCH_SKYPE_MAIN_WINDOW  = "[CLASS:tSkMainForm]";

	public SkypeClient(ConfigurationManager cmn) {
		super(cmn);
		initDriver();
	}

    private void initDriver() {
        getDriver().manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        getDriver().manage().timeouts().pageLoadTimeout(2, TimeUnit.SECONDS);
    }

    public void start() {
        String ndsUrl = getCfg().getConfigItem("skype.start_url", "skype.exe");

        getDriver().get(ndsUrl);
    }

    public LoginPage getLoginPage() {
        start();
        return new LoginPage(getCfg());
    }

}
