package com.ibagroup.workfusion.rpa.systems.invoiceplane.pages;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibagroup.workfusion.rpa.core.clients.RobotDriverWrapper;
import com.ibagroup.workfusion.rpa.core.config.ConfigurationManager;
import com.ibagroup.workfusion.rpa.core.pagefactory.Wait;
import com.ibagroup.workfusion.rpa.core.pagefactory.Wait.WaitFunc;

public class MenuNavigationBar extends RobotDriverWrapper {

    private static final Logger logger = LoggerFactory.getLogger(MenuNavigationBar.class);
    
    @Wait(waitFunc = WaitFunc.CLICKABLE, value = 40)
    @FindBy(xpath = "//a[contains(@class,'logout')]")
    private WebElement logoutButton;

    @Wait(waitFunc = WaitFunc.CLICKABLE, value = 40)
    @FindBy(xpath = "//*[@id='ip-navbar-collapse']//li/a[text()='Dashboard']")
    private WebElement dashboardMenu;

    @Wait(waitFunc = WaitFunc.CLICKABLE, value = 40)
    @FindBy(xpath = "//*[@id='ip-navbar-collapse']//li/a//span[text()='Products']/parent::*")
    private WebElement productsMenu;

    @Wait(waitFunc = WaitFunc.CLICKABLE, value = 40)
    @FindBy(xpath = "//*[@id='ip-navbar-collapse']//li/ul/li/a[text()='View products']")
    private WebElement viewProductsMenuItem;

    public MenuNavigationBar(ConfigurationManager cmn) {
        super(cmn);
    }

    public ProductsPage openProducts() {
    	productsMenu.click();
    	viewProductsMenuItem.click();

    	return new ProductsPage(getCfg());
    }

    public void openDashboard() {
    	dashboardMenu.click();
    }

    // If it is necessary to logout from Invoice Plane explicitly
    public void logout() {
        try {
            logoutButton.click();
        } catch (TimeoutException ex) {
            logger.info("Timed out on waiting logout button");
        }
    }

}
