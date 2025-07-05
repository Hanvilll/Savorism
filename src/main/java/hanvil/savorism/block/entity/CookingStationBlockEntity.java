package hanvil.savorism.block.entity;

import com.mojang.serialization.Codec;
import eu.pb4.sgui.api.gui.SimpleGui;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public final class CookingStationBlockEntity extends LootableContainerBlockEntity implements TickableContents, SidedInventory {
    private static final int[] SLOTS = IntStream.range(0, 27).toArray();
    private final LongSet parts = new LongArraySet();
    private DefaultedList<ItemStack> inventory;
    private long lastTicked = -1;
    private int waterLevel = 0;
    //private int loadedTime;
    private boolean requestUpdate;

    public CookingStationBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(SavorismBlockEntities.COOKING_STATION, blockPos, blockState);
        this.inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
    }

    private void requestUpdate() {
        this.requestUpdate = true;
    }

    protected void writeData(WriteView view) {
        super.writeData(view);
        if (!this.writeLootTable(view)) {
            Inventories.writeData(view, this.inventory);
        }

        view.putLong("LastTicked", this.lastTicked);
        view.putInt("WaterLevel", this.waterLevel);
    }

    public void readData(ReadView view) {
        super.readData(view);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (!this.readLootTable(view)) {
            Inventories.readData(view, this.inventory);
        }

        this.lastTicked = view.getLong("LastTicked", 0);
        this.waterLevel = view.getInt("WaterLevel", 0);
    }

    public void tickContents(double l) {
    }

    @Override
    protected Text getContainerName() {
        return Text.literal("Cooking Station");
        //return this.material.name();
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return null;
    }

    @Override
    protected DefaultedList<ItemStack> getHeldStacks() {
        return this.inventory;
    }

    @Override
    protected void setHeldStacks(DefaultedList<ItemStack> inventory) {
        this.inventory = inventory;
    }

    @Override
    public int size() {
        return 27;
    }

    public void addPart(BlockPos pos) {
        this.parts.add(pos.asLong());
        this.markDirty();
    }

    public LongSet getParts() {
        return this.parts;
    }

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        super.onBlockReplaced(pos, oldState);

        if (this.world != null) {
            world.breakBlock(pos, true);
        }
    }

    public void openGui(ServerPlayerEntity player) {
        new Gui(player);
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
        return canInsert(stack);
    }

    public boolean canInsert(ItemStack stack) {
        return true;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return false;
    }

    private class Gui extends SimpleGui {
        public Gui(ServerPlayerEntity player) {
            super(ScreenHandlerType.GENERIC_9X3, player, false);
            this.setTitle(CookingStationBlockEntity.this.getDisplayName());

            int[] blockedSlots = {0, 3, 6, 7, 8, 9, 12, 13, 14, 15, 17, 18, 21, 23, 24, 25, 26};
            int[] ingredientSlots = {1, 2, 10, 11, 19, 20};
            int[] waterSlots = {4};
            int[] bucketSlots = {5};
            int[] bowlSlots = {22};
            int[] resultSlots = {16};

            for (int slotNumber : blockedSlots) {
                this.setSlotRedirect(slotNumber, new Slot(CookingStationBlockEntity.this, slotNumber, 0, 0) {
                    @Override
                    public boolean canInsert(ItemStack stack) {
                        return false;
                    }

                    @Override
                    public boolean canTakeItems(PlayerEntity playerEntity) {
                        return false;
                    }
                });
            }

            for (int slotNumber : ingredientSlots) {
                this.setSlotRedirect(slotNumber, new Slot(CookingStationBlockEntity.this, slotNumber, 0, 0) {
                    @Override
                    public boolean canInsert(ItemStack stack) {
                        return true;
                    }

                    @Override
                    public boolean canTakeItems(PlayerEntity playerEntity) {
                        return true;
                    }
                });
            }

            for (int slotNumber : waterSlots) {
                this.setSlotRedirect(slotNumber, new Slot(CookingStationBlockEntity.this, slotNumber, 0, 0) {
                    @Override
                    public boolean canInsert(ItemStack stack) {
                        return stack.isOf(Items.WATER_BUCKET);
                    }

                    @Override
                    public boolean canTakeItems(PlayerEntity playerEntity) {
                        return true;
                    }
                });
            }

            for (int slotNumber : bucketSlots) {
                this.setSlotRedirect(slotNumber, new Slot(CookingStationBlockEntity.this, slotNumber, 0, 0) {
                    @Override
                    public boolean canInsert(ItemStack stack) {
                        return false;
                    }

                    @Override
                    public boolean canTakeItems(PlayerEntity playerEntity) {
                        return true;
                    }
                });
            }

            for (int slotNumber : bowlSlots) {
                this.setSlotRedirect(slotNumber, new Slot(CookingStationBlockEntity.this, slotNumber, 0, 0) {
                    @Override
                    public boolean canInsert(ItemStack stack) {
                        return stack.isOf(Items.BOWL);
                    }

                    @Override
                    public boolean canTakeItems(PlayerEntity playerEntity) {
                        return true;
                    }
                });
            }

            for (int slotNumber : resultSlots) {
                this.setSlotRedirect(slotNumber, new Slot(CookingStationBlockEntity.this, slotNumber, 0, 0) {
                    @Override
                    public boolean canInsert(ItemStack stack) {
                        return false;
                    }

                    @Override
                    public boolean canTakeItems(PlayerEntity playerEntity) {
                        return true;
                    }
                });
            }

            this.open();
        }


        @Override
        public void onTick() {
            if (CookingStationBlockEntity.this.isRemoved()
                    || CookingStationBlockEntity.this.getPos().getSquaredDistanceFromCenter(this.player.getX(), this.player.getY(), this.player.getZ()) > 20 * 20) {
                this.close();
            }

            CookingStationBlockEntity.this.requestUpdate();
            super.onTick();
        }
    }
}