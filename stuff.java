

public void runTests(Player player) {

	long startTime, totalTime;
	
	for (int i = 0; i < 10; i++) {
		startTime = System.currentTimeMillis();
		for (int j = 0; j < 100; j++) {
			method1(player);
		}
		totalTime = System.currentTimeMillis() - startTime;
		System.out.println("method 1, interation " + i + ": " + totalTime + " ms");
	}
	
	for (int i = 0; i < 10; i++) {
		startTime = System.currentTimeMillis();
		for (int j = 0; j < 100; j++) {
			method2(player);
		}
		totalTime = System.currentTimeMillis() - startTime;
		System.out.println("method 2, interation " + i + ": " + totalTime + " ms");
	}

}

public String method1(Player player) {
	Location ploc = player.getLocation();
	Location bloc = ploc.toVector().add(ploc.getDirection().normalize().setY(0)).toLocation(ploc.getWorld());
	BlockFace face = ploc.getWorld().getBlockAt(ploc).getFace(ploc.getWorld().getBlockAt(bloc));
	return face.toString();
}

public String method2(Player player) {
    double rot = (player.getLocation().getYaw() - 90) % 360;
	if (rot < 0) {
		rot += 360.0;
	}
	return getDirection(rot);
}

private static String getDirection(double rot) {
	if (0 <= rot && rot < 22.5) {
		return "North";
	} else if (22.5 <= rot && rot < 67.5) {
		return "Northeast";
	} else if (67.5 <= rot && rot < 112.5) {
		return "East";
	} else if (112.5 <= rot && rot < 157.5) {
		return "Southeast";
	} else if (157.5 <= rot && rot < 202.5) {
		return "South";
	} else if (202.5 <= rot && rot < 247.5) {
		return "Southwest";
	} else if (247.5 <= rot && rot < 292.5) {
		return "West";
	} else if (292.5 <= rot && rot < 337.5) {
		return "Northwest";
	} else if (337.5 <= rot && rot < 360.0) {
		return "North";
	} else {
		return null;
	}
}