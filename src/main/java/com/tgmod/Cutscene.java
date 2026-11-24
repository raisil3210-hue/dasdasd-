package com.tgmod;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

import java.util.*;

/** Sistema de cutscenes: titulo/legenda, cortes de camera (modo espectador), tela escura e sons. */
public class Cutscene {
    /** cam = deslocamento relativo ao ponto de origem (null = sem corte de camera). */
    public record Step(int delay, String title, String sub, Vec3d cam, float yaw, float pitch, boolean dark, String sound) {}

    private static class Run {
        List<Step> steps;
        int i = 0, wait = 0;
        Vec3d origin, startPos;
        float sy, sp;
        GameMode prev;
        boolean spec;
        Runnable end;
    }

    private static final Map<UUID, Run> RUNS = new HashMap<>();

    public static boolean isPlaying(ServerPlayerEntity p) { return RUNS.containsKey(p.getUuid()); }

    public static void play(ServerPlayerEntity p, List<Step> steps, Vec3d origin, Runnable end) {
        if (steps == null || steps.isEmpty()) { end.run(); return; }
        Run r = new Run();
        r.steps = steps; r.origin = origin; r.end = end;
        r.startPos = p.getPos(); r.sy = p.getYaw(); r.sp = p.getPitch();
        r.prev = p.interactionManager.getGameMode();
        r.spec = steps.stream().anyMatch(s -> s.cam() != null);
        int total = steps.stream().mapToInt(Step::delay).sum() + 20;
        if (r.spec) {
            p.changeGameMode(GameMode.SPECTATOR);
        } else {
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, total, 6, false, false));
            p.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, total, 4, false, false));
        }
        RUNS.put(p.getUuid(), r);
    }

    public static void tick(MinecraftServer s) {
        List<Runnable> after = new ArrayList<>();
        Iterator<Map.Entry<UUID, Run>> it = RUNS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Run> e = it.next();
            ServerPlayerEntity p = s.getPlayerManager().getPlayer(e.getKey());
            Run r = e.getValue();
            if (p == null) { it.remove(); continue; }
            if (r.wait > 0) { r.wait--; continue; }
            if (r.i >= r.steps.size()) {
                if (r.spec) {
                    p.changeGameMode(r.prev);
                    p.teleport(p.getServerWorld(), r.startPos.x, r.startPos.y, r.startPos.z, r.sy, r.sp);
                }
                after.add(r.end);
                it.remove();
                continue;
            }
            Step st = r.steps.get(r.i++);
            show(p, r, st);
            r.wait = st.delay();
        }
        after.forEach(Runnable::run);
    }

    private static void show(ServerPlayerEntity p, Run r, Step st) {
        if (st.cam() != null) {
            Vec3d c = r.origin.add(st.cam());
            p.teleport(p.getServerWorld(), c.x, c.y, c.z, st.yaw(), st.pitch());
        }
        if (st.dark()) p.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, st.delay() + 10, 0, false, false));
        p.networkHandler.sendPacket(new TitleFadeS2CPacket(5, Math.max(10, st.delay() - 15), 10));
        p.networkHandler.sendPacket(new SubtitleS2CPacket(Text.literal(st.sub() == null ? "" : st.sub())));
        p.networkHandler.sendPacket(new TitleS2CPacket(Text.literal(st.title() == null ? "" : st.title())));
        if (st.sound() != null) {
            SoundEvent se = Registries.SOUND_EVENT.get(new Identifier(st.sound()));
            if (se != null) p.playSound(se, 1f, 1f);
        }
    }
}
