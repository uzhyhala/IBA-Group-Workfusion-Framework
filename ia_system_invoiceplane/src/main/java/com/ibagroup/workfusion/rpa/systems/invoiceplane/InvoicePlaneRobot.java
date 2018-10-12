package com.ibagroup.workfusion.rpa.systems.invoiceplane;

import com.freedomoss.crowdcontrol.webharvest.web.dto.SecureEntryDTO;
import com.ibagroup.workfusion.rpa.core.config.ConfigurationManager;
import com.ibagroup.workfusion.rpa.core.robots.RobotProtocol;
import com.ibagroup.workfusion.rpa.systems.invoiceplane.clients.InvoicePlaneClient;
import com.ibagroup.workfusion.rpa.systems.invoiceplane.pages.LoginPage;
import com.ibagroup.workfusion.rpa.systems.invoiceplane.pages.MainPage;
import com.ibagroup.workfusion.rpa.systems.invoiceplane.pages.MenuNavigationBar;

public interface InvoicePlaneRobot extends RobotProtocol {
	
    default MainPage initRobot(SecureEntryDTO loginCreds) {
    	InvoicePlaneClient client = new InvoicePlaneClient(getCfg());

        LoginPage loginPage = client.getLoginPage();
        storeCurrentMetadata();

        MainPage mainPage = loginPage.login(loginCreds);
        setMainPage(mainPage);
        storeCurrentMetadata();

        MenuNavigationBar menuNavigationBar = new MenuNavigationBar(getCfg());
        setMenuNavigationBar(menuNavigationBar);

        return mainPage;
    }

    default void finiliseRobot() {
        MenuNavigationBar menuNavigationBar = getMenuNavigationBar();
        if (menuNavigationBar != null) {
            getMenuNavigationBar().logout();

            setMenuNavigationBar(null);
        }
    }

    ConfigurationManager getCfg();

    MainPage getMainPage();
    void setMainPage(MainPage mainPage);

    MenuNavigationBar getMenuNavigationBar();
    void setMenuNavigationBar(MenuNavigationBar menuNavigationBar);

}