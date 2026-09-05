package com.hasturian.repairforge.block.entity;

import com.hasturian.repairforge.RepairForgeMod;
import com.hasturian.repairforge.block.RepairForgeBlock;
import com.hasturian.repairforge.screen.RepairForgeScreenHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class RepairForgeBlockEntity extends BlockEntity
        implements NamedScreenHandlerFactory, ImplementedInventory, SidedInventory {

    public static final int INPUT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;

    /** Сколько тиков горения нужно на 1 единицу прочности. 2 тика = 10 прочности/сек. */
    public static final int TICKS_PER_DURABILITY = 2;

    // Индексы PropertyDelegate (синхронизируются с клиентом автоматически)
    public static final int PROP_BURN_TIME = 0;
    public static final int PROP_FUEL_TIME = 1;
    public static final int PROP_COUNT = 2;

    private static final int[] TOP_SLOTS = {INPUT_SLOT};
    private static final int[] BOTTOM_SLOTS = {OUTPUT_SLOT, FUEL_SLOT};
    private static final int[] SIDE_SLOTS = {FUEL_SLOT};

    private static Map<Item, Integer> fuelTimes;

    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(3, ItemStack.EMPTY);

    private int burnTime;        // сколько тиков ещё горит текущее топливо
    private int fuelTime;        // полное время горения текущего топлива (для индикатора)
    private int repairTicks;     // накопленные тики до следующей единицы прочности

    private final PropertyDelegate propertyDelegate = new PropertyDelegate() {
        @Override
        public int get(int index) {
            return switch (index) {
                case PROP_BURN_TIME -> burnTime;
                case PROP_FUEL_TIME -> fuelTime;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case PROP_BURN_TIME -> burnTime = value;
                case PROP_FUEL_TIME -> fuelTime = value;
            }
        }

        @Override
        public int size() {
            return PROP_COUNT;
        }
    };

    public RepairForgeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.REPAIR_FORGE, pos, state);
    }

    // ------------------------------------------------------------ топливо

    /**
     * Используем ванильную карту топлива. Fabric API (FuelRegistry) подмешивает в неё
     * топливо из других модов Prominence, так что всё, что горит в печке, горит и здесь.
     */
    public static int getFuelTime(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (fuelTimes == null) {
            fuelTimes = AbstractFurnaceBlockEntity.createFuelTimeMap();
        }
        return fuelTimes.getOrDefault(stack.getItem(), 0);
    }

    public static boolean isFuel(ItemStack stack) {
        return getFuelTime(stack) > 0;
    }

    public boolean isBurning() {
        return burnTime > 0;
    }

    // ---------------------------------------------------------------- тик

    public static void tick(World world, BlockPos pos, BlockState state, RepairForgeBlockEntity be) {
        if (world.isClient) return;

        boolean wasBurning = be.isBurning();
        boolean dirty = false;

        if (be.isBurning()) {
            be.burnTime--;
        }

        ItemStack input = be.getStack(INPUT_SLOT);
        ItemStack fuel = be.getStack(FUEL_SLOT);
        boolean needsRepair = needsRepair(input);

        // 1. Разжигаем новое топливо, только если есть что чинить.
        if (!be.isBurning() && needsRepair && !fuel.isEmpty()) {
            int time = getFuelTime(fuel);
            if (time > 0) {
                be.burnTime = time;
                be.fuelTime = time;
                dirty = true;

                Item fuelItem = fuel.getItem();
                fuel.decrement(1);
                if (fuel.isEmpty()) {
                    Item remainder = fuelItem.getRecipeRemainder(); // ведро от лавы и т.п.
                    be.setStack(FUEL_SLOT, remainder == null ? ItemStack.EMPTY : new ItemStack(remainder));
                }
            }
        }

        // 2. Чиним, пока горит.
        if (be.isBurning() && needsRepair) {
            be.repairTicks++;
            if (be.repairTicks >= TICKS_PER_DURABILITY) {
                be.repairTicks = 0;
                input.setDamage(input.getDamage() - 1);
                dirty = true;
            }
        } else {
            be.repairTicks = 0;
        }

        // 3. Полностью починенный предмет перекладываем в выходной слот.
        if (!input.isEmpty() && input.isDamageable() && input.getDamage() <= 0
                && be.getStack(OUTPUT_SLOT).isEmpty()) {
            be.setStack(OUTPUT_SLOT, input.copy());
            be.setStack(INPUT_SLOT, ItemStack.EMPTY);
            dirty = true;
        }

        // 4. Обновляем состояние LIT (свечение + текстура).
        if (wasBurning != be.isBurning()) {
            dirty = true;
            state = state.with(RepairForgeBlock.LIT, be.isBurning());
            world.setBlockState(pos, state, Block.NOTIFY_ALL);
        }

        if (dirty) {
            markDirty(world, pos, state);
        }
    }

    private static boolean needsRepair(ItemStack stack) {
        return RepairForgeMod.isRepairable(stack) && stack.getDamage() > 0;
    }

    // ------------------------------------------------------------ инвентарь

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public int getMaxCountPerStack() {
        return 64;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return switch (slot) {
            case INPUT_SLOT -> RepairForgeMod.isRepairable(stack) && getStack(INPUT_SLOT).isEmpty();
            case FUEL_SLOT -> isFuel(stack);
            default -> false;
        };
    }

    @Override
    public void markDirty() {
        if (world != null) {
            markDirty(world, pos, getCachedState());
        }
    }

    // --- SidedInventory: воронки. Сверху — предметы, сбоку — топливо, снизу — забирать результат.

    @Override
    public int[] getAvailableSlots(Direction side) {
        if (side == Direction.DOWN) return BOTTOM_SLOTS;
        if (side == Direction.UP) return TOP_SLOTS;
        return SIDE_SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return isValid(slot, stack);
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        if (dir == Direction.DOWN && slot == FUEL_SLOT) {
            // Из топливного слота вниз отдаём только пустые вёдра, как ванильная печка.
            return stack.isOf(net.minecraft.item.Items.BUCKET);
        }
        return slot == OUTPUT_SLOT;
    }

    // ---------------------------------------------------------------- GUI

    @Override
    public Text getDisplayName() {
        return Text.translatable("container.repairforge.repair_forge");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new RepairForgeScreenHandler(syncId, playerInventory, this, propertyDelegate);
    }

    // ---------------------------------------------------------------- NBT

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);
        nbt.putInt("BurnTime", burnTime);
        nbt.putInt("FuelTime", fuelTime);
        nbt.putInt("RepairTicks", repairTicks);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        inventory.clear();
        Inventories.readNbt(nbt, inventory);
        burnTime = nbt.getInt("BurnTime");
        fuelTime = nbt.getInt("FuelTime");
        repairTicks = nbt.getInt("RepairTicks");
    }
}
