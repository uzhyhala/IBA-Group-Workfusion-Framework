package com.ibagroup.workfusion.rpa.systems.skype;

import com.freedomoss.crowdcontrol.webharvest.web.dto.SecureEntryDTO;
import com.ibagroup.workfusion.rpa.core.config.ConfigurationManager;
import com.ibagroup.workfusion.rpa.core.robots.RobotProtocol;
import com.ibagroup.workfusion.rpa.systems.skype.clients.SkypeClient;
import com.ibagroup.workfusion.rpa.systems.skype.pages.LoginPage;
import com.ibagroup.workfusion.rpa.systems.skype.pages.MainPage;

public interface SkypeRobot extends RobotProtocol {
	
    default void initRobot(SecureEntryDTO loginCreds) {
    	SkypeClient client = new SkypeClient(getCfg());

        LoginPage loginPage = client.getLoginPage();
        storeCurrentMetadata();

        MainPage mainPage = loginPage.login(loginCreds);
        setMainPage(mainPage);
        storeCurrentMetadata();
    }

    default void finiliseRobot() {
        MainPage mainPage = getMainPage();
        if (mainPage != null) {
            mainPage.logout();

            setMainPage(null);
        }
    }

    ConfigurationManager getCfg();

    MainPage getMainPage();
    void setMainPage(MainPage mainPage);

}