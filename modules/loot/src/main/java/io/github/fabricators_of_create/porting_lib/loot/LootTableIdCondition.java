package io.github.fabricators_of_create.porting_lib.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;

import com.mojang.serialization.Codec;

import io.github.fabricators_of_create.porting_lib.core.PortingLib;

import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;

public class LootTableIdCondition implements LootItemCondition {
	public static final Codec<LootTableIdCondition> CODEC = ResourceLocation.CODEC.xmap(LootTableIdCondition::new, condition -> condition.targetLootTableId);
	public static final LootItemConditionType LOOT_TABLE_ID = new LootItemConditionType(CODEC);
	public static final ResourceLocation UNKNOWN_LOOT_TABLE = PortingLib.id("unknown_loot_table");

	private final ResourceLocation targetLootTableId;

	private LootTableIdCondition(final ResourceLocation targetLootTableId) {
		this.targetLootTableId = targetLootTableId;
	}

	@Override
	public LootItemConditionType getType() {
		return LOOT_TABLE_ID;
	}

	@Override
	public boolean test(LootContext lootContext) {
		return lootContext.getQueriedLootTableId().equals(this.targetLootTableId);
	}

	public static Builder builder(final ResourceLocation targetLootTableId) {
		return new Builder(targetLootTableId);
	}

	public static class Builder implements LootItemCondition.Builder {
		private final ResourceLocation targetLootTableId;

		public Builder(ResourceLocation targetLootTableId) {
			if (targetLootTableId == null) throw new IllegalArgumentException("Target loot table must not be null");
			this.targetLootTableId = targetLootTableId;
		}

		@Override
		public LootItemCondition build() {
			return new LootTableIdCondition(this.targetLootTableId);
		}
	}
}
