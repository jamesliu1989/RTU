package com.nju.rtu.util;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

public class Config {

	private static PropertiesConfiguration config = null;
	static {
			try {
				config = new PropertiesConfiguration("conf/config.properties");
			} catch (ConfigurationException e) {
				e.printStackTrace();
			}
	}

	public static String getValue(String key) {
		return config.getString(key, null);
	}
    	
	public static void setValue(String key, Object value) {
		config.setProperty(key, value);
		try {
			config.save();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}
    
}