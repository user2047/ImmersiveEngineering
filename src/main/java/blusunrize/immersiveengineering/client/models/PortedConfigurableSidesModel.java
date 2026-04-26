/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models;

import blusunrize.immersiveengineering.api.IEEnums.IOSideConfig;
import blusunrize.immersiveengineering.api.utils.DirectionUtils;
import blusunrize.immersiveengineering.client.utils.ModelUtils;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

public record PortedConfigurableSidesModel(
		String type,
		Identifier baseName,
		TextureSlots.Data textureSlots,
		@Nullable ItemTransforms transforms,
		@Nullable Identifier parent
) implements UnbakedModel
{
	public static final Loader LOADER = new Loader();

	@Override
	public TextureSlots.Data textureSlots()
	{
		return textureSlots;
	}

	@Override
	public @Nullable ItemTransforms transforms()
	{
		return transforms;
	}

	@Override
	public UnbakedGeometry geometry()
	{
		return this::bakeGeometry;
	}

	@Override
	public @Nullable Identifier parent()
	{
		return parent;
	}

	private QuadCollection bakeGeometry(
			TextureSlots textureSlots, ModelBaker modelBaker, ModelState modelState, ModelDebugName name
	)
	{
		QuadCollection.Builder result = new QuadCollection.Builder();
		Transformation transform = modelState.transformation().blockCenterToCorner();
		putFace(result, transform, Direction.DOWN, new Vec3[]{
				new Vec3(0, 0, 0), new Vec3(0, 0, 1), new Vec3(1, 0, 1), new Vec3(1, 0, 0)
		}, new double[]{0, 16, 16, 0}, true, spriteFor(Direction.DOWN, textureSlots, modelBaker, name));
		putFace(result, transform, Direction.UP, new Vec3[]{
				new Vec3(0, 1, 0), new Vec3(0, 1, 1), new Vec3(1, 1, 1), new Vec3(1, 1, 0)
		}, new double[]{0, 0, 16, 16}, false, spriteFor(Direction.UP, textureSlots, modelBaker, name));
		putFace(result, transform, Direction.NORTH, new Vec3[]{
				new Vec3(1, 0, 0), new Vec3(1, 1, 0), new Vec3(0, 1, 0), new Vec3(0, 0, 0)
		}, new double[]{0, 16, 16, 0}, true, spriteFor(Direction.NORTH, textureSlots, modelBaker, name));
		putFace(result, transform, Direction.SOUTH, new Vec3[]{
				new Vec3(1, 0, 1), new Vec3(1, 1, 1), new Vec3(0, 1, 1), new Vec3(0, 0, 1)
		}, new double[]{16, 16, 0, 0}, false, spriteFor(Direction.SOUTH, textureSlots, modelBaker, name));
		putFace(result, transform, Direction.WEST, new Vec3[]{
				new Vec3(0, 0, 0), new Vec3(0, 1, 0), new Vec3(0, 1, 1), new Vec3(0, 0, 1)
		}, new double[]{0, 16, 16, 0}, true, spriteFor(Direction.WEST, textureSlots, modelBaker, name));
		putFace(result, transform, Direction.EAST, new Vec3[]{
				new Vec3(1, 0, 0), new Vec3(1, 1, 0), new Vec3(1, 1, 1), new Vec3(1, 0, 1)
		}, new double[]{16, 16, 0, 0}, false, spriteFor(Direction.EAST, textureSlots, modelBaker, name));
		return result.build();
	}

	private TextureAtlasSprite spriteFor(
			Direction side, TextureSlots textureSlots, ModelBaker modelBaker, ModelDebugName debugName
	)
	{
		Identifier texture = textureFor(side, IOSideConfig.NONE);
		Material.Baked material = modelBaker.materials().resolveSlot(textureSlots, texture.toString(), debugName);
		return material.sprite();
	}

	private Identifier textureFor(Direction side, IOSideConfig cfg)
	{
		return baseName.withSuffix("_"+textureName(side, cfg));
	}

	private String textureName(Direction side, IOSideConfig cfg)
	{
		String sideName = switch(type)
		{
			case "side_top_bottom" -> side.getAxis()==Direction.Axis.Y?side.getSerializedName(): "side";
			case "side_vertical" -> side.getAxis()==Direction.Axis.Y?"up": "side";
			case "vertical" -> side.getAxis()==Direction.Axis.Y?"up": "side";
			case "all_same_texture" -> "side";
			default -> side.getSerializedName();
		};
		if("vertical".equals(type)&&side.getAxis()!=Direction.Axis.Y)
			return sideName;
		return sideName+"_"+cfg.getTextureName();
	}

	private static void putFace(
			QuadCollection.Builder result, Transformation transform, Direction direction, Vec3[] vertices,
			double[] uv, boolean invert, TextureAtlasSprite sprite
	)
	{
		Vec3[] transformed = new Vec3[vertices.length];
		for(int i = 0; i < vertices.length; i++)
			transformed[i] = transformPosition(transform, vertices[i]);
		BakedQuad quad = ModelUtils.createBakedQuad(transformed, direction, sprite, uv, new float[]{1, 1, 1, 1}, invert);
		result.addCulledFace(direction, quad);
	}

	private static Vec3 transformPosition(Transformation transform, Vec3 position)
	{
		Vector4f transformed = new Vector4f((float)position.x, (float)position.y, (float)position.z, 1);
		transform.transformPosition(transformed);
		transformed.mul(1/transformed.w());
		return new Vec3(
				snap(transformed.x()),
				snap(transformed.y()),
				snap(transformed.z())
		);
	}

	private static float snap(float value)
	{
		final float epsilon = 1e-5f;
		if(Math.abs(value) < epsilon)
			return 0;
		if(Math.abs(value-1) < epsilon)
			return 1;
		return value;
	}

	public static class Loader implements UnbakedModelLoader<PortedConfigurableSidesModel>
	{
		@Override
		public PortedConfigurableSidesModel read(JsonObject modelContents, JsonDeserializationContext context)
				throws JsonParseException
		{
			String type = modelContents.get("type").getAsString();
			Identifier baseName = Identifier.parse(modelContents.get("base_name").getAsString());
			TextureSlots.Data textures = TextureSlots.parseTextureMap(collectTextureSlots(modelContents, type, baseName));
			ItemTransforms transforms = modelContents.has("display")?
					context.deserialize(modelContents.get("display"), ItemTransforms.class):
					null;
			Identifier parent = modelContents.has("parent")?
					Identifier.parse(modelContents.get("parent").getAsString()):
					null;
			return new PortedConfigurableSidesModel(type, baseName, textures, transforms, parent);
		}

		private static JsonObject collectTextureSlots(JsonObject modelContents, String type, Identifier baseName)
		{
			JsonObject textures = modelContents.has("textures")?
					modelContents.getAsJsonObject("textures").deepCopy():
					new JsonObject();
			PortedConfigurableSidesModel namer = new PortedConfigurableSidesModel(
					type, baseName, TextureSlots.Data.EMPTY, null, null
			);
			for(Direction side : DirectionUtils.VALUES)
				for(IOSideConfig cfg : IOSideConfig.values())
				{
					Identifier texture = namer.textureFor(side, cfg);
					if(!textures.has(texture.toString()))
						textures.addProperty(texture.toString(), texture.toString());
				}
			Identifier particle = namer.textureFor(Direction.DOWN, IOSideConfig.NONE);
			if(!textures.has("particle"))
				textures.addProperty("particle", particle.toString());
			return textures;
		}
	}
}
