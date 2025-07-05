package hanvil.savorism.block.entity;

import eu.pb4.polymer.core.api.block.PolymerBlockUtils;
import hanvil.savorism.block.SavorismBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import static hanvil.savorism.HanvilsSavorism.id;

public class SavorismBlockEntities {
    public static BlockEntityType<CookingStationBlockEntity> COOKING_STATION;

    public static void register() {
        COOKING_STATION = register("cooking_station", FabricBlockEntityTypeBuilder.create(CookingStationBlockEntity::new, SavorismBlocks.COOKING_STATION_BLOCK).build());
    }

    private static <T extends BlockEntity> BlockEntityType<T> register(String path, BlockEntityType<T> block) {
        Registry.register(Registries.BLOCK_ENTITY_TYPE, id(path), block);
        PolymerBlockUtils.registerBlockEntity(block);
        return block;
    }
}