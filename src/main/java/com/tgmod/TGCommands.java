package com.tgmod;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class TGCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> dispatcher.register(
            CommandManager.literal("tg")
                .then(CommandManager.literal("start").executes(c -> { StoryManager.start(c.getSource().getPlayerOrThrow()); return 1; }))
                .then(CommandManager.literal("next").executes(c -> { StoryManager.next(c.getSource().getPlayerOrThrow()); return 1; }))
                .then(CommandManager.literal("chapter").then(CommandManager.argument("n", IntegerArgumentType.integer(0, 9))
                    .executes(c -> { StoryManager.startChapter(c.getSource().getPlayerOrThrow(), IntegerArgumentType.getInteger(c, "n")); return 1; })))
                .then(CommandManager.literal("status").executes(c -> {
                    ServerPlayerEntity p = c.getSource().getPlayerOrThrow();
                    TGData.P d = TGData.get(p.getServer()).of(p);
                    p.sendMessage(Text.literal("\u00a76Capitulo: " + d.chapter + " | Ghoul: " + d.ghoul + " | RC: " + d.rc + "/" + d.maxRc), false);
                    return 1; }))
                .then(CommandManager.literal("kakugan").executes(c -> { PlayerTicker.toggleKakugan(c.getSource().getPlayerOrThrow()); return 1; }))
                .then(CommandManager.literal("ghoul").requires(s -> s.hasPermissionLevel(2)).executes(c -> {
                    ServerPlayerEntity p = c.getSource().getPlayerOrThrow();
                    TGData.P d = TGData.get(p.getServer()).of(p);
                    d.ghoul = !d.ghoul;
                    p.sendMessage(Text.literal("Ghoul: " + d.ghoul), false);
                    return 1; }))
                .then(CommandManager.literal("give").requires(s -> s.hasPermissionLevel(2)).executes(c -> {
                    ServerPlayerEntity p = c.getSource().getPlayerOrThrow();
                    for (var i : new net.minecraft.item.Item[]{ModItems.UKAKU, ModItems.KOUKAKU, ModItems.RINKAKU, ModItems.BIKAKU,
                            ModItems.CENTIPEDE, ModItems.EYEPATCH, ModItems.MASK, ModItems.COFFEE, ModItems.FLESH})
                        p.giveItemStack(new ItemStack(i, i == ModItems.COFFEE || i == ModItems.FLESH ? 16 : 1));
                    return 1; }))
                .then(CommandManager.literal("build").requires(s -> s.hasPermissionLevel(2))
                    .then(CommandManager.argument("nome", StringArgumentType.word())
                        .suggests((ctx, b) -> { Structures.NAMES.forEach(b::suggest); return b.buildFuture(); })
                        .executes(c -> {
                            ServerPlayerEntity p = c.getSource().getPlayerOrThrow();
                            Structures.build(p.getServerWorld(), StringArgumentType.getString(c, "nome"), p.getBlockPos().add(12, 0, 0));
                            return 1; })))
        ));
    }
}
