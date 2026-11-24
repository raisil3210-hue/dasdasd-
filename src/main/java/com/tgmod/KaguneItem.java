package com.tgmod;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class KaguneItem extends Item {
    public enum Type {
        UKAKU(15, 30, "Ukaku: rajada de projeteis"),
        KOUKAKU(20, 60, "Koukaku: escudo cristalino"),
        RINKAKU(25, 40, "Rinkaku: tentaculos em area"),
        BIKAKU(20, 50, "Bikaku: investida veloz"),
        CENTIPEDE(45, 80, "Centopeia: 4 tentaculos (One-Eyed)");
        public final int cost, cooldown;
        public final String desc;
        Type(int cost, int cooldown, String desc) { this.cost = cost; this.cooldown = cooldown; this.desc = desc; }
    }

    private final Type type;

    public KaguneItem(Type t) {
        super(new net.fabricmc.fabric.api.item.v1.FabricItemSettings().maxCount(1));
        this.type = t;
    }

    @Override
    public TypedActionResult<ItemStack> use(World w, PlayerEntity p, Hand h) {
        ItemStack st = p.getStackInHand(h);
        if (w.isClient || !(p instanceof ServerPlayerEntity sp)) return TypedActionResult.pass(st);
        TGData.P d = TGData.get(sp.getServer()).of(sp);
        if (!d.ghoul) {
            sp.sendMessage(Text.literal("\u00a7cVoce nao e um ghoul."), true);
            return TypedActionResult.fail(st);
        }
        if (d.rc < type.cost) {
            sp.sendMessage(Text.literal("\u00a7cRC insuficiente! Coma (\u00a7fRefeicao de Ghoul\u00a7c) ou tome cafe."), true);
            return TypedActionResult.fail(st);
        }
        d.rc -= type.cost;
        Kagunes.cast(sp, type);
        sp.getItemCooldownManager().set(this, type.cooldown);
        sp.sendMessage(Text.literal("\u00a74" + type.desc), true);
        return TypedActionResult.success(st);
    }
}
