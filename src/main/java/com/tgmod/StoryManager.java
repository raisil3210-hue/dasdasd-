package com.tgmod;

import com.tgmod.Cutscene.Step;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.mob.VindicatorEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;
import java.util.function.Consumer;

/**
 * Historia: Tokyo Ghoul T1 (caps 0-6) e T2 / root A (caps 7-9), do ponto de vista do Kaneki.
 * Adaptacao livre e resumida para o jogo.
 */
public class StoryManager {
    enum Kind { AUTO, BOSS, COFFEE }

    record Chapter(String name, String season, String objective, Kind kind, String structure,
                   String boss, int hp, int dmg, int minions,
                   List<Step> intro, List<Step> outro, Consumer<ServerPlayerEntity> reward) {}

    static class Active {
        UUID player; ServerBossBar bar; Chapter ch; int idx, ticks; boolean scripted; ServerWorld world;
        List<UUID> minions = new ArrayList<>();
    }

    static final List<Chapter> CH = new ArrayList<>();
    static final Map<UUID, Active> BOSSES = new HashMap<>();

    // ---------- helpers de cutscene ----------
    static Step t(int d, String title, String sub) { return new Step(d, title, sub, null, 0, 0, false, null); }
    static Step ts(int d, String title, String sub, String sound) { return new Step(d, title, sub, null, 0, 0, false, sound); }
    static Step dark(int d, String title, String sub) { return new Step(d, title, sub, null, 0, 0, true, "minecraft:ambient.soul_sand_valley.mood"); }
    static Step cam(int d, String title, String sub, double x, double y, double z, float yaw, float pitch) {
        return new Step(d, title, sub, new Vec3d(x, y, z), yaw, pitch, false, null);
    }
    static List<Step> L(Step... s) { return Arrays.asList(s); }
    static void give(ServerPlayerEntity p, Item i, int n) { p.giveItemStack(new ItemStack(i, n)); }
    static TGData.P pd(ServerPlayerEntity p) { return TGData.get(p.getServer()).of(p); }

    static {
        // ===================== TEMPORADA 1 =====================
        CH.add(new Chapter("Prologo: Kaneki Ken e Hide", "T1", "Assista a cutscene.", Kind.AUTO, "anteiku", null, 0, 0, 0,
                L(cam(70, "\u00a7fTokyo, 20a ala", "Um estudante comum, Kaneki Ken, ama livros e cafe.", 4, 3, -6, -30, 10),
                  cam(70, "\u00a77Hide", "Seu melhor amigo avisa: ghouls estao devorando pessoas na cidade.", -2, 3, 4, 120, 5),
                  cam(80, "\u00a7dRize Kamishiro", "Uma bela leitora do Anteiku chama sua atencao...", 8, 2, 6, 200, 0),
                  t(60, "\u00a77Ela o convida para um encontro.", "")),
                L(), p -> { give(p, Items.BOOK, 1); }));

        CH.add(new Chapter("O Acidente de Rize", "T1", "Sobreviva a Rize. Algo vai cair sobre ela...", Kind.BOSS, "construcao", "Rize Kamishiro", 120, 6, 0,
                L(ts(70, "\u00a74Noite", "Rize o leva a uma obra abandonada.", "minecraft:ambient.cave"),
                  ts(70, "\u00a74\"Voce parece delicioso.\"", "Ela revela sua verdadeira forma: uma ghoul.", "minecraft:entity.wither.ambient"),
                  t(50, "\u00a7cFUJA OU LUTE!", "")),
                L(cam(60, "\u00a78Estruturas de aco desabam.", "Rize e atingida.", 6, 3, -8, 20, 10),
                  dark(80, "\u00a77Hospital", "Os cirurgioes transplantam orgaos de Rize para salvar sua vida."),
                  dark(80, "\u00a74Voce acorda diferente.", "Agora e meio-ghoul. Nada mais tem sabor... so carne.")),
                p -> {
                    TGData.P d = pd(p); d.ghoul = true; d.rc = d.maxRc;
                    give(p, ModItems.RINKAKU, 1); give(p, ModItems.FLESH, 3);
                    p.sendMessage(Text.literal("\u00a74Voce obteve o Kagune Rinkaku de Rize. Segure-o e use (botao direito)."), false);
                }));

        CH.add(new Chapter("Anteiku", "T1", "Beba um Cafe do Anteiku.", Kind.COFFEE, "anteiku", null, 0, 0, 0,
                L(ts(70, "\u00a76Anteiku", "Um cafe tranquilo na 20a ala.", "minecraft:block.bell.use"),
                  t(70, "\u00a7fYoshimura", "O gerente o acolhe: ghouls tambem podem viver em paz."),
                  t(70, "\u00a79Touka Kirishima", "Uma garcom de olhar duro lhe ensina a esconder a fome.")),
                L(t(60, "\u00a76\"Aqui, voce nao precisa estar sozinho.\"", "Voce agora trabalha no Anteiku.")),
                p -> { give(p, ModItems.COFFEE, 3); give(p, ModItems.MASK, 1); }));

        CH.add(new Chapter("Nishiki Nishio", "T1", "Derrote Nishiki no beco.", Kind.BOSS, "beco", "Nishiki Nishio", 80, 5, 0,
                L(t(60, "\u00a7cNishiki", "Um ghoul arrogante que odeia o Anteiku... e voce."),
                  t(60, "\u00a74\"Aquele cheiro de meio-ghoul...\"", "Primeira luta de verdade do Kaneki.")),
                L(t(70, "\u00a7fVoce vence, mas ve o custo da luta.", "A fome ainda esta la.")),
                p -> { give(p, ModItems.FLESH, 4); give(p, ModItems.COFFEE, 2); }));

        CH.add(new Chapter("Hinami e Amon", "T1", "Derrote o Investigador Amon (CCG).", Kind.BOSS, "ruinas", "Koutarou Amon", 140, 6, 2,
                L(t(70, "\u00a7bHinami", "Uma garotinha ghoul perdida, procurada pela CCG."),
                  t(70, "\u00a7cCCG", "A Comissao de Combate a Ghouls cerca as ruinas."),
                  ts(60, "\u00a74\"Proteja-a!\"", "", "minecraft:entity.vindicator.celebrate")),
                L(t(70, "\u00a7fHinami e salva.", "Mas a guerra entre ghouls e humanos so esta comecando.")),
                p -> { pd(p).maxRc += 20; give(p, ModItems.FLESH, 3); }));

        CH.add(new Chapter("Gourmet Tsukiyama", "T1", "Derrote Shuu Tsukiyama.", Kind.BOSS, "restaurante", "Shuu Tsukiyama", 110, 6, 0,
                L(t(70, "\u00a75Tsukiyama", "Um ghoul gourmet obcecado por voce."),
                  ts(70, "\u00a75\"Que aroma...\"", "O jantar vai virar uma cacada.", "minecraft:block.note_block.harp")),
                L(t(60, "\u00a7fVoce sobrevive ao jantar.", "Seu corpo ja nao e o mesmo.")),
                p -> { pd(p).maxRc += 20; give(p, ModItems.BIKAKU, 1); }));

        CH.add(new Chapter("Yamori e a Cela", "T1", "Sobreviva e derrote Yamori (Jason).", Kind.BOSS, "cela", "Yamori (Jason)", 200, 8, 0,
                L(dark(70, "\u00a78Sequestro", "Kaneki acorda preso numa cela do Aogiri."),
                  dark(80, "\u00a74Dias sem fim", "Dor e escuridao. Rize sussurra na sua mente."),
                  dark(80, "\u00a7f1000 - 7", "Ele conta... para nao enlouquecer."),
                  ts(70, "\u00a74\"Se e para ser devorado...\"", "\"...entao devore!\"", "minecraft:entity.wither.spawn")),
                L(t(60, "\u00a7fKaneki vence.", "Seus cabelos ficam brancos."),
                  t(70, "\u00a74Um novo Kaneki nasce.", "O kagune Centopeia desperta."),
                  t(70, "\u00a7f== FIM DA TEMPORADA 1 ==", "")),
                p -> {
                    TGData.P d = pd(p); d.maxRc += 60; d.rc = d.maxRc;
                    give(p, ModItems.CENTIPEDE, 1); give(p, ModItems.EYEPATCH, 1);
                    p.sendMessage(Text.literal("\u00a74Voce ganhou o Kagune Centopeia e o Tapa-olho (Kakugan: botao direito)."), false);
                }));

        // ===================== TEMPORADA 2 (root A) =====================
        CH.add(new Chapter("Aogiri Tree", "T2", "Assista a cutscene.", Kind.AUTO, "aogiri", null, 0, 0, 0,
                L(t(70, "\u00a7f== TEMPORADA 2: root A ==", ""),
                  cam(80, "\u00a78Kaneki se afasta de todos.", "Decidido a nunca mais ser fraco.", 14, 4, -14, 330, 10),
                  cam(80, "\u00a74Aogiri Tree", "Ele se une ao grupo que o torturou... para proteger quem ama.", -6, 6, 10, 70, 5),
                  t(80, "\u00a7fO Olho Unico", "O Eyepatch aparece.")),
                L(), p -> give(p, ModItems.MASK, 1)));

        CH.add(new Chapter("Operacao contra o Anteiku", "T2", "Derrote o esquadrao da CCG e o Lider Hairu.", Kind.BOSS, "anteiku", "Lider Hairu (CCG)", 150, 7, 5,
                L(t(70, "\u00a7cCCG", "A CCG decide eliminar o Anteiku."),
                  t(70, "\u00a76Touka", "Ela e os outros resistem de dentro do cafe."),
                  ts(60, "\u00a74O Eyepatch chega.", "", "minecraft:entity.ender_dragon.growl")),
                L(t(70, "\u00a7fO cafe cai, mas nao a esperanca.", "Kaneki entende o preco de proteger.")),
                p -> { pd(p).maxRc += 40; give(p, ModItems.KOUKAKU, 1); give(p, ModItems.UKAKU, 1); }));

        CH.add(new Chapter("Arima Kishou (Final)", "T2", "Derrote o Ceifador da CCG.", Kind.BOSS, "ccg", "Arima Kishou", 300, 10, 0,
                L(t(70, "\u00a7fArima Kishou", "O investigador mais forte da CCG."),
                  ts(70, "\u00a74\"Um ghoul de um olho so...\"", "", "minecraft:block.beacon.activate"),
                  ts(70, "\u00a74Kaneki vs Arima", "O confronto final.", "minecraft:entity.wither.spawn")),
                L(t(70, "\u00a7fA batalha termina.", "O preco foi alto."),
                  t(80, "\u00a74Kaneki Ken, o Olho Unico.", "Nem humano, nem ghoul. Ele e os dois."),
                  t(80, "\u00a7f== FIM DA TEMPORADA 2 ==", "Obrigado por jogar!")),
                p -> { TGData.P d = pd(p); d.maxRc += 100; d.rc = d.maxRc; give(p, ModItems.FLESH, 8); }));
    }

    // ---------- API ----------
    public static void start(ServerPlayerEntity sp) {
        TGData.P d = pd(sp);
        d.ghoul = false; d.kakugan = false; d.rc = 100; d.maxRc = 100;
        startChapter(sp, 0);
    }

    public static void next(ServerPlayerEntity sp) { startChapter(sp, pd(sp).chapter + 1); }

    public static void startChapter(ServerPlayerEntity sp, int idx) {
        if (idx < 0 || idx >= CH.size()) { sp.sendMessage(Text.literal("\u00a7eNao ha mais capitulos."), false); return; }
        if (Cutscene.isPlaying(sp)) return;
        TGData.P d = pd(sp);
        d.chapter = idx;
        Chapter c = CH.get(idx);
        BlockPos origin = sp.getBlockPos().add(12, 0, 0);
        if (c.structure != null) Structures.build(sp.getServerWorld(), c.structure, origin);
        sp.sendMessage(Text.literal("\u00a76[" + c.season + "] Cap. " + idx + ": \u00a7f" + c.name), false);
        sp.sendMessage(Text.literal("\u00a77Objetivo: " + c.objective), false);
        Cutscene.play(sp, c.intro, Vec3d.ofBottomCenter(sp.getBlockPos()), () -> afterIntro(sp, idx));
    }

    private static void afterIntro(ServerPlayerEntity sp, int idx) {
        Chapter c = CH.get(idx);
        if (c.kind == Kind.AUTO) complete(sp, idx);
        else if (c.kind == Kind.BOSS) spawnBoss(sp, idx);
    }

    public static void onCoffee(ServerPlayerEntity sp) {
        TGData.P d = pd(sp);
        if (d.chapter >= 0 && d.chapter < CH.size() && CH.get(d.chapter).kind == Kind.COFFEE) complete(sp, d.chapter);
    }

    private static void complete(ServerPlayerEntity sp, int idx) {
        Chapter c = CH.get(idx);
        Cutscene.play(sp, c.outro, Vec3d.ofBottomCenter(sp.getBlockPos()), () -> {
            c.reward.accept(sp);
            sp.sendMessage(Text.literal("\u00a7a\u2714 Capitulo concluido! Use \u00a7f/tg next \u00a7apara continuar."), false);
        });
    }

    private static void spawnBoss(ServerPlayerEntity sp, int idx) {
        Chapter c = CH.get(idx);
        ServerWorld w = sp.getServerWorld();
        Vec3d look = sp.getRotationVec(1f).multiply(7, 0, 7);
        VindicatorEntity boss = makeMob(w, c.boss, c.hp, c.dmg, "\u00a7c", sp.getPos().add(look));
        boss.equipStack(EquipmentSlot.HEAD, new ItemStack(ModItems.MASK));
        boss.setGlowing(true);
        Active a = new Active();
        a.player = sp.getUuid(); a.ch = c; a.idx = idx; a.world = w;
        a.bar = new ServerBossBar(Text.literal("\u00a7c" + c.boss), BossBar.Color.RED, BossBar.Style.PROGRESS);
        a.bar.addPlayer(sp);
        for (int i = 0; i < c.minions; i++) {
            VindicatorEntity m = makeMob(w, "Investigador CCG", 30, 4, "\u00a77", sp.getPos().add(look).add(i * 1.5 - 3, 0, 2));
            m.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            m.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
            a.minions.add(m.getUuid());
        }
        BOSSES.put(boss.getUuid(), a);
        sp.sendMessage(Text.literal("\u00a7c\u2694 " + c.boss + " apareceu!"), false);
    }

    private static VindicatorEntity makeMob(ServerWorld w, String name, int hp, int dmg, String color, Vec3d pos) {
        VindicatorEntity e = EntityType.VINDICATOR.create(w);
        e.refreshPositionAndAngles(pos.x, pos.y, pos.z, 0, 0);
        e.setCustomName(Text.literal(color + name));
        e.setCustomNameVisible(true);
        e.setPersistent();
        e.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH).setBaseValue(hp);
        e.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE).setBaseValue(dmg);
        e.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED).setBaseValue(0.34);
        e.getAttributeInstance(EntityAttributes.GENERIC_FOLLOW_RANGE).setBaseValue(40);
        e.setHealth(hp);
        e.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
        e.setEquipmentDropChance(EquipmentSlot.MAINHAND, 0f);
        e.setEquipmentDropChance(EquipmentSlot.HEAD, 0f);
        e.setEquipmentDropChance(EquipmentSlot.CHEST, 0f);
        w.spawnEntity(e);
        return e;
    }

    public static void onDeath(LivingEntity entity) {
        Active a = BOSSES.remove(entity.getUuid());
        if (a == null) return;
        MinecraftServer s = entity.getServer();
        if (s == null) return;
        a.bar.clearPlayers();
        for (UUID m : a.minions) { Entity e = a.world.getEntity(m); if (e != null) e.discard(); }
        ServerPlayerEntity sp = s.getPlayerManager().getPlayer(a.player);
        if (sp != null) complete(sp, a.idx);
    }

    private static void abort(Active a, UUID bossId, String msg, MinecraftServer s) {
        BOSSES.remove(bossId);
        a.bar.clearPlayers();
        Entity b = a.world.getEntity(bossId);
        if (b != null) b.discard();
        for (UUID m : a.minions) { Entity e = a.world.getEntity(m); if (e != null) e.discard(); }
        ServerPlayerEntity sp = s.getPlayerManager().getPlayer(a.player);
        if (sp != null) sp.sendMessage(Text.literal(msg), false);
    }

    public static void tick(MinecraftServer s) {
        for (Map.Entry<UUID, Active> en : new ArrayList<>(BOSSES.entrySet())) {
            Active a = en.getValue();
            a.ticks++;
            ServerPlayerEntity pl = s.getPlayerManager().getPlayer(a.player);
            if (pl == null || !pl.isAlive()) { abort(a, en.getKey(), "\u00a7cVoce caiu... use /tg chapter " + a.idx + " para tentar de novo.", s); continue; }
            Entity be = a.world.getEntity(en.getKey());
            if (!(be instanceof LivingEntity boss)) { BOSSES.remove(en.getKey()); a.bar.clearPlayers(); continue; }
            a.bar.setPercent(Math.max(0f, boss.getHealth() / boss.getMaxHealth()));
            if (a.ticks % 4 == 0)
                a.world.spawnParticles(ParticleTypes.CRIMSON_SPORE, boss.getX(), boss.getY() + 1.2, boss.getZ(), 6, 0.4, 0.6, 0.4, 0.0);
            // cena do acidente: Rize e esmagada pelas vigas
            if (a.idx == 1 && !a.scripted && boss.getHealth() <= boss.getMaxHealth() * 0.35f) {
                a.scripted = true;
                a.world.spawnParticles(ParticleTypes.EXPLOSION, boss.getX(), boss.getY() + 2, boss.getZ(), 6, 1, 1, 1, 0);
                boss.damage(a.world.getDamageSources().genericKill(), 1000f);
                continue;
            }
            // ataque especial: golpe de kagune em area
            if (a.ticks % 80 == 0 && pl.squaredDistanceTo(boss) < 49) {
                pl.damage(a.world.getDamageSources().mobAttack(boss), a.ch.dmg() * 0.6f);
                pl.takeKnockback(1.0, boss.getX() - pl.getX(), boss.getZ() - pl.getZ());
                a.world.spawnParticles(ParticleTypes.SWEEP_ATTACK, boss.getX(), boss.getY() + 1, boss.getZ(), 10, 2, 0.3, 2, 0);
            }
        }
    }
}
