package com.nisovin.yapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class TrackedNodeList {

	private PermissionContainer source;
	private Map<String, TrackedNode> realNodes = new HashMap<String, TrackedNode>();
	private List<TrackedNode> allNodes = new ArrayList<TrackedNode>();
	
	public TrackedNodeList(PermissionContainer source) {
		this.source = source;
	}
	
	public void add(PermissionNode node, PermissionContainer container, String world) {
		add(node.getNodeName(), node.getValue(), container, world);
		// TODO: process star nodes
	}
	
	public void add(PermissionAttachmentInfo perm) {
		add(perm.getPermission(), perm.getValue(), null, null);
	}
	
	private void add(String permission, boolean value, PermissionContainer container, String world) {
		TrackedNode existingNode = realNodes.get(permission);
		NodeState state = NodeState.NORMAL;
		if (existingNode != null) {
			if (existingNode.getValue() == true) {
				state = NodeState.REDEFINED;
			} else {
				state = NodeState.OVERRIDDEN;
			}
		}
		TrackedNode newNode = new TrackedNode(permission, value, container, world, state);
		allNodes.add(newNode);
		if (existingNode == null) {
			realNodes.put(permission, newNode);
		}
	}
	
	public List<TrackedNode> getTrackedNodes() {
		return allNodes;
	}
	
	public class TrackedNode {
		
		String name;
		boolean value;
		PermissionContainer container;
		String world;
		NodeState state;

		public TrackedNode(String node, boolean value, PermissionContainer container, String world, NodeState state) {
			this.name = node;
			this.value = value;
			this.container = container;
			this.world = world;
			this.state = state;
		}
		
		public boolean getValue() {
			return value;
		}
		
		public String toString() {
			String s = "";
			if (state == NodeState.NORMAL) {
				if (value) {
					s += ChatColor.GREEN + " + ";
				} else {
					s += ChatColor.RED + " - ";
				}
			} else if (state == NodeState.REDEFINED) {
				s += ChatColor.GRAY;
				if (value) {
					s += " + ";
				} else {
					s += " - ";
				}
			} else if (state == NodeState.OVERRIDDEN) {
				s += ChatColor.GRAY + " x ";
			}
			s += name + " (";
			if (state == NodeState.REDEFINED) {
				s += "redefined, ";
			} else if (state == NodeState.OVERRIDDEN) {
				s += "overridden, ";
			}
			if (container == null) {
				s += "baseline";
			} else if (container == source) {
				s += "self";
			} else if (container instanceof Group) {
				s += "g:" + container.getName();
			}
			if (world != null) {
				s += ", w:" + world;
			}
			s += ")";
			return s;
		}
		
	}
	
	private enum NodeState {
		NORMAL, REDEFINED, OVERRIDDEN
	}
	
	
}
