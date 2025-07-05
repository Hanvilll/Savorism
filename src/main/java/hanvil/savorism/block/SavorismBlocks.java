package hanvil.savorism.block;

import hanvil.savorism.block.entity.CookingStationBlockEntity;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static hanvil.savorism.HanvilsSavorism.id;

public class SavorismBlocks {
    //private static final AbstractBlock.TypedContextPredicate<EntityType<?>> BLOCK_SPAWNS = (state, world, pos, type) -> false;

    public static final Block COOKING_STATION_BLOCK = register("cooking_station_block", AbstractBlock.Settings.create().breakInstantly().noCollision().solidBlock(Blocks::never), CookingStationBlock::new);

    //private static <T extends Block> T register(String path, Function<AbstractBlock.Settings, T> block) {
    //    return register(path, AbstractBlock.Settings.create(), block);
    //}
    private static <T extends Block> T register(String path, AbstractBlock.Settings settings, Function<AbstractBlock.Settings, T> block) {
        return Registry.register(Registries.BLOCK, id(path), block.apply(settings.registryKey(RegistryKey.of(RegistryKeys.BLOCK, id(path)))));
    }
}