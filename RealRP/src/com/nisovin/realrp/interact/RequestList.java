package com.nisovin.realrp.interact;

import java.util.HashMap;
import java.util.TreeSet;

import org.bukkit.entity.Player;

import com.nisovin.realrp.RealRP;

public class RequestList {

	private int nextId = 1;
	private int maxId = RealRP.settings().irMaxRequestId;
	private HashMap<Integer, InteractRequest> requests = new HashMap<Integer, InteractRequest>();
	private TreeSet<InteractRequest> sortedRequests = new TreeSet<InteractRequest>();
	private InteractRequest[] requestArray = null;	
	
	public RequestList() {
		
	}
	
	public void add(InteractRequest request) {
		request.setId(nextId);
		requests.put(nextId, request);
		sortedRequests.add(request);
		requestArray = null;
		nextId++;
		if (nextId > maxId) {
			nextId = 1;
		}
	}
	
	public InteractRequest get(int id) {
		return requests.get(id);
	}
	
	public InteractRequest remove(int id) {
		InteractRequest request = requests.get(id);
		if (request != null) {
			requests.remove(id);
			sortedRequests.remove(request);
			requestArray = null;
			return request;
		} else {
			return null;
		}
	}
	
	public InteractRequest remove(InteractRequest request) {
		return remove(request.getId());
	}
	
	public void sendList(Player player, int page) {
		if (requestArray == null) {
			requestArray = new InteractRequest[sortedRequests.size()];
			requestArray = sortedRequests.toArray(requestArray);
		}
		int pageSize = RealRP.settings().irListPageSize;
		int pages = sortedRequests.size() / pageSize + 1;
		page = page % pages;
		
		for (int i = page*pageSize; i < page*pageSize+pageSize && i < sortedRequests.size(); i++) {
			RealRP.sendMessage(player, requestArray[i].getRequestLine());
		}
	}
}
