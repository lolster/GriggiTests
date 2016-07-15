package com.griggi.app;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.exec.util.StringUtils;
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
	private static String DATA_SHARE_NODE;
	private static String DATA_SHARE_USER;
	private static int TIMEOUT_SEC;
	private static Double DELTA;
	private static List<String> nodeList; // list of all nodes related to the
											// user.
	private static List<String> nodeListId;
	// private static List<String> nodeListAdmin; //list of all nodes which
	// USERNAME is admin of

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
						return d.findElements(By.cssSelector("h1#router-speed.text-success")).size() > 0
								&& d.findElement(By.cssSelector("h1#router-speed.text-success")).getText().length() > 0;
					}
				});
				System.out.println("Expected: " + ans + " Mbps" + "\nActual:"
						+ driver.findElements(By.cssSelector("h1.text-success")).get(0).getText());
				// the index of which h1.text-success element to select depends
				// on the order of the
				// widgets in the dashboard. in this case, it is the second
				// h1.text-success element
				// which is needed, hence index of 1 (0 based index)
				Assert.assertEquals(ans + " Mbps",
						driver.findElement(By.cssSelector("h1#router-speed.text-success")).getText());
			} catch (ClassNotFoundException ex) {
				ex.printStackTrace();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}

		}
	}

	@Test(description = "Checks the available quota of the user with the DB")
	public void availableQuota() {

		// queries
		String karmaDataQuery = "select karma_data from ap_user where username=" + USERNAME;
		String freeDataQuery = "select freedata from userdatas where userid=" + USERNAME + " and nodeid = ";
		String usedDataQuery = "select useddata from userdatas where userid=" + USERNAME + " and nodeid = ";
		String maxUserDataQuery = "select max_user_data from nodes where id = ";

		for (String nodeID : nodeListId) {
			System.out.println("nodeID: " + nodeID);
			double karmaData = 0; // gb
			double freeData = 0; // gb
			double usedData = 0; // mb
			double maxUserData = 0; // gb
			List<String> tempList = new ArrayList<>();

			// getting maxUserData
			try {
				SQLHandler sh = new SQLHandler();
				String t = sh.queryExecute(maxUserDataQuery + nodeID, 1).get(0);
				if (t != null) {
					maxUserData = Double.parseDouble(t);
				} else {
					maxUserData = -1;
				}
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}

			// getting karma data
			try {
				SQLHandler sh = new SQLHandler();
				tempList = sh.queryExecute(karmaDataQuery, 1);
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}

			// Assert.assertNotEquals(null, tempList);
			Assert.assertEquals(1, tempList.size()); // only one user should be
														// selected

			if (!(tempList.get(0) == null)) {
				karmaData = Double.parseDouble(tempList.get(0));
			}

			// getting freedata
			try {
				SQLHandler sh = new SQLHandler();
				System.out.println(freeDataQuery + nodeID);
				tempList = sh.queryExecute(freeDataQuery + nodeID, 1);
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}

			// Assert.assertNotEquals(null, tempList);
			Assert.assertEquals(1, tempList.size());

			if (!(tempList.get(0) == null)) {
				freeData = Double.parseDouble(tempList.get(0));
			}

			// getting used data
			try {
				SQLHandler sh = new SQLHandler();
				tempList = sh.queryExecute(usedDataQuery + nodeID, 1);
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}

			// Assert.assertNotEquals(null, tempList);
			Assert.assertEquals(1, tempList.size());

			if (!(tempList.get(0) == null)) {
				usedData = Double.parseDouble(tempList.get(0));
			}

			// rounding to 2 decimal places
			// available quota = min(maxuserdata, freedata+karmadata-useddata)
			double availableQuota = 0;
			if (maxUserData != -1) {
				availableQuota = Math.min(maxUserData,
						Math.round((karmaData + freeData - (usedData / 1024.0)) * 100.0) / 100.0);
			} else {
				availableQuota = Math.round((karmaData + freeData - (usedData / 1024.0)) * 100.0) / 100.0;
			}

			driver.get(URL + "/node/" + nodeID);
			// need to wait until the content of the required widget/box in
			// dashboard is loaded
			(new WebDriverWait(driver, TIMEOUT_SEC)).until(new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver d) {
					return d.findElement(By.cssSelector("h1#avail-quota > span")).getText().length() > 0;
				}
			});
			mobileElement = driver.findElement(By.cssSelector("h1#avail-quota > span"));

			// if available quota drops below 1gb, is it shown in mb
			// in dashboard
			if (availableQuota < 1) {
				availableQuota = karmaData * 1024 + freeData * 1024 - usedData;
				// System.out.println(availableQuota + " MB");
				if (availableQuota <= 0) {
					Assert.assertEquals("0", mobileElement.getText().trim());
				} else {
					Assert.assertEquals(availableQuota + " MB", mobileElement.getText().trim());
				}
			} else {
				// we can directly compare
				Assert.assertEquals(availableQuota, Double.parseDouble(mobileElement.getText().split(" ")[0]), DELTA);
			}

			if (availableQuota > 0) {
				List<WebElement> list = driver.findElements(By.cssSelector("h1#avail-quota ~ small span.text-success"));
				Assert.assertEquals(Math.round((usedData / 1024) * 100.0) / 100.0,
						Double.parseDouble(list.get(0).getText().trim().split(" ")[0]), DELTA);
				String maxUserDataTemp = "";
				try {
					SQLHandler sh = new SQLHandler();
					maxUserDataTemp = sh.queryExecute(maxUserDataQuery + nodeID, 1).get(0);
				} catch (ClassNotFoundException | SQLException e) {
					e.printStackTrace();
				}
				Assert.assertEquals(maxUserDataTemp + " GB", list.get(1).getText().trim());
			} else {
				List<WebElement> list = driver.findElements(By.cssSelector("h1#avail-quota ~ small span.text-success"));
				Assert.assertEquals(Math.round((usedData / 1024) * 100.0) / 100.0,
						Double.parseDouble(list.get(0).getText().trim().split(" ")[0]), DELTA);
			}
		}
	}

	@Test(description = "Tests the router fup limit shown to user in dashboard")
	public void routerFUPLimit() {
		Calendar c = Calendar.getInstance();
		// int day = c.get(Calendar.DAY_OF_MONTH);
		int month = c.get(Calendar.MONTH) + 1; // months are 0 based
		int year = c.get(Calendar.YEAR);
		int nextMonth = month + 1;
		int nextYear = year;
		if (nextMonth > 12) {
			nextMonth = 1;
			nextYear++;
		}
		int[] daysArray = { 31, 30, 31, 30, 31, 30, 31, 31, 30, 31, 30 };
		// query to fetch billing day
		String getBillingDayQuery = "select billing_start_date from nodes where id = ";

		for (String nodeID : nodeListId) {
			int billingDay = 1;
			int nextBillingDay = 0;
			try {
				SQLHandler sh = new SQLHandler();
				billingDay = Integer.parseInt(sh.queryExecute(getBillingDayQuery + nodeID, 1).get(0));
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}
			nextBillingDay = billingDay - 1;
			if (nextBillingDay == 0) {
				nextBillingDay = daysArray[month]; // the end of the month
			}

			String getTotalDataUsedQuery = "SELECT SUM(incoming)+SUM(outgoing) AS total_usage FROM (SELECT incoming, outgoing FROM connections WHERE node_id = "
					+ nodeID + " AND date(updated_at) BETWEEN '" + year + "-" + month + "-" + billingDay + "' AND '"
					+ nextYear + "-" + nextMonth + "-" + nextBillingDay + "') AS t1;";
			// System.out.println(getTotalDataUsedQuery);
			double totalDataUsed = 0;
			try {
				SQLHandler sh = new SQLHandler();
				String r = sh.queryExecute(getTotalDataUsedQuery, 1).get(0);
				if (r != null) {
					totalDataUsed = Double.parseDouble(r);
				} else {
					totalDataUsed = 0;
				}
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}
			// in bytes, convert to gb
			totalDataUsed = totalDataUsed / (1024.0 * 1024.0 * 1024.0);
			System.out.println(nodeID + ": " + Math.round(totalDataUsed * 100.0) / 100.0);

			driver.get(URL + "/node/" + nodeID);
			(new WebDriverWait(driver, TIMEOUT_SEC)).until(new ExpectedCondition<Boolean>() {
				public Boolean apply(WebDriver d) {
					return d.findElements(By.cssSelector("h1#total-used-data > span")).size() > 0 && d
							.findElements(By.cssSelector("h1#total-used-data > span")).get(0).getText().length() > 0;
				}
			});
			mobileElement = driver.findElements(By.cssSelector("h1#total-used-data > span")).get(0);
			System.out.println("");
			Assert.assertEquals(Math.round(totalDataUsed * 100.0) / 100.0, Double.parseDouble(mobileElement.getText()),
					0.01);
		}
	}

	@Test(description = "Tests the data sharing feature")
	public void dataSharing() {
		/*
		 * Timeline of test: Check if DATA_SHARE_USER has existing entry in
		 * sharedata for DATA_SHARE_NODE and USERNAME Delete DATA_USER_SHARE
		 * entry in sharedata for DATA_SHARE_NODE and USERNAME if exists Share
		 * data to a new user Increase the data shared Reduce the data shared
		 * Delete entry
		 */

		// check DATA_SHARE_USER entry exists and delete if necessary
		String dataShareIdQ = "select id from sharedatas where nodeid=" + DATA_SHARE_NODE + " and donorid = " + USERNAME
				+ " and userid = " + DATA_SHARE_USER;
		try {
			SQLHandler sh = new SQLHandler();
			List<String> t = sh.queryExecute(dataShareIdQ, 1);
			if (t.size() != 0) {
				Assert.assertEquals(1, t.size());
				Assert.assertNotNull(t.get(0));
				driver.get(URL + "/sharedata/delete?id=" + t.get(0).toString());
			}
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}

		// fetch current value of avail_quota
		// if not above certain limit, will fail the test
		// needs atleast 10gb avail_quota for test
		Double availQuota = 0d;
		driver.get(URL + "/node/" + DATA_SHARE_NODE);
		(new WebDriverWait(driver, TIMEOUT_SEC)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				return d.findElement(By.cssSelector("h1#avail-quota > span")).getText().length() > 0;
			}
		});
		mobileElement = driver.findElement(By.cssSelector("h1#avail-quota > span"));
		availQuota = Double.parseDouble(mobileElement.getText().trim().split(" ")[0]);
		if (availQuota < 10) {
			System.out.println("Cannot perform data-share test - user does not have atleast than 10 gb of data");
			Assert.assertEquals(10.0, availQuota, 0.0); // see message above
		}

		// share 8 gb
		driver.get(URL + "/sharedata/node/" + DATA_SHARE_NODE + "/sharedata/new");
		mobileElement = driver.findElement(By.cssSelector("input#sharedata_userid.form-control"));
		mobileElement.sendKeys(DATA_SHARE_USER);
		mobileElement = driver.findElement(By.cssSelector("input#sharedata_sharedata.form-control"));
		mobileElement.sendKeys("8");
		mobileElement.submit();
		driver.get(URL + "/node/" + DATA_SHARE_NODE);
		(new WebDriverWait(driver, TIMEOUT_SEC)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				return d.findElement(By.cssSelector("h1#avail-quota > span")).getText().length() > 0;
			}
		});
		mobileElement = driver.findElement(By.cssSelector("h1#avail-quota > span"));
		Assert.assertEquals(availQuota - 8, Double.parseDouble(mobileElement.getText().trim().split(" ")[0]), DELTA);

		// String dataShareIdQ = "select id from sharedatas where nodeid=" +
		// DATA_SHARE_NODE + " and donorid = " + USERNAME + " and userid = " +
		// DATA_SHARE_USER;
		String dataShareID = "";
		try {
			SQLHandler sh = new SQLHandler();
			List<String> t = sh.queryExecute(dataShareIdQ, 1);
			Assert.assertEquals(1, t.size());
			Assert.assertNotNull(t.get(0));
			dataShareID = t.get(0).toString();
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
		}

		// increase to 10 gb
		driver.get(URL + "/sharedata/edit?id=" + dataShareID);
		mobileElement = driver.findElement(By.cssSelector("input#sharedata_sharedata.form-control"));
		mobileElement.clear();
		mobileElement.sendKeys("10");
		mobileElement.submit();
		// driver.get(URL + "/node/" + DATA_SHARE_NODE);
		(new WebDriverWait(driver, TIMEOUT_SEC)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				return d.findElement(By.cssSelector("h1#avail-quota > span")).getText().length() > 0;
			}
		});
		mobileElement = driver.findElement(By.cssSelector("h1#avail-quota > span"));
		Assert.assertEquals(availQuota - 10, Double.parseDouble(mobileElement.getText().trim().split(" ")[0]), DELTA);

		// reduce to 5gb
		driver.get(URL + "/sharedata/edit?id=" + dataShareID);
		mobileElement = driver.findElement(By.cssSelector("input#sharedata_sharedata.form-control"));
		mobileElement.clear();
		mobileElement.sendKeys("5");
		mobileElement.submit();
		// driver.get(URL + "/node/" + DATA_SHARE_NODE);
		(new WebDriverWait(driver, TIMEOUT_SEC)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				return d.findElement(By.cssSelector("h1#avail-quota > span")).getText().length() > 0;
			}
		});
		mobileElement = driver.findElement(By.cssSelector("h1#avail-quota > span"));
		Assert.assertEquals(availQuota - 5, Double.parseDouble(mobileElement.getText().trim().split(" ")[0]), DELTA);

		// delete entry
		driver.get(URL + "/sharedata/delete?id=" + dataShareID);
		driver.get(URL + "/node/" + DATA_SHARE_NODE);
		(new WebDriverWait(driver, TIMEOUT_SEC)).until(new ExpectedCondition<Boolean>() {
			public Boolean apply(WebDriver d) {
				return d.findElement(By.cssSelector("h1#avail-quota > span")).getText().length() > 0;
			}
		});
		mobileElement = driver.findElement(By.cssSelector("h1#avail-quota > span"));
		Assert.assertEquals(availQuota, Double.parseDouble(mobileElement.getText().trim().split(" ")[0]), DELTA);
	}

	@Test(description = "Checks the data that has been shared to the user on all the nodes")
	public void dataRecieved() {
		String getDataShared = "SELECT sharedata, donorid FROM `sharedatas` where userid=" + USERNAME + " and nodeid=";
		System.out.println(nodeListId);
		for (String nodeID : nodeListId) {
			// get the data shared to USERNAME on this nodeID
			List<ArrayList<String>> dataSharedList = null;
			try {
				SQLHandler sh = new SQLHandler();
				dataSharedList = sh.queryExecuteList(getDataShared + nodeID, 2);
			} catch (ClassNotFoundException | SQLException e) {
				e.printStackTrace();
			}

			driver.get(URL + "/node/" + nodeID);
			(new WebDriverWait(driver, TIMEOUT_SEC))
					.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("table#table-data-recieved")));
			mobileElement = driver.findElement(By.cssSelector("table#table-data-recieved"));
			List<WebElement> tableRows = mobileElement.findElements(By.cssSelector("tbody tr"));
			// alt
			/*
			 * List<WebElement> tableRows =
			 * mobileElement.findElements(By.cssSelector(
			 * "table#table-data-recieved tbody tr"));
			 */

			if (dataSharedList.size() == 0) {
				// the table should appear with no rows
				Assert.assertEquals(0, tableRows.size());
			} else {
				// table should appear with rows
				//same number of rows
				Assert.assertEquals(dataSharedList.size(), tableRows.size());
				for(int i = 0; i < tableRows.size(); i++) {
					WebElement row = tableRows.get(i);
					//need to wait for a few seconds while table is loaded for some reason
					try { Thread.sleep(1000); } catch(InterruptedException e) { e.printStackTrace(); }  
					List<WebElement> cells = row.findElements(By.cssSelector("td"));
					Assert.assertEquals(dataSharedList.get(i).get(0).trim(), cells.get(0).getText().trim());
					Assert.assertEquals(dataSharedList.get(i).get(1).trim(), cells.get(1).getText().trim());
				}
			}
		}
	}

	/*
	 * @Test(description = "Tests the data allocation for a user.") public void
	 * dataAllocation() { int id = 1734; // id for 9535354545 on nodeid 2 in
	 * userdatas String initAmt = "0"; String finalAmt = "100"; String q =
	 * "select freedata from userdatas where id = " + id; SQLHandler sh; String
	 * dataAllocated = "NA";
	 * 
	 * // selecting the user to allocate and deallocate to. String
	 * adminCheckQuery = "select id from nodes where public_phone_number = " +
	 * USERNAME + " limit 1;"; String userSelectQuery =
	 * "select id from userdatas where nodeid = "; try { sh = new SQLHandler();
	 * List<String> temp = sh.queryExecute(adminCheckQuery, 1); // if
	 * (temp.size() > 0) { for (String w : temp) { List<String> tempUsers =
	 * sh.queryExecute(userSelectQuery + w, 1); // if (tempUsers.size() > 0) {
	 * id = Integer.parseInt(tempUsers.get(0)); break; } } // if we get here,
	 * then the user's admin nodes do not have any // person connected to them.
	 * System.out.println("[dataAllocation] " + USERNAME +
	 * " has no one connected to admin nodes."); return; } // exit if user has
	 * no admin nodes else { System.out.println("[dataAllocation] " + USERNAME +
	 * " has no admin nodes."); return; } } catch (ClassNotFoundException |
	 * SQLException e1) { e1.printStackTrace(); }
	 * 
	 * // add 0gb to user on his admin node
	 * driver.get("http://authpuppy.localhost.com/userdata/edit?id=" + id);
	 * mobileElement =
	 * driver.findElement(By.cssSelector("input#userdata_freedata"));
	 * mobileElement.clear(); mobileElement.sendKeys(initAmt);
	 * mobileElement.submit();
	 * 
	 * try { sh = new SQLHandler(); dataAllocated = sh.queryExecute(q,
	 * 1).get(0).toString(); } catch (ClassNotFoundException | SQLException e) {
	 * e.printStackTrace(); } Assert.assertEquals(initAmt, dataAllocated);
	 * 
	 * // add 100gb to user on his admin node
	 * driver.get("http://authpuppy.localhost.com/userdata/edit?id=" + id);
	 * mobileElement =
	 * driver.findElement(By.cssSelector("input#userdata_freedata"));
	 * mobileElement.clear(); mobileElement.sendKeys(finalAmt);
	 * mobileElement.submit();
	 * 
	 * try { sh = new SQLHandler(); dataAllocated = sh.queryExecute(q,
	 * 1).get(0).toString(); } catch (ClassNotFoundException | SQLException e) {
	 * e.printStackTrace(); } Assert.assertEquals(finalAmt, dataAllocated);
	 * 
	 * // add 100gb to user on his admin node
	 * driver.get("http://authpuppy.localhost.com/userdata/edit?id=" + id);
	 * mobileElement =
	 * driver.findElement(By.cssSelector("input#userdata_freedata"));
	 * mobileElement.clear(); mobileElement.sendKeys(initAmt);
	 * mobileElement.submit();
	 * 
	 * // one final check, since we can do it, why not try { sh = new
	 * SQLHandler(); dataAllocated = sh.queryExecute(q, 1).get(0).toString(); }
	 * catch (ClassNotFoundException | SQLException e) { e.printStackTrace(); }
	 * Assert.assertEquals(initAmt, dataAllocated);
	 * 
	 * }
	 */

	@Test(description = "Tests the UI for the data topup page - I")
	public void dataTopUpUI1() {
		driver.get("http://authpuppy.localhost.com/payment/new");
		// checking ui elements
		(new WebDriverWait(driver, TIMEOUT_SEC))
				.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input#ap_user_payment_buy_data")));
	}

	@Test(description = "Tests the UI for the data topup page - II")
	public void dataTopUpUI2() {
		// checking ui elements
		(new WebDriverWait(driver, TIMEOUT_SEC))
				.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("input#pay_submit")));
	}

	@Test(description = "Tests the UI for the data topup page - III")
	public void dataTopUpUI3() {
		// checking ui elements
		(new WebDriverWait(driver, TIMEOUT_SEC))
				.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".text-muted")));
	}

	@Test(description = "Tests the UI for the data topup page - Input (Invalid)")
	public void dataTopUpInput() {
		driver.get("http://authpuppy.localhost.com/payment/new");
		mobileElement = driver.findElement(By.id("ap_user_payment_buy_data"));
		mobileElement.sendKeys("asdf");

		WebElement openModal = driver.findElement(By.id("pay_submit"));
		String[] rupee = openModal.getText().split(" ");
		String regex = "\\d+";
		Assert.assertTrue(rupee[2].matches(regex));

		/*
		 * //merchant DIV (new WebDriverWait(driver, TIMEOUT_SEC))
		 * .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(
		 * "div#merchant")));
		 * 
		 * //merchant name (new WebDriverWait(driver, TIMEOUT_SEC))
		 * .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(
		 * "div#merchant-name")));
		 * 
		 * //Amount purchased (new WebDriverWait(driver, TIMEOUT_SEC))
		 * .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(
		 * "div#amount")));
		 * 
		 * //merchant caption (new WebDriverWait(driver, TIMEOUT_SEC))
		 * .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(
		 * "div#merchant-desc")));
		 * 
		 * //checking contact (new WebDriverWait(driver, TIMEOUT_SEC))
		 * .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(
		 * "input#contact")));
		 * 
		 * //checking email (new WebDriverWait(driver, TIMEOUT_SEC))
		 * .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(
		 * "input#email")));
		 * 
		 * //checking payment options (new WebDriverWait(driver, TIMEOUT_SEC))
		 * .until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(
		 * "div#payment-options")));
		 * 
		 */
	}

	@Test(description = "Tests the amount calculation for the data topup page")
	public void dataTopUpAmount() {
		driver.get("http://authpuppy.localhost.com/payment/new");
		mobileElement = driver.findElement(By.id("ap_user_payment_buy_data"));
		mobileElement.sendKeys("123");

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
		DELTA = Double.parseDouble(context.getCurrentXmlTest().getParameter("delta-comparisons"));
		DATA_SHARE_NODE = context.getCurrentXmlTest().getParameter("data-share-node");
		DATA_SHARE_USER = context.getCurrentXmlTest().getParameter("data-share-user");
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
