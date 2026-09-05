package com.hasturian.repairforge.block.entity;

import com.hasturian.repairforge.RepairForgeMod;
import com.hasturian.repairforge.block.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class ModBlockEntities {

    public static BlockEntityType<RepairForgeBlockEntity> REPAIR_FORGE;

    public static void register() {
        REPAIR_FORGE = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                RepairForgeMod.id("repair_forge"),
                FabricBlockEntityTypeBuilder
                        .create(RepairForgeBlockEntity::new, ModBlocks.REPAIR_FORGE)
                        .build()
        );
    }
}
