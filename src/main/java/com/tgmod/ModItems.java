package com.tgmod;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item UKAKU = reg("kagune_ukaku", new KaguneItem(KaguneItem.Type.UKAKU));
    public static final Item KOUKAKU = reg("kagune_koukaku", new KaguneItem(KaguneItem.Type.KOUKAKU));
    public static final Item RINKAKU = reg("kagune_rinkaku", new KaguneItem(KaguneItem.Type.RINKAKU));
    public static final Item BIKAKU = reg("kagune_bikaku", new KaguneItem(KaguneItem.Type.BIKAKU));
    public static final Item CENTIPEDE = reg("kagune_centipede", new KaguneItem(KaguneItem.Type.CENTIPEDE));
    public static final Item EYEPATCH = reg("eyepatch", new EyepatchItem(new FabricItemSettings().maxCount(1)));
    public static final Item MASK = reg("ghoul_mask", new Item(new FabricItemSettings().maxCount(1)));
    public static final Item COFFEE = reg("anteiku_coffee", new RcFood(new FabricItemSettings().food(
            new FoodComponent.Builder().hunger(1).saturationModifier(0.2f).alwaysEdible().build()), 15, true));
    public static final Item FLESH = reg("ghoul_flesh", new RcFood(new FabricItemSettings().food(
            new FoodComponent.Builder().hunger(6).saturationModifier(0.8f).meat().alwaysEdible().build()), 45, false));

    private static Item reg(String id, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(TGMod.MODID, id), item);
    }

    public static void register() {
        Registry.register(Registries.ITEM_GROUP, new Identifier(TGMod.MODID, "main"),
                FabricItemGroup.builder()
                        .icon(() -> new ItemStack(RINKAKU))
                        .displayName(Text.translatable("itemGroup.tgmod.main"))
                        .entries((ctx, e) -> {
                            e.add(UKAKU); e.add(KOUKAKU); e.add(RINKAKU); e.add(BIKAKU); e.add(CENTIPEDE);
                            e.add(EYEPATCH); e.add(MASK); e.add(COFFEE); e.add(FLESH);
                        }).build());
    }
}
