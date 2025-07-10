package hanvil.savorism.block.entity;

import eu.pb4.sgui.api.gui.SimpleGui;
import hanvil.savorism.HanvilsSavorism;
import hanvil.savorism.item.Recipe;
import hanvil.savorism.item.Recipes;
import net.fabricmc.fabric.api.item.v1.FabricItemStack;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.component.ComponentType;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import net.minecraft.component.DataComponentTypes;

import java.util.*;
import java.util.stream.IntStream;

public final class CookingStationBlockEntity extends LootableContainerBlockEntity implements TickableContents, SidedInventory {
    private static final int[] SLOTS = IntStream.range(0, 27).toArray();

    public static final int[] BLOCKED_SLOTS = {0, 3, 6, 7, 8, 9, 12, 13, 14, 15, 17, 18, 21, 23, 24, 25, 26};
    public static final int[] INGREDIETN_SLOTS = {1, 2, 10, 11, 19, 20};
    public static final int WATER_BUCKET_SLOT = 4;
    public static final int BUCKET_SLOT = 5;
    public static final int BOWL_SLOT = 22;
    public static final int RESULT_SLOT = 16;

    private DefaultedList<ItemStack> inventory;
    private long lastTicked = -1;
    private int waterLevel = 0;
    private boolean requestUpdate;

    private boolean isOnCraft = false;
    private int tickUntilCraft;
    private Recipe recipe;

    public CookingStationBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(SavorismBlockEntities.COOKING_STATION, blockPos, blockState);
        this.inventory = DefaultedList.ofSize(27, ItemStack.EMPTY);
    }

    public static <T extends BlockEntity> void ticker(World world, BlockPos pos, BlockState state, T t) {
        if (!(t instanceof CookingStationBlockEntity)) {
            return;
        }

        var cookingStation = (CookingStationBlockEntity) t;

        if (cookingStation.requestUpdate) {
            cookingStation.updateAges();
        }
    }

    private void requestUpdate() {
        this.requestUpdate = true;
    }

    public void updateAges() {
        var currentTime = world.getTime();


        if (this.lastTicked == -1) {
            this.lastTicked = world.getTime();
            return;
        }

        this.tickContents(currentTime - this.lastTicked);

        this.lastTicked = currentTime;
        this.requestUpdate = false;
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
        if (this.isOnCraft) {
            this.nextCraftIteration(l);
        }
        else if (this.waterLevel > 0 || this.tryAddWaterLevel()) {
            Recipe availableRecipe = this.getAvailableRecipe();
            if (availableRecipe != null && this.isResultSlotAvailable(availableRecipe)) {
                this.startCraftProcess(availableRecipe);
            }
        }
        ItemStack stack = new ItemStack(Items.MACE, 1);

        ArrayList<String> z = new ArrayList<>();

        z.add("bat");
        CustomModelDataComponent tag = new CustomModelDataComponent(new ArrayList<Float>(), new ArrayList<Boolean>(), z, new ArrayList<Integer>());
        //NbtList list = new NbtList();
        //list.add(NbtString.of("bat"));
        //tag.strings().add("bat");
        stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, tag);

        //NbtComponent.set(DataComponentTypes.CUSTOM_MODEL_DATA, stack, (NbtCompound tag2) -> tag2.put("strings", list));

        //stack.set(DataComponentTypes.CUSTOM_DATA, tag);
        //NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, tag);

        //NbtComponent.set(DataComponentTypes.CUSTOM_DATA, stack, (NbtCompound tag) -> tag.putInt("CustomModelData", 42));

        super.setStack(12, stack);
        /*NbtComponent.set(
                ComponentType.builder(),
                stack,
                (NbtCompound tag) -> tag.putInt("CustomModelData", 2)
        );*/
    }

    private boolean tryAddWaterLevel() {
        ItemStack waterBucketStack = this.getStack(WATER_BUCKET_SLOT);
        ItemStack emptyBucketStack = this.getStack(BUCKET_SLOT);
        if (waterBucketStack.getItem() == Items.WATER_BUCKET) {
            int count = emptyBucketStack.getCount() + 1;;
            if (count > Items.BUCKET.getMaxCount())
                return false;

            this.waterLevel = 16;
            super.setStack(WATER_BUCKET_SLOT, ItemStack.EMPTY);
            super.setStack(BUCKET_SLOT, new ItemStack(Items.BUCKET, count));
            this.updateWaterLevelPreview();

            return true;
        }

        return false;
    }

    private boolean removeWaterLevel() {
        if (this.waterLevel > 0) {
            this.waterLevel--;
            this.updateWaterLevelPreview();
            return true;
        }

        return false;
    }

    private void updateWaterLevelPreview() {
        if (this.waterLevel > 0) {
            super.setStack(12, new ItemStack(Items.ICE, this.waterLevel));
        }
        else {
            super.setStack(12, new ItemStack(Items.BARRIER, 1));
        }
    }

    private void updateCraftLevelPreview(int tickUntilCraft) {
        super.setStack(26, new ItemStack(Items.STRUCTURE_VOID, tickUntilCraft));
    }

    @Nullable
    private Recipe getAvailableRecipe() {
        HashMap<Item, Integer> ingredientMap = new HashMap<>();

        for (int slot : INGREDIETN_SLOTS) {
            ItemStack stack = this.getStack(slot);
            if (!stack.isEmpty()) {
                Item stackItem = stack.getItem();
                Integer stackCount = stack.getCount();
                if (ingredientMap.containsKey(stackItem)){
                    ingredientMap.merge(stackItem, stackCount, Integer::sum);
                }
                else {
                    ingredientMap.put(stackItem, stackCount);
                }
            }
        }

        Map<Item, Integer> recipeKey = null;
        for (Map<Item, Integer> key : Recipes.ALL_COOKING_STATION_RECIPES.keySet()) {
            boolean isValidRecipe = true;
            for (Map.Entry<Item, Integer> requirement : key.entrySet()) {
                int existent = ingredientMap.getOrDefault(requirement.getKey(), 0);
                if (existent < requirement.getValue()) {
                    isValidRecipe = false;
                    break;
                }
            }

            if (isValidRecipe) {
                recipeKey = key;
            }
        }

        if (recipeKey != null) {
            return new Recipe(recipeKey, Recipes.ALL_COOKING_STATION_RECIPES.get(recipeKey).copy());
        }

        return null;
    }

    private boolean isResultSlotAvailable(Recipe recipe) {
        ItemStack resultStack = this.getStack(RESULT_SLOT);

        if (resultStack.isEmpty()) {
            return true;
        }
        else {
            if (resultStack.getItem() == recipe.GetResult().getItem()) {
                if (resultStack.getCount() + recipe.GetResult().getCount() <= recipe.GetResult().getItem().getMaxCount()) {
                    return true;
                }
                return false;
            }
            return false;
        }
    }

    private boolean takeAwayIngredients(Recipe recipe) {
        HashMap<Integer, Integer> itemsTotake = new HashMap<>();
        for (Item recipeItem : recipe.GetIngredients().keySet()) {
            int necessaryCount = recipe.GetIngredients().get(recipeItem);

            for (int slot : INGREDIETN_SLOTS) {
                ItemStack ingredient = this.getStack(slot);
                Item ingredientItem = ingredient.getItem();

                if (recipeItem == ingredientItem) {
                    int ingredientCount = ingredient.getCount();
                    if (ingredientCount >= necessaryCount) {
                        itemsTotake.put(slot, necessaryCount);
                        necessaryCount = 0;
                        break;
                    }
                    itemsTotake.put(slot, ingredientCount);
                    necessaryCount -= ingredientCount;
                }
            }

            if (necessaryCount > 0) {
                return false;
            }
        }

        for (int slot : itemsTotake.keySet()) {
            ItemStack slotStack = this.getStack(slot);
            int changedValue = slotStack.getCount() - itemsTotake.get(slot);
            if (changedValue < 0) {
                super.setStack(slot, ItemStack.EMPTY);
            }
            else {
                super.setStack(slot, new ItemStack(slotStack.getItem(), changedValue));
            }
        }

        return true;
    }

    private void startCraftProcess(Recipe recipe) {
        this.recipe = recipe;
        this.tickUntilCraft = 60;
        this.isOnCraft = true;
    }

    private boolean checkCraftAvailability() {
        if (!this.isOnCraft) {
            return false;
        }
        for (Item recipeItem : this.recipe.GetIngredients().keySet()) {
            int necessaryCount = this.recipe.GetIngredients().get(recipeItem);

            for (int slot : INGREDIETN_SLOTS) {
                ItemStack ingredient = this.getStack(slot);
                Item ingredientItem = ingredient.getItem();

                if (recipeItem == ingredientItem) {
                    int ingredientCount = ingredient.getCount();
                    if (ingredientCount >= necessaryCount) {
                        necessaryCount = 0;
                        break;
                    }
                    necessaryCount -= ingredientCount;
                }
            }
            if (necessaryCount > 0) {
                this.terminateCraftProcess();
                return false;
            }
        }


        return true;
    }

    private void terminateCraftProcess() {
        this.recipe = null;
        this.tickUntilCraft = 0;
        this.isOnCraft = false;

        super.setStack(26, new ItemStack(Items.BARRIER, 1));
    }

    private void nextCraftIteration(double l) {
        while (l > 60) {
            if (!this.craft()) {
                return;
            }

            l -= 60;
        }

        this.tickUntilCraft -= (int) l;

        if (this.tickUntilCraft > 0) {
            this.updateCraftLevelPreview(this.tickUntilCraft);
        } else {
            this.craft();
        }
    }

    private boolean craft() {
        if (this.takeAwayIngredients(this.recipe) && this.removeWaterLevel()) {
            if (this.getStack(RESULT_SLOT).isEmpty()) {
                super.setStack(RESULT_SLOT, this.recipe.GetResult());
            }
            else {
                int count1 = this.recipe.GetResult().getCount();
                int count2 = this.getStack(RESULT_SLOT).getCount();
                ItemStack res = new ItemStack(this.recipe.GetResult().getItem(), count1+count2);
                super.setStack(RESULT_SLOT, res);
            }

            if (this.checkCraftAvailability()) {
                this.startCraftProcess(this.recipe);
            }
            this.updateCraftLevelPreview(1);
            return true;
        }

        this.terminateCraftProcess();
        this.updateCraftLevelPreview(1);
        return false;
    }

    private void checkItemChanges() {
        if (this.isOnCraft) {
            this.checkCraftAvailability();
        }
        else if (this.waterLevel > 0 || this.tryAddWaterLevel()) {
            Recipe availableRecipe = this.getAvailableRecipe();
            if (availableRecipe != null && this.isResultSlotAvailable(availableRecipe)) {
                this.startCraftProcess(availableRecipe);
            }
        }
    }

    @Override
    protected Text getContainerName() {
        return Text.literal("Cooking Station");
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
        return false;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return false;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack returnValue = super.removeStack(slot, amount);

        this.checkItemChanges();

        return returnValue;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack returnValue = super.removeStack(slot);

        this.checkItemChanges();

        return returnValue;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        super.setStack(slot, stack);

        this.checkItemChanges();
    }

    private class Gui extends SimpleGui {
        public Gui(ServerPlayerEntity player) {
            super(ScreenHandlerType.GENERIC_9X3, player, false);
            this.setTitle(CookingStationBlockEntity.this.getDisplayName());


            for (int slotNumber : BLOCKED_SLOTS) {
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

            for (int slotNumber : INGREDIETN_SLOTS) {
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

            this.setSlotRedirect(WATER_BUCKET_SLOT, new Slot(CookingStationBlockEntity.this, WATER_BUCKET_SLOT, 0, 0) {
                @Override
                public boolean canInsert(ItemStack stack) {
                    return stack.isOf(Items.WATER_BUCKET);
                }

                @Override
                public boolean canTakeItems(PlayerEntity playerEntity) {
                    return true;
                }
            });

            this.setSlotRedirect(BUCKET_SLOT, new Slot(CookingStationBlockEntity.this, BUCKET_SLOT, 0, 0) {
                @Override
                public boolean canInsert(ItemStack stack) {
                    return false;
                }

                @Override
                public boolean canTakeItems(PlayerEntity playerEntity) {
                    return true;
                }
            });

            this.setSlotRedirect(BOWL_SLOT, new Slot(CookingStationBlockEntity.this, BOWL_SLOT, 0, 0) {
                @Override
                public boolean canInsert(ItemStack stack) {
                    return stack.isOf(Items.BOWL);
                }

                @Override
                public boolean canTakeItems(PlayerEntity playerEntity) {
                    return true;
                }
            });

            this.setSlotRedirect(RESULT_SLOT, new Slot(CookingStationBlockEntity.this, RESULT_SLOT, 0, 0) {
                @Override
                public boolean canInsert(ItemStack stack) {
                    return false;
                }

                @Override
                public boolean canTakeItems(PlayerEntity playerEntity) {
                    return true;
                }
            });

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