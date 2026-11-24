package com.tgmod;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.HashSet;
import java.util.Set;

/** Habilidades dos kagunes. */
public class Kagunes {
    private static void hit(ServerPlayerEntity p, LivingEntity e, float dmg) {
        e.damage(p.getServerWorld().getDamageSources().playerAttack(p), dmg);
    }

    public static void cast(ServerPlayerEntity p, KaguneItem.Type t) {
        ServerWorld w = p.getServerWorld();
        Vec3d look = p.getRotationVec(1f).normalize();
        Vec3d eye = p.getEyePos();
        switch (t) {
            case UKAKU -> {
                Set<LivingEntity> hit = new HashSet<>();
                for (int i = 1; i <= 16; i++) {
                    Vec3d pt = eye.add(look.multiply(i));
                    w.spawnParticles(ParticleTypes.CRIT, pt.x, pt.y, pt.z, 6, 0.15, 0.15, 0.15, 0.02);
                    w.spawnParticles(ParticleTypes.CRIMSON_SPORE, pt.x, pt.y, pt.z, 3, 0.1, 0.1, 0.1, 0.0);
                    for (LivingEntity e : w.getEntitiesByClass(LivingEntity.class, new Box(pt, pt).expand(1.3), x -> x != p && x.isAlive()))
                        if (hit.add(e)) hit(p, e, 7f);
                }
            }
            case KOUKAKU -> {
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 120, 2));
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 120, 2));
                w.spawnParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1, p.getZ(), 40, 0.6, 0.8, 0.6, 0.05);
            }
            case RINKAKU -> aoe(p, w, 6, 8f, false);
            case BIKAKU -> {
                p.addVelocity(look.x * 2.0, 0.25, look.z * 2.0);
                p.velocityModified = true;
                p.networkHandler.sendPacket(new EntityVelocityUpdateS2CPacket(p));
                Vec3d c = p.getPos().add(look.multiply(3));
                for (LivingEntity e : w.getEntitiesByClass(LivingEntity.class, new Box(c, c).expand(3), x -> x != p && x.isAlive()))
                    hit(p, e, 6f);
                w.spawnParticles(ParticleTypes.FLAME, p.getX(), p.getY() + 1, p.getZ(), 30, 0.4, 0.4, 0.4, 0.1);
            }
            case CENTIPEDE -> aoe(p, w, 9, 12f, true);
        }
    }

    private static void aoe(ServerPlayerEntity p, ServerWorld w, double r, float dmg, boolean strong) {
        for (int a = 0; a < 360; a += 10) {
            double rad = Math.toRadians(a);
            for (double d = 1; d <= r; d += 1.0)
                w.spawnParticles(strong ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.CRIMSON_SPORE,
                        p.getX() + Math.cos(rad) * d, p.getY() + 0.8, p.getZ() + Math.sin(rad) * d, 1, 0, 0.1, 0, 0);
        }
        w.spawnParticles(ParticleTypes.SWEEP_ATTACK, p.getX(), p.getY() + 1, p.getZ(), 12, r / 2, 0.3, r / 2, 0);
        for (LivingEntity e : w.getEntitiesByClass(LivingEntity.class, p.getBoundingBox().expand(r), x -> x != p && x.isAlive())) {
            hit(p, e, dmg);
            e.takeKnockback(0.8, p.getX() - e.getX(), p.getZ() - e.getZ());
            if (strong) {
                e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 60, 2));
                p.heal(2f);
            }
        }
    }
}
