package com.hasturian.repairforge;

import com.hasturian.repairforge.block.ModBlocks;
import com.hasturian.repairforge.block.entity.ModBlockEntities;
import com.hasturian.repairforge.screen.ModScreenHandlers;
import net.fabricmc.api.ModInitializer;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.item.Item;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepairForgeMod implements ModInitializer {
    public static final String MOD_ID = "repairforge";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    /** Предметы из этого тега горн чинить откажется (удобно для баланса модпака). */
    public static final TagKey<Item> REPAIR_BLACKLIST =
            TagKey.of(RegistryKeys.ITEM, id("repair_blacklist"));

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    /** Единая проверка "можно ли вообще положить этот предмет в горн". */
    public static boolean isRepairable(ItemStack stack) {
        return !stack.isEmpty()
                && stack.isDamageable()
                && !stack.isIn(REPAIR_BLACKLIST);
    }

    @Override
    public void onInitialize() {
        ModBlocks.register();
        ModBlockEntities.register();
        ModScreenHandlers.register();
        LOGGER.info("[Repair Forge] Ремонтный горн разожжён.");
    }
}
