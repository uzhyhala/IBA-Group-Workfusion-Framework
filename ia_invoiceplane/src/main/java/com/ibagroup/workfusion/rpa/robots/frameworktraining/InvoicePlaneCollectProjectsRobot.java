package com.ibagroup.workfusion.rpa.robots.frameworktraining;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.freedomoss.crowdcontrol.webharvest.web.dto.SecureEntryDTO;
import com.ibagroup.workfusion.rpa.core.CommonConstants;
import com.ibagroup.workfusion.rpa.core.annotations.OnError;
import com.ibagroup.workfusion.rpa.core.config.ConfigurationManager.Formatter;
import com.ibagroup.workfusion.rpa.core.robots.UiRobotCapabilities;
import com.ibagroup.workfusion.rpa.core.security.SecurityUtils;
import com.ibagroup.workfusion.rpa.systems.invoiceplane.InvoicePlaneRobot;
import com.ibagroup.workfusion.rpa.systems.invoiceplane.pages.MainPage;
import com.ibagroup.workfusion.rpa.systems.invoiceplane.pages.MenuNavigationBar;
import com.ibagroup.workfusion.rpa.systems.invoiceplane.pages.ProductsPage;
import com.ibagroup.workfusion.rpa.systems.invoiceplane.to.ProductTO;

public class InvoicePlaneCollectProjectsRobot extends UiRobotCapabilities implements InvoicePlaneRobot {

    private static final Logger logger = LoggerFactory.getLogger(InvoicePlaneCollectProjectsRobot.class);

    private MainPage mainPage = null;
    private MenuNavigationBar menuNavigationBar = null;

    private List<ProductTO> products;

    private long startTime;
    private long endTime;

    public String perform() {
        SecureEntryDTO credentials = new SecurityUtils(getBinding()).getSecureEntry("invoice_plane");

        this.startTime = new Date().getTime();
        initRobot(credentials);

        int expectedProductsCount = getCfg().getConfigItem("products_count", 20, Formatter.INT).intValue();
        // click Products -> View products
        ProductsPage productsPage = getMenuNavigationBar().openProducts();
        products = new ArrayList<ProductTO>();
        while (true) {
            products.addAll(productsPage.getProducts());
            products = products.stream().filter(distinctByKey(p -> p.getProductName().toLowerCase())).collect(Collectors.toList());

            if (products.size() > expectedProductsCount || !productsPage.nextPage()) {
                break;
            }
        }
        // leave only first 20 products
        if (expectedProductsCount < products.size()) {
            products.subList(expectedProductsCount, products.size()).clear();
        }
        logger.debug("Extracted products count: " + products.size());

        finiliseRobot();
        this.endTime = new Date().getTime();

        return CommonConstants.SUCCESS_CLMN_PROCEED;
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    @OnError
    public String handleError(Object self, Method thisMethod, Method proceed, Throwable throwable, Object[] args) {
        finiliseRobot();

        this.endTime = new Date().getTime();

        return CommonConstants.SUCCESS_CLMN_FAILED;
    }

    public List<ProductTO> getProducts() {
        return products;
    }

    public long getRpaDuration() {
        return this.endTime - this.startTime;
    }

    @Override
    public MainPage getMainPage() {
        return mainPage;
    }

    @Override
    public void setMainPage(MainPage mainPage) {
        this.mainPage = mainPage;
    }

    @Override
    public MenuNavigationBar getMenuNavigationBar() {
        return menuNavigationBar;
    }

    @Override
    public void setMenuNavigationBar(MenuNavigationBar menuNavigationBar) {
        this.menuNavigationBar = menuNavigationBar;
    }
}
