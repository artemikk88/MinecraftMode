package com.hasturian.repairforge.screen;

import com.hasturian.repairforge.RepairForgeMod;
import com.hasturian.repairforge.block.entity.RepairForgeBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public class RepairForgeScreenHandler extends ScreenHandler {

    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;

    /** Клиентский конструктор (вызывается из ScreenHandlerType). */
    public RepairForgeScreenHandler(int syncId, PlayerInventory playerInventory) {
        this(syncId, playerInventory, new SimpleInventory(3),
                new ArrayPropertyDelegate(RepairForgeBlockEntity.PROP_COUNT));
    }

    /** Серверный конструктор. */
    public RepairForgeScreenHandler(int syncId, PlayerInventory playerInventory,
                                    Inventory inventory, PropertyDelegate propertyDelegate) {
        super(ModScreenHandlers.REPAIR_FORGE, syncId);
        checkSize(inventory, 3);
        checkDataCount(propertyDelegate, RepairForgeBlockEntity.PROP_COUNT);
        this.inventory = inventory;
        this.propertyDelegate = propertyDelegate;
        inventory.onOpen(playerInventory.player);

        // Координаты слотов совпадают с GUI ванильной печки — используем её текстуру как заглушку.
        addSlot(new Slot(inventory, RepairForgeBlockEntity.INPUT_SLOT, 56, 17) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return RepairForgeMod.isRepairable(stack);
            }

            @Override
            public int getMaxItemCount() {
                return 1;
            }
        });
        addSlot(new Slot(inventory, RepairForgeBlockEntity.FUEL_SLOT, 56, 53) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return RepairForgeBlockEntity.isFuel(stack);
            }
        });
        addSlot(new Slot(inventory, RepairForgeBlockEntity.OUTPUT_SLOT, 116, 35) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return false;
            }
        });

        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        addProperties(propertyDelegate);
    }

    // ------------------------------------------------------------ данные для экрана

    public boolean isBurning() {
        return propertyDelegate.get(RepairForgeBlockEntity.PROP_BURN_TIME) > 0;
    }

    /** Высота огонька 0..13 как у печки. */
    public int getFuelProgress() {
        int fuelTime = propertyDelegate.get(RepairForgeBlockEntity.PROP_FUEL_TIME);
        if (fuelTime == 0) fuelTime = 200;
        return propertyDelegate.get(RepairForgeBlockEntity.PROP_BURN_TIME) * 13 / fuelTime;
    }

    /** Ширина стрелки 0..24: сколько процентов прочности уже восстановлено. */
    public int getRepairProgress() {
        ItemStack stack = inventory.getStack(RepairForgeBlockEntity.INPUT_SLOT);
        if (stack.isEmpty() || !stack.isDamageable()) return 0;
        int max = stack.getMaxDamage();
        if (max <= 0) return 0;
        return (max - stack.getDamage()) * 24 / max;
    }

    // ------------------------------------------------------------ стандарт

    @Override
    public boolean canUse(PlayerEntity player) {
        return inventory.canPlayerUse(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slotIndex) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = slots.get(slotIndex);
        if (slot != null && slot.hasStack()) {
            ItemStack original = slot.getStack();
            newStack = original.copy();

            if (slotIndex < 3) {
                // Из горна — в инвентарь игрока
                if (!insertItem(original, 3, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickTransfer(original, newStack);
            } else {
                // Из инвентаря игрока — в подходящий слот горна
                if (RepairForgeMod.isRepairable(original) && original.getDamage() > 0) {
                    if (!insertItem(original, 0, 1, false)) return ItemStack.EMPTY;
                } else if (RepairForgeBlockEntity.isFuel(original)) {
                    if (!insertItem(original, 1, 2, false)) return ItemStack.EMPTY;
                } else if (slotIndex < 30) {
                    if (!insertItem(original, 30, 39, false)) return ItemStack.EMPTY;
                } else if (!insertItem(original, 3, 30, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (original.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
            if (original.getCount() == newStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTakeItem(player, original);
        }
        return newStack;
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        inventory.onClose(player);
    }

    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int col = 0; col < 9; ++col) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }
}
