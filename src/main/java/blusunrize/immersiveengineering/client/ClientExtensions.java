/*
 * BluSunrize
 * Copyright (c) 2024
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.client.ieobj.ItemCallback;
import blusunrize.immersiveengineering.common.register.IEFluids;
import blusunrize.immersiveengineering.common.register.IEItems.Misc;
import blusunrize.immersiveengineering.common.register.IEItems.Tools;
import blusunrize.immersiveengineering.common.register.IEItems.Weapons;
import blusunrize.immersiveengineering.common.register.IEPotions;
import blusunrize.immersiveengineering.common.register.IEPotions.IEPotion;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientMobEffectExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.FluidStack;

import static blusunrize.immersiveengineering.common.fluids.PotionFluid.PotionFluidType.TEXTURE_FLOW;
import static blusunrize.immersiveengineering.common.fluids.PotionFluid.PotionFluidType.TEXTURE_STILL;

@EventBusSubscriber(value = Dist.CLIENT, modid = ImmersiveEngineering.MODID)
public class ClientExtensions
{
	@SubscribeEvent
	public static void registerClientExtensions(RegisterClientExtensionsEvent ev)
	{
		ev.registerItem(
				ItemCallback.USE_IEOBJ_RENDER,
				Tools.BUZZSAW.asItem(),
				Tools.DRILL.asItem(),
				Weapons.CHEMTHROWER.asItem(),
				Weapons.RAILGUN.asItem(),
				Weapons.REVOLVER.asItem(),
				Misc.FLUORESCENT_TUBE.asItem(),
				Misc.SHIELD.asItem()
		);
		for(var potion : IEPotions.REGISTER.getEntries())
		{
			var iePotion = (IEPotion)potion.get();
			ev.registerMobEffect(
					new IClientMobEffectExtensions()
					{
						public boolean isVisibleInGui(MobEffectInstance instance)
						{
							return iePotion.showInHud;
						}

						public boolean isVisibleInInventory(MobEffectInstance instance)
						{
							return iePotion.showInInventory;
						}
					},
					iePotion
			);
		}
		ev.registerFluidType(
				new IClientFluidTypeExtensions()
				{
					public Identifier getStillTexture()
					{
						return TEXTURE_STILL;
					}

					public Identifier getFlowingTexture()
					{
						return TEXTURE_FLOW;
					}

					public int getTintColor(FluidStack stack)
					{
						var potionData = stack.get(DataComponents.POTION_CONTENTS);
						if(potionData==null)
							return 0xff0000ff;
						return 0xff000000|potionData.getColor();
					}
				},
				IEFluids.POTION_TYPE.value()
		);
		for(var fluid : IEFluids.ALL_ENTRIES)
			ev.registerFluidType(
					new IClientFluidTypeExtensions()
					{
						public Identifier getStillTexture()
						{
							return fluid.stillTexture();
						}

						public Identifier getFlowingTexture()
						{
							return fluid.flowingTexture();
						}
					},
					fluid.type().value()
			);
	}
}
