import net.minecraft.server.Container;
import net.minecraft.server.ContainerPlayer;
import net.minecraft.server.CraftingManager;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.InventoryCrafting;
import net.minecraft.server.ItemStack;

public class ControlledCraftingManager extends CraftingManager {
	
	protected ControlledCraftingManager() {
		super();
		
		Field field = CraftingManager.getClass().getDeclaredField("a");
		field.setAccissible(true);
		field.set(null, this);
	}
	
	@Override
	public ItemStack a(InventoryCrafting inventorycrafting) {
		ItemStack i = super.a(inventorycrafting);
		if (i==null) {
			return null;
		}		
		
		// get container object
		Field field = InventoryCrafting.getClass().getDeclaredField("c");
		field.setAccessible(true);
		Object oContainer = field.get(inventorycrafting);
		
		// get player name
		String name = null;
		if (oContainer instanceof ContainerPlayer) {
			Field field2 = Container.getClass().getDeclaredField("g");
			field2.setAccessible(true);
			Object oList = field.get(oContainer);
			if (((ArrayList)oList).size() < 1) {
				return null;
			}
			EntityPlayer player = (EntityPlayer)((ArrayList)oList).get(0);
			name = player.name;
		}
		
		if (name == null) {
			return i;
		} else {
			System.out.println("Player " + name + " is crafting " + i.id);
		}
	}
	
}