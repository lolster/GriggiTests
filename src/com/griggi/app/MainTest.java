package com.griggi.app;

import java.sql.SQLException;
import java.util.List;

import org.junit.Assert;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.ExpectedCondition;
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
	private static List<String> nodeList; // list of all nodes related to the
											// user.
	private static List<String> nodeListId;

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
	public void loginGood1() {
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
		(new WebDriverWait(driver, TIMEOUT_SEC)).until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(
				"aside#menu div#navigation div.profile-picture div.stats-label div.dropdown a.dropdown-toggle small.text-muted span.text-success")));
	}

	@Test(description = "Verifies that after a bad login, user can enter")
	public void loginGood2() {
		/*
		 * Login as a user Enter a bad password initially Enter a good password
		 * later after realising that password was wrong Ensure that page has
		 * loaded
		 */
		driver.get(URL);
		mobileElement = driver.findElement(By.id("pocha_pocha_mobile"));
		mobileElement.sendKeys(USERNAME);
		mobileElement.submit();

		// wait for password input
		(new WebDriverWait(driver, TIMEOUT_SEC))
				.until(ExpectedConditions.presenceOfElementLocated(By.id("apAuthLocalUser_password")));

		// enter wrong password
		String badPasswordPostfix = "asdf";
		mobileElement = driver.findElement(By.id("apAuthLocalUser_password"));
		mobileElement.sendKeys(PASSWORD + badPasswordPostfix);
		mobileElement.submit();

		// ensure that the user is notified of error in authentication
		// i.e., make sure that red point is there and error message is correct
		(new WebDriverWait(driver, TIMEOUT_SEC))
				.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("ul.error_list li:nth-child(1)")));
		mobileElement = driver.findElement(By.cssSelector("ul.error_list li:nth-child(1)"));
		Assert.assertEquals("The password you entered is not valid.", mobileElement.getText());

		// enter good password
		mobileElement = driver.findElement(By.id("apAuthLocalUser_password"));
		mobileElement.sendKeys(PASSWORD);
		mobileElement.submit();

		// verify user has logged in and is seeing their dashboard
		// searches for the profile picture element
		(new WebDriverWait(driver, TIMEOUT_SEC)).until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(
				"aside#menu div#navigation div.profile-picture div.stats-label div.dropdown a.dropdown-toggle small.text-muted span.text-success")));
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
		// String query = "select name from nodes where id in (select distinct
		// node_id from connections where identity = '9538155667' order by id
		// desc);";
		String query = "select distinct nodes.mass_transit_info from nodes inner join connections on nodes.id = connections.node_id where connections.identity = '"
				+ USERNAME + "' order by connections.id desc;";
		SQLHandler sh = null;
		nodeList = null;
		try {
			sh = new SQLHandler();
			nodeList = sh.queryExecute(query, 1);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		int i = 1;
		for (String e : nodeList) {
			// System.out.println(driver.findElement(By.cssSelector("ul#side-menu
			// li:nth-child(" + i + ") a span")).getText());
			Assert.assertEquals(e.toLowerCase(),
					driver.findElement(By.cssSelector("ul#side-menu li:nth-child(" + i + ") a span")).getText()
							.toLowerCase());
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

	@Test(description = "Admin node arrow exists")
	public void nodeAdminDropdownArrow() {
		String query = "select id from nodes where public_phone_number =" + USERNAME;
		SQLHandler sh = null;
		List<String> nodeListId = null;
		try {
			sh = new SQLHandler();
			nodeListId = sh.queryExecute(query, 1);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		// admin must have connected with the node they are admin of
		Assert.assertFalse(nodeListId.retainAll(nodeList));
		for (String e : nodeListId) {
			System.out.println("ul.nav#side-menu > li > a[href='/node/" + e + "'] > span.fa.arrow");
			mobileElement = driver
					.findElement(By.cssSelector("ul.nav#side-menu > li > a[href='/node/" + e + "'] > span.fa.arrow"));
		}
	}

	@Test(description = "Router Speed")
	public void routerSpeed() {

		String f = "select distinct nodes.id from nodes inner join connections on nodes.id = connections.node_id where connections.identity = '"
				+ USERNAME + "' order by connections.id desc;";
		try {
			SQLHandler df = new SQLHandler();
			nodeListId = df.queryExecute(f, 1);
		} catch (ClassNotFoundException | SQLException e1) {
			e1.printStackTrace();
		}

		for (String e : nodeListId) {
			String query = "select civic_number from nodes where id = '" + e + "'";
			try {
				SQLHandler sh = new SQLHandler();
				String ans = sh.queryExecute(query, 1).get(0);
				driver.get(URL + "/node/" + e);
				// need to wait until the content of the required widget/box in
				// dashboard is loaded
				(new WebDriverWait(driver, TIMEOUT_SEC)).until(new ExpectedCondition<Boolean>() {
					public Boolean apply(WebDriver d) {
						return d.findElements(By.cssSelector("h1.text-success")).get(0).getText().length() > 0;
					}
				});
				// System.out.println("Expected: " + ans + " Mbps" + "\nActual:
				// " +
				// driver.findElements(By.cssSelector("h1.text-success")).get(0).getText());
				Assert.assertEquals(ans + " Mbps",
						driver.findElements(By.cssSelector("h1.text-success")).get(0).getText());
			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}

		}
	}

	@Test(description = "Checks the available quota of the user with the DB")
	public void availableQuota() {

		String f = "select distinct nodes.id from nodes inner join connections on nodes.id = connections.node_id where connections.identity = '"
				+ USERNAME + "' order by connections.id desc;";
		try {
			SQLHandler df = new SQLHandler();
			nodeListId = df.queryExecute(f, 1);
		} catch (ClassNotFoundException | SQLException e1) {
			e1.printStackTrace();
		}

		for (String e : nodeListId) {
			String query = "select civic_number from nodes where id = '" + e + "'";
			try {
				SQLHandler sh = new SQLHandler();
				String ans = sh.queryExecute(query, 1).get(0);
				driver.get(URL + "/node/" + e);
				// need to wait until the content of the required widget/box in
				// dashboard is loaded
				(new WebDriverWait(driver, TIMEOUT_SEC)).until(new ExpectedCondition<Boolean>() {
					public Boolean apply(WebDriver d) {
						return d.findElements(By.cssSelector("h1.text-success")).get(0).getText().length() > 0;
					}
				});
				Assert.assertEquals(ans + " Mbps",
						driver.findElements(By.cssSelector("h1.text-success")).get(0).getText());
			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}

		}
	}

	@Test(description = "Tests the data allocation for a user.")
	public void dataAllocation() {
		/*
		 * Tests the data allocation for a single user on his admin node to
		 * himself. Reset the data allocated to 0GB Set to 100GB Reset to 0GB
		 * QUICK AND DIRTY, TO BE CLEANED UP
		 */

		int id = 1734; // id for 9535354545 on nodeid 2 in userdatas
		String initAmt = "0";
		String finalAmt = "100";
		String q = "select freedata from userdatas where id = " + id;
		SQLHandler sh;
		String dataAllocated = "NA";
		
		//selecting the user to allocate and deallocate to.
		String adminCheckQuery = "select id from nodes where public_phone_number = " + USERNAME + " limit 1;";
		String userSelectQuery = "select id from userdatas where nodeid = ";
		try {
			sh = new SQLHandler();
			List<String> temp = sh.queryExecute(adminCheckQuery, 0);//.get(0).toString();
			if (temp.size() > 0) {
				for(String w : temp) {
					List<String> tempUsers = sh.queryExecute(userSelectQuery + w, 0);
					if(tempUsers.size() > 0) {
						id = Integer.parseInt(tempUsers.get(0));
						break;
					}
				}
			}
			//exit if user has no admin nodes
			else {
				System.out.println("[dataAllocation] " + USERNAME + " has no admin nodes.");
				return;
			}
		} catch (ClassNotFoundException | SQLException e1) {
			e1.printStackTrace();
		}

		// add 0gb to user on his admin node
		driver.get("http://authpuppy.localhost.com/userdata/edit?id=" + id);
		mobileElement = driver.findElement(By.cssSelector("input#userdata_freedata"));
		mobileElement.clear();
		mobileElement.sendKeys(initAmt);
		mobileElement.submit();

		try {
			sh = new SQLHandler();
			dataAllocated = sh.queryExecute(q, 1).get(0).toString();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		Assert.assertEquals(initAmt, dataAllocated);

		// add 100gb to user on his admin node
		driver.get("http://authpuppy.localhost.com/userdata/edit?id=" + id);
		mobileElement = driver.findElement(By.cssSelector("input#userdata_freedata"));
		mobileElement.clear();
		mobileElement.sendKeys(finalAmt);
		mobileElement.submit();

		try {
			sh = new SQLHandler();
			dataAllocated = sh.queryExecute(q, 1).get(0).toString();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		Assert.assertEquals(finalAmt, dataAllocated);

		// add 100gb to user on his admin node
		driver.get("http://authpuppy.localhost.com/userdata/edit?id=" + id);
		mobileElement = driver.findElement(By.cssSelector("input#userdata_freedata"));
		mobileElement.clear();
		mobileElement.sendKeys(initAmt);
		mobileElement.submit();

		// one final check, since we can do it, why not
		try {
			sh = new SQLHandler();
			dataAllocated = sh.queryExecute(q, 1).get(0).toString();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}
		Assert.assertEquals(initAmt, dataAllocated);

	}

	@BeforeTest(alwaysRun = false)
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

	// misc
	public void sleepFor(int sec) {
		try {
			Thread.sleep(sec * 1000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
