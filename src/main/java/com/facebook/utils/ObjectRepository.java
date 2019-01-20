package com.facebook.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.openqa.selenium.By;

public class ObjectRepository {
	static Logger LOGGER = Logger.getLogger(ObjectRepository.class);

	private static HashMap<String, String> elementMap;

	public ObjectRepository(String orPath) throws IOException {
		LOGGER.info("======Started populating element collection map======");

		long startTime = System.currentTimeMillis();
		elementMap = new HashMap<>();

		ExcelObject excelObject = new ExcelObject(new File(orPath).getCanonicalPath());
		HashMap<Integer, List<Object>> queryResult = excelObject.getExcelData("UIElements", false);
		excelObject.closeWorkbook();

		for (int counter = 2; counter <= queryResult.size(); counter++) {
			List<Object> objInfo = queryResult.get(counter);

			String screenName = objInfo.get(0).toString().trim().toUpperCase();
			String elementName = objInfo.get(1).toString().trim().toUpperCase();
			String locatorType = objInfo.get(2).toString().trim().toUpperCase();
			String locatorValue = objInfo.get(3).toString().trim();

			if (!elementMap.containsKey(screenName + ":=" + elementName)) {
				elementMap.put(screenName + ":=" + elementName, locatorType + "##" + locatorValue);
			}
		}

		long finishTime = System.currentTimeMillis();

		LOGGER.info(
				"======Time taken to populate object repository in millis is " + (finishTime - startTime) + "======");
		LOGGER.info("======Number of object in Object Map is " + (elementMap.size() - 1) + "======");
	}

	public String[] getObject(String screenName, String fieldName) {
		String[] returnObject = new String[2];
		String elementKey = screenName.toUpperCase() + ":=" + fieldName.toUpperCase();

		if (elementMap.containsKey(elementKey)) {
			returnObject[0] = (elementMap.get(elementKey).split("##"))[0];
			returnObject[1] = (elementMap.get(elementKey).split("##"))[1];
		} else {
			return null;
		}

		return returnObject;
	}

	public By getLocator(String screenName, String fieldName) {
		By byObject = null;

		String[] getObject = getObject(screenName, fieldName);

		if (getObject != null) {
			switch (getObject[0]) {
			case "ID":
				byObject = By.id(getObject[1]);
				break;
			case "NAME":
				byObject = By.name(getObject[1]);
				break;
			case "XPATH":
				byObject = By.xpath(getObject[1]);
				break;
			case "CLASS":
			case "CLASSNAME":
				byObject = By.className(getObject[1]);
				break;
			case "TAG":
			case "TAGNAME":
				byObject = By.tagName(getObject[1]);
				break;
			case "LINK":
			case "LINKTEXT":
				byObject = By.linkText(getObject[1]);
				break;
			case "PARTIALLINKTEXT":
				byObject = By.partialLinkText(getObject[1]);
				break;
			case "CSS":
			case "CSSSELECTOR":
				byObject = By.cssSelector(getObject[1]);
				break;
			default:
				break;
			}
		}
		return byObject;
	}
}