package banduty.stoneycore.mixin;

import banduty.stoneycore.items.itemgroup.SCTextureData;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> {

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
        if (selectedTab != null && ((SCTextureData) selectedTab).isCustom()) {
            return selectedTab.getBackgroundTexture();
        }
        return original;
    }

    @ModifyArg(method = "renderBg", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"))
    private ResourceLocation injectCustomScrollbarTexture(ResourceLocation texture) {
        if (selectedTab == null || !((SCTextureData) selectedTab).isCustom()) {
            return texture;
        }
        SCTextureData holder = (SCTextureData) selectedTab;
        return this.canScroll() ? holder.getScrollerSprite() : holder.getScrollerDisabledSprite();
    }

    @ModifyArg(method = "renderTabButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;blitSprite(Lnet/minecraft/resources/ResourceLocation;IIII)V"))
    private ResourceLocation injectCustomTabTexture(ResourceLocation texture, @Local(argsOnly = true) CreativeModeTab currentTab) {
        if (currentTab == null || !((SCTextureData) selectedTab).isCustom()) {
            return texture;
        }
        SCTextureData holder = (SCTextureData) selectedTab;

        boolean isSelected = (currentTab == selectedTab);
        boolean isTopRow = (currentTab.row() == CreativeModeTab.Row.TOP);
        int column = Math.min(currentTab.column(), 6);

        ResourceLocation[] selectedArr;
        ResourceLocation[] unselectedArr;
        if (isTopRow) {
            selectedArr = holder.getSelectedTopTabs();
            unselectedArr = holder.getUnselectedTopTabs();
        } else {
            selectedArr = holder.getSelectedBottomTabs();
            unselectedArr = holder.getUnselectedBottomTabs();
        }
        return isSelected ? selectedArr[column] : unselectedArr[column];
    }

    public CreativeModeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu screenHandler, Inventory playerInventory, Component text) {
        super(screenHandler, playerInventory, text);
    }
}