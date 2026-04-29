/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks.metal;

import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.api.fluid.FluidUtils;
import blusunrize.immersiveengineering.client.utils.TextUtils;
import blusunrize.immersiveengineering.common.blocks.BlockCapabilityRegistration.BECapabilityRegistrar;
import blusunrize.immersiveengineering.common.blocks.IEBaseBlockEntity;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockEntityDrop;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IBlockOverlayText;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IComparatorOverride;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IPlayerInteraction;
import blusunrize.immersiveengineering.common.register.IEBlockEntities;
import blusunrize.immersiveengineering.common.register.IEDataComponents;
import blusunrize.immersiveengineering.common.util.FluidStackCompat;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

public class CreativeFluidTankBlockEntity extends IEBaseBlockEntity implements IBlockOverlayText,
		IPlayerInteraction, IBlockEntityDrop, IComparatorOverride
{
	private FluidStack fluid = FluidStack.EMPTY;
	private final IFluidHandler pipeHandler = new CreativeTankFluidHandler(false);
	private final IFluidHandler bucketHandler = new CreativeTankFluidHandler(true);

	public CreativeFluidTankBlockEntity(BlockPos pos, BlockState state)
	{
		super(IEBlockEntities.CREATIVE_FLUID_TANK.get(), pos, state);
	}

	public void readCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		fluid = normalizeFluid(FluidStackCompat.parseOptional(provider, nbt.getCompoundOrEmpty("fluid")));
	}

	public void writeCustomNBT(CompoundTag nbt, boolean descPacket, Provider provider)
	{
		if(!fluid.isEmpty())
			nbt.put("fluid", FluidStackCompat.saveOptional(fluid, provider));
	}

	public static void registerCapabilities(BECapabilityRegistrar<CreativeFluidTankBlockEntity> registrar)
	{
		registrar.register(Capabilities.Fluid.BLOCK, (be, side) -> be.pipeHandler);
	}

	public InteractionResult interact(Direction side, Player player, InteractionHand hand, ItemStack heldItem, float hitX, float hitY, float hitZ)
	{
		if(FluidUtils.interactWithFluidHandler(player, hand, bucketHandler))
			return InteractionResult.SUCCESS;
		return InteractionResult.PASS;
	}

	@Nullable
	public Component[] getOverlayText(@Nullable BlockState blockState, Player player, HitResult rtr, boolean hammer)
	{
		if(rtr.getType()==Type.MISS)
			return null;
		if(Utils.isFluidRelatedItemStack(player.getItemInHand(InteractionHand.MAIN_HAND)))
		{
			if(fluid.isEmpty())
				return new Component[]{TextUtils.formatFluidStack(FluidStack.EMPTY)};
			return new Component[]{Component.translatable(Lib.DESC_INFO+"creativeFluidTank", fluid.getHoverName())};
		}
		return null;
	}

	public void getBlockEntityDrop(LootContext context, Consumer<ItemStack> drop)
	{
		ItemStack stack = new ItemStack(getBlockState().getBlock(), 1);
		if(!fluid.isEmpty())
			stack.set(IEDataComponents.GENERIC_FLUID, SimpleFluidContent.copyOf(fluid));
		drop.accept(stack);
	}

	public void onBEPlaced(BlockPlaceContext ctx)
	{
		setFluid(ctx.getItemInHand().getOrDefault(IEDataComponents.GENERIC_FLUID, SimpleFluidContent.EMPTY).copy(), false);
	}

	public int getComparatorInputOverride()
	{
		return fluid.isEmpty()?0: 15;
	}

	private void setFluid(FluidStack newFluid, boolean notify)
	{
		newFluid = normalizeFluid(newFluid);
		if(FluidStack.isSameFluidSameComponents(fluid, newFluid))
			return;
		fluid = newFluid;
		setChanged();
		if(notify)
			markContainingBlockForUpdate(null);
	}

	private static FluidStack normalizeFluid(FluidStack stack)
	{
		if(stack.isEmpty())
			return FluidStack.EMPTY;
		return stack.copyWithAmount(FluidType.BUCKET_VOLUME);
	}

	private class CreativeTankFluidHandler implements IFluidHandler
	{
		private final boolean allowSettingFluid;

		private CreativeTankFluidHandler(boolean allowSettingFluid)
		{
			this.allowSettingFluid = allowSettingFluid;
		}

		public int getTanks()
		{
			return 1;
		}

		@Nonnull
		public FluidStack getFluidInTank(int tank)
		{
			if(tank!=0||fluid.isEmpty())
				return FluidStack.EMPTY;
			return fluid.copyWithAmount(Integer.MAX_VALUE);
		}

		public int getTankCapacity(int tank)
		{
			return tank==0?Integer.MAX_VALUE: 0;
		}

		public boolean isFluidValid(int tank, @Nonnull FluidStack stack)
		{
			return tank==0&&!stack.isEmpty();
		}

		public int fill(FluidStack resource, FluidAction action)
		{
			if(!allowSettingFluid||resource.isEmpty())
				return 0;
			if(action.execute())
				setFluid(resource, true);
			return resource.getAmount();
		}

		@Nonnull
		public FluidStack drain(FluidStack resource, FluidAction action)
		{
			if(resource.isEmpty()||fluid.isEmpty()||!FluidStack.isSameFluidSameComponents(resource, fluid))
				return FluidStack.EMPTY;
			return fluid.copyWithAmount(resource.getAmount());
		}

		@Nonnull
		public FluidStack drain(int maxDrain, FluidAction action)
		{
			if(maxDrain <= 0||fluid.isEmpty())
				return FluidStack.EMPTY;
			return fluid.copyWithAmount(maxDrain);
		}
	}
}
