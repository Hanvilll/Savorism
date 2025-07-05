package hanvil.savorism.item;

import com.mojang.serialization.Codec;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.minecraft.component.ComponentType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import static hanvil.savorism.HanvilsSavorism.id;

public class SavorismComponents {
    public static final ComponentType<Integer> BOOK_PAGE = register("book_page",
            ComponentType.<Integer>builder().codec(Codec.INT).build());

    public static final ComponentType<Integer> TICK_COUNT = register("tick_count",
            ComponentType.<Integer>builder().codec(Codec.INT).build());

    public static void register() {
    }

    private static <T> ComponentType<T> register(String path, ComponentType<T> block) {
        PolymerComponent.registerDataComponent(block);
        return Registry.register(Registries.DATA_COMPONENT_TYPE, id(path), block);
    }
}
