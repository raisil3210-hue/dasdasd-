package com.tgmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public class TGMod implements ModInitializer {
    public static final String MODID = "tgmod";

    @Override
    public void onInitialize() {
        ModItems.register();
        TGCommands.register();
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Cutscene.tick(server);
            StoryManager.tick(server);
            PlayerTicker.tick(server);
        });
        ServerLivingEntityEvents.AFTER_DEATH.register((entity, source) -> StoryManager.onDeath(entity));
    }
}
