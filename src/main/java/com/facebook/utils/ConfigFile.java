package com.facebook.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigFile {
	private Properties prop = new Properties();
	
	public ConfigFile(String configPath) throws IOException {
//		try {
//			FileInputStream fis = new FileInputStream(configPath);
//			prop.load(fis);
//			fis.close();
//		} catch(Exception e) {
//			e.printStackTrace();
//		}
		
		FileInputStream fis = new FileInputStream(configPath);
		prop.load(fis);
		fis.close();
	}
	
	public Properties getProp() {
		return this.prop;
	}
	
	public boolean keyExists(String keyName) {
		return prop.containsKey(keyName);
	}
	
	public String getPropertyValue(String keyName) {
		if(keyExists(keyName)) {
			return prop.getProperty(keyName);
		} else {
			return null;
		}
	}
}