/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.client.utils.GuiHelper;
import blusunrize.immersiveengineering.client.utils.RenderTypeCompat;
import blusunrize.immersiveengineering.common.blocks.metal.CreativeFluidTankBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.fluids.FluidStack;
import org.joml.Quaternionf;

public class CreativeFluidTankRenderer extends IEBlockEntityRenderer<CreativeFluidTankBlockEntity>
{
	private static final float MIN = 2;
	private static final float MAX = 14;
	private static final float SIZE = MAX-MIN;

	public void submit(
			RenderState<CreativeFluidTankBlockEntity> state, PoseStack poseStack,
			SubmitNodeCollector nodes, CameraRenderState cameraState
	)
	{
		if(state.blockEntity==null||state.blockEntity.getStoredFluid().isEmpty())
			return;
		nodes.submitCustomGeometry(
				poseStack,
				RenderTypeCompat.translucent(),
				(pose, consumer) -> {
					PoseStack renderPose = new PoseStack();
					renderPose.last().set(pose);
					render(
							state.blockEntity, state.partialTicks, renderPose, type -> consumer,
							state.lightCoords, OverlayTexture.NO_OVERLAY
					);
				}
		);
	}

	public void render(
			CreativeFluidTankBlockEntity blockEntity,
			float partialTicks,
			PoseStack matrixStack,
			MultiBufferSource bufferIn,
			int combinedLightIn,
			int combinedOverlayIn
	)
	{
		FluidStack fluid = blockEntity.getStoredFluid();
		if(fluid.isEmpty())
			return;

		VertexConsumer builder = bufferIn.getBuffer(RenderTypeCompat.translucent());
		matrixStack.pushPose();
		matrixStack.scale(1/16f, 1/16f, 1/16f);

		drawFace(builder, matrixStack, fluid, MIN, MIN, MAX+.01f, 0, 0);
		drawFace(builder, matrixStack, fluid, MAX, MIN, MIN-.01f, 0, Mth.PI);
		drawFace(builder, matrixStack, fluid, MAX+.01f, MIN, MIN, 0, -Mth.HALF_PI);
		drawFace(builder, matrixStack, fluid, MIN-.01f, MIN, MAX, 0, Mth.HALF_PI);
		drawFace(builder, matrixStack, fluid, MIN, MAX+.01f, MIN, Mth.HALF_PI, 0);
		drawFace(builder, matrixStack, fluid, MIN, MIN-.01f, MAX, -Mth.HALF_PI, 0);

		matrixStack.popPose();
	}

	private static void drawFace(
			VertexConsumer builder, PoseStack matrixStack, FluidStack fluid,
			float x, float y, float z, float xRot, float yRot
	)
	{
		matrixStack.pushPose();
		matrixStack.translate(x, y, z);
		if(yRot!=0)
			matrixStack.mulPose(new Quaternionf().rotateY(yRot));
		if(xRot!=0)
			matrixStack.mulPose(new Quaternionf().rotateX(xRot));
		GuiHelper.drawRepeatedFluidSprite(builder, matrixStack, fluid, 0, 0, SIZE, SIZE);
		matrixStack.popPose();
	}
}
