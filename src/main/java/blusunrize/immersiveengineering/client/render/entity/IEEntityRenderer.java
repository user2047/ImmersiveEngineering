package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.client.utils.RenderTypeCompat;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

public class IEEntityRenderer<T extends Entity> extends EntityRenderer<T, IEEntityRenderer.RenderState<T>>
{
	public IEEntityRenderer(EntityRendererProvider.Context context)
	{
		super(context);
	}

	public RenderState<T> createRenderState()
	{
		return new RenderState<>();
	}

	public void extractRenderState(T entity, RenderState<T> state, float partialTicks)
	{
		super.extractRenderState(entity, state, partialTicks);
		state.entity = entity;
		state.partialTicks = partialTicks;
	}

	public void submit(RenderState<T> state, PoseStack poseStack, SubmitNodeCollector nodes, CameraRenderState cameraState)
	{
		if(state.entity==null)
			return;
		nodes.submitCustomGeometry(
				poseStack,
				RenderTypeCompat.solid(),
				(pose, consumer) -> render(
						state.entity, 0, state.partialTicks, poseStack, type -> consumer, state.lightCoords
				)
		);
	}

	public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int packedLight)
	{
	}

	@Nullable
	public Identifier getTextureLocation(T entity)
	{
		return null;
	}

	public static class RenderState<T extends Entity> extends EntityRenderState
	{
		private T entity;
		private float partialTicks;
	}
}
