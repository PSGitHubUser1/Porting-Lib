package io.github.fabricators_of_create.porting_lib.models.mixin;

import io.github.fabricators_of_create.porting_lib.models.extensions.BlockParticleOptionExtensions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.IronGolem;

import net.minecraft.world.level.Level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(IronGolem.class)
public abstract class IronGolemMixin extends Entity {
	public IronGolemMixin(EntityType<?> entityType, Level level) {
		super(entityType, level);
	}

	@ModifyArg(
			method = "aiStep",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/Level;addParticle(Lnet/minecraft/core/particles/ParticleOptions;DDDDDD)V"
			)
	)
	private ParticleOptions addSourcePos(ParticleOptions options) {
		return BlockParticleOptionExtensions.setSourceFromEntity(options, this);
	}
}