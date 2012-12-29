package com.nisovin.yapp.storage;

import java.util.Map;

import com.nisovin.yapp.Group;
import com.nisovin.yapp.PermissionContainer;

public interface StorageMethod {

	public void fillGroupMap(Map<String, Group> groups);
	
	public void load(PermissionContainer container);
	
	public void save(PermissionContainer container);
	
}
