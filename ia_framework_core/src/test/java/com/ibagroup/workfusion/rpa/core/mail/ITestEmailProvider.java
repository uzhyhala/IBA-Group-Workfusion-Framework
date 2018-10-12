package com.ibagroup.workfusion.rpa.core.mail;

import com.ibagroup.workfusion.rpa.core.config.PropertyConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ibagroup.workfusion.rpa.core.config.ConfigurationManager;

public class ITestEmailProvider {

  private static final Logger logger = LoggerFactory.getLogger(ITestEmailProvider.class);

  @Test
  public void testEmailProvider() {
    ConfigurationManager config =
        new PropertyConfiguration("properties/systems/email/Email.properties");

    SendEmailSmtpNonAuth auth = new SendEmailSmtpNonAuth(config.getConfigItem("smtp_host"));
    boolean result = auth.sendSimpleEmail(config.getConfigItem("test_email_from"),
        config.getConfigItem("test_email_to"), config.getConfigItem("test_email_body"),
        config.getConfigItem("test_email_subject"));
    logger.info("" + result);
    Assert.assertTrue(result);
  }
}
