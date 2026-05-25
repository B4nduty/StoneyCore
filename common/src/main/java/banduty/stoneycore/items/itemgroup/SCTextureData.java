package banduty.stoneycore.items.itemgroup;

import net.minecraft.resources.ResourceLocation;

public interface SCTextureData {
    boolean isCustom();
    void setCustom(boolean custom);

    ResourceLocation getScrollerSprite();
    void setScrollerSprite(ResourceLocation sprite);

    ResourceLocation getScrollerDisabledSprite();
    void setScrollerDisabledSprite(ResourceLocation sprite);

    ResourceLocation[] getUnselectedTopTabs();
    void setUnselectedTopTabs(ResourceLocation[] tabs);

    ResourceLocation[] getSelectedTopTabs();
    void setSelectedTopTabs(ResourceLocation[] tabs);

    ResourceLocation[] getUnselectedBottomTabs();
    void setUnselectedBottomTabs(ResourceLocation[] tabs);

    ResourceLocation[] getSelectedBottomTabs();
    void setSelectedBottomTabs(ResourceLocation[] tabs);
}