package com.nisovin.yapp;

import org.bukkit.permissions.PermissionAttachment;

public class PermissionNode {
	
	private String node;
	private boolean value;
	
	public PermissionNode(String node, boolean value) {
		this.node = node.trim();
		this.value = value;
	}
	
	public PermissionNode(String nodeData) {
		nodeData = nodeData.trim();
		if (nodeData.charAt(0) == '+') {
			this.node = nodeData.substring(1).trim();
			this.value = true;
		} else if (nodeData.charAt(0) == '-') {
			this.node = nodeData.substring(1).trim();
			this.value = false;
		} else if (nodeData.contains(":")) {
			String[] data = nodeData.split(":", 2);
			this.node = data[0].trim();
			String val = data[1].trim();
			if (val.equalsIgnoreCase("false") || val.equalsIgnoreCase("off")) {
				this.value = false;
			} else {
				this.value = true;
			}
		} else {
			this.node = nodeData;
			this.value = true;
		}
	}
	
	public String getNodeName() {
		return node;
	}
	
	public boolean getValue() {
		return value;
	}
	
	public void addTo(PermissionAttachment attachment) {
		attachment.setPermission(node, value);
	}
	
	@Override
	public String toString() {
		return node + " : " + (value?"true":"false");
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof PermissionNode && ((PermissionNode)o).node.equals(this.node)) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return node.hashCode();
	}
}
