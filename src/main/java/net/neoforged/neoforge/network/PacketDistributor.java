package net.neoforged.neoforge.network;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

public class PacketDistributor
{
	public static void sendToServer(Object message)
	{
	}

	public static void sendToAllPlayers(Object message)
	{
	}

	public static void sendToPlayer(ServerPlayer player, Object message)
	{
	}

	public static void sendToPlayersTrackingEntity(Entity entity, Object message)
	{
	}

	public static void sendToPlayersTrackingChunk(ServerLevel level, ChunkPos chunk, Object message)
	{
	}

	public static void sendToPlayersInDimension(ServerLevel level, Object message)
	{
	}
}
