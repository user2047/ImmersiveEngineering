/*
 * BluSunrize
 * Copyright (c) 2026
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.common.util.IELogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.neoforged.fml.loading.FMLPaths;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class ChopAnimationTuning
{
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String CONFIG_FILE = "ie_chopanim.json";
	private static final int AUTO_RELOAD_INTERVAL = 20;
	public static final Value ARM_START = new Value("armStart", 30);
	public static final Value ARM_END = new Value("armEnd", -8);
	public static final Value AXE_START = new Value("axeStart", 95);
	public static final Value AXE_END = new Value("axeEnd", -32);
	public static final Value HAND_START_X = new Value("handStartX", .5F);
	public static final Value HAND_START_Y = new Value("handStartY", -.22F);
	public static final Value HAND_START_REACH = new Value("handStartReach", 1.65F);
	public static final Value HAND_TARGET_BASE_X = new Value("handTargetBaseX", -.06F);
	public static final Value HAND_TARGET_SIDE_SCALE = new Value("handTargetSideScale", .12F);
	public static final Value HAND_TARGET_BASE_Y = new Value("handTargetBaseY", -.3F);
	public static final Value HAND_TARGET_VERTICAL_SCALE = new Value("handTargetVerticalScale", .4F);
	public static final Value HAND_TARGET_REACH_OFFSET = new Value("handTargetReachOffset", -.2F);
	public static final Value HAND_TARGET_REACH_MIN = new Value("handTargetReachMin", 2.45F);
	public static final Value HAND_TARGET_REACH_MAX = new Value("handTargetReachMax", 3.45F);
	public static final Value EQUIP_DROP = new Value("equipDrop", .12F);
	public static final Value AXE_GRIP_PIVOT_X = new Value("axeGripPivotX", .2F);
	public static final Value AXE_GRIP_PIVOT_Y = new Value("axeGripPivotY", .36F);
	public static final Value ITEM_YAW = new Value("itemYaw", 180);
	public static final Value ITEM_PITCH = new Value("itemPitch", 65);
	public static final Value ITEM_ROLL = new Value("itemRoll", 0);
	public static final Value ITEM_SCALE = new Value("itemScale", 1.15F);
	public static final Value ARM_MODEL_OFFSET_X = new Value("armModelOffsetX", .52F);
	public static final Value ARM_MODEL_OFFSET_Y = new Value("armModelOffsetY", -.74F);
	public static final Value ARM_MODEL_OFFSET_Z = new Value("armModelOffsetZ", .5F);
	public static final Value ARM_MODEL_YAW = new Value("armModelYaw", -4);
	public static final Value ARM_MODEL_PIVOT_X = new Value("armModelPivotX", -1);
	public static final Value ARM_MODEL_PIVOT_Y = new Value("armModelPivotY", 3.6F);
	public static final Value ARM_MODEL_PIVOT_Z = new Value("armModelPivotZ", 3.5F);
	public static final Value ARM_MODEL_ROLL = new Value("armModelRoll", 114);
	public static final Value ARM_MODEL_PITCH = new Value("armModelPitch", 196);
	public static final Value ARM_MODEL_YAW_2 = new Value("armModelYaw2", -132);
	public static final Value ARM_MODEL_FINAL_X = new Value("armModelFinalX", 5.6F);
	public static final Value TARGET_SIDE_BASE = new Value("targetSideBase", 0);
	public static final Value TARGET_Y = new Value("targetY", -.34F);
	public static final Value TARGET_FALLBACK_REACH = new Value("targetFallbackReach", 3.35F);
	public static final Value TARGET_REACH_OFFSET = new Value("targetReachOffset", .95F);
	public static final Value TARGET_REACH_MIN = new Value("targetReachMin", 3.25F);
	public static final Value TARGET_REACH_MAX = new Value("targetReachMax", 4.75F);
	public static final Value TARGET_SIDE_SCALE = new Value("targetSideScale", .15F);
	public static final Value TARGET_SIDE_CLAMP = new Value("targetSideClamp", .08F);
	public static final Value TARGET_VERTICAL_SCALE = new Value("targetVerticalScale", .55F);
	public static final Value TARGET_VERTICAL_MIN = new Value("targetVerticalMin", -.05F);
	public static final Value TARGET_VERTICAL_MAX = new Value("targetVerticalMax", .35F);
	private static final List<Value> VALUES = List.of(
			ARM_START, ARM_END, AXE_START, AXE_END,
			HAND_START_X, HAND_START_Y, HAND_START_REACH,
			HAND_TARGET_BASE_X, HAND_TARGET_SIDE_SCALE, HAND_TARGET_BASE_Y, HAND_TARGET_VERTICAL_SCALE,
			HAND_TARGET_REACH_OFFSET, HAND_TARGET_REACH_MIN, HAND_TARGET_REACH_MAX, EQUIP_DROP,
			AXE_GRIP_PIVOT_X, AXE_GRIP_PIVOT_Y, ITEM_YAW, ITEM_PITCH, ITEM_ROLL, ITEM_SCALE,
			ARM_MODEL_OFFSET_X, ARM_MODEL_OFFSET_Y, ARM_MODEL_OFFSET_Z, ARM_MODEL_YAW,
			ARM_MODEL_PIVOT_X, ARM_MODEL_PIVOT_Y, ARM_MODEL_PIVOT_Z,
			ARM_MODEL_ROLL, ARM_MODEL_PITCH, ARM_MODEL_YAW_2, ARM_MODEL_FINAL_X,
			TARGET_SIDE_BASE, TARGET_Y, TARGET_FALLBACK_REACH, TARGET_REACH_OFFSET,
			TARGET_REACH_MIN, TARGET_REACH_MAX, TARGET_SIDE_SCALE, TARGET_SIDE_CLAMP,
			TARGET_VERTICAL_SCALE, TARGET_VERTICAL_MIN, TARGET_VERTICAL_MAX
	);
	private static int ticksUntilAutoReload = 1;
	private static long lastSeenConfigModified = Long.MIN_VALUE;

	private ChopAnimationTuning()
	{
	}

	public static List<Value> values()
	{
		return VALUES;
	}

	public static void reset()
	{
		VALUES.forEach(Value::reset);
	}

	public static void tickAutoReload()
	{
		if(--ticksUntilAutoReload > 0)
			return;
		ticksUntilAutoReload = AUTO_RELOAD_INTERVAL;
		String message = reloadFromFileIfChanged();
		if(message!=null&&message.startsWith("Failed "))
			IELogger.warn(message);
	}

	public static Path getConfigPath()
	{
		return FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILE);
	}

	public static String reloadFromFile()
	{
		Path path = getConfigPath();
		if(!Files.exists(path))
			return saveToFile();
		try(BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8))
		{
			lastSeenConfigModified = Files.getLastModifiedTime(path).toMillis();
			JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
			for(Value value : VALUES)
				if(json.has(value.name())&&json.get(value.name()).isJsonPrimitive())
					value.set(json.get(value.name()).getAsFloat());
			return "Loaded chop animation values from "+path.toAbsolutePath();
		} catch(Exception e)
		{
			lastSeenConfigModified = getLastModified(path);
			return "Failed to load chop animation values from "+path.toAbsolutePath()+": "+e.getMessage();
		}
	}

	public static String reloadFromFileIfChanged()
	{
		Path path = getConfigPath();
		if(!Files.exists(path))
		{
			if(lastSeenConfigModified==Long.MIN_VALUE)
				return saveToFile();
			lastSeenConfigModified = Long.MIN_VALUE;
			return null;
		}
		long modified = getLastModified(path);
		if(modified==lastSeenConfigModified)
			return null;
		return reloadFromFile();
	}

	public static String saveToFile()
	{
		Path path = getConfigPath();
		try
		{
			Files.createDirectories(path.getParent());
			JsonObject json = new JsonObject();
			for(Value value : VALUES)
				json.addProperty(value.name(), value.get());
			try(BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8))
			{
				GSON.toJson(json, writer);
			}
			lastSeenConfigModified = Files.getLastModifiedTime(path).toMillis();
			return "Saved chop animation values to "+path.toAbsolutePath();
		} catch(Exception e)
		{
			return "Failed to save chop animation values to "+path.toAbsolutePath()+": "+e.getMessage();
		}
	}

	private static long getLastModified(Path path)
	{
		try
		{
			return Files.exists(path)?Files.getLastModifiedTime(path).toMillis(): Long.MIN_VALUE;
		} catch(Exception e)
		{
			return Long.MIN_VALUE;
		}
	}

	public static final class Value
	{
		private final String name;
		private final float defaultValue;
		private float value;

		private Value(String name, float defaultValue)
		{
			this.name = name;
			this.defaultValue = defaultValue;
			this.value = defaultValue;
		}

		public String name()
		{
			return name;
		}

		public float get()
		{
			return value;
		}

		public void set(float value)
		{
			this.value = value;
		}

		public float defaultValue()
		{
			return defaultValue;
		}

		public void reset()
		{
			value = defaultValue;
		}
	}
}
