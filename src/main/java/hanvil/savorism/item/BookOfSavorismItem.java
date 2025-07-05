package hanvil.savorism.item;

import hanvil.savorism.HanvilsSavorism;
import hanvil.savorism.GenericModInfo;
import hanvil.savorism.other.BrewUtils;

import eu.pb4.polymer.core.api.item.PolymerItem;
import eu.pb4.sgui.api.elements.BookElementBuilder;
import eu.pb4.sgui.api.gui.BookGui;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.Person;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.*;
import net.minecraft.util.*;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BookOfSavorismItem extends Item implements PolymerItem {
    public BookOfSavorismItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity player) {
            new Gui(player, hand).open();
            return ActionResult.SUCCESS_SERVER;
        }

        return super.use(world, user, hand);
    }

    @Override
    public Item getPolymerItem(ItemStack itemStack, PacketContext context) {
        return Items.WRITTEN_BOOK;
    }

    @Override
    public @Nullable Identifier getPolymerItemModel(ItemStack stack, PacketContext context) {
        return null;
    }

    public static void build() {
        var builder = new BookElementBuilder();
        Gui.BOOKS.clear();

        builder.addPage(
                Texts.join(List.of(GenericModInfo.getIconBook()), Text.literal("\n")),
                Text.empty(),
                Text.empty().append(Text.translatable("item.savorism.book_of_savorism")
                                .formatted(Formatting.BOLD, Formatting.UNDERLINE, Formatting.BLUE))

                        .append(Text.literal(" \uD83E\uDDEA").formatted(Formatting.DARK_RED)),
                Text.empty(),
                Text.literal("Savorism by Hanvil").formatted(Formatting.DARK_GRAY)
        );

        builder.addPage(
                Text.translatable("hanvil.savorism.cooking_station").formatted(Formatting.BOLD, Formatting.UNDERLINE, Formatting.GREEN),
                Text.empty(),
                Text.translatable("hanvil.savorism.cooking_station.text")
        );

        builder.setComponent(DataComponentTypes.WRITTEN_BOOK_CONTENT, builder.getComponent(DataComponentTypes.WRITTEN_BOOK_CONTENT).asResolved());

        Gui.indexBook = builder.asStack();
    }

    public static final class Gui extends BookGui {
        public static final List<ItemStack> BOOKS = new ArrayList<>();

        public static ItemStack indexBook;
        private final ItemStack stack;
        private final Hand hand;

        public Gui(ServerPlayerEntity player, Hand hand) {
            super(player, indexBook);
            this.stack = player.getStackInHand(hand);
            this.hand = hand;
            this.setPage(Math.min(stack.getOrDefault(SavorismComponents.BOOK_PAGE, 0),
                    indexBook.get(DataComponentTypes.WRITTEN_BOOK_CONTENT).getPages(false).size()));
        }

        @Override
        public void onTakeBookButton() {
            if (this.book != indexBook) {
                this.player.playSoundToPlayer(SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.BLOCKS, 1f, 1);
                var page = this.stack.getOrDefault(SavorismComponents.BOOK_PAGE, 0);
                this.book = indexBook;
                this.screenHandler.sendContentUpdates();
                this.setPage(page);
            } else {
                this.close();
            }
        }

        @Override
        public void setPage(int page) {
            this.player.playSoundToPlayer(SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.BLOCKS, 1f, 1);
            if (page >= 1000 && BOOKS.size() > page - 1000) {
                this.book = BOOKS.get(page - 1000);
                this.screenHandler.sendContentUpdates();
                super.setPage(0);
                return;
            }

            super.setPage(page);
            if (this.book == indexBook && this.stack == this.player.getStackInHand(hand)) {
                this.stack.set(SavorismComponents.BOOK_PAGE, page);
            }
        }
    }
}