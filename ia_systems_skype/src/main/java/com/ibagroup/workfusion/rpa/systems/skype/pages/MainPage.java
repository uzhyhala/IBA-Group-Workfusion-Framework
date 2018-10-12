package com.ibagroup.workfusion.rpa.systems.skype.pages;

import java.util.List;

import com.ibagroup.workfusion.rpa.systems.skype.clients.SkypeClient;
import com.ibagroup.workfusion.rpa.systems.skype.popups.PopupHandled;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibagroup.workfusion.rpa.core.clients.RobotDriverWrapper;
import com.ibagroup.workfusion.rpa.core.config.ConfigurationManager;
import com.ibagroup.workfusion.rpa.core.config.ConfigurationManager.Formatter;
import com.ibagroup.workfusion.rpa.core.pagefactory.Wait;
import com.ibagroup.workfusion.rpa.core.pagefactory.Wait.WaitFunc;
import com.workfusion.rpa.helpers.RPA;

public class MainPage extends RobotDriverWrapper {

    private static final Logger logger = LoggerFactory.getLogger(MainPage.class);

    @Wait(waitFunc = WaitFunc.CLICKABLE, value = 20)
    @FindBy(css = "[CLASS:TSearchControl; INSTANCE:1]")
    private WebElement searchControl;

    @Wait(waitFunc = WaitFunc.CLICKABLE, value = 20)
    @FindBy(css = "[CLASS:TChatRichEdit; INSTANCE:1]")
    private WebElement messageArea;

    private LoginPage loginPage;

    public MainPage(ConfigurationManager cmn, LoginPage loginPage) {
        super(cmn);
        this.loginPage = loginPage;
        waitAndSwitchToWindow(SkypeClient.SEARCH_SKYPE_MAIN_WINDOW, 30);
    }

    public void sendMessageToRecipient(String recipientLogin, List<String> messages) {
        new PopupHandled(getCfg()).closeUpdatePopup(SkypeClient.SEARCH_SKYPE_MAIN_WINDOW);
      
        searchControl.click();
        getDriver().getKeyboard().sendKeys(recipientLogin);
        getDriver().getKeyboard().sendKeys(Keys.ENTER);

        messageArea.click();
        for (String message : messages) {
            RPA.setClipboardText(message);
            getDriver().getKeyboard().sendKeys(("{CTRLDOWN}v{CTRLUP}"));
            getDriver().getKeyboard().sendKeys(Keys.ENTER);
        }
        RPA.sleep(getCfg().getConfigItem("skype.loading_wait_time_after_login", 5000, Formatter.INT));
    }

    public LoginPage logout() {
        logger.info("Pressing logout button");

        getDriver().getKeyboard().sendKeys(("{ALTDOWN}s{ALTUP}"));
        getDriver().getKeyboard().sendKeys("{UP 2}");
        getDriver().getKeyboard().sendKeys(Keys.ENTER);

        waitAndSwitchToWindow(SkypeClient.SEARCH_SKYPE_LOGIN_WINDOW, 20);

        return loginPage;
    }
}
