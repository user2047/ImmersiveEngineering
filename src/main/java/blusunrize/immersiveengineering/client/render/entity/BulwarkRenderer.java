package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.entities.illager.Bulwark;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class BulwarkRenderer extends IEEntityRenderer<Bulwark>
{
	private static final Identifier TEXTURE = IEApi.ieLoc("textures/entity/illager/bulwark.png");

	public BulwarkRenderer(EntityRendererProvider.Context context)
	{
		super(context);
	}

	public Identifier getTextureLocation(Bulwark entity)
	{
		return TEXTURE;
	}
}
