package com.ibagroup.workfusion.rpa.systems.invoiceplane.pages;

import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.freedomoss.crowdcontrol.webharvest.web.dto.SecureEntryDTO;
import com.ibagroup.workfusion.rpa.core.clients.RobotDriverWrapper;
import com.ibagroup.workfusion.rpa.core.config.ConfigurationManager;
import com.ibagroup.workfusion.rpa.core.pagefactory.Wait;
import com.ibagroup.workfusion.rpa.core.pagefactory.Wait.WaitFunc;
import com.ibagroup.workfusion.rpa.systems.InvalidLoginException;

public class LoginPage extends RobotDriverWrapper {

    private static final Logger logger = LoggerFactory.getLogger(LoginPage.class);

    private static final int WAIT_FIELD = 20;

    @FindBy(id = "email")
    @Wait(waitFunc = WaitFunc.CLICKABLE, value = WAIT_FIELD)
    private WebElement email;

    @FindBy(id = "password")
    @Wait(waitFunc = WaitFunc.CLICKABLE, value = WAIT_FIELD)
    private WebElement password;

    @FindBy(xpath = "//input[@type='submit']")
    @Wait(waitFunc = WaitFunc.CLICKABLE, value = WAIT_FIELD)
    private WebElement submit;

    @Wait(waitFunc = WaitFunc.VISIBLE, value = 40)
    @FindBy(xpath = "//*[self::div[@id='main-area'] or self::div[@id='login']/div[contains(@class,'alert')]]")
    private WebElement loginFailed;

    @FindBy(xpath = "//div[@id='login']/div[contains(@class,'alert')]")
    @Wait(waitFunc = WaitFunc.VISIBLE, value = 5)
    private WebElement loginFailedMessage;

    public LoginPage(ConfigurationManager cmn) {
        super(cmn);
    }

    public MainPage login(SecureEntryDTO invoicePlaneCred) {
        email.click();
        email.clear();
        email.sendKeys(invoicePlaneCred.getKey());

        password.click();
        password.clear();
        password.sendKeys(invoicePlaneCred.getValue());

        submit.click();

        try {
        	String actualId = loginFailed.getAttribute("id");
        	logger.debug(actualId);
            if (!"main-area".equalsIgnoreCase(actualId)) {
                throw new InvalidLoginException(loginFailedMessage.getText() + "\n" + "User Id: " + invoicePlaneCred.getKey());
            }
        } catch (TimeoutException e) {
            logger.debug("Unkown error during Invoice Place authorisation process.");
            throw new RuntimeException();
        }

        return new MainPage(getCfg());
    }

}