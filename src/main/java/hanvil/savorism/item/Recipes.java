    package hanvil.savorism.item;

    import net.minecraft.item.Item;
    import net.minecraft.item.ItemStack;
    import net.minecraft.item.Items;

    import java.util.ArrayList;
    import java.util.HashMap;
    import java.util.Map;


    public class Recipes {
        public static final HashMap<Map<Item, Integer>, ItemStack> ALL_COOKING_STATION_RECIPES = Recipes.generateCookingStationRecipes();

        private static HashMap<Map<Item, Integer>, ItemStack> generateCookingStationRecipes() {
            HashMap<Map<Item, Integer>, ItemStack> resultMap = new HashMap<>();

            Map<Item, Integer> key1 = Map.of(Items.IRON_INGOT, 1);
            Map<Item, Integer> key2 = Map.of(Items.STICK, 2);
            Map<Item, Integer> key3 = Map.of(Items.COAL, 1, Items.IRON_ORE, 1);
            Map<Item, Integer> key4 = Map.of(Items.AMETHYST_SHARD, 16);

            ItemStack r_key1 = new ItemStack(Items.DIAMOND, 1);
            ItemStack r_key2 = new ItemStack(Items.OAK_PLANKS, 1);
            ItemStack r_key3 = new ItemStack(Items.NETHERITE_INGOT, 1);
            ItemStack r_key4 = new ItemStack(Items.EMERALD, 16);

            resultMap.put(key1, r_key1);
            resultMap.put(key2, r_key2);
            resultMap.put(key3, r_key3);
            resultMap.put(key4, r_key4);

            return resultMap;
        }
    }
