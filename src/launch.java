import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;

public class launch {

	public static void main(String[] args) {

		// Create a new instance of Chrome Browser
		WebDriver driver = new ChromeDriver();
		System.out.println("Launching Chromium [OK]");
		// Maximize the Browser window
		driver.manage().window().maximize();
		
		// Open the URL in Chrome browser
		driver.get("http://app.griggi.com/");
		System.out.println("Navigated to url [OK]");
		// Get the ref mobile input element
		WebElement mobileElement = driver.findElement(By
				.id("pocha_pocha_mobile"));
		// entering mobile number
		mobileElement.sendKeys("9538155667");
		System.out.println("Entering mobile number [OK]");
		//Submitting the form
		mobileElement.submit();
		System.out.println("Mobile number submitted [OK]");
		// ref to password input field
		WebElement passElement = driver.findElement(By
				.id("apAuthLocalUser_password"));
		passElement.sendKeys("aura1234");
		System.out.println("Password [OK]");
		//submitting the form
		passElement.submit();
		
		driver.quit();
	}
}