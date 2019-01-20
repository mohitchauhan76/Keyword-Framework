package com.facebook.test;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.PropertyConfigurator;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import com.facebook.utils.ConfigFile;
import com.facebook.utils.ExcelObject;
import com.facebook.utils.ExtentManager;
import com.facebook.utils.ObjectRepository;
import com.facebook.utils.UIUtility;
import com.relevantcodes.extentreports.ExtentTest;
import com.relevantcodes.extentreports.LogStatus;

public class TestDriver {
	private WebDriver driver = null;
	static ConfigFile appConfig;
	static ObjectRepository or;
	SoftAssert softAssertion = new SoftAssert();
	ExtentTest extentTest;

	static {
		try {
			appConfig = new ConfigFile("Framework//Config//AppConfig.prop");
			or = new ObjectRepository("Framework//OR//ObjectRepository.xlsx");
			PropertyConfigurator.configure("log4j.properties");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@BeforeClass
	public void suiteSetup() throws IOException {
		String browserName = appConfig.getPropertyValue("BrowserName");

		try {
			switch (browserName.toUpperCase()) {
			case "CHROME":
				driver = UIUtility.createDriver(browserName, appConfig.getPropertyValue("ChromeDriverPath"));
				break;
			case "FIREFOX":
				driver = UIUtility.createDriver(browserName, appConfig.getPropertyValue("GeckoDriverPath"));
				break;
			case "IE":
			case "INTERNETEXPLORER":
				driver = UIUtility.createDriver(browserName, appConfig.getPropertyValue("IEDriverPath"));
				break;
			default:
				break;
			}
		} catch (Exception e) {
			System.out.println("Exception in creating driver");
		}
	}

	@BeforeMethod
	public void testSetup() {
		try {
			driver.get(appConfig.getPropertyValue("AppURL"));
			driver.manage().window().maximize();

			int waitTime = Integer.parseInt(appConfig.getPropertyValue("AVGWAITTTIME"));
			driver.manage().timeouts().implicitlyWait(waitTime, TimeUnit.SECONDS);
			driver.manage().timeouts().pageLoadTimeout(waitTime, TimeUnit.SECONDS);
			driver.manage().timeouts().setScriptTimeout(waitTime, TimeUnit.SECONDS);
		} catch (Exception e) {
			System.out.println("Exception while setting up driver");
		}
	}

	@Test(priority = 1, enabled = true, dataProvider = "getTest", dataProviderClass = com.facebook.data.TestData.class)
	public void testScript(String tcName, String execute) {
		try {
			extentTest = ExtentManager.getInstance().startTest(tcName);

			UIUtility.takeScreenshot(driver, "Framework\\Results\\Screenshot_" + tcName + "_Pre.png");

			ExcelObject excelObj = new ExcelObject("Framework\\Scripts\\TestScripts.xlsx");
			HashMap<Integer, List<Object>> data = excelObj.getExcelData("Scripts", "TCName=" + tcName);
			excelObj.closeWorkbook();

			for (int i = 2; i <= data.size(); i++) {
				List<Object> listData = data.get(i);

				String action = listData.get(1).toString();
				String screenName = listData.get(2).toString();
				String objectName = listData.get(3).toString();
				String fieldValue = listData.get(4).toString();

				extentTest.log(LogStatus.INFO, "Executing action " + action);

				switch (action.toUpperCase()) {
				case "NAVIGATE":
					driver.get(fieldValue);
					extentTest.log(LogStatus.PASS, "Successfully navigated to " + fieldValue);
					break;
				case "CLICK":
					driver.findElement(or.getLocator(screenName, objectName)).click();
					extentTest.log(LogStatus.PASS,
							"Successfully clicked on " + objectName + " of screen " + screenName);
					break;
				case "VERIFYTITLE":
					if (driver.getTitle().equalsIgnoreCase(fieldValue)) {
						extentTest.log(LogStatus.PASS, "Successfully validated title " + fieldValue);
					} else {
						extentTest.log(LogStatus.FAIL,
								"Failed to validate title " + driver.getTitle() + ". Expected value is " + fieldValue);
					}
					// softAssertion.assertEquals(driver.getTitle(), fieldValue);
					break;
				case "ENTER":
					driver.findElement(or.getLocator(screenName, objectName)).sendKeys(fieldValue);
					extentTest.log(LogStatus.PASS, "Successfully enter value " + fieldValue + " in " + objectName
							+ " of screen " + screenName);
					break;
				case "SELECT":
					UIUtility.selectValue(driver, or.getLocator(screenName, objectName), "text", fieldValue);
					extentTest.log(LogStatus.PASS, "Successfully selected option text " + fieldValue + " in "
							+ objectName + " of screen " + screenName);
					break;
				case "WAITTIME":
					Thread.sleep(Integer.parseInt(fieldValue));
					extentTest.log(LogStatus.PASS, "Waited for time " + fieldValue);
					break;
				case "VERIFYELEMENT":
					if (UIUtility.isElementExist(driver, or.getLocator(screenName, objectName))) {
						extentTest.log(LogStatus.PASS, "Successfully validated that object " + objectName
								+ " of screen " + screenName + " exists");
					} else {
						extentTest.log(LogStatus.PASS,
								"Failed - Object " + objectName + " of screen " + screenName + " does not exist");
					}
					// softAssertion.assertTrue(UIUtility.isElementExist(driver,
					// or.getLocator(screenName, objectName)));
					break;
				case "VERIFYELEMENTNOTPRESENT":
					if (UIUtility.isElementExist(driver, or.getLocator(screenName, objectName))) {
						extentTest.log(LogStatus.FAIL,
								"Failed - Object " + objectName + " of screen " + screenName + " exists");
					} else {
						extentTest.log(LogStatus.PASS, "Successfully validated that Object " + objectName
								+ " of screen " + screenName + " does not exist");
					}

					// softAssertion.assertFalse(UIUtility.isElementExist(driver,
					// or.getLocator(screenName, objectName)));
					break;
				case "VERIFYTEXT":
					String text = driver.findElement(or.getLocator(screenName, objectName)).getText();
					if (text.equalsIgnoreCase(fieldValue)) {
						extentTest.log(LogStatus.PASS, "Successfully validated text of object " + objectName
								+ " of screen " + screenName + " is " + fieldValue);
					} else {
						extentTest.log(LogStatus.PASS, "Failed - Text of " + objectName + " of screen " + screenName
								+ " is " + text + " but expected value is " + fieldValue);
					}
					// softAssertion.assertEquals(driver.findElement(or.getLocator(screenName,
					// objectName)).getText(),
					// fieldValue);
					break;
				default:
					break;
				}
			}
			UIUtility.takeScreenshot(driver, "Framework\\Results\\Screenshot_" + tcName + "_Post.png");
			softAssertion.assertAll();

			ExtentManager.getInstance().endTest(extentTest);
			ExtentManager.getInstance().flush();
		} catch (Exception e) {
			extentTest.log(LogStatus.ERROR, "Exception " + e.getClass().getSimpleName() + "occured");
		}
	}

	@AfterClass
	public void tearDown() {
		if (driver != null) {
			driver.quit();
		}
		ExtentManager.getInstance().close();
	}
}