package com.griggi.app;

import java.sql.SQLException;
import java.util.List;

import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeTest;
import org.testng.ITestContext;
import org.testng.annotations.AfterTest;

public class MainTest {

	private static WebDriver driver;
	private static WebElement mobileElement;
	private static String URL;
	private static String USERNAME;
	private static String PASSWORD;
	private static int TIMEOUT_SEC;

	@Test(description = "Logging in. - Too long")
	public void loginBad1() {
		driver.get(URL);
		mobileElement = driver.findElement(By.id("pocha_pocha_mobile"));

		// Too long numbers
		mobileElement.sendKeys(USERNAME + "99");
		mobileElement.submit();

		// wait for password input
		(new WebDriverWait(driver, TIMEOUT_SEC))
				.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("ul.error_list li:nth-child(1)")));
	}

	@Test(description = "Logging in. - Too short")
	public void loginBad2() {
		driver.get(URL);
		mobileElement = driver.findElement(By.id("pocha_pocha_mobile"));

		// Too short numbers
		mobileElement.sendKeys("99");
		mobileElement.submit();

		// wait for password input
		(new WebDriverWait(driver, TIMEOUT_SEC))
				.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("ul.error_list li:nth-child(1)")));
	}

	@Test(description = "Logging in. - Invalid")
	public void loginBad3() {
		driver.get(URL);
		mobileElement = driver.findElement(By.id("pocha_pocha_mobile"));

		// Invalid numbers
		mobileElement.sendKeys("asdf");
		mobileElement.submit();

		// wait for password input
		(new WebDriverWait(driver, TIMEOUT_SEC))
				.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("ul.error_list li:nth-child(2)")));
	}

	@Test(description = "Logging in. - GOOD")
	public void loginGood() {
		driver.get(URL);
		mobileElement = driver.findElement(By.id("pocha_pocha_mobile"));
		mobileElement.sendKeys(USERNAME);
		mobileElement.submit();

		// wait for password input
		(new WebDriverWait(driver, TIMEOUT_SEC))
				.until(ExpectedConditions.presenceOfElementLocated(By.id("apAuthLocalUser_password")));
		mobileElement = driver.findElement(By.id("apAuthLocalUser_password"));
		mobileElement.sendKeys(PASSWORD);
		mobileElement.submit();

		// wait to get confirmation that it is loaded
		// TODO: put appropriate selector
		// (new WebDriverWait(driver, TIMEOUT_SEC))
		// .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#apAuthLocalUser_password")));
	}

	@Test(description = "Check Dashboard UI - 1 (Nav)")
	public void dashboardUICheck1() {
		// checking ui elements
		(new WebDriverWait(driver, TIMEOUT_SEC))
				.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("ul#side-menu")));
		List<WebElement> list = driver.findElements(By.cssSelector("ul#side-menu > li"));
		Assert.assertNotEquals(0, list.size());
	}

	@Test(description = "Check Dashboard UI - 2 (Widgets)")
	public void dashboardUICheck2() {
		// checking ui elements
		(new WebDriverWait(driver, TIMEOUT_SEC))
				.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div.content")));
	}

	@Test(description = "Check Dashboard UI - 3 (Header)")
	public void dashboardUICheck3() {
		// checking ui elements
		(new WebDriverWait(driver, TIMEOUT_SEC))
				.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("div#header")));
	}
	
	@Test(description = "Verify Nav from database - List of nodes")
	public void listOfNodes() {
		//String query = "select name from nodes where id in (select distinct node_id from connections where identity = '9538155667' order by id desc);";
		String query = "select distinct nodes.mass_transit_info from nodes inner join connections on nodes.id = connections.node_id where connections.identity = '" + USERNAME + "' order by connections.id desc;";
		SQLHandler sh = null;
		List<String> nodeList = null;
		try {
			sh = new SQLHandler();
			nodeList = sh.queryExecute(query, 1);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		int i = 1;
		for(String e : nodeList) {
			System.out.println(driver.findElement(By.cssSelector("ul#side-menu li:nth-child(" + i + ") a span")).getText());
			Assert.assertEquals(e.toLowerCase(), driver.findElement(By.cssSelector("ul#side-menu li:nth-child(" + i + ") a span")).getText().toLowerCase() );
			i++;
		}
	}
	
	@Test(description = "Verify the url for the lastest node on dashboard")
	public void verifyLatestNodeURL() {
		String query = "select distinct node_id from connections where identity = '9538155667' order by id desc limit 1;";
		String currentUrl = driver.getCurrentUrl();
		SQLHandler sh = null;
		String nodeId = "";
		try {
			sh = new SQLHandler();
			nodeId = sh.queryExecute(query, 1).get(0);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		Assert.assertEquals(URL + "/node/" + nodeId, currentUrl);
	}
	
	@BeforeTest(alwaysRun = true)
	public void beforeTest(ITestContext context) {
		// Reference to Chrome
		driver = new ChromeDriver();
		// Maximize the Browser window
		driver.manage().window().maximize();

		// Initialise parameters
		URL = context.getCurrentXmlTest().getParameter("base_url");
		USERNAME = context.getCurrentXmlTest().getParameter("username");
		PASSWORD = context.getCurrentXmlTest().getParameter("password");
		TIMEOUT_SEC = Integer.parseInt(context.getCurrentXmlTest().getParameter("timeout_sec"));
	}

	@AfterTest
	public void afterTest() {
		driver.close();
	}
}
