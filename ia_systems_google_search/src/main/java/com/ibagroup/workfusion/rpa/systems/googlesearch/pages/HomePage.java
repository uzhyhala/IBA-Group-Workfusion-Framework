package com.ibagroup.workfusion.rpa.systems.googlesearch.pages;

import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.ibagroup.workfusion.rpa.core.clients.RobotDriverWrapper;
import com.ibagroup.workfusion.rpa.core.config.ConfigurationManager;
import com.ibagroup.workfusion.rpa.core.pagefactory.Wait;
import com.ibagroup.workfusion.rpa.core.pagefactory.Wait.WaitFunc;

public class HomePage extends RobotDriverWrapper {

    private static final int WAIT_FIELD = 20;

    @FindBy(xpath = "//input[@type='text']")
    @Wait(waitFunc = WaitFunc.CLICKABLE, value = WAIT_FIELD)
    private WebElement searchField;

    public HomePage(ConfigurationManager cmn) {
        super(cmn);
    }

    public ResultPage search(String searchString) {
        searchField.click();
        searchField.clear();
        searchField.sendKeys(searchString);

        getDriver().getKeyboard().sendKeys(Keys.ENTER);

        return new ResultPage(getCfg());
    }

}