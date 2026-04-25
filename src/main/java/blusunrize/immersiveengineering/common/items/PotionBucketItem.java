/*
 * BluSunrize
 * Copyright (c) 2023
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.items;

import blusunrize.immersiveengineering.common.fluids.PotionFluid;
import blusunrize.immersiveengineering.common.items.ItemCapabilityRegistration.ItemCapabilityRegistrar;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import net.minecraft.core.Holder;
import net.minecraft.core.Holder.Reference;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab.Output;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static net.minecraft.core.component.DataComponents.POTION_CONTENTS;

public class PotionBucketItem extends IEBaseItem
{
	public PotionBucketItem()
	{
		super(itemProperties().stacksTo(1).component(POTION_CONTENTS, PotionContents.EMPTY));
	}

	public static ItemStack forPotion(Holder<Potion> type)
	{
		if(type==Potions.WATER||type==null)
			return new ItemStack(Items.WATER_BUCKET);
		ItemStack result = new ItemStack(Misc.POTION_BUCKET);
		result.set(POTION_CONTENTS, new PotionContents(type));
		return result;
	}

	public void fillCreativeTab(Output out)
	{
	}

	public static void registerCapabilities(ItemCapabilityRegistrar registrar)
	{
		registrar.register(Capabilities.Fluid.ITEM, (stack, $) -> new FluidHandler(stack));
	}

	@Nonnull
	public Component getName(@Nonnull ItemStack stack)
	{
		return Component.translatable(
				"item.immersiveengineering.potion_bucket", getPotionName(stack.get(POTION_CONTENTS).potion())
		);
	}

	private static Component getPotionName(Optional<Holder<Potion>> potion)
	{
		return potion.map(value -> new PotionContents(value).getName(Items.POTION.getDescriptionId()+".effect."))
				.orElse(Component.empty());
	}

	@Nonnull
	public InteractionResult use(
			@Nonnull Level worldIn, @Nonnull Player playerIn, @Nonnull InteractionHand handIn
	)
	{
		ItemStack stack = playerIn.getItemInHand(handIn);
		return InteractionResult.PASS;
	}

	public void appendHoverText(
			@Nonnull ItemStack stack, TooltipContext ctx, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn
	)
	{
		PotionContents contents = stack.getOrDefault(POTION_CONTENTS, PotionContents.EMPTY);
		PotionContents.addPotionTooltip(contents.getAllEffects(), tooltip::add, 1.0F, ctx.tickRate());
	}

	private static class FluidHandler implements IFluidHandlerItem
	{
		private final ItemStack stack;
		private boolean empty = false;

		private FluidHandler(ItemStack stack)
		{
			this.stack = stack;
		}

		private FluidStack getFluid()
		{
			if(empty)
				return FluidStack.EMPTY;
			else
				return PotionFluid.getFluidStackForType(stack.get(POTION_CONTENTS).potion(), FluidType.BUCKET_VOLUME, PotionFluid.PotionBottleType.REGULAR);
		}

		@Nonnull
		public ItemStack getContainer()
		{
			return empty?new ItemStack(Items.BUCKET): stack;
		}

		public int getTanks()
		{
			return 1;
		}

		@Nonnull
		public FluidStack getFluidInTank(int tank)
		{
			if(tank==0)
				return getFluid();
			else
				return FluidStack.EMPTY;
		}

		public int getTankCapacity(int tank)
		{
			return tank==0?FluidType.BUCKET_VOLUME: 0;
		}

		public boolean isFluidValid(int tank, @Nonnull FluidStack stack)
		{
			return false;
		}

		public int fill(FluidStack resource, FluidAction action)
		{
			return 0;
		}

		@Nonnull
		public FluidStack drain(FluidStack resource, FluidAction action)
		{
			FluidStack fluid = getFluid();
			if(!FluidStack.isSameFluidSameComponents(fluid, resource))
				return FluidStack.EMPTY;
			return drain(resource.getAmount(), action);
		}

		@Nonnull
		public FluidStack drain(int maxDrain, FluidAction action)
		{
			if(empty||stack.getCount() > 1||maxDrain < FluidType.BUCKET_VOLUME)
				return FluidStack.EMPTY;

			FluidStack potion = getFluid();
			if(action.execute())
				empty = true;
			return potion;
		}
	}
}
