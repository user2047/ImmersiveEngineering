package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.entities.illager.Fusilier;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class FusilierRenderer extends IEEntityRenderer<Fusilier>
{
	private static final Identifier TEXTURE = IEApi.ieLoc("textures/entity/illager/fusilier.png");

	public FusilierRenderer(EntityRendererProvider.Context context)
	{
		super(context);
	}

	public Identifier getTextureLocation(Fusilier entity)
	{
		return TEXTURE;
	}
}
