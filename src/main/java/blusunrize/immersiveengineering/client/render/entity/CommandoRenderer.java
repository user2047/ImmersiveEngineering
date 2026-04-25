package blusunrize.immersiveengineering.client.render.entity;

import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.common.entities.illager.Commando;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;

public class CommandoRenderer extends IEEntityRenderer<Commando>
{
	private static final Identifier TEXTURE = IEApi.ieLoc("textures/entity/illager/commando.png");

	public CommandoRenderer(EntityRendererProvider.Context context)
	{
		super(context);
	}

	public Identifier getTextureLocation(Commando entity)
	{
		return TEXTURE;
	}
}
