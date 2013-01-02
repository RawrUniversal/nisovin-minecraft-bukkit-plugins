package com.nisovin.barnyard;

import java.util.LinkedList;
import java.util.Queue;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;

public class BlockUpdateQueue {

	BarnYardBlitz plugin;
	Queue<BlockUpdate> queue = new LinkedList<BlockUpdate>();
	int taskId = -1;
	
	public BlockUpdateQueue(BarnYardBlitz plugin) {
		this.plugin = plugin;
	}
	
	public void add(Block block, int id, byte data) {
		queue.add(new BlockUpdate(block, id, data));
		startTask();
	}
	
	public int size() {
		return queue.size();
	}
	
	void startTask() {
		if (taskId < 0) {
			taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new UpdateTask(), 1, 1);
		}
	}
	
	void stopTask() {
		if (taskId > 0) {
			Bukkit.getScheduler().cancelTask(taskId);
			taskId = -1;
		}
	}
	
	class BlockUpdate {
		Block block;
		int type;
		byte data;
		public BlockUpdate(Block block, int type, byte data) {
			this.block = block;
			this.type = type;
			this.data = data;
		}
	}
	
	class UpdateTask implements Runnable {
		
		public void run() {
			int count = 0;
			int stop = plugin.blockUpdatesPerTick;
			if (queue.size() > plugin.blockUpdatesPerTick * plugin.captureInterval) {
				stop *= 5;
			}
			while (count < stop && queue.size() > 0) {
				BlockUpdate update = queue.poll();
				if (update != null) {
					update.block.setTypeIdAndData(update.type, update.data, false);
				}
				count++;
			}
			if (queue.size() == 0) {
				stopTask();
			}
		}
		
	}
	
}
