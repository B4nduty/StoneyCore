package banduty.stoneycore.mixin;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.platform.Services;
import banduty.stoneycore.util.data.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.data.playerdata.StaminaData;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsStorage;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class FGuiMixin {
    @Unique
    private static final ResourceLocation STAMINA = new ResourceLocation(StoneyCore.MOD_ID, "textures/overlay/stamina_bar.png");
    @Unique
    private static final ResourceLocation STAMINA_OVERLAY = new ResourceLocation(StoneyCore.MOD_ID, "textures/overlay/stamina_bar_overlay.png");
    @Unique
    private static final ResourceLocation STAMINA_EMPTY = new ResourceLocation(StoneyCore.MOD_ID, "textures/overlay/stamina_bar_empty.png");
    @Unique
    private static final ResourceLocation STAMINA_BLOCKED = new ResourceLocation(StoneyCore.MOD_ID, "textures/overlay/stamina_bar_blocked.png");
    @Unique
    private static final int STAMINA_UNIT_SIZE = 8;
    @Unique
    private static final int EMPTY_STAMINA_WIDTH = 9;
    @Unique
    private static final int EMPTY_STAMINA_HEIGHT = 9;
    @Unique
    private static final int STAMINA_BAR_WIDTH = 9;
    @Unique
    private static final int STAMINA_BAR_HEIGHT = 9;

    @Inject(method = "renderPlayerHealth", at = @At("HEAD"))
    private void stoneycore$renderPlayerHealth(GuiGraphics guiGraphics, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || player.isSpectator() || player.isCreative() || !stoneyCore$ableStaminaOverlay(player)) return;
        double maxStamina = player.getAttributeValue(Services.ATTRIBUTES.getMaxStamina());
        if (maxStamina <= 0) return;

        int staminaBarX = client.getWindow().getGuiScaledWidth() / 2;
        int staminaBarY = stoneyCore$getStaminaBarYPosition(player);
        double stamina = StaminaData.getStamina(player);
        boolean isStaminaBlocked = StaminaData.isStaminaBlocked((IEntityDataSaver) player);

        stoneyCore$renderStaminaBar(guiGraphics, staminaBarX, staminaBarY, stamina, isStaminaBlocked);
    }

    @Unique
    private boolean stoneyCore$ableStaminaOverlay(Player player) {
        if (player == null) return false;

        boolean hasSCWeapon = WeaponDefinitionsStorage.isMelee(player.getMainHandItem());
        if (hasSCWeapon) return true;
        for (ItemStack stack : player.getArmorSlots()) {
            if (ArmorDefinitionsStorage.containsItem(stack.getItem())) {
                return true;
            }
        }
        return false;
    }

    @Unique
    private int stoneyCore$getStaminaBarYPosition(LocalPlayer player) {
        Minecraft client = Minecraft.getInstance();

        int windowHeight = client.getWindow().getGuiScaledHeight();
        return player.getAirSupply() < player.getMaxAirSupply() ? windowHeight - 59 : windowHeight - 49;
    }

    @Unique
    private void stoneyCore$renderStaminaBar(GuiGraphics guiGraphics, int staminaBarX, int staminaBarY, double stamina, boolean isStaminaBlocked) {
        int yOffset = StoneyCore.getConfig().visualOptions().getStaminaBarYOffset();
        int baseY = staminaBarY - yOffset;

        // Render empty stamina units
        for (int i = 0; i < 10; i++) {
            int x = staminaBarX + 82 - (i * STAMINA_UNIT_SIZE);
            stoneyCore$renderStaminaUnit(guiGraphics, x, baseY, STAMINA_EMPTY, EMPTY_STAMINA_WIDTH, EMPTY_STAMINA_HEIGHT);
        }

        float[][] rowColors = {
                {1.0f, 1.0f, 0.0f}, // Yellow
                {0.0f, 1.0f, 0.0f}, // Green
                {0.0f, 0.5f, 1.0f}, // Blue
                {1.0f, 0.0f, 1.0f}, // Magenta
                {1.0f, 0.5f, 0.0f}, // Orange
                {0.5f, 0.0f, 0.5f}, // Purple
                {1.0f, 1.0f, 1.0f}  // White (fallback/default)
        };

        int maxUnitsPerRow = 10;
        int rows = (int) Math.ceil(stamina / 2 / maxUnitsPerRow);

        for (int row = 0; row < rows; row++) {

            float[] color = rowColors[Math.min(row, rowColors.length - 1)];

            for (int i = 0; i < maxUnitsPerRow; i++) {
                int unitIndex = (row * maxUnitsPerRow + i) * 2;
                if (unitIndex >= stamina) break;

                if (!isStaminaBlocked) guiGraphics.setColor(color[0], color[1], color[2], 1.0f);
                int x = staminaBarX + 82 - (i * STAMINA_UNIT_SIZE);
                ResourceLocation texture = isStaminaBlocked ? STAMINA_BLOCKED : STAMINA;
                RenderSystem.setShaderTexture(0, texture);
                stoneyCore$renderStaminaUnit(guiGraphics, x, baseY, texture, STAMINA_BAR_WIDTH, STAMINA_BAR_HEIGHT);

                guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);

                if (!isStaminaBlocked) stoneyCore$renderStaminaUnit(guiGraphics, x, baseY, STAMINA_OVERLAY, STAMINA_BAR_WIDTH, STAMINA_BAR_HEIGHT);
            }
        }
    }

    @Unique
    private void stoneyCore$renderStaminaUnit(GuiGraphics guiGraphics, int x, int y, ResourceLocation texture, int width, int height) {
        guiGraphics.blit(texture, x, y, 0, 0, width, height, width, height);
    }
}