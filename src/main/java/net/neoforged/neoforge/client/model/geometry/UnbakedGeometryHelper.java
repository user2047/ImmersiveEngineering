package net.neoforged.neoforge.client.model.geometry;

import net.minecraft.client.resources.model.sprite.Material;

public class UnbakedGeometryHelper
{
	public static Material resolveDirtyMaterial(String name, IGeometryBakingContext owner)
	{
		Material material = owner.getMaterial(name);
		if(material!=null)
			return material;
		return owner.getMaterial("particle");
	}
}
