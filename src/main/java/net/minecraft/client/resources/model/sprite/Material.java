package net.minecraft.client.resources.model.sprite;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.Identifier;

public record Material(Identifier atlasLocation, Identifier texture)
{
	public Material(Identifier atlasLocation, Identifier texture)
	{
		this.atlasLocation = atlasLocation;
		this.texture = texture;
	}

	public TextureAtlasSprite sprite()
	{
		return null;
	}
}
