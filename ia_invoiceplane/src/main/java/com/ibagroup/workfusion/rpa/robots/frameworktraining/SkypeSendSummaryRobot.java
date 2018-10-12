package com.ibagroup.workfusion.rpa.robots.frameworktraining;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.freedomoss.crowdcontrol.webharvest.web.dto.SecureEntryDTO;
import com.freedomoss.workfusion.utils.gson.GsonUtils;
import com.google.gson.reflect.TypeToken;
import com.ibagroup.workfusion.rpa.core.BindingUtils;
import com.ibagroup.workfusion.rpa.core.CommonConstants;
import com.ibagroup.workfusion.rpa.core.MachineTask;
import com.ibagroup.workfusion.rpa.core.annotations.OnError;
import com.ibagroup.workfusion.rpa.core.datastore.DataStoreQuery;
import com.ibagroup.workfusion.rpa.core.mis.LoggableMethod;
import com.ibagroup.workfusion.rpa.core.robots.UiRobotCapabilities;
import com.ibagroup.workfusion.rpa.core.security.SecurityUtils;
import com.workfusion.rpa.helpers.RPA;
import com.ibagroup.workfusion.rpa.systems.googlesearch.to.ProductSummaryTO;
import com.ibagroup.workfusion.rpa.systems.skype.SkypeRobot;
import com.ibagroup.workfusion.rpa.systems.skype.pages.MainPage;

public class SkypeSendSummaryRobot extends UiRobotCapabilities implements SkypeRobot {

    private static final Logger logger = LoggerFactory.getLogger(SkypeSendSummaryRobot.class);

    private static final Type TYPE_PRODUCT_SUMMARY_ROW = new TypeToken<List<ProductSummaryTO>>() {}.getType();

    private MainPage mainPage = null;

    @LoggableMethod(module = "skype_send_summary", operation = "perform")
    public String perform() {
        RPA.enableTypeOnScreen();

        SecureEntryDTO credentials = new SecurityUtils(getBinding()).getSecureEntry("skype");
        initRobot(credentials);

        getMainPage().sendMessageToRecipient(getCfg().getConfigItem("skype.recipient_address"), getSummary());

        finiliseRobot();

        return CommonConstants.SUCCESS_CLMN_PROCEED;
    }

    private List<String> getSummary() {
        String processGuid = BindingUtils.getWebHarvestTaskItem(getBinding()).getRun().getRootRunUuid();

        String successfullyPassedRecordsSQL =
                "select * from @this where " + MachineTask.PROCESS_UUID + " = '" + processGuid + "'";

        Optional<List<Map<String, String>>> records =
                new DataStoreQuery(getBinding()).executeQuery(TrainingConstants.INVOICEPLANE_RECORDS_DS, successfullyPassedRecordsSQL).getSelectResultAsMapRows();

        List<String> result = new ArrayList<String>();
        if (records.isPresent()) {
            for (Map<String, String> row : records.get()) {
                String productResultJson = row.get(TrainingConstants.PRODUCT_RESULT_JSON);
                List<ProductSummaryTO> productSummaryList =
                        GsonUtils.GSON.<List<ProductSummaryTO>>fromJson(productResultJson, TYPE_PRODUCT_SUMMARY_ROW);

                StringBuffer sb = new StringBuffer();
                sb.append("===========================================").append("\n");
                sb.append("Product Name: " + row.get(TrainingConstants.PRODUCT_NAME)).append("\n\n");
                sb.append("Google Search RPA duration: " + row.get(TrainingConstants.GOOGLESEARCH_RPA_DURATION)).append("ms").append("\n");
                sb.append("Invoice Plane RPA duration: " + row.get(TrainingConstants.INVOICEPLANE_RPA_DURATION)).append("ms").append("\n");
                sb.append("Product links: ").append("\n");
                for (ProductSummaryTO summaryTO : productSummaryList) {
                    sb.append("\t").append(summaryTO.getProductLink()).append("\n");
                }
                sb.append("===========================================");

                result.add(sb.toString());
            }
        }

        return result;
    }

    @OnError
    public String handleError() {
        finiliseRobot();
        return CommonConstants.SUCCESS_CLMN_FAILED;
    }

    @Override
    public MainPage getMainPage() {
        return mainPage;
    }

    @Override
    public void setMainPage(MainPage mainPage) {
        this.mainPage = mainPage;
    }
}
