package blusunrize.immersiveengineering.common.util;

import blusunrize.immersiveengineering.common.util.inventory.SlotwiseItemHandler;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.neoforge.items.ItemStackHandler;

public class ItemHandlerCompat
{
	public static Tag serializeNBT(Object handler, Provider provider)
	{
		if(handler instanceof SlotwiseItemHandler slotwise)
			return slotwise.serializeNBT(provider);
		if(handler instanceof ItemStackHandler itemStackHandler)
		{
			TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, provider);
			itemStackHandler.serialize(output);
			return output.buildResult();
		}
		return new CompoundTag();
	}

	public static void deserializeNBT(Object handler, Provider provider, CompoundTag nbt)
	{
		if(handler instanceof SlotwiseItemHandler slotwise)
			slotwise.deserializeNBT(provider, nbt);
		else if(handler instanceof ItemStackHandler itemStackHandler)
			itemStackHandler.deserialize(TagValueInput.create(ProblemReporter.DISCARDING, provider, nbt));
	}
}
