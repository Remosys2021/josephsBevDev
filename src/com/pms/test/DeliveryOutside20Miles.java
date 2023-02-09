package com.pms.test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.aventstack.extentreports.reporter.configuration.ChartLocation;
import com.pms.util.Constants;
import com.pms.util.LocatorType;
import com.pms.util.WebPageNavigation;
import com.pms.util.WebelementType;
import com.pms.util.XLSReader;
import com.sun.xml.bind.v2.runtime.reflect.opt.Const;

import groovyjarjarantlr4.v4.runtime.tree.xpath.XPath;
import jxl.read.biff.BiffException;

public class DeliveryOutside20Miles {

	private XLSReader reader;
	private WebPageNavigation navigation;
	public static ExtentReports extent = new ExtentReports();
	public static ExtentHtmlReporter htmlReporter;
	public static ExtentTest logger;

	@BeforeSuite
	public void beforeSuiteSetup() {
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		htmlReporter = new ExtentHtmlReporter("./Reports/DeliveryOutside20Miles" + timeStamp + ".html");
		extent.attachReporter(htmlReporter);
		htmlReporter.config().setChartVisibilityOnOpen(true);
		htmlReporter.config().setTestViewChartLocation(ChartLocation.TOP);
		htmlReporter.config().setDocumentTitle("LiquorCart");
	}

	@BeforeMethod
	@Parameters("browserName")
	public void launchUrl(String browserName) throws BiffException, IOException {
		reader = new XLSReader();

		this.navigation = new WebPageNavigation(reader, browserName);

		htmlReporter.config().setReportName("LiquorCart " + browserName);

	}

	@Test(alwaysRun = true)
	public void checkoutforNonDeliverableAdd() throws IOException, Exception {
		WebPageNavigation.openBrowser(Constants.JOSEPHDEV_ENVIRONMENT_LC);

		JavascriptExecutor js = (JavascriptExecutor) this.navigation.getDriver();

		logger = extent.createTest("SignIn Store");

		WebElement SignInButton = this.navigation.chooseElement(WebelementType.XPATH, Constants.XPATH_SIGNIN_BTN);
		SignInButton.click();

		// Thread.sleep(3000);

		this.navigation.loginToLc();

		logger.log(Status.PASS, "User Signed in Succesfully!");

		logger = extent.createTest("Add item and proceed");

		for (int k = 2; k <= this.reader.getRowCount("addToCart"); k++) {

			JavascriptExecutor js1 = (JavascriptExecutor) this.navigation.getDriver();
			js1.executeScript("window.scrollBy(0,450)", "");

			WebElement search = this.navigation.chooseElement(WebelementType.XPATH, Constants.XPATH_SEARCHBAR);
			search.clear();
			search.sendKeys(this.reader.getCellData("addToCart", "Items", k));
			search.submit();
			System.out.println("Search submit");
			// Thread.sleep(6000);
			this.navigation.fluentWait(this.navigation.getDriver(),
					this.navigation.chooseLocator(LocatorType.XPATH, Constants.XPATH_SEARCHRESULTS_TEXT));

			System.out.println("Wait applied after search submit");

			WebElement productNamesList = this.navigation.chooseElement(WebelementType.XPATH,
					"//ol[@class='products list items product-items']//li[" + k + "]");
			System.out.println("Captured Product list");

			if (productNamesList.getText().contains(this.reader.getCellData("addToCart", "Items", k))) {

				System.out.println("Comparing the search product to the excel name");
				WebElement addBTN = this.navigation.chooseElement(WebelementType.XPATH,
						"//ol[@class='products list items product-items']//li[" + k
								+ "]//div//div//div//div[@class='actions-primary']//button//span[text()='Add to Cart']");

				this.navigation.scrollHandle(addBTN);
				System.out.println("Scrolled till BTN");

				this.navigation.fluentWait(this.navigation.getDriver(),
						this.navigation.chooseLocator(LocatorType.XPATH, Constants.XPATH_ADDTOCART_BTN));
				System.out.println("WAIT APPLIED");
				addBTN.click();
				System.out.println("ADD TO CART CLICKED");

				logger.log(Status.PASS, "Item Added :" + this.reader.getCellData("addToWishList", "Items", k));

				// Thread.sleep(3000);

			} else {
				logger.log(Status.FAIL, "Item not added/available.");
				this.navigation.getScreenshot(this.navigation.getDriver(), "Product not available");
			}
		}

		js.executeScript("window.scrollBy(0,-450)");

		// Thread.sleep(2000);

		this.navigation.fluentWait(this.navigation.getDriver(),
				this.navigation.chooseLocator(LocatorType.XPATH, Constants.XPATH_CART_ITEMSLABEL));

		WebElement clickOnCart = this.navigation.chooseElement(WebelementType.XPATH,
				Constants.XPATH_SHOPPING_CART_ICON);
		clickOnCart.click();

		List<WebElement> listOfItems = this.navigation.getDriver().findElements(By.className("minicart-items"));

		for (int h = 0; h < listOfItems.size(); h++) {
			String itemsName = listOfItems.get(h).getText();

			logger.log(Status.INFO, "List of items in cart :" + itemsName);
		}

		WebElement proceedToCheckout = this.navigation.chooseElement(WebelementType.XPATH,
				"//button[text()='Proceed to Checkout']");
		proceedToCheckout.click();

		// Thread.sleep(20000);

		System.out.println("Waiting for Order Type Text to be shown");
		this.navigation.fluentWait(this.navigation.getDriver(),
				this.navigation.chooseLocator(LocatorType.XPATH, Constants.XPATH_ORDERTYPE_TEXT));
		System.out.println("Order Type text is visible");

		logger = extent.createTest("Shipping");

		System.out.println("Capturing the Order Type elements");
		this.navigation.fluentWait(this.navigation.getDriver(),
				this.navigation.chooseLocator(LocatorType.XPATH, Constants.XPATH_STOREPICKUP_TEXT));

		List<WebElement> listOfElements = this.navigation.getDriver()
				.findElements(By.xpath("//*[@id='checkout-shipping-method-load']/table/tbody"));

		System.out.println("Printing the size of list ORDER TYPE:" + listOfElements.size());

		for (int k = 0; k < listOfElements.size(); k++) {
			String listOfItemText = listOfElements.get(k).getText();
			logger.log(Status.INFO, "Order Type :\n" + listOfItemText);
			Thread.sleep(5000);

			System.out.println("Trying to select the Delivery Order Type");
			if (listOfItemText.contains("Delivery")) {
				WebElement radio = this.navigation.chooseElement(WebelementType.XPATH,
						"//*[@id='checkout-shipping-method-load']/table/tbody/tr[2]/td[1]/input");
				Thread.sleep(3000);
				radio.click();
				System.out.println("Delivery order type is selected");

				logger.log(Status.INFO, "Delivery selected");

				break;
			}

		}

		logger = extent.createTest("Select the Deliverable Address");
		short i = 0;
		List<WebElement> addList = this.navigation.chooseElements(WebelementType.XPATH,
				"//div[@class='field addresses']//div[@class='control']//div[contains(@class,'selected')]");

		System.out.println("Iterating the address list and matching the addresses");

		for (WebElement wb : addList) {
			i++;
			String text = wb.getText();

			if (text.contains(this.reader.getCellData("DeliveryOutside20Miles", "address", 2))) {
				this.navigation.scrollHandle(wb);

				System.out.println("Scrolled till the matched address");

				Thread.sleep(5000);

				List<WebElement> ship = this.navigation.chooseElements(WebelementType.XPATH,
						"//div[@class='field addresses']//div[@class='control']//div[contains(@class,'selected')][" + i
								+ "]//following-sibling::button//span[text()='Ship Here']");

				Thread.sleep(5000);

				int buttonCount = ship.size();

				if (buttonCount == 0) {

					logger.log(Status.INFO, "Desired Address is already selected");
				} else {
					ship.get(0).click();

					logger.log(Status.INFO, "The address is selected");
				}

				// Thread.sleep(10000);
				break;

			}
		}

		// Thread.sleep(10000);

		WebElement next = this.navigation.chooseElement(WebelementType.XPATH,
				"//*[@id='shipping-method-buttons-container']/div/button");
		this.navigation.scrollHandle(next);
		next.click();

		logger.log(Status.INFO, "Next button is clicked");

		// Thread.sleep(2000);

		try {

			System.out.println("Applying wait for SORRY ERROR text");
			this.navigation.fluentWait(this.navigation.getDriver(),
					this.navigation.chooseLocator(LocatorType.XPATH, Constants.XPATH_ERRORTEXT_NONDELIVERABLE_ADDRESS));
			System.out.println("WAIT APPLIED");

			WebElement errorText = this.navigation.chooseElement(WebelementType.XPATH,
					Constants.XPATH_ERRORTEXT_NONDELIVERABLE_ADDRESS);

			this.navigation.scrollHandle(errorText);

			if (errorText.isDisplayed()) {

				logger.log(Status.PASS, "The selected address is not deliverable : " + errorText.getText());
			} else {
				logger.log(Status.FAIL, "The error text is not displayed");
			}
		}

		catch (Exception e) {
			e.printStackTrace();
			Assert.assertTrue(false);
		}

	}

	@Test(dependsOnMethods = { "checkoutforNonDeliverableAdd" })
	public void signOut() throws InterruptedException {

		ExtentTest logger = extent.createTest("Logout");

		WebElement signOut = this.navigation.chooseElement(WebelementType.XPATH, Constants.XPATH_SIGNOUT_BTN);

		this.navigation.scrollHandle(signOut);
		signOut.click();

		logger.log(Status.PASS, "Logout Successful");

		this.navigation.fluentWait(this.navigation.getDriver(),
				this.navigation.chooseLocator(LocatorType.XPATH, Constants.XPATH_SIGNIN_BTN));

	}

	@AfterMethod
	public void getResult(ITestResult result) throws Exception {
		try {
			@SuppressWarnings("static-access")
			String screenshotPath = this.navigation.getScreenshot(this.navigation.getDriver(), result.getName());
			if (result.getStatus() == ITestResult.FAILURE) {
				logger.addScreenCaptureFromPath(screenshotPath);

				logger.log(Status.FAIL, "Test Case failed");

			} else if (result.getStatus() == ITestResult.SKIP) {
				logger.log(Status.SKIP, "Test Case Skipped is " + result.getName());
			}
		} catch (Exception e) {
			logger.log(Status.ERROR, "Test Error");
		}
	}

	@AfterTest
	public void tearDown() {
		this.navigation.getDriver().quit();
	}

	@AfterSuite(alwaysRun = true)
	public void afterSuite() {
		extent.flush();
	}

}
