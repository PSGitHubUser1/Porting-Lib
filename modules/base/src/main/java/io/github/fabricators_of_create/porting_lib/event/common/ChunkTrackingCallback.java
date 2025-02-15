package io.github.fabricators_of_create.porting_lib.event.common;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

public interface ChunkTrackingCallback {

	Event<Watch> WATCH = EventFactory.createArrayBacked(Watch.class, callbacks -> (entity, chunk, level) -> {
		for (Watch e : callbacks)
			e.onChunkWatch(entity, chunk, level);
	});

	Event<Unwatch> UNWATCH = EventFactory.createArrayBacked(Unwatch.class, callbacks -> (entity, chunkpos, level) -> {
		for (Unwatch e : callbacks)
			e.onChunkUnwatch(entity, chunkpos, level);
	});

	@FunctionalInterface
	interface Watch {
		void onChunkWatch(ServerPlayer entity, LevelChunk chunk, ServerLevel level);
	}

	@FunctionalInterface
	interface Unwatch {
		void onChunkUnwatch(ServerPlayer entity, ChunkPos chunkpos, ServerLevel level);
	}
}
