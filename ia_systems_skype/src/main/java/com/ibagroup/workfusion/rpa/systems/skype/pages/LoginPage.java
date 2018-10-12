package com.ibagroup.workfusion.rpa.systems.skype.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.freedomoss.crowdcontrol.webharvest.web.dto.SecureEntryDTO;
import com.ibagroup.workfusion.rpa.core.clients.RobotDriverWrapper;
import com.ibagroup.workfusion.rpa.core.config.ConfigurationManager;
import com.ibagroup.workfusion.rpa.core.pagefactory.Wait;
import com.ibagroup.workfusion.rpa.core.pagefactory.Wait.WaitFunc;
import com.workfusion.rpa.helpers.RPA;
import com.ibagroup.workfusion.rpa.systems.skype.clients.SkypeClient;
import com.ibagroup.workfusion.rpa.systems.skype.popups.PopupHandled;

public class LoginPage extends RobotDriverWrapper {

    @Wait(waitFunc = WaitFunc.CLICKABLE, value = 30)
    @FindBy(css = "[CLASS:Edit; INSTANCE:1]")
    private WebElement userName;

    public LoginPage(ConfigurationManager cmn) {
        super(cmn);
        waitAndSwitchToWindow(SkypeClient.SEARCH_SKYPE_LOGIN_WINDOW, Integer.parseInt(getCfg().getConfigItem("skype.start_timeout", "30")));
    }

    public MainPage login(SecureEntryDTO logonCreds) {
        new PopupHandled(getCfg()).closeUpdatePopup(SkypeClient.SEARCH_SKYPE_LOGIN_WINDOW);
        RPA.sleep(1000);
        userName.click();
        userName.clear();
        userName.sendKeys(logonCreds.getKey());

        getDriver().getKeyboard().sendKeys(Keys.ENTER);

        waitForElement(ExpectedConditions.elementToBeClickable(By.cssSelector("[CLASS:Internet Explorer_Server; INSTANCE:1]")), 30);
        RPA.sleep(1000);

        getDriver().getKeyboard().sendKeys(logonCreds.getValue());
        getDriver().getKeyboard().sendKeys(Keys.ENTER);

        return new MainPage(getCfg(), this);
    }
}