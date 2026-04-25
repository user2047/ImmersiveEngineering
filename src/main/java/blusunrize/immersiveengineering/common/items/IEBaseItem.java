/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.common.register.IEItems;
import blusunrize.immersiveengineering.common.register.IEMenuTypes;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.ItemContainerType;
import blusunrize.immersiveengineering.common.register.IEMenuTypes.ItemContainerTypeNew;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class IEBaseItem extends Item
{
	private int burnTime = 0;
	private boolean isHidden = false;

	public IEBaseItem()
	{
		this(itemProperties());
	}

	public IEBaseItem(Properties props)
	{
		super(props);
	}

	protected static Properties itemProperties()
	{
		return IEItems.defaultProperties();
	}

	public IEBaseItem setBurnTime(int burnTime)
	{
		this.burnTime = burnTime;
		return this;
	}

	public int getBurnTime(ItemStack itemStack, RecipeType<?> type)
	{
		return burnTime;
	}

	public boolean isHidden()
	{
		return isHidden;
	}

	protected void openGui(Player player, InteractionHand hand)
	{
		openGui(player, hand==InteractionHand.MAIN_HAND?EquipmentSlot.MAINHAND: EquipmentSlot.OFFHAND);
	}

	public void fillCreativeTab(Output out)
	{
		out.accept(this);
	}

	protected void openGui(Player player, EquipmentSlot slot)
	{
		ItemStack stack = player.getItemBySlot(slot);
		ItemContainerTypeNew<?> typeNew = getContainerTypeNew();
		if(typeNew!=null)
			player.openMenu(
					new SimpleMenuProvider((id, inv, p) -> typeNew.create(id, inv, slot, stack), Component.empty())
			);
		else
		{
			ItemContainerType<?> typeOld = getContainerType();
			if(typeOld!=null)
				player.openMenu(
						new SimpleMenuProvider(
								(id, inv, p) -> typeOld.create(id, inv, player.level(), slot, stack),
								Component.empty()
						),
						buffer -> buffer.writeInt(slot.ordinal())
				);
		}
	}

	public boolean isRepairable(@Nonnull ItemStack stack)
	{
		return false;
	}

	public boolean isIERepairable(@Nonnull ItemStack stack)
	{
		return false;
	}

	public boolean isBookEnchantable(ItemStack stack, ItemStack book)
	{
		return false;
	}

	public String getDescriptionId(ItemStack stack)
	{
		return getDescriptionId();
	}

	public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot)
	{
		super.inventoryTick(stack, level, entity, slot);
		inventoryTick(stack, level, entity, slot.ordinal(), false);
	}

	public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected)
	{
	}

	public void onCraftedBy(ItemStack stack, Player player)
	{
		super.onCraftedBy(stack, player);
		onCraftedBy(stack, player.level(), player);
	}

	public void onCraftedBy(ItemStack stack, Level level, Player player)
	{
	}

	public void appendHoverText(
			ItemStack stack, TooltipContext ctx, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag
	)
	{
		super.appendHoverText(stack, ctx, display, tooltip, flag);
		List<Component> legacyTooltip = new ArrayList<>();
		appendHoverText(stack, ctx, legacyTooltip, flag);
		legacyTooltip.forEach(tooltip);
	}

	public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag)
	{
	}

	@Nullable
	protected IEMenuTypes.ItemContainerType<?> getContainerType()
	{
		return null;
	}

	@Nullable
	protected IEMenuTypes.ItemContainerTypeNew<?> getContainerTypeNew()
	{
		return null;
	}

	public int getBarColor(ItemStack pStack)
	{
		// All our items use the vanilla color gradient, even if they use different getBarWidth implementation
		return Mth.hsvToRgb(Math.max(0.0F, getBarWidth(pStack)/(float)MAX_BAR_WIDTH)/3.0F, 1.0F, 1.0F);
	}
}
