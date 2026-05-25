package banduty.stoneycore.mixin;

import banduty.stoneycore.items.itemgroup.SCTextureData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CreativeModeTab.class)
public class CreativeModeTabMixin implements SCTextureData {
    @Unique private boolean isCustomTab = false;
    @Unique private ResourceLocation scrollerSprite;
    @Unique
    private ResourceLocation scrollerDisabledSprite;
    @Unique private ResourceLocation[] unselectedTopTabs;
    @Unique private ResourceLocation[] selectedTopTabs;
    @Unique private ResourceLocation[] unselectedBottomTabs;
    @Unique private ResourceLocation[] selectedBottomTabs;

    @Override public boolean isCustom() { return isCustomTab; }
    @Override public void setCustom(boolean custom) { this.isCustomTab = custom; }

    @Override public ResourceLocation getScrollerSprite() { return scrollerSprite; }
    @Override public void setScrollerSprite(ResourceLocation sprite) { this.scrollerSprite = sprite; }

    @Override public ResourceLocation getScrollerDisabledSprite() { return scrollerDisabledSprite; }
    @Override public void setScrollerDisabledSprite(ResourceLocation sprite) { this.scrollerDisabledSprite = sprite; }

    @Override public ResourceLocation[] getUnselectedTopTabs() { return unselectedTopTabs; }
    @Override public void setUnselectedTopTabs(ResourceLocation[] tabs) { this.unselectedTopTabs = tabs; }

    @Override public ResourceLocation[] getSelectedTopTabs() { return selectedTopTabs; }
    @Override public void setSelectedTopTabs(ResourceLocation[] tabs) { this.selectedTopTabs = tabs; }

    @Override public ResourceLocation[] getUnselectedBottomTabs() { return unselectedBottomTabs; }
    @Override public void setUnselectedBottomTabs(ResourceLocation[] tabs) { this.unselectedBottomTabs = tabs; }

    @Override public ResourceLocation[] getSelectedBottomTabs() { return selectedBottomTabs; }
    @Override public void setSelectedBottomTabs(ResourceLocation[] tabs) { this.selectedBottomTabs = tabs; }
}