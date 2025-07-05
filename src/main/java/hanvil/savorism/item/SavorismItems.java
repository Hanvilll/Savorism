package hanvil.savorism.item;

import hanvil.savorism.block.SavorismBlocks;
import static hanvil.savorism.HanvilsSavorism.id;

import eu.pb4.polymer.core.api.item.PolymerBlockItem;
import eu.pb4.polymer.core.api.item.PolymerItemGroupUtils;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

import java.util.function.Function;

public class SavorismItems {
    public static final Item BOOK_ITEM = register("book_of_savorism", BookOfSavorismItem::new);

    public static final PolymerBlockItem COOKING_STATION = register("cooking_station", (s) -> new PolymerBlockItem(
            SavorismBlocks.COOKING_STATION_BLOCK, s.maxCount(64).useBlockPrefixedTranslationKey(), Items.SMOKER
    ));

    public static final ItemGroup ITEM_GROUP = ItemGroup.create(null, -1)
            .displayName(Text.literal("Savorism"))
            .icon(Items.GOLDEN_CARROT::getDefaultStack)

            .entries((ctx, e) -> {
                e.add(BOOK_ITEM);
                e.add(COOKING_STATION);
            })
            .build();

    public static void register() {
        PolymerItemGroupUtils.registerPolymerItemGroup(id("items"), ITEM_GROUP);
    }

    private static <T extends Item> T register(String path, Function<Item.Settings, T> block) {
        return Registry.register(Registries.ITEM, id(path), block.apply(new Item.Settings().registryKey(RegistryKey.of(RegistryKeys.ITEM, id(path)))));
    }
}
