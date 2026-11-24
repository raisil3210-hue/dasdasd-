package com.tgmod;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Dados persistentes por jogador: RC, ghoul, kakugan, capitulo. */
public class TGData extends PersistentState {
    public static class P {
        public int rc = 100, maxRc = 100, chapter = -1;
        public boolean ghoul, kakugan;
    }

    private final Map<UUID, P> map = new HashMap<>();

    public static TGData get(MinecraftServer s) {
        return s.getOverworld().getPersistentStateManager().getOrCreate(TGData::load, TGData::new, "tgmod_data");
    }

    public P of(PlayerEntity p) {
        markDirty();
        return map.computeIfAbsent(p.getUuid(), k -> new P());
    }

    public static TGData load(NbtCompound n) {
        TGData d = new TGData();
        NbtCompound pl = n.getCompound("players");
        for (String k : pl.getKeys()) {
            NbtCompound c = pl.getCompound(k);
            P p = new P();
            p.rc = c.getInt("rc");
            p.maxRc = c.contains("max") ? c.getInt("max") : 100;
            p.chapter = c.contains("ch") ? c.getInt("ch") : -1;
            p.ghoul = c.getBoolean("g");
            p.kakugan = c.getBoolean("k");
            d.map.put(UUID.fromString(k), p);
        }
        return d;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound n) {
        NbtCompound pl = new NbtCompound();
        map.forEach((u, p) -> {
            NbtCompound c = new NbtCompound();
            c.putInt("rc", p.rc);
            c.putInt("max", p.maxRc);
            c.putInt("ch", p.chapter);
            c.putBoolean("g", p.ghoul);
            c.putBoolean("k", p.kakugan);
            pl.put(u.toString(), c);
        });
        n.put("players", pl);
        return n;
    }
}
