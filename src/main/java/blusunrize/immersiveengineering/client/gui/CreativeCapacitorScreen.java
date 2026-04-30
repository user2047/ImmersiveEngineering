/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.gui;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.Lib;
import blusunrize.immersiveengineering.client.gui.elements.ITooltipWidget;
import blusunrize.immersiveengineering.common.gui.CreativeCapacitorMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class CreativeCapacitorScreen extends IEContainerScreen<CreativeCapacitorMenu>
{
	private static final Identifier BACKGROUND = makeTextureLocation("creative_capacitor");

	public CreativeCapacitorScreen(CreativeCapacitorMenu container, Inventory inventoryPlayer, Component title)
	{
		super(container, inventoryPlayer, title, BACKGROUND, 176, 186);
	}

	protected void init()
	{
		super.init();
		addSideButton(Direction.UP, 75, 20);
		addSideButton(Direction.NORTH, 75, 42);
		addSideButton(Direction.WEST, 47, 64);
		addSideButton(Direction.SOUTH, 75, 64);
		addSideButton(Direction.EAST, 103, 64);
		addSideButton(Direction.DOWN, 75, 86);
	}

	private void addSideButton(Direction side, int x, int y)
	{
		this.addRenderableWidget(new SideConfigButton(
				leftPos+x, topPos+y, side,
				() -> menu.getSideConfig(side),
				next -> {
					CompoundTag message = new CompoundTag();
					message.putInt("side", side.ordinal());
					message.putInt("sideConfig", next.ordinal());
					sendUpdateToServer(message);
				}
		));
	}

	protected void drawBackgroundTexture(GuiGraphicsExtractor graphics)
	{
		graphics.fill(leftPos, topPos, leftPos+imageWidth, topPos+imageHeight, 0xff1d2326);
		graphics.fill(leftPos+1, topPos+1, leftPos+imageWidth-1, topPos+imageHeight-1, 0xff2b3235);
		graphics.fill(leftPos+7, topPos+16, leftPos+169, topPos+98, 0xff22282b);
		graphics.fill(leftPos+7, topPos+103, leftPos+169, topPos+180, 0xff22282b);
		graphics.fill(leftPos, topPos, leftPos+imageWidth, topPos+1, Lib.COLOUR_I_ImmersiveOrange);
	}

	protected void renderLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY)
	{
		super.renderLabels(graphics, mouseX, mouseY);
		graphics.text(this.font, Component.literal("Output: Infinite IF/t"), 8, 22, 0xe0e0e0, false);
		graphics.text(this.font, Component.literal("Sides"), 8, 36, 0xe0e0e0, false);
	}

	private static class SideConfigButton extends Button implements ITooltipWidget
	{
		private final Direction side;
		private final Supplier<IOSideConfig> config;
		private final Consumer<IOSideConfig> handler;

		private SideConfigButton(
				int x, int y, Direction side, Supplier<IOSideConfig> config, Consumer<IOSideConfig> handler
		)
		{
			super(x, y, 26, 18, Component.empty(), button -> {
			}, DEFAULT_NARRATION);
			this.side = side;
			this.config = config;
			this.handler = handler;
		}

		public void onPress(InputWithModifiers input)
		{
			handler.accept(IOSideConfig.next(config.get()));
		}

		public void renderWidget(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
		{
			this.isHovered = mouseX >= getX()&&mouseY >= getY()&&mouseX < getX()+width&&mouseY < getY()+height;
			IOSideConfig state = config.get();
			int colour = switch(state)
			{
				case INPUT -> 0xff2f6fa3;
				case OUTPUT -> 0xffc07828;
				case NONE -> 0xff53585c;
			};
			if(this.isHovered)
				colour = brighten(colour);
			graphics.fill(getX(), getY(), getX()+width, getY()+height, 0xff111719);
			graphics.fill(getX()+1, getY()+1, getX()+width-1, getY()+height-1, colour);
			Font font = Minecraft.getInstance().font;
			graphics.centeredText(font, shortSideName(side), getX()+width/2, getY()+2, 0xffffffff);
			graphics.centeredText(font, getConfigLabel(state), getX()+width/2, getY()+10, 0xffffffff);
		}

		protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks)
		{
			renderWidget(graphics, mouseX, mouseY, partialTicks);
		}

		public void gatherTooltip(int mouseX, int mouseY, List<Component> tooltip)
		{
			tooltip.add(Component.translatable(Lib.DESC_INFO+"blockSide."+side.getSerializedName()));
			tooltip.add(Component.empty().append(config.get().getTextComponent()).withStyle(ChatFormatting.GRAY));
		}

		private static String getConfigLabel(IOSideConfig config)
		{
			return switch(config)
			{
				case INPUT -> "IN";
				case OUTPUT -> "OUT";
				case NONE -> "OFF";
			};
		}

		private static int brighten(int colour)
		{
			int r = Math.min(255, ((colour>>16)&255)+28);
			int g = Math.min(255, ((colour>>8)&255)+28);
			int b = Math.min(255, (colour&255)+28);
			return 0xff000000|(r<<16)|(g<<8)|b;
		}

		private static String shortSideName(Direction side)
		{
			return switch(side)
			{
				case DOWN -> "DN";
				case UP -> "UP";
				case NORTH -> "N";
				case SOUTH -> "S";
				case WEST -> "W";
				case EAST -> "E";
			};
		}
	}
}
