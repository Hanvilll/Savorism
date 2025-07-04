package hanvil.savorism;

import hanvil.savorism.item.BookOfSavorismItem;
import hanvil.savorism.item.SavorismItems;
import hanvil.savorism.item.SavorismComponents;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import eu.pb4.polymer.common.api.PolymerCommonUtils;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import it.unimi.dsi.fastutil.objects.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class HanvilsSavorism implements ModInitializer {
	//public static final String MOD_ID = "hanvils-savorism";
	public static final String MOD_ID = "hanvils-savorism";

	public static final Map<Item, Identifier> CONTAINER_TO_INGMIX_MODEL = new IdentityHashMap<>();
	public static Ingredient containerIngredient = Ingredient.ofItems(Items.GLASS_BOTTLE);

	public static final Logger LOGGER = LogUtils.getLogger();

	public static final boolean IS_DEV = FabricLoader.getInstance().isDevelopmentEnvironment();
	public static final boolean DISPLAY_DEV = IS_DEV && true;
	public static final boolean USE_GENERATOR = IS_DEV && true;

	private static ServerWorld overworld = null;

	public static Identifier id(String path) {
		return Identifier.of(MOD_ID, path);
	}

	@Nullable
	public static ServerWorld getOverworld() {
		return overworld;
	}

	@Override
	public void onInitialize() {
		GenericModInfo.build(FabricLoader.getInstance().getModContainer(MOD_ID).get());
		PolymerResourcePackUtils.addModAssets(HanvilsSavorism.MOD_ID);

		SavorismItems.register();
		SavorismComponents.register();
		//BrewBlocks.register();
		//BrewComponents.register();
		//BrewBlockEntities.register();
		//BrewItems.register();
		//BrewGameRules.register();
		//BrewNetworking.register();

		var id = id("early_reload");

		ServerLifecycleEvents.SERVER_STARTED.addPhaseOrdering(id, Event.DEFAULT_PHASE);
		ServerLifecycleEvents.SERVER_STARTED.register(id, HanvilsSavorism::onServerStarted);
		ServerLifecycleEvents.SERVER_STOPPED.register((s) -> {
			overworld = null;
		});
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.addPhaseOrdering(id, Event.DEFAULT_PHASE);


		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register(id, (x, y, z) -> {
			//HanvilsSavorism.loadDrinks(x);
			BookOfSavorismItem.build();
		});


		//CommandRegistrationCallback.EVENT.register(BrewCommands::register);

		//UseBlockCallback.EVENT.register(BrewCauldronBlock::handleUseEvent);

		/*if (FabricLoader.getInstance().isModLoaded("polydex")) {
			PolydexCompatImpl.init();
		}*/
	}

	public static void clearData() {
		CONTAINER_TO_INGMIX_MODEL.clear();
		containerIngredient = Ingredient.ofItems(Items.GLASS_BOTTLE);
	}


	private static void onServerStarted(MinecraftServer server) {
		overworld = server.getOverworld();

		BookOfSavorismItem.build();
	}

}