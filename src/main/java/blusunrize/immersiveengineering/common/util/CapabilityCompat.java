package blusunrize.immersiveengineering.common.util;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.ItemCapability;

public class CapabilityCompat
{
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static <T> T getItemCapability(ItemStack stack, ItemCapability capability)
	{
		return (T)stack.getCapability(capability, null);
	}
}
