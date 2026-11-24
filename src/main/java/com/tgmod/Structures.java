package com.tgmod;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.List;
import java.util.Random;

/** Estruturas geradas por codigo (sem NBT). Melhor usar mundo superplano. */
public class Structures {
    public static final List<String> NAMES = List.of("anteiku", "construcao", "beco", "ruinas", "restaurante", "cela", "aogiri", "ccg");

    public static void build(ServerWorld w, String name, BlockPos o) {
        switch (name) {
            case "anteiku" -> anteiku(w, o);
            case "construcao" -> construcao(w, o);
            case "beco" -> beco(w, o);
            case "ruinas" -> ruinas(w, o);
            case "restaurante" -> restaurante(w, o);
            case "cela" -> cela(w, o);
            case "aogiri" -> aogiri(w, o);
            case "ccg" -> ccg(w, o);
        }
    }

    // ---- helpers ----
    static void fill(ServerWorld w, BlockPos o, int x1, int y1, int z1, int x2, int y2, int z2, Block b) {
        BlockState s = b.getDefaultState();
        for (int x = x1; x <= x2; x++) for (int y = y1; y <= y2; y++) for (int z = z1; z <= z2; z++)
            w.setBlockState(o.add(x, y, z), s, Block.NOTIFY_LISTENERS);
    }
    static void set(ServerWorld w, BlockPos o, int x, int y, int z, Block b) {
        w.setBlockState(o.add(x, y, z), b.getDefaultState(), Block.NOTIFY_LISTENERS);
    }
    static void clear(ServerWorld w, BlockPos o, int sx, int sy, int sz) { fill(w, o, 0, 0, 0, sx, sy, sz, Blocks.AIR); }
    /** caixa oca: paredes + interior vazio. */
    static void shell(ServerWorld w, BlockPos o, int x1, int y1, int z1, int x2, int y2, int z2, Block wall) {
        fill(w, o, x1, y1, z1, x2, y2, z2, wall);
        fill(w, o, x1 + 1, y1, z1 + 1, x2 - 1, y2, z2 - 1, Blocks.AIR);
    }

    // ---- estruturas ----
    static void anteiku(ServerWorld w, BlockPos o) {
        clear(w, o, 14, 8, 10);
        fill(w, o, 0, -1, 0, 13, -1, 9, Blocks.SPRUCE_PLANKS);
        shell(w, o, 0, 0, 0, 13, 4, 9, Blocks.BRICKS);
        fill(w, o, 0, 5, 0, 13, 5, 9, Blocks.DARK_OAK_PLANKS);
        fill(w, o, 3, 1, 0, 10, 3, 0, Blocks.GLASS);
        fill(w, o, 6, 0, 0, 7, 1, 0, Blocks.AIR); // porta
        fill(w, o, 2, 0, 3, 9, 0, 3, Blocks.POLISHED_ANDESITE); // balcao
        set(w, o, 3, 1, 3, Blocks.BREWING_STAND);
        set(w, o, 5, 1, 3, Blocks.CAULDRON);
        set(w, o, 7, 1, 3, Blocks.LANTERN);
        for (int x : new int[]{3, 6, 9}) { set(w, o, x, 0, 6, Blocks.OAK_FENCE); set(w, o, x, 1, 6, Blocks.OAK_PRESSURE_PLATE); }
        set(w, o, 1, 4, 1, Blocks.LANTERN);
        set(w, o, 12, 4, 8, Blocks.LANTERN);
    }

    static void construcao(ServerWorld w, BlockPos o) {
        Random r = new Random(o.asLong());
        clear(w, o, 16, 14, 16);
        fill(w, o, 0, -1, 0, 15, -1, 15, Blocks.GRAY_CONCRETE);
        for (int x : new int[]{0, 7, 15}) for (int z : new int[]{0, 7, 15}) fill(w, o, x, 0, z, x, 11, z, Blocks.IRON_BLOCK);
        fill(w, o, 0, 4, 0, 15, 4, 15, Blocks.GRAY_CONCRETE);
        fill(w, o, 0, 9, 0, 15, 9, 15, Blocks.GRAY_CONCRETE);
        fill(w, o, 5, 4, 5, 9, 4, 9, Blocks.AIR);  // buracos
        fill(w, o, 5, 9, 5, 9, 9, 9, Blocks.AIR);
        for (int i = 0; i < 40; i++) {
            int x = r.nextInt(16), z = r.nextInt(16);
            set(w, o, x, 0, z, r.nextBoolean() ? Blocks.COBBLESTONE : Blocks.GRAVEL);
        }
        fill(w, o, 3, 5, 3, 3, 8, 3, Blocks.SCAFFOLDING);
        fill(w, o, 12, 5, 12, 12, 8, 12, Blocks.SCAFFOLDING);
        fill(w, o, 7, 5, 0, 7, 8, 0, Blocks.IRON_BARS);
    }

    static void beco(ServerWorld w, BlockPos o) {
        Random r = new Random(o.asLong());
        clear(w, o, 7, 10, 24);
        fill(w, o, 0, -1, 0, 6, -1, 24, Blocks.STONE_BRICKS);
        fill(w, o, 0, 0, 0, 0, 8, 24, Blocks.STONE_BRICKS);
        fill(w, o, 6, 0, 0, 6, 8, 24, Blocks.STONE_BRICKS);
        for (int z = 2; z < 24; z += 6) { set(w, o, 1, 3, z, Blocks.LANTERN); set(w, o, 5, 3, z, Blocks.LANTERN); }
        for (int i = 0; i < 8; i++) set(w, o, 1 + r.nextInt(5), 0, r.nextInt(24), Blocks.BARREL);
    }

    static void ruinas(ServerWorld w, BlockPos o) {
        Random r = new Random(o.asLong());
        clear(w, o, 20, 9, 20);
        for (int x = 0; x < 20; x++) for (int z = 0; z < 20; z++)
            if (r.nextInt(4) != 0) set(w, o, x, -1, z, r.nextBoolean() ? Blocks.COBBLESTONE : Blocks.MOSSY_COBBLESTONE);
        for (int x = 0; x < 20; x++) {
            int h1 = 2 + r.nextInt(6), h2 = 2 + r.nextInt(6);
            fill(w, o, x, 0, 0, x, h1, 0, Blocks.STONE_BRICKS);
            fill(w, o, x, 0, 19, x, h2, 19, Blocks.CRACKED_STONE_BRICKS);
        }
        for (int z = 0; z < 20; z++) {
            fill(w, o, 0, 0, z, 0, 2 + r.nextInt(6), z, Blocks.STONE_BRICKS);
            fill(w, o, 19, 0, z, 19, 2 + r.nextInt(6), z, Blocks.CRACKED_STONE_BRICKS);
        }
        for (int i = 0; i < 15; i++) set(w, o, 2 + r.nextInt(16), 0, 2 + r.nextInt(16), Blocks.COBBLESTONE);
    }

    static void restaurante(ServerWorld w, BlockPos o) {
        clear(w, o, 16, 8, 12);
        fill(w, o, 0, -1, 0, 15, -1, 11, Blocks.BLACK_CONCRETE);
        shell(w, o, 0, 0, 0, 15, 5, 11, Blocks.BLACK_CONCRETE);
        fill(w, o, 0, 6, 0, 15, 6, 11, Blocks.BLACK_CONCRETE);
        fill(w, o, 6, 0, 0, 9, 1, 0, Blocks.AIR);
        fill(w, o, 7, 0, 1, 8, 0, 10, Blocks.RED_CARPET);
        fill(w, o, 4, 0, 4, 11, 0, 7, Blocks.DARK_OAK_PLANKS);
        for (int x = 4; x <= 11; x += 2) set(w, o, x, 1, 5, Blocks.RED_CANDLE);
        for (int y = 3; y <= 5; y++) set(w, o, 7, y, 5, Blocks.CHAIN);
        set(w, o, 7, 2, 5, Blocks.SOUL_LANTERN);
    }

    static void cela(ServerWorld w, BlockPos o) {
        clear(w, o, 9, 7, 9);
        fill(w, o, 0, -1, 0, 8, -1, 8, Blocks.DEEPSLATE_TILES);
        shell(w, o, 0, 0, 0, 8, 4, 8, Blocks.DEEPSLATE_BRICKS);
        fill(w, o, 0, 5, 0, 8, 5, 8, Blocks.DEEPSLATE_BRICKS);
        fill(w, o, 4, 0, 0, 4, 1, 0, Blocks.IRON_BARS); // porta gradeada
        for (int z = 1; z < 8; z += 3) set(w, o, 0, 2, z, Blocks.IRON_BARS);
        set(w, o, 4, 3, 4, Blocks.CHAIN); set(w, o, 4, 4, 4, Blocks.CHAIN);
        set(w, o, 4, 2, 4, Blocks.SOUL_LANTERN);
    }

    static void aogiri(ServerWorld w, BlockPos o) {
        clear(w, o, 26, 34, 26);
        fill(w, o, 0, -1, 0, 25, -1, 25, Blocks.COARSE_DIRT);
        for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
            if (Math.abs(dx) == 2 && Math.abs(dz) == 2) continue;
            fill(w, o, 13 + dx, 0, 13 + dz, 13 + dx, 30, 13 + dz, Blocks.DARK_OAK_LOG);
        }
        for (int y : new int[]{8, 16, 24}) {
            int rad = 9 - (y / 8);
            for (int dx = -rad; dx <= rad; dx++) for (int dz = -rad; dz <= rad; dz++)
                if (dx * dx + dz * dz <= rad * rad) set(w, o, 13 + dx, y, 13 + dz, Blocks.BONE_BLOCK);
            for (int a = 0; a < 360; a += 45) set(w, o, 13 + (int) Math.round(Math.cos(Math.toRadians(a)) * rad), y + 1, 13 + (int) Math.round(Math.sin(Math.toRadians(a)) * rad), Blocks.SOUL_LANTERN);
        }
        for (int dx = -7; dx <= 7; dx++) for (int dz = -7; dz <= 7; dz++)
            if (dx * dx + dz * dz <= 49) set(w, o, 13 + dx, 31, 13 + dz, Blocks.DARK_OAK_LEAVES);
    }

    static void ccg(ServerWorld w, BlockPos o) {
        clear(w, o, 17, 33, 17);
        fill(w, o, 0, -1, 0, 16, -1, 16, Blocks.SMOOTH_STONE);
        shell(w, o, 0, 0, 0, 16, 29, 16, Blocks.QUARTZ_BLOCK);
        for (int y = 4; y <= 28; y += 5) { fill(w, o, 0, y, 0, 16, y, 0, Blocks.SEA_LANTERN); fill(w, o, 0, y - 2, 0, 16, y - 1, 0, Blocks.GLASS); }
        fill(w, o, 7, 0, 0, 9, 2, 0, Blocks.AIR);
        for (int y = 5; y <= 25; y += 5) fill(w, o, 1, y, 1, 15, y, 15, Blocks.QUARTZ_BLOCK);
        fill(w, o, 0, 30, 0, 16, 30, 16, Blocks.WHITE_CONCRETE);
        fill(w, o, 6, 30, 6, 10, 30, 10, Blocks.YELLOW_CONCRETE);
    }
}
