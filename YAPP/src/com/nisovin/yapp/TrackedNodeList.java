package com.nisovin.yapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class TrackedNodeList {

	private PermissionContainer source;
	private Map<String, TrackedNode> realNodes = new HashMap<String, TrackedNode>();
	private List<TrackedNode> allNodes = new ArrayList<TrackedNode>();
	
	public TrackedNodeList(PermissionContainer source) {
		this.source = source;
	}
	
	public void add(PermissionNode node, PermissionContainer container, String world) {
		TrackedNode existingNode = realNodes.get(node.getNodeName());
		NodeState state = NodeState.NORMAL;
		if (existingNode != null) {
			if (existingNode.getValue() == node.getValue()) {
				state = NodeState.REDEFINED;
			} else {
				state = NodeState.OVERRIDDEN;
			}
		}
		TrackedNode newNode = new TrackedNode(node, container, world, state);
		allNodes.add(newNode);
		if (existingNode == null) {
			realNodes.put(node.getNodeName(), newNode);
		}
	}
	
	public void add(Permission perm) {
		TrackedNode existingNode = realNodes.get(perm.getName());
		NodeState state = NodeState.NORMAL;
		if (existingNode != null) {
			if (existingNode.getValue() == true) {
				state = NodeState.REDEFINED;
			} else {
				state = NodeState.OVERRIDDEN;
			}
		}
		TrackedNode newNode = new TrackedNode(perm, null, state);
		allNodes.add(newNode);
		if (existingNode == null) {
			realNodes.put(perm.getName(), newNode);
		}
	}
	
	public void add(PermissionAttachmentInfo perm) {
		TrackedNode existingNode = realNodes.get(perm.getPermission());
		NodeState state = NodeState.NORMAL;
		if (existingNode != null) {
			if (existingNode.getValue() == true) {
				state = NodeState.REDEFINED;
			} else {
				state = NodeState.OVERRIDDEN;
			}
		}
		TrackedNode newNode = new TrackedNode(perm, null, state);
		allNodes.add(newNode);
		if (existingNode == null) {
			realNodes.put(perm.getPermission(), newNode);
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
		
		public TrackedNode(PermissionNode node, PermissionContainer container, String world, NodeState state) {
			this.name = node.getNodeName();
			this.value = node.getValue();
			this.container = container;
			this.world = world;
			this.state = state;
		}
		
		public TrackedNode(Permission perm, PermissionContainer container, NodeState state) {
			this.name = perm.getName();
			this.value = true;
			this.container = container;
			this.world = null;
			this.state = state;
		}
		
		public TrackedNode(PermissionAttachmentInfo perm, PermissionContainer container, NodeState state) {
			this.name = perm.getPermission();
			this.value = perm.getValue();
			this.container = container;
			this.world = null;
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
