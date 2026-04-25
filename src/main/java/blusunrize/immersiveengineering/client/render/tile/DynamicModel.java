/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client.render.tile;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.IEApi;
import blusunrize.immersiveengineering.client.ClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.model.data.ModelData;

import java.util.ArrayList;
import java.util.List;

public class DynamicModel
{
	private static final List<ModelResourceLocation> MODELS = new ArrayList<>();

	public static void registerModels(ModelEvent.RegisterStandalone ev)
	{
	}

	private final ModelResourceLocation name;

	public DynamicModel(String desc)
	{
		// TODO does this work?
		this.name = new ModelResourceLocation(IEApi.ieLoc("dynamic/"+desc), "standalone");
		MODELS.add(this.name);
	}

	public BakedModel get()
	{
		final BlockRenderDispatcher blockRenderer = ClientUtils.getBlockRenderer();
		return blockRenderer.getBlockModelShaper().getModelManager().getModel(name);
	}

	public List<BakedQuad> getNullQuads()
	{
		return getNullQuads(ModelData.EMPTY);
	}

	public List<BakedQuad> getNullQuads(ModelData data)
	{
		return get().getQuads(null, null, ApiUtils.RANDOM_SOURCE, data, null);
	}

	public Identifier getName()
	{
		return name.id();
	}
}
