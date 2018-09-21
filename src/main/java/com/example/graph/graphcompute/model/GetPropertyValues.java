package com.example.graph.graphcompute.model;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

public class GetPropertyValues {
	private String urlDC = ""; // algorithm to calculate for the latest

//	read the config files and populate the elements
	public void readValues() {
		try {
			Properties properties = new Properties();
			String propertiesFile = "config.properties";

			InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propertiesFile);

			if (inputStream != null) {
				properties.load(inputStream);
			} else {
				throw new FileNotFoundException("property file '" + propertiesFile + "' not found");
			}

			// get the property value
			urlDC = properties.getProperty("url-datacenter");
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
	}

	public String getUrlDC() {
		return urlDC;
	}

	public void setUrlDC(String urlDC) {
		this.urlDC = urlDC;
	}

}
