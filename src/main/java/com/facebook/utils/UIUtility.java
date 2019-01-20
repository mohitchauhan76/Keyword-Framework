package com.facebook.utils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

public class UIUtility {
	public static WebDriver createDriver(String browserName, String driverPath) {
		WebDriver driver = null;

		switch (browserName.toUpperCase()) {
		case "CHROME":
			System.setProperty("webdriver.chrome.driver", new File(driverPath).getAbsolutePath());
			driver = new ChromeDriver();
			break;
		case "FIREFOX":
			System.setProperty("webdriver.gecko.driver", new File(driverPath).getAbsolutePath());
			driver = new FirefoxDriver();
			break;
		case "IE":
		case "INTERNETEXPLORER":
			System.setProperty("webdriver.ie.driver", new File(driverPath).getAbsolutePath());
			driver = new InternetExplorerDriver();
			break;
		default:
			break;
		}

		return driver;
	}

	public static boolean isElementExist(WebDriver driver, By by) {
		List<WebElement> elements = driver.findElements(by);

		if (elements.size() == 0) {
			return false;
		} else {
			return true;
		}
		// return (driver.findElements(by).size() != 0);
	}

	public static void takeScreenshot(WebDriver driver, String filePath) {
		TakesScreenshot ts = (TakesScreenshot) driver;
		File f = ts.getScreenshotAs(OutputType.FILE);
		f.renameTo(new File(filePath));
	}

	public static String takeScreenshotAsDate(WebDriver driver, String filePrefix) {
		TakesScreenshot ts = (TakesScreenshot) driver;
		File f = ts.getScreenshotAs(OutputType.FILE);
		DateFormat df = new SimpleDateFormat("dd-MM-yyyy_hhmmss");
		String date = df.format(new Date());
		File dest = new File(filePrefix + "\\Screenshot_" + date + ".png");
		f.renameTo(dest);
		return dest.getAbsolutePath();
	}

	public static void inputTextJScript(WebDriver driver, WebElement element, String data) {
		element.clear();

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}

		executeScript(driver, "arguments[0].scrollIntoView(true);", element);
		executeScript(driver, "arguments[0].setAttribute('value', '" + data + "');", element);
	}

	public static Object executeScript(WebDriver driver, String script, Object... args) {
		return ((JavascriptExecutor) (driver)).executeScript(script, args);
	}

	public static void highLightElement(WebDriver driver, WebElement element) {
		executeScript(driver, "arguments[0].setAttribute('style', 'border: 2px solid blue;');", element);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {

		}

		executeScript(driver, "arguments[0].setAttribute('style', 'border: 3px solid blue;');", element);
	}

	public static void selectValue(WebDriver driver, By by, String optionText) {
		optionText = optionText.trim();

		if (optionText.toLowerCase().startsWith("index=")) {
			selectValue(driver, by, "index", optionText.replaceAll("index=", ""));
		} else if (optionText.toLowerCase().startsWith("text=")) {
			selectValue(driver, by, "text", optionText.replaceAll("text=", ""));
		} else if (optionText.toLowerCase().startsWith("containstext=")) {
			selectValue(driver, by, "containstext", optionText.replaceAll("containstext=", ""));
		} else if (optionText.startsWith("value=")) {
			selectValue(driver, by, "value", optionText.replaceAll("value=", ""));
		} else {
			new Select(driver.findElement(by)).selectByVisibleText(optionText);
		}
	}

	public static void selectValue(WebDriver driver, By by, String selectBy, String option) {
		Select select = new Select(driver.findElement(by));

		switch (selectBy.toLowerCase()) {
		case "index":
			select.selectByIndex(Integer.valueOf(option));
			break;
		case "text":
			select.selectByVisibleText(option);
			break;
		case "value":
			select.selectByValue(option);
			break;
		case "containstext":
			int indexNum = 1;
			for (WebElement element : select.getOptions()) {
				if (element.getText().toLowerCase().contains(option.toLowerCase())) {
					select.selectByIndex(indexNum);
					break;
				}
				indexNum++;
			}
			break;
		default:
			break;
		}
	}

	public static void clickElementJScript(WebDriver driver, By by) {
		WebElement element = null;

		try {
			element = findElement(driver, by, 20);
		} catch (Exception e) {
		}

		clickElementJScript(driver, element);
	}

	public static void clickElementJScript(WebDriver driver, WebElement element) {
		executeScript(driver, "arguments[0].scrollIntoView(true);", element);
		executeScript(driver, "arguments[0].click();", element);
	}

	public static WebElement findElement(WebDriver driver, By by, int waitTime) {
		WebDriverWait wWait = new WebDriverWait(driver, waitTime);
		return wWait.until(ExpectedConditions.presenceOfElementLocated(by));
	}
}