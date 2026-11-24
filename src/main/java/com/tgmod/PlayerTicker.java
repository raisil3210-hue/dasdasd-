package com.tgmod;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/** RC, fome de ghoul e Kakugan. */
public class PlayerTicker {
    public static void toggleKakugan(ServerPlayerEntity sp) {
        TGData.P d = TGData.get(sp.getServer()).of(sp);
        if (!d.ghoul) { sp.sendMessage(Text.literal("\u00a7cVoce nao e um ghoul."), true); return; }
        if (!d.kakugan && d.rc < 5) { sp.sendMessage(Text.literal("\u00a7cRC insuficiente."), true); return; }
        d.kakugan = !d.kakugan;
        if (!d.kakugan) sp.removeStatusEffect(StatusEffects.NIGHT_VISION);
        sp.sendMessage(Text.literal(d.kakugan ? "\u00a74Kakugan ativado" : "\u00a77Kakugan desativado"), true);
    }

    public static void tick(MinecraftServer s) {
        if (s.getTicks() % 20 != 0) return;
        TGData data = TGData.get(s);
        for (ServerPlayerEntity p : s.getPlayerManager().getPlayerList()) {
            TGData.P d = data.of(p);
            if (!d.ghoul) continue;
            if (d.kakugan) {
                d.rc -= 1;
                if (d.rc <= 0) {
                    d.rc = 0;
                    d.kakugan = false;
                    p.removeStatusEffect(StatusEffects.NIGHT_VISION);
                    p.sendMessage(Text.literal("\u00a7cSem RC - Kakugan desligou."), true);
                } else {
                    p.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 260, 0, false, false));
                    p.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, 60, 1, false, false));
                    p.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 60, 0, false, false));
                    p.getServerWorld().spawnParticles(ParticleTypes.CRIMSON_SPORE, p.getX(), p.getEyeY(), p.getZ(), 5, 0.3, 0.2, 0.3, 0.0);
                }
            } else if (s.getTicks() % 40 == 0 && d.rc < d.maxRc) {
                d.rc++;
            }
            if (d.rc == 0) { // fome de ghoul
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 60, 1, false, false));
                p.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 60, 1, false, false));
            }
            p.sendMessage(Text.literal("\u00a74RC \u00a7f" + d.rc + "/" + d.maxRc + "  \u00a74Kakugan: \u00a7f" + (d.kakugan ? "ON" : "OFF")), true);
        }
    }
}
