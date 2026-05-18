/*
 * BluSunrize
 * Copyright (c) 2022
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.util.commands;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.ChopAnimationTuning;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Lib.MODID, value = Dist.CLIENT)
public class ClientCommands
{
	@SubscribeEvent
	public static void registerClientCommands(RegisterClientCommandsEvent ev)
	{
		LiteralArgumentBuilder<CommandSourceStack> main = Commands.literal("cie");
		main.then(createResetRender())
				.then(createResetManual())
				.then(createChopAnim());
		ev.getDispatcher().register(main);
	}

	public static LiteralArgumentBuilder<CommandSourceStack> createResetRender()
	{
		LiteralArgumentBuilder<CommandSourceStack> ret = Commands.literal("resetrender");
		ret.executes(context -> {
			ImmersiveEngineering.proxy.clearRenderCaches();
			return Command.SINGLE_SUCCESS;
		});
		return ret;
	}

	public static LiteralArgumentBuilder<CommandSourceStack> createResetManual()
	{
		LiteralArgumentBuilder<CommandSourceStack> ret = Commands.literal("resetmanual");
		ret.executes(context -> {
			ImmersiveEngineering.proxy.resetManual();
			return Command.SINGLE_SUCCESS;
		});
		return ret;
	}

	public static LiteralArgumentBuilder<CommandSourceStack> createChopAnim()
	{
		LiteralArgumentBuilder<CommandSourceStack> ret = Commands.literal("chopanim");
		ret.then(Commands.literal("get").executes(context -> {
			StringBuilder values = new StringBuilder("Chop animation values:");
			for(ChopAnimationTuning.Value value : ChopAnimationTuning.values())
				values.append("\n").append(value.name()).append(" = ").append(value.get());
			context.getSource().sendSuccess(() -> Component.literal(values.toString()), false);
			return Command.SINGLE_SUCCESS;
		}));
		ret.then(Commands.literal("path").executes(context -> {
			context.getSource().sendSuccess(
					() -> Component.literal(ChopAnimationTuning.getConfigPath().toAbsolutePath().toString()),
					false
			);
			return Command.SINGLE_SUCCESS;
		}));
		ret.then(Commands.literal("reload").executes(context -> {
			sendChopAnimMessage(context.getSource(), ChopAnimationTuning.reloadFromFile());
			return Command.SINGLE_SUCCESS;
		}));
		ret.then(Commands.literal("save").executes(context -> {
			sendChopAnimMessage(context.getSource(), ChopAnimationTuning.saveToFile());
			return Command.SINGLE_SUCCESS;
		}));
		ret.then(Commands.literal("reset").executes(context -> {
			ChopAnimationTuning.reset();
			sendChopAnimMessage(context.getSource(), "Reset chop animation values. "+ChopAnimationTuning.saveToFile());
			return Command.SINGLE_SUCCESS;
		}));
		for(ChopAnimationTuning.Value value : ChopAnimationTuning.values())
			ret.then(createChopAnimValue(value));
		return ret;
	}

	private static LiteralArgumentBuilder<CommandSourceStack> createChopAnimValue(ChopAnimationTuning.Value value)
	{
		return Commands.literal(value.name())
				.executes(context -> {
					sendChopAnimValue(context.getSource(), value);
					return Command.SINGLE_SUCCESS;
				})
				.then(Commands.argument("value", FloatArgumentType.floatArg())
						.executes(context -> {
							value.set(FloatArgumentType.getFloat(context, "value"));
							sendChopAnimValue(context.getSource(), value, ChopAnimationTuning.saveToFile());
							return Command.SINGLE_SUCCESS;
						}));
	}

	private static void sendChopAnimValue(CommandSourceStack source, ChopAnimationTuning.Value value)
	{
		sendChopAnimValue(source, value, null);
	}

	private static void sendChopAnimValue(CommandSourceStack source, ChopAnimationTuning.Value value, String extra)
	{
		source.sendSuccess(
				() -> Component.literal(
						value.name()+" = "+value.get()+" (default "+value.defaultValue()+")"
								+(extra!=null?"\n"+extra: "")
				),
				false
		);
	}

	private static void sendChopAnimMessage(CommandSourceStack source, String message)
	{
		source.sendSuccess(() -> Component.literal(message), false);
	}
}
