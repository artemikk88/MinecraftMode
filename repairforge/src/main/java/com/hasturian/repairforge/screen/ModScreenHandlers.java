package com.hasturian.repairforge.screen;

import com.hasturian.repairforge.RepairForgeMod;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.screen.ScreenHandlerType;

public class ModScreenHandlers {

    public static final ScreenHandlerType<RepairForgeScreenHandler> REPAIR_FORGE =
            new ScreenHandlerType<>(RepairForgeScreenHandler::new, FeatureFlags.VANILLA_FEATURES);

    public static void register() {
        Registry.register(Registries.SCREEN_HANDLER, RepairForgeMod.id("repair_forge"), REPAIR_FORGE);
    }
}
