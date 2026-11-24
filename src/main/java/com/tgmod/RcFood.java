package com.tgmod;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

/** Comida de ghoul: recupera RC. O cafe tambem avanca o capitulo do Anteiku. */
public class RcFood extends Item {
    private final int rc;
    private final boolean coffee;

    public RcFood(Settings s, int rc, boolean coffee) {
        super(s);
        this.rc = rc;
        this.coffee = coffee;
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World w, LivingEntity user) {
        ItemStack r = super.finishUsing(stack, w, user);
        if (!w.isClient && user instanceof ServerPlayerEntity sp) {
            TGData.P d = TGData.get(sp.getServer()).of(sp);
            if (d.ghoul) d.rc = Math.min(d.maxRc, d.rc + rc);
            if (coffee) StoryManager.onCoffee(sp);
        }
        return r;
    }
}
