package com.ibagroup.workfusion.rpa.core;

import java.util.Properties;
import org.apache.commons.io.FilenameUtils;
import org.junit.Before;
import org.powermock.core.classloader.annotations.PowerMockIgnore;

@PowerMockIgnore({ "org.apache.commons.*", "javax.net.ssl.*", "javax.crypto.*", "org.apache.log4j.*" })
public class BaseIntegrationTest {

	@Before
	public void initSystemProperties() {

		Properties properties = System.getProperties();
		properties.setProperty("javax.net.ssl.keyStore", getFullePath("auth/SIT-keystore.jks"));
		properties.setProperty("javax.net.ssl.keyStorePassword", "145293");
		properties.setProperty("javax.net.ssl.trustStore", getFullePath("auth/SBSA-PKO-truststore.jks"));
		properties.setProperty("javax.net.ssl.trustStorePassword", "TheAnchorOfTrust");
		System.setProperties(properties);
	}

	private String getFullePath(String relative) {
		return FilenameUtils.concat(this.getClass().getClassLoader().getResource("").getPath(), relative);
	}

}
