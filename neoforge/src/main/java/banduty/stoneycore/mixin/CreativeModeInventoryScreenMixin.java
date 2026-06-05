package banduty.stoneycore.mixin;

import banduty.stoneycore.items.itemgroup.SCTextureData;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.client.gui.CreativeTabsScreenPage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {

    @Shadow
    private CreativeTabsScreenPage currentPage;
    @Shadow
    private static CreativeModeTab selectedTab;

    @Shadow
    protected abstract boolean canScroll();

    @ModifyArg(
            method = "renderBg",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V",
                    ordinal = 0
            )
    )
    private ResourceLocation injectCustomGroupTexture(ResourceLocation original) {
        if (selectedTab == null || !((SCTextureData) selectedTab).isCustom()) {
            return original;
        }
        return selectedTab.getBackgroundTexture();
    }

    @Redirect(
            method = "renderBg",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"
            )
    )
    private void redirectScrollbarSprite(GuiGraphics instance, ResourceLocation texture, int x, int y, int width, int height) {
        if (selectedTab == null || !((SCTextureData) selectedTab).isCustom()) {
            if (texture != null) {
                instance.blitSprite(texture, x, y, width, height);
            }
            return;
        }

        SCTextureData holder = (SCTextureData) selectedTab;
        ResourceLocation customScroll = this.canScroll() ? holder.getScrollerSprite() : holder.getScrollerDisabledSprite();

        if (customScroll != null) {
            instance.blitSprite(customScroll, x, y, width, height);
        } else if (texture != null) {
            instance.blitSprite(texture, x, y, width, height);
        }
    }

    @ModifyArg(method = "renderTabButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"))
    private ResourceLocation injectCustomTabTexture(ResourceLocation texture, @Local(argsOnly = true) CreativeModeTab currentTab) {
        if (currentTab == null || selectedTab == null || !((SCTextureData) selectedTab).isCustom()) {
            return texture;
        }
        SCTextureData holder = (SCTextureData) selectedTab;

        boolean isSelected = (currentTab == selectedTab);
        boolean isTopRow = this.currentPage.isTop(currentTab);
        int column = this.currentPage.getColumn(currentTab);

        ResourceLocation[] selectedArr = isTopRow ? holder.getSelectedTopTabs() : holder.getSelectedBottomTabs();
        ResourceLocation[] unselectedArr = isTopRow ? holder.getUnselectedTopTabs() : holder.getUnselectedBottomTabs();

        if (selectedArr == null || unselectedArr == null) return texture;

        ResourceLocation customTexture = isSelected ? selectedArr[column] : unselectedArr[column];
        return customTexture != null ? customTexture : texture;
    }

    public CreativeModeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu screenHandler, Inventory playerInventory, Component text) {
        super(screenHandler, playerInventory, text);
    }
}