package io.github.fabricators_of_create.porting_lib.transfer.cache;

import io.github.fabricators_of_create.porting_lib.transfer.StorageProvider;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jetbrains.annotations.Nullable;

/**
 * This should not be used directly. Use {@link StorageProvider} instead.
 */
@Internal
@SuppressWarnings("NonExtendableApiUsage")
public class ClientFluidLookupCache implements BlockApiCache<Storage<FluidVariant>, Direction>, ClientBlockApiCache {
	private final ClientLevel world;
	private final BlockPos pos;
	private boolean blockEntityCacheValid = false;
	private BlockEntity cachedBlockEntity = null;

	public static BlockApiCache<Storage<FluidVariant>, Direction> get(Level level, BlockPos pos) {
		if (level instanceof ClientLevel c)
			return new ClientFluidLookupCache(c, pos);
		return new EmptyFluidLookupCache(pos);
	}

	public ClientFluidLookupCache(ClientLevel world, BlockPos pos) {
		world.port_lib$registerCache(pos ,this);
		this.world = world;
		this.pos = pos.immutable();
	}

	public void invalidate() {
		blockEntityCacheValid = false;
		cachedBlockEntity = null;
	}

	@Nullable
	@Override
	public Storage<FluidVariant> find(@Nullable BlockState state, @Nullable Direction context) {
		// Update block entity cache
		getBlockEntity();
		// Query the provider
		if (cachedBlockEntity == null)
			return null;
		return TransferUtil.getFluidStorage(world, pos, cachedBlockEntity, context);
	}

	@Override
	@Nullable
	public BlockEntity getBlockEntity() {
		if (!blockEntityCacheValid) {
			cachedBlockEntity = world.getBlockEntity(pos);
			blockEntityCacheValid = true;
		}

		return cachedBlockEntity;
	}

	@Override
	public BlockApiLookup<Storage<FluidVariant>, Direction> getLookup() {
		return FluidStorage.SIDED;
	}

	@Override
	public ServerLevel getWorld() {
		throw new UnsupportedOperationException("Cannot call getWorld on a client-side cache as only ServerLevels are supported");
	}

	@Override
	public BlockPos getPos() {
		return pos;
	}
}