package io.github.fabricators_of_create.porting_lib.event.common;

import io.github.fabricators_of_create.porting_lib.block.CustomExpBlock;
import io.github.fabricators_of_create.porting_lib.core.event.BaseEvent;
import io.github.fabricators_of_create.porting_lib.util.PortingHooks;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

import org.jetbrains.annotations.Nullable;

public abstract class BlockEvents extends BaseEvent {

	public static final Event<BlockBreak> BLOCK_BREAK = EventFactory.createArrayBacked(BlockBreak.class, callbacks -> event -> {
		for(BlockBreak e : callbacks)
			e.onBlockBreak(event);
	});

	public interface BlockBreak {
		void onBlockBreak(BreakEvent event);
	}

	/**
	 * Invoked before a block is placed from the head of {@link BlockItem#useOn(UseOnContext)}. Called on both client and server.
	 * Return null to fall back to further processing. Any non-null value will result in placement being cancelled.
	 */
	public static final Event<BeforePlace> BEFORE_PLACE = EventFactory.createArrayBacked(BeforePlace.class, callbacks -> context -> {
		for (BeforePlace callback : callbacks) {
			InteractionResult result = callback.beforePlace(context);
			if (result != null)
				return result;
		}
		return null;
	});

	public interface BeforePlace {
		@Nullable
		InteractionResult beforePlace(BlockPlaceContext ctx);
	}

	/**
	 * Invoked after a block is placed, from {@link BlockItem#useOn(UseOnContext)}. Called on both client and server.
	 */
	public static final Event<AfterPlace> AFTER_PLACE = EventFactory.createArrayBacked(AfterPlace.class, callbacks -> context -> {
		for (AfterPlace callback : callbacks)
			callback.afterPlace(context);
	});

	public interface AfterPlace {
		void afterPlace(BlockPlaceContext ctx);
	}

	private final LevelAccessor level;
	private final BlockPos pos;
	private final BlockState state;
	public BlockEvents(LevelAccessor world, BlockPos pos, BlockState state) {
		this.pos = pos;
		this.level = world;
		this.state = state;
	}

	public LevelAccessor getLevel()
	{
		return level;
	}

	@Deprecated(forRemoval = true)
	public LevelAccessor getWorld()
	{
		return level;
	}

	public BlockPos getPos()
	{
		return pos;
	}

	public BlockState getState()
	{
		return state;
	}

	public static class BreakEvent extends BlockEvents {
		/** Reference to the Player who broke the block. If no player is available, use a EntityFakePlayer */
		private final Player player;
		private int exp;

		public BreakEvent(Level world, BlockPos pos, BlockState state, Player player) {
			super(world, pos, state);
			this.player = player;

			if (state == null || !PortingHooks.isCorrectToolForDrops(state, player)) { // Handle empty block or player unable to break block scenario
				this.exp = 0;
			} else{
				int bonusLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, player.getMainHandItem());
				int silklevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SILK_TOUCH, player.getMainHandItem());
				this.exp = state.getBlock() instanceof CustomExpBlock exp ? exp.getExpDrop(state, world, world.getRandom(), pos, bonusLevel, silklevel) : 0;
			}
		}

		public Player getPlayer()
		{
			return player;
		}

		/**
		 * Get the experience dropped by the block after the event has processed
		 *
		 * @return The experience to drop or 0 if the event was canceled
		 */
		public int getExpToDrop()
		{
			return this.isCanceled() ? 0 : exp;
		}

		/**
		 * Set the amount of experience dropped by the block after the event has processed
		 *
		 * @param exp 1 or higher to drop experience, else nothing will drop
		 */
		public void setExpToDrop(int exp)
		{
			this.exp = exp;
		}

		@Override
		public void sendEvent() {
			BLOCK_BREAK.invoker().onBlockBreak(this);
		}
	}
}
