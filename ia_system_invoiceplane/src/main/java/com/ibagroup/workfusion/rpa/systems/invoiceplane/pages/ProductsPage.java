package com.ibagroup.workfusion.rpa.systems.invoiceplane.pages;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.ibagroup.workfusion.rpa.systems.invoiceplane.to.ProductTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.ibagroup.workfusion.rpa.core.clients.RobotDriverWrapper;
import com.ibagroup.workfusion.rpa.core.config.ConfigurationManager;
import com.ibagroup.workfusion.rpa.core.pagefactory.Wait;
import com.ibagroup.workfusion.rpa.core.pagefactory.Wait.WaitFunc;

public class ProductsPage extends RobotDriverWrapper {

    @FindBy(xpath = "//div[@id='content']//table/tbody/tr")
    @Wait(waitFunc = WaitFunc.CLICKABLE, value = 20)
    private List<WebElement> products;

    @FindBy(xpath = "//div[@id='headerbar']/div[@class='pull-right']//a[@title='First' and not(contains(@class,'disabled'))]")
    private WebElement firstPage;

    @FindBy(xpath = "//div[@id='headerbar']/div[@class='pull-right']//a[@title='Last' and not(contains(@class,'disabled'))]")
    private WebElement lastPage;

    @FindBy(xpath = "//div[@id='headerbar']/div[@class='pull-right']//a[@title='Next' and not(contains(@class,'disabled'))]")
    private WebElement nextPage;

    @FindBy(xpath = "//div[@id='headerbar']/div[@class='pull-right']//a[@title='Prev' and not(contains(@class,'disabled'))]")
    private WebElement prevPage;

    public ProductsPage(ConfigurationManager cmn) {
        super(cmn);
    }

    private static long counter = 0;

    public List<ProductTO> getProducts() {
    	List<ProductTO> result = new ArrayList<ProductTO>();
    	if (products != null) {
    		result = products.stream().map(product -> {
    			Document doc = Jsoup.parse(product.getAttribute("outerHTML"), "", Parser.xmlParser());

    			ProductTO productTO = new ProductTO();
                productTO.setIndex(counter++);
    			productTO.setFamily(doc.select("td:nth-child(1)").text());
    			productTO.setSku(doc.select("td:nth-child(2)").text());
    			productTO.setProductName(doc.select("td:nth-child(3)").text());
    			productTO.setProductDescription(doc.select("td:nth-child(4)").text());
    			productTO.setPrice(doc.select("td:nth-child(5)").text());
    			productTO.setTaxRate(doc.select("td:nth-child(6)").text());
    			return productTO;
    		}).collect(Collectors.toList());
    	}
    	
    	return result;
    }

    public boolean firstPage() {
    	try {
    		firstPage.click();
    		return true;
    	} catch (WebDriverException e) {
    		return false;
    	}
    }

    public boolean lastPage() {
    	try {
    		lastPage.click();
    		return true;
    	} catch (WebDriverException e) {
    		return false;
    	}
    }

    public boolean nextPage() {
    	try {
    		nextPage.click();
    		return true;
    	} catch (WebDriverException e) {
    		return false;
    	}
    }

    public boolean prevPage() {
    	try {
    		prevPage.click();
    		return true;
    	} catch (WebDriverException e) {
    		return false;
    	}
    }

}
