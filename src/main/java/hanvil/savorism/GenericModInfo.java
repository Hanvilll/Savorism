package hanvil.savorism;

import net.fabricmc.loader.api.ModContainer;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import javax.imageio.ImageIO;
import java.net.URI;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GenericModInfo {
    private static Text[] icon = new Text[0];
    private static Text[] iconBook = new Text[0];
    private static Text[] about = new Text[0];
    private static Text[] consoleAbout = new Text[0];

    public static void build(ModContainer container) {
        {
            final String chr = "█";
            var icon = new ArrayList<MutableText>();
            var iconBook = new ArrayList<MutableText>();
            try {
                {
                    var source = ImageIO.read(Files.newInputStream(container.getPath(container.getMetadata().getIconPath(16).get())));

                    for (int y = 0; y < source.getHeight(); y++) {
                        var base = Text.literal("");
                        int line = 0;
                        int color = source.getRGB(0, y) & 0xFFFFFF;
                        for (int x = 0; x < source.getWidth(); x++) {
                            int colorPixel = source.getRGB(x, y) & 0xFFFFFF;

                            if (color == colorPixel) {
                                line++;
                            } else {
                                base.append(Text.literal(chr.repeat(line)).setStyle(Style.EMPTY.withColor(color).withShadowColor(color | 0xFF000000)));
                                color = colorPixel;
                                line = 1;
                            }
                        }

                        base.append(Text.literal(chr.repeat(line)).setStyle(Style.EMPTY.withColor(color).withShadowColor(color | 0xFF000000)));
                        icon.add(base);
                    }
                }
                {
                    var source = ImageIO.read(Files.newInputStream(container.getPath("assets/brewery/icon_book.png")));

                    for (int y = 0; y < source.getHeight(); y++) {
                        var base = Text.literal("");
                        int line = 0;
                        int color = source.getRGB(0, y) & 0xFFFFFF;
                        for (int x = 0; x < source.getWidth(); x++) {
                            int colorPixel = source.getRGB(x, y) & 0xFFFFFF;

                            if (color == colorPixel) {
                                line++;
                            } else {
                                base.append(Text.literal(chr.repeat(line)).setStyle(Style.EMPTY.withColor(color).withShadowColor(color | 0xFF000000)));
                                color = colorPixel;
                                line = 1;
                            }
                        }

                        base.append(Text.literal(chr.repeat(line)).setStyle(Style.EMPTY.withColor(color).withShadowColor(color | 0xFF000000)));
                        iconBook.add(base);
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
                while (icon.size() < 16) {
                    icon.add(Text.literal("/!\\ [ Invalid icon file ] /!\\").setStyle(Style.EMPTY.withColor(0xFF0000).withItalic(true)));
                }
            }

            GenericModInfo.icon = icon.toArray(new Text[0]);
            GenericModInfo.iconBook = iconBook.toArray(new Text[0]);
        }

        {
            var about = new ArrayList<Text>();
            var aboutBasic = new ArrayList<Text>();
            var output = new ArrayList<Text>();

            try {
                about.add(Text.literal(container.getMetadata().getName()).setStyle(Style.EMPTY.withColor(0xffcc00).withBold(true).withClickEvent(new ClickEvent.OpenUrl(URI.create(container.getMetadata().getContact().get("github").orElse("https://pb4.eu"))))));
                about.add(Text.translatable("text.hanvil.about.version").setStyle(Style.EMPTY.withColor(0xf7e1a7))
                        .append(Text.literal(container.getMetadata().getVersion().getFriendlyString()).setStyle(Style.EMPTY.withColor(Formatting.WHITE))));

                aboutBasic.addAll(about);
                aboutBasic.add(Text.empty());
                aboutBasic.add(Text.of(container.getMetadata().getDescription()));

                var contributors = new ArrayList<String>();
                contributors.addAll(container.getMetadata().getAuthors().stream().map((p) -> p.getName()).collect(Collectors.toList()));
                contributors.addAll(container.getMetadata().getContributors().stream().map((p) -> p.getName()).collect(Collectors.toList()));

                about.add(Text.literal("")
                        .append(Text.translatable("text.savorism.about.contributors")
                                .setStyle(Style.EMPTY.withColor(Formatting.AQUA)
                                        .withHoverEvent(new HoverEvent.ShowText(
                                                Text.literal(String.join(", ", contributors)
                                        ))
                                )))
                        .append("")
                        .setStyle(Style.EMPTY.withColor(Formatting.DARK_GRAY)));
                about.add(Text.empty());

                var desc = new ArrayList<>(List.of(container.getMetadata().getDescription().split(" ")));

                if (desc.size() > 0) {
                    StringBuilder descPart = new StringBuilder();
                    while (!desc.isEmpty()) {
                        int appendLenght = (descPart.isEmpty() ? 0 : descPart.length() + 1) + desc.get(0).length();
                        if (appendLenght > 24 && !descPart.isEmpty()) {
                            about.add(Text.literal(descPart.toString()).setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
                            descPart = new StringBuilder();
                        }

                        (descPart.isEmpty() ? descPart : descPart.append(" ")).append(desc.remove(0));
                    }

                    if (!descPart.isEmpty()) {
                        about.add(Text.literal(descPart.toString()).setStyle(Style.EMPTY.withColor(Formatting.GRAY)));
                    }
                }

                if (icon.length > about.size() + 2) {
                    int a = 0;
                    for (int i = 0; i < icon.length; i++) {
                        if (i == (icon.length - about.size() - 1) / 2 + a && a < about.size()) {
                            output.add(icon[i].copy().append(Text.literal("  ").setStyle(Style.EMPTY.withItalic(false)).append(about.get(a++))));
                        } else {
                            output.add(icon[i]);
                        }
                    }
                } else {
                    Collections.addAll(output, icon);
                    output.addAll(about);
                }
            } catch (Throwable e) {
                e.printStackTrace();
                var invalid = Text.literal("/!\\ [ Invalid about mod info ] /!\\").setStyle(Style.EMPTY.withColor(0xFF0000).withItalic(true));

                output.add(invalid);
                about.add(invalid);
            }

            GenericModInfo.about = output.toArray(new Text[0]);
            GenericModInfo.consoleAbout = aboutBasic.toArray(new Text[0]);
        }
    }

    public static Text[] getIcon() {
        return icon;
    }

    public static Text[] getIconBook() {
        return iconBook;
    }

    public static Text[] getAboutFull() {
        return about;
    }

    public static Text[] getAboutConsole() {
        return consoleAbout;
    }
}
