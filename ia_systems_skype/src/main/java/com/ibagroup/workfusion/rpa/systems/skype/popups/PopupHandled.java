package com.ibagroup.workfusion.rpa.systems.skype.popups;

import org.openqa.selenium.By;
import com.ibagroup.workfusion.rpa.core.clients.RobotDriverWrapper;
import com.ibagroup.workfusion.rpa.core.config.ConfigurationManager;

public class PopupHandled extends RobotDriverWrapper {

  private static final String UPDATE_POPUP = "[CLASS:TUpgradeForm;REGEXPTITLE:(.*)Skype(.*)]";
  
  public PopupHandled(ConfigurationManager cmn) {
    super(cmn);
  }

  public void closeUpdatePopup(String window){
    waitAndSwitchToWindow(UPDATE_POPUP, 30);
    getDriver().findElement(By.cssSelector("[CLASS:Button; INSTANCE: 2]")).click();
    waitAndSwitchToWindow(window, Integer.parseInt(getCfg().getConfigItem("skype.start_timeout", "30")));
  }
}
