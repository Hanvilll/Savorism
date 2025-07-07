package hanvil.savorism.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class Recipe {
    private Map<Item, Integer> ingredients;
    private ItemStack result;

    public Map<Item, Integer> GetIngredients() {
        return this.ingredients;
    }

    public ItemStack GetResult() {
        return this.result.copy();
    }

    public Recipe(Map<Item, Integer> ingredients, ItemStack result) {
        this.ingredients = ingredients;
        this.result = result;
    }
}
