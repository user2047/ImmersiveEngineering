/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.split;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public record PortedBasicSplitModel(
		UnbakedModel innerModel,
		List<Vec3i> parts,
		Vec3i size,
		boolean dynamic,
		TextureSlots.Data textureSlots,
		@Nullable ItemTransforms transforms,
		@Nullable Identifier parent
) implements UnbakedModel
{
	public static final Loader LOADER = new Loader();

	@Override
	public TextureSlots.Data textureSlots()
	{
		return textureSlots.values().isEmpty()?innerModel.textureSlots(): textureSlots;
	}

	@Override
	public @Nullable ItemTransforms transforms()
	{
		return transforms;
	}

	@Override
	public @Nullable Identifier parent()
	{
		return parent;
	}

	@Override
	public UnbakedGeometry geometry()
	{
		return this::bakeGeometry;
	}

	private QuadCollection bakeGeometry(
			TextureSlots textureSlots, ModelBaker modelBaker, ModelState modelState, ModelDebugName name
	)
	{
		UnbakedGeometry innerGeometry = innerModel.geometry();
		if(innerGeometry==null)
			return QuadCollection.EMPTY;
		TextureSlots innerSlots = new TextureSlots.Resolver()
				.addLast(this.textureSlots)
				.addLast(innerModel.textureSlots())
				.resolve(name);
		return innerGeometry.bake(innerSlots, modelBaker, modelState, name);
	}

	public static class Loader implements UnbakedModelLoader<PortedBasicSplitModel>
	{
		private static final String PARTS = "split_parts";
		private static final String INNER_MODEL = "inner_model";
		private static final String DYNAMIC = "dynamic";

		@Override
		public PortedBasicSplitModel read(JsonObject modelContents, JsonDeserializationContext context)
				throws JsonParseException
		{
			UnbakedModel innerModel = context.deserialize(modelContents.get(INNER_MODEL), UnbakedModel.class);
			List<Vec3i> parts = readParts(modelContents.getAsJsonArray(PARTS));
			BoundingBox box = pointBB(parts.getFirst());
			for(Vec3i part : parts)
				box.encapsulate(pointBB(part));
			TextureSlots.Data textures = modelContents.has("textures")?
					TextureSlots.parseTextureMap(modelContents.getAsJsonObject("textures")):
					TextureSlots.Data.EMPTY;
			ItemTransforms transforms = modelContents.has("display")?
					context.deserialize(modelContents.get("display"), ItemTransforms.class):
					null;
			Identifier parent = modelContents.has("parent")?
					Identifier.parse(modelContents.get("parent").getAsString()):
					null;
			boolean dynamic = modelContents.has(DYNAMIC)&&modelContents.get(DYNAMIC).getAsBoolean();
			return new PortedBasicSplitModel(
					innerModel, parts, new Vec3i(box.getXSpan(), box.getYSpan(), box.getZSpan()),
					dynamic, textures, transforms, parent
			);
		}

		private static List<Vec3i> readParts(JsonArray partsJson)
		{
			List<Vec3i> parts = new ArrayList<>(partsJson.size());
			for(JsonElement element : partsJson)
			{
				JsonArray coordinates = element.getAsJsonArray();
				parts.add(new Vec3i(
						coordinates.get(0).getAsInt(),
						coordinates.get(1).getAsInt(),
						coordinates.get(2).getAsInt()
				));
			}
			return parts;
		}

		private static BoundingBox pointBB(Vec3i point)
		{
			return new BoundingBox(new BlockPos(point));
		}
	}
}
