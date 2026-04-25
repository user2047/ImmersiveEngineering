package net.minecraft.client.resources.model.geometry;

import com.mojang.blaze3d.platform.Transparency;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;
import org.joml.Vector3fc;

public class BakedQuad
{
	private final int[] vertices;
	private final int tintIndex;
	private final Direction direction;
	private final TextureAtlasSprite sprite;
	private final boolean shade;

	public BakedQuad(int[] vertices, int tintIndex, Direction direction, TextureAtlasSprite sprite, boolean shade)
	{
		this.vertices = vertices;
		this.tintIndex = tintIndex;
		this.direction = direction;
		this.sprite = sprite;
		this.shade = shade;
	}

	public BakedQuad(
			Vector3fc p0, Vector3fc p1, Vector3fc p2, Vector3fc p3,
			long uv0, long uv1, long uv2, long uv3, Direction direction, MaterialInfo materialInfo
	)
	{
		this(new int[32], materialInfo.tintIndex(), direction, materialInfo.sprite(), materialInfo.shade());
	}

	public int[] getVertices()
	{
		return vertices;
	}

	public int getTintIndex()
	{
		return tintIndex;
	}

	public Direction getDirection()
	{
		return direction;
	}

	public TextureAtlasSprite getSprite()
	{
		return sprite;
	}

	public boolean isShade()
	{
		return shade;
	}

	public MaterialInfo materialInfo()
	{
		return new MaterialInfo(sprite, null, null, tintIndex, shade, 0);
	}

	public record MaterialInfo(TextureAtlasSprite sprite, ChunkSectionLayer layer, RenderType itemRenderType, int tintIndex, boolean shade, int lightEmission)
	{
		public static MaterialInfo of(Material.Baked material, Transparency transparency, int tintIndex, boolean shade, int lightEmission)
		{
			return new MaterialInfo(material.sprite(), null, null, tintIndex, shade, lightEmission);
		}

		public int flags()
		{
			return 0;
		}
	}
}
