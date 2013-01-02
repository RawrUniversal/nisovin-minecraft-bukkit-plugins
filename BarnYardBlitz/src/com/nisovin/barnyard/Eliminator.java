package com.nisovin.barnyard;

public class Eliminator implements Runnable {

	boolean stopped = false;
	
	public Eliminator() {
		Thread thread = new Thread(this);
		thread.start();
	}

	@Override
	public void run() {
		try {
			Thread.sleep(10 * 60 * 1000);
			if (stopped) return;
			eliminate();
			Thread.sleep(4 * 60 * 1000);
			if (stopped) return;
			eliminate();
			Thread.sleep(4 * 60 * 1000);
			if (stopped) return;
			eliminate();
			Thread.sleep(4 * 60 * 1000);
			if (stopped) return;
			eliminate();
			Thread.sleep(4 * 60 * 1000);
			if (stopped) return;
			eliminate();
			Thread.sleep(4 * 60 * 1000);
			if (stopped) return;
			eliminate();
			// declare winner
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void eliminate() {
	}
	
	public void stop() {
		stopped = true;
	}
	
}
