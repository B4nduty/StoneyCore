package banduty.stoneycore.items.itemgroup;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SCItemGroup {

    private final ResourceLocation id;
    private Supplier<ItemStack> iconSupplier = () -> ItemStack.EMPTY;
    private Component title;
    private ResourceLocation backgroundTexture;
    private ResourceLocation scrollerSprite;
    private ResourceLocation scrollerDisabledSprite;
    private ResourceLocation[] unselectedTopTabs;
    private ResourceLocation[] selectedTopTabs;
    private ResourceLocation[] unselectedBottomTabs;
    private ResourceLocation[] selectedBottomTabs;
    private final List<Consumer<CreativeModeTab.Output>> entryBuilders = new ArrayList<>();

    private SCItemGroup(ResourceLocation id) {
        this.id = id;
        this.title = Component.translatable("itemGroup." + id.getNamespace() + "." + id.getPath());
    }

    public static SCItemGroup create(ResourceLocation id) {
        return new SCItemGroup(id);
    }

    public SCItemGroup icon(Supplier<ItemStack> iconSupplier) {
        this.iconSupplier = iconSupplier;
        return this;
    }

    public SCItemGroup title(Component title) {
        this.title = title;
        return this;
    }

    public SCItemGroup backgroundTexture(ResourceLocation backgroundTexture) {
        this.backgroundTexture = backgroundTexture;
        return this;
    }

    public SCItemGroup scrollerSprites(ResourceLocation enabled, ResourceLocation disabled) {
        this.scrollerSprite = enabled;
        this.scrollerDisabledSprite = disabled;
        return this;
    }

    public SCItemGroup topTabSprites(ResourceLocation[] unselected, ResourceLocation[] selected) {
        this.unselectedTopTabs = unselected;
        this.selectedTopTabs = selected;
        return this;
    }

    public SCItemGroup bottomTabSprites(ResourceLocation[] unselected, ResourceLocation[] selected) {
        this.unselectedBottomTabs = unselected;
        this.selectedBottomTabs = selected;
        return this;
    }

    public SCItemGroup appendItems(Consumer<CreativeModeTab.Output> entries) {
        this.entryBuilders.add(entries);
        return this;
    }

    public CreativeModeTab build() {
        CreativeModeTab.Builder vanillaBuilder = CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0) // Rows/columns can be assigned during registration if needed
                .icon(this.iconSupplier)
                .title(this.title)
                .displayItems((parameters, output) -> {
                    for (Consumer<CreativeModeTab.Output> builder : entryBuilders) {
                        builder.accept(output);
                    }
                });

        if (this.backgroundTexture != null) {
            vanillaBuilder.backgroundTexture(this.backgroundTexture);
        }

        CreativeModeTab buildTab = vanillaBuilder.build();

        SCTextureData holder = (SCTextureData) buildTab;
        holder.setCustom(true);
        holder.setScrollerSprite(this.scrollerSprite);
        holder.setScrollerDisabledSprite(this.scrollerDisabledSprite);
        holder.setUnselectedTopTabs(this.unselectedTopTabs);
        holder.setSelectedTopTabs(this.selectedTopTabs);
        holder.setUnselectedBottomTabs(this.unselectedBottomTabs);
        holder.setSelectedBottomTabs(this.selectedBottomTabs);

        return buildTab;
    }
}