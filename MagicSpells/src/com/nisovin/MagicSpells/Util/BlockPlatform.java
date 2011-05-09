public class BlockPlatform {

	private Material platformType;
	private Material replaceType;
	private Block center;
	private int size;
	private boolean moving;
	private String type;
	private Set<Block> blocks;
		
	public BlockPlatform(Material platformType, Material replaceType, Block center, int size, boolean moving, String type) {
		this.platformType = platformType;
		this.replaceType = replaceType;
		this.center = center;
		this.size = size;
		this.moving = moving;
		this.type = type;
		
		if (moving) {
			blocks = new HashSet<Block>();
		}
		
		createPlatform();
	}
	
	public void createPlatform() {
		Set<Block> platform = new HashSet<Block>();
		Block block;
		
		// get platform blocks
		if (type.equals("square")) {
			for (int x = center.getX()-size; x <= center.getX()+size; x++) {
				for (int z = center.getZ()-size, z <= center.getZ()+size; z++) {
					int y = center.getY();
					block = center.getWorld().getBlockAt(x,y,z);
					if (block.getType() == replaceType || blocks.contains(block)) {
						// only add if it's a replaceable block or if it is already part of the platform
						platform.add(block);
					}
				}
			}
		}
		
		// remove old platform blocks
		if (moving) {
			for (block : blocks) {
				if (!platform.contains(block) && block.getType() == platformType) {
					block.setType(replaceType);
				}
			}
		}
		
		// add new platform blocks
		for (block : platform) {
			if (!blocks.contains(block)) {
				block.setType(platformType);
			}
		}
		
		// update platform block set
		if (moving) {
			blocks = platform;
		}
	}
	
	public void movePlatform(Block center) {
		this.center = center;
		createPlatform();
	}
	

}