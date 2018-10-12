package com.ibagroup.workfusion.rpa.systems.googlesearch;

import com.ibagroup.workfusion.rpa.systems.googlesearch.clients.GoogleSearchClient;
import com.ibagroup.workfusion.rpa.systems.googlesearch.pages.HomePage;
import com.ibagroup.workfusion.rpa.systems.googlesearch.pages.ResultPage;
import com.ibagroup.workfusion.rpa.core.config.ConfigurationManager;
import com.ibagroup.workfusion.rpa.core.robots.RobotProtocol;

public interface GoogleSearchRobot extends RobotProtocol {
	
    default ResultPage initRobot(String searchString) {
    	GoogleSearchClient client = new GoogleSearchClient(getCfg());

        HomePage homePage = client.getHomePage();
        storeCurrentMetadata();

        ResultPage resultPage = homePage.search(searchString);
        setResultPage(resultPage);
        storeCurrentMetadata();

        return resultPage;
    }

    ConfigurationManager getCfg();

    ResultPage getResultPage();
    void setResultPage(ResultPage resultPage);

}