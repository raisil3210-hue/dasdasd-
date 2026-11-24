package com.tgmod;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

/** Botao direito: liga/desliga o Kakugan. */
public class EyepatchItem extends Item {
    public EyepatchItem(Settings s) { super(s); }

    @Override
    public TypedActionResult<ItemStack> use(World w, PlayerEntity p, Hand h) {
        ItemStack st = p.getStackInHand(h);
        if (!w.isClient && p instanceof ServerPlayerEntity sp) PlayerTicker.toggleKakugan(sp);
        return TypedActionResult.success(st);
    }
}
