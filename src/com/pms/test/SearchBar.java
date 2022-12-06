package com.pms.test;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.openqa.selenium.WebElement;
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
import com.pms.util.WebPageNavigation;
import com.pms.util.WebelementType;
import com.pms.util.XLSReader;

import jxl.read.biff.BiffException;

public class SearchBar 
{
	private XLSReader reader;
	private WebPageNavigation navigation;
	public static ExtentReports extent =new ExtentReports();
	public static ExtentHtmlReporter htmlReporter;
	public static ExtentTest logger;

	@BeforeSuite
	public void beforeSuiteSetup() 
	{
		String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date());
		htmlReporter = new ExtentHtmlReporter("./Reports/SearchBar "+ timeStamp +".html");
		extent.attachReporter(htmlReporter);
		htmlReporter.config().setChartVisibilityOnOpen(true);
		htmlReporter.config().setTestViewChartLocation(ChartLocation.TOP);
		htmlReporter.config().setDocumentTitle("LiquorCart ");
	}

	@BeforeMethod
	@Parameters("browserName")
	public void launchUrl(String browserName) throws BiffException, IOException
	{				
		reader = new XLSReader();
		
		this.navigation = new WebPageNavigation(reader,browserName);

		htmlReporter.config().setReportName("LiquorCart "+browserName);

	}
	@Test(alwaysRun = true)
	public void searchValid() throws IOException, Exception
	{	
		WebPageNavigation.openBrowser(Constants.JOSEPHDEV_ENVIRONMENT_LC);	

		logger = extent.createTest("Select Location");
		
		logger.log(Status.PASS, "Navigated to Sharon's Liquor Store");
		
		WebElement SignInButton = this.navigation.chooseElement(WebelementType.XPATH, "//a[text()='Sign In']");
		SignInButton.click();

		Thread.sleep(3000);

		this.navigation.loginToLc();

		logger.log(Status.PASS, "User Signed in Succesfully!");

		logger = extent.createTest("Search Bar : Valid Entry");

		WebElement search = this.navigation.chooseElement(WebelementType.XPATH, "//input[@id='search']");
		search.clear();
		search.sendKeys(this.reader.getCellData("SearchBar", "SearchBarText", 2));
		search.submit();

		WebElement actualResult = this.navigation.chooseElement(WebelementType.CLASS_NAME, "product-item-link");
		String actualResultText = actualResult.getText();

		if(actualResultText.contains(this.reader.getCellData("SearchBar", "ExpectedResult", 2)));
		{
			logger.log(Status.PASS, "Search result is correct!");

		}
	}

	@Test(dependsOnMethods = { "searchValid" })
	public void invalidSearch()
	{
		logger = extent.createTest("Search Bar : InValid Entry");
		
		WebElement search = this.navigation.chooseElement(WebelementType.XPATH, "//input[@id='search']");
		search.clear();
		search.sendKeys(this.reader.getCellData("SearchBar", "SearchBarText", 3));
		search.submit();

		WebElement	wrnngMsg = this.navigation.chooseElement(WebelementType.XPATH, "//*[@id=\"maincontent\"]/div[3]/div[1]/div[2]/div");
		String wrnngMsgText = wrnngMsg.getText();

		logger.log(Status.INFO, "There is no search result! " +wrnngMsgText);

		WebElement signOut = this.navigation.chooseElement(WebelementType.XPATH, "//a[text()='Sign Out']");
		signOut.click();

		logger.log(Status.PASS, "Signed Out");
	}

	@AfterMethod
	public void getResult(ITestResult result) throws Exception
	{
		try {
			@SuppressWarnings("static-access")
			String screenshotPath = this.navigation.getScreenshot(this.navigation.getDriver(),result.getName());
			if(result.getStatus() == ITestResult.FAILURE){
				logger.addScreenCaptureFromPath(screenshotPath);
				logger.log(Status.ERROR, "Test Case Failed");
			}else if(result.getStatus() == ITestResult.SKIP){
				logger.log(Status.SKIP, "Test Case Skipped is "+result.getName());
			}}catch(Exception e)
		{
				logger.log(Status.ERROR, "Test Error");
		}
	}
	@AfterTest
	public void tearDown()
	{
		this.navigation.getDriver().quit();
	}

	@AfterSuite(alwaysRun = true)
	public void afterSuite() {
		extent.flush();
	}
}
