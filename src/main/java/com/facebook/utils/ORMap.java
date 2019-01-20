package com.facebook.utils;

import java.io.IOException;

import org.openqa.selenium.By;

public class ORMap {
	private ConfigFile orConfig;
	
	public ORMap(String orPath) {
		try {
			orConfig = new ConfigFile(orPath);
		} catch (IOException e) {
			System.out.println("Wrong OR PAth");
		}
	}
	
	public String[] getLocatorValues(String objectName) {
		String value = orConfig.getPropertyValue(objectName);
		String arr[] = value.split("::");
		return arr;
	}
	
	public By getLocator(String objectName) {
		String value = orConfig.getPropertyValue(objectName);
		String arr[] = value.split("::");
		
		switch (arr[0].trim().toUpperCase()) {
		case "ID":
			return By.id(arr[1].trim());
		case "XPATH":
			return By.xpath(arr[1].trim());
		case "CLASS":
		case "CLASSNAME":
			return By.className(arr[1].trim());
		case "LINKTEXT":
		case "LINK":
			return By.linkText(arr[1].trim());
		case "TAG":
		case "TAGNAME":
			return By.tagName(arr[1].trim());
		case "NAME":
			return By.name(arr[1].trim());
		case "CSS":
		case "CSSSELECTOR":
			return By.cssSelector(arr[1].trim());
		default:
			break;
		}
		return null;
	}
}