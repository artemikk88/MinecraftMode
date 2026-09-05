package com.hasturian.repairforge.block;

import com.hasturian.repairforge.RepairForgeMod;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModBlocks {

    public static final Block REPAIR_FORGE = new RepairForgeBlock(
            FabricBlockSettings.copyOf(Blocks.FURNACE)
                    .luminance(state -> state.get(RepairForgeBlock.LIT) ? 13 : 0)
    );

    public static void register() {
        Registry.register(Registries.BLOCK, RepairForgeMod.id("repair_forge"), REPAIR_FORGE);
        Registry.register(Registries.ITEM, RepairForgeMod.id("repair_forge"),
                new BlockItem(REPAIR_FORGE, new Item.Settings()));

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
                .register(entries -> entries.add(REPAIR_FORGE));
    }
}
