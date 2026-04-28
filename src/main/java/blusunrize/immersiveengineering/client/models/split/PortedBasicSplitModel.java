/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.models.split;

import blusunrize.immersiveengineering.api.IEProperties.Model;
import blusunrize.immersiveengineering.api.client.IModelOffsetProvider;
import blusunrize.immersiveengineering.client.models.split.PolygonUtils.ExtraQuadData;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import malte0811.modelsplitter.ClumpedModel;
import malte0811.modelsplitter.SplitModel;
import malte0811.modelsplitter.math.ModelSplitterVec3i;
import malte0811.modelsplitter.model.OBJModel;
import malte0811.modelsplitter.model.Polygon;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import net.neoforged.neoforge.client.model.UnbakedModelLoader;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;
import net.neoforged.neoforge.model.data.ModelData;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Map.Entry;

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
	public static final MapCodec<UnbakedBlockStateModel> BLOCK_STATE_CODEC = UnbakedBlockStateModel.MAP_CODEC;

	@Override
	public TextureSlots.Data textureSlots()
	{
		if(textureSlots.values().isEmpty())
			return innerModel.textureSlots();
		if(innerModel.textureSlots().values().isEmpty())
			return textureSlots;
		Map<String, TextureSlots.SlotContents> mergedSlots = new HashMap<>(innerModel.textureSlots().values());
		mergedSlots.putAll(textureSlots.values());
		return new TextureSlots.Data(mergedSlots);
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
		return innerGeometry.bake(textureSlots, modelBaker, modelState, name);
	}

	private static Map<BlockPos, BlockStateModelPart> splitToParts(
			List<BakedQuad> in, Set<Vec3i> parts, ModelState transform,
			boolean ambientOcclusion, Material.Baked particleMaterial
	)
	{
		List<Polygon<ExtraQuadData>> polys = in.stream()
				.map(PolygonUtils::toPolygon)
				.collect(Collectors.toList());
		SplitModel<ExtraQuadData> splitData = new SplitModel<>(new OBJModel<>(polys));
		Set<ModelSplitterVec3i> partsBMS = parts.stream()
				.map(v -> new ModelSplitterVec3i(v.getX(), v.getY(), v.getZ()))
				.collect(Collectors.toSet());
		ClumpedModel<ExtraQuadData> clumpedModel = new ClumpedModel<>(splitData, partsBMS);

		Map<BlockPos, BlockStateModelPart> map = new HashMap<>();
		for(Entry<ModelSplitterVec3i, OBJModel<ExtraQuadData>> entry : clumpedModel.getClumpedParts().entrySet())
		{
			QuadCollection.Builder subModelFaces = new QuadCollection.Builder();
			for(Polygon<ExtraQuadData> polygon : entry.getValue().getFaces())
				subModelFaces.addUnculledFace(PolygonUtils.toBakedQuad(polygon, transform));
			BlockPos mcKey = new BlockPos(entry.getKey().x(), entry.getKey().y(), entry.getKey().z());
			map.put(mcKey, new StaticPart(subModelFaces.build(), ambientOcclusion, particleMaterial));
		}
		return map;
	}

	private static @Nullable PortedBasicSplitModel findSplitModel(ResolvedModel model)
	{
		ResolvedModel current = model;
		while(current!=null)
		{
			if(current.wrapped() instanceof PortedBasicSplitModel splitModel)
				return splitModel;
			current = current.parent();
		}
		return null;
	}

	public record UnbakedBlockStateModel(
			Identifier model,
			Variant.SimpleModelState modelState
	) implements CustomUnbakedBlockStateModel
	{
		public static final MapCodec<UnbakedBlockStateModel> MAP_CODEC = RecordCodecBuilder.mapCodec(
				instance -> instance.group(
						Identifier.CODEC.fieldOf("model").forGetter(UnbakedBlockStateModel::model),
						Variant.SimpleModelState.MAP_CODEC.forGetter(UnbakedBlockStateModel::modelState)
				).apply(instance, UnbakedBlockStateModel::new)
		);

		@Override
		public BlockStateModel bake(ModelBaker modelBaker)
		{
			ResolvedModel resolvedModel = modelBaker.getModel(model);
			PortedBasicSplitModel splitModel = findSplitModel(resolvedModel);
			ModelState finalTransform = modelState.asModelState();
			if(splitModel==null)
				return new SingleVariant(SimpleModelWrapper.bake(modelBaker, resolvedModel, finalTransform));

			TextureSlots textureSlots = resolvedModel.getTopTextureSlots();
			boolean ambientOcclusion = resolvedModel.getTopAmbientOcclusion();
			Material.Baked particleMaterial = resolvedModel.resolveParticleMaterial(textureSlots, modelBaker);
			QuadCollection fallbackQuads = resolvedModel.bakeTopGeometry(textureSlots, modelBaker, finalTransform);
			QuadCollection unrotatedQuads = resolvedModel.bakeTopGeometry(textureSlots, modelBaker, BlockModelRotation.IDENTITY);
			Set<Vec3i> parts = new HashSet<>(splitModel.parts);
			Map<BlockPos, BlockStateModelPart> splitParts = splitToParts(
					unrotatedQuads.getAll(), parts, finalTransform, ambientOcclusion, particleMaterial
			);
			return new SplitBlockStateModel(
					splitModel.size,
					splitParts,
					new StaticPart(fallbackQuads, ambientOcclusion, particleMaterial),
					particleMaterial
			);
		}

		@Override
		public void resolveDependencies(Resolver resolver)
		{
			resolver.markDependency(model);
		}

		@Override
		public MapCodec<? extends CustomUnbakedBlockStateModel> codec()
		{
			return MAP_CODEC;
		}
	}

	private record SplitBlockStateModel(
			Vec3i size,
			Map<BlockPos, BlockStateModelPart> splitParts,
			BlockStateModelPart fallback,
			Material.Baked particleMaterial
	) implements DynamicBlockStateModel
	{
		@Override
		public void collectParts(RandomSource random, List<BlockStateModelPart> parts)
		{
			parts.add(fallback);
		}

		@Override
		public void collectParts(
				BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random,
				List<BlockStateModelPart> parts
		)
		{
			BlockPos offset = getModelOffset(level, pos, state);
			if(offset==null)
				parts.add(fallback);
			else
			{
				BlockStateModelPart splitPart = splitParts.get(offset);
				if(splitPart!=null)
					parts.add(splitPart);
			}
		}

		@Override
		public @Nullable Object createGeometryKey(
				BlockAndTintGetter level, BlockPos pos, BlockState state, RandomSource random
		)
		{
			BlockPos offset = getModelOffset(level, pos, state);
			return new GeometryKey(this, offset==null?null: offset.immutable());
		}

		private @Nullable BlockPos getModelOffset(BlockAndTintGetter level, BlockPos pos, BlockState state)
		{
			ModelData modelData = level.getModelData(pos);
			BlockPos offset = modelData.get(Model.SUBMODEL_OFFSET);
			if(offset!=null)
				return offset;

			BlockEntity blockEntity = level.getBlockEntity(pos);
			if(blockEntity instanceof IModelOffsetProvider offsetProvider)
				return offsetProvider.getModelOffset(state, size);
			if(state.getBlock() instanceof IModelOffsetProvider offsetProvider)
				return offsetProvider.getModelOffset(state, size);
			return null;
		}

		@Override
		public Material.Baked particleMaterial()
		{
			return particleMaterial;
		}

		@Override
		public int materialFlags()
		{
			int flags = fallback.materialFlags();
			for(BlockStateModelPart part : splitParts.values())
				flags |= part.materialFlags();
			return flags;
		}
	}

	private record GeometryKey(SplitBlockStateModel model, @Nullable BlockPos offset)
	{
	}

	private record StaticPart(
			QuadCollection quads,
			boolean usesAmbientOcclusion,
			Material.Baked particleMaterial
	) implements BlockStateModelPart
	{
		@Override
		public List<BakedQuad> getQuads(@Nullable Direction direction)
		{
			return quads.getQuads(direction);
		}

		@Override
		public boolean useAmbientOcclusion()
		{
			return usesAmbientOcclusion;
		}

		@Override
		public int materialFlags()
		{
			return quads.materialFlags();
		}
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
