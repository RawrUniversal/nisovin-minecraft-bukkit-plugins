package com.nisovin.yapp;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class SimpleConfig {

	private Map<String,Map<String,String>> values;
	
	public SimpleConfig(File file) {
		values = new HashMap<String, Map<String,String>>();
		
		if (file.exists()) {
			try {
				String section = "";
				Scanner scanner = new Scanner(file);
				String line, key, val;
				String[] data;
				while (scanner.hasNext()) {
					line = scanner.nextLine().trim();
					
					if (line.startsWith("#") || line.startsWith("//")) {
						// ignore
						continue;
					} else if (line.startsWith("=")) {
						section = line.replace("=", "").trim().toLowerCase();
						continue;
					} else if (line.contains(":")) {
						data = line.split(":");
						key = data[0].trim();
						val = data[1].trim();
					} else if (line.contains("=")) {
						data = line.split("=");
						key = data[0].trim();
						val = data[1].trim();
					} else {
						// no key/value pair, so ignoring line
						continue;
					}
					if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
						val = val.substring(1, val.length() - 2);
					}
					Map<String,String> secdata = values.get(section);
					if (secdata == null) {
						secdata = new HashMap<String,String>();
						values.put(section, secdata);
					}
					secdata.put(key.toLowerCase(), val);
				}
				scanner.close();
			} catch (IOException e) {
			}
		}
	}
	
	private String getValue(String path) {
		path = path.toLowerCase();
		if (path.contains(".")) {
			String[] pathdata = path.split("\\.");
			Map<String,String> secdata = values.get(pathdata[0]);
			if (secdata == null) {
				return null;
			} else {
				return secdata.get(pathdata[1]);
			}
		} else {
			Map<String,String> secdata = values.get("");
			if (secdata == null) {
				return null;
			} else {
				return secdata.get(path);
			}
		}
	}
	
	public String getString(String path) {
		return getValue(path);
	}
	
	public int getint(String path) {
		String val = getValue(path);
		if (val == null) {
			return 0;
		} else {
			int i = 0;
			try {
				i = Integer.parseInt(val);
			} catch (NumberFormatException e) {				
			}
			return i;
		}
	}
	
	public Integer getInteger(String path) {
		String val = getValue(path);
		if (val == null) {
			return null;
		} else {
			try {
				return Integer.valueOf(val);
			} catch (NumberFormatException e) {
				return null;
			}
		}
	}
	
	public double getdouble(String path) {
		String val = getValue(path);
		if (val == null) {
			return 0;
		} else {
			double i = 0;
			try {
				i = Double.parseDouble(val);
			} catch (NumberFormatException e) {				
			}
			return i;
		}
	}
	
	public Double getDouble(String path) {
		String val = getValue(path);
		if (val == null) {
			return null;
		} else {
			try {
				return Double.valueOf(val);
			} catch (NumberFormatException e) {
				return null;
			}
		}
	}
	
	public boolean getboolean(String path) {
		String val = getValue(path);
		if (val == null) {
			return false;
		} else {
			val = val.toLowerCase();
			if (val.equalsIgnoreCase("true") || val.startsWith("y") || val.equalsIgnoreCase("on")) {
				return true;
			} else {
				return false;
			}
		}
	}
	
	public Boolean getBoolean(String path) {
		String val = getValue(path);
		if (val == null) {
			return null;
		} else {
			val = val.toLowerCase();
			if (val.equalsIgnoreCase("true") || val.startsWith("y") || val.equalsIgnoreCase("on")) {
				return true;
			} else if (val.equalsIgnoreCase("false") || val.startsWith("n") || val.equalsIgnoreCase("off")) {
				return false;
			} else {
				return null;
			}
		}
	}
	
}
