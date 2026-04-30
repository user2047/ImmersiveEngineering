/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.gui;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.common.blocks.metal.CapacitorCreativeBlockEntity;
import blusunrize.immersiveengineering.common.gui.sync.GenericContainerData;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;

import javax.annotation.Nullable;

public class CreativeCapacitorMenu extends IEContainerMenu
{
	private final int[] sideConfig = new int[DirectionUtils.VALUES.length];
	@Nullable
	private final CapacitorCreativeBlockEntity capacitor;

	public static CreativeCapacitorMenu makeServer(
			MenuType<?> type, int id, Inventory invPlayer, CapacitorCreativeBlockEntity be
	)
	{
		return new CreativeCapacitorMenu(blockCtx(type, id, be), invPlayer, be);
	}

	public static CreativeCapacitorMenu makeClient(MenuType<?> type, int id, Inventory invPlayer)
	{
		return new CreativeCapacitorMenu(clientCtx(type, id), invPlayer, null);
	}

	private CreativeCapacitorMenu(
			MenuContext ctx, Inventory inventoryPlayer, @Nullable CapacitorCreativeBlockEntity capacitor
	)
	{
		super(ctx);
		this.capacitor = capacitor;
		ownSlotCount = 0;

		for(Direction side : DirectionUtils.VALUES)
			sideConfig[side.ordinal()] = IOSideConfig.OUTPUT.ordinal();

		for(int i = 0; i < 3; i++)
			for(int j = 0; j < 9; j++)
				addSlot(new Slot(inventoryPlayer, j+i*9+9, 8+j*18, 104+i*18));
		for(int i = 0; i < 9; i++)
			addSlot(new Slot(inventoryPlayer, i, 8+i*18, 162));

		for(Direction side : DirectionUtils.VALUES)
			addGenericData(GenericContainerData.int32(
					() -> this.capacitor!=null?this.capacitor.getSideConfig(side).ordinal(): this.sideConfig[side.ordinal()],
					value -> this.sideConfig[side.ordinal()] = value
			));
	}

	public IOSideConfig getSideConfig(Direction side)
	{
		int ordinal = sideConfig[side.ordinal()];
		if(ordinal < 0||ordinal >= IOSideConfig.VALUES.length)
			return IOSideConfig.NONE;
		return IOSideConfig.VALUES[ordinal];
	}

	public void receiveMessageFromScreen(CompoundTag nbt)
	{
		if(capacitor==null||!nbt.contains("side")||!nbt.contains("sideConfig"))
			return;
		int sideId = nbt.getIntOr("side", 0);
		int configId = nbt.getIntOr("sideConfig", IOSideConfig.OUTPUT.ordinal());
		if(sideId < 0||sideId >= DirectionUtils.VALUES.length||configId < 0||configId >= IOSideConfig.VALUES.length)
			return;

		Direction side = DirectionUtils.VALUES[sideId];
		IOSideConfig config = IOSideConfig.VALUES[configId];
		capacitor.setSideConfig(side, config);
		sideConfig[side.ordinal()] = config.ordinal();
	}
}
