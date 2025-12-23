package banduty.stoneycore.mixin;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.util.SCDamageCalculator;
import banduty.stoneycore.util.bettercombatlogic.SCBetterCombat;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsLoader;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsLoader;
import banduty.stoneycore.util.data.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.data.playerdata.StaminaData;
import banduty.stoneycore.util.render.TextureData;
import banduty.stoneycore.util.weaponutil.SCWeaponUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class GuiMixin {
    @Unique
    private static final ResourceLocation TOO_FAR_CLOSE = new ResourceLocation(StoneyCore.MOD_ID, "textures/overlay/too_far_close.png");

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void stoneycore$renderCrosshair(GuiGraphics guiGraphics, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) return;

        ItemStack mainHandStack = player.getMainHandItem();
        Item item = mainHandStack.getItem();
        if (!WeaponDefinitionsLoader.isMelee(item)) return;

        Vec3 playerPos = player.position();
        double distance = client.crosshairPickEntity == null
                ? 9999
                : playerPos.distanceTo(client.crosshairPickEntity.position());

        WeaponDefinitionsLoader.DefinitionData weaponData = WeaponDefinitionsLoader.getData(item);

        SCDamageCalculator.DamageType damageType = SCBetterCombat.determineDamageType(mainHandStack, weaponData, player);
        renderCrosshair(item, guiGraphics, distance, damageType);

        ci.cancel();
    }

    @Unique
    private void renderCrosshair(Item item, GuiGraphics guiGraphics, double distance, SCDamageCalculator.DamageType damageType) {
        Minecraft client = Minecraft.getInstance();
        int centerX = client.getWindow().getGuiScaledWidth() / 2;
        int centerY = client.getWindow().getGuiScaledHeight() / 2;

        ResourceLocation[] textures = {
                TOO_FAR_CLOSE,
                getCrosshair(damageType, "effective"),
                getCrosshair(damageType, "critical"),
                getCrosshair(damageType, "effective"),
                getCrosshair(damageType, "maximum")
        };

        for (int i = 0; i < textures.length; i++) {
            double radius = SCWeaponUtil.getRadius(item, i) + 0.25F;
            if (distance <= radius) {
                renderCrosshairTexture(guiGraphics, textures[i], centerX, centerY, getColorForIndex(i));
                return;
            }
        }

        renderCrosshairTexture(guiGraphics, TOO_FAR_CLOSE, centerX, centerY, StoneyCore.getConfig().visualOptions.hexColorTooFarClose());
    }

    @Unique
    private void renderCrosshairTexture(GuiGraphics guiGraphics, ResourceLocation tex, int centerX, int centerY, int hexColor) {
        TextureData texData = getTextureData(tex);
        float[] rgb = hexToRGB(hexColor);

        RenderSystem.setShaderTexture(0, tex);
        RenderSystem.setShaderColor(rgb[0], rgb[1], rgb[2], 1.0f);
        guiGraphics.blit(tex, centerX - texData.offsetX(), centerY - texData.offsetY(), 0, 0, texData.width(), texData.height(), texData.width(), texData.height());
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Unique
    private int getColorForIndex(int i) {
        return switch (i) {
            case 0 -> StoneyCore.getConfig().visualOptions.hexColorTooFarClose();
            case 1, 3 -> StoneyCore.getConfig().visualOptions.hexColorEffective();
            case 2 -> StoneyCore.getConfig().visualOptions.hexColorCritical();
            case 4 -> StoneyCore.getConfig().visualOptions.hexColorMaximum();
            default -> 0xFFFFFF;
        };
    }

    @Unique
    private float[] hexToRGB(int hex) {
        return new float[] {
                ((hex >> 16) & 0xFF) / 255.0f,
                ((hex >> 8) & 0xFF) / 255.0f,
                (hex & 0xFF) / 255.0f
        };
    }

    @Unique
    private static ResourceLocation getCrosshair(SCDamageCalculator.DamageType damageType, String crosshairType) {
        return new ResourceLocation(StoneyCore.MOD_ID, "textures/overlay/" + damageType.name().toLowerCase() + "_" + crosshairType + ".png");
    }

    @Unique
    private TextureData getTextureData(ResourceLocation texture) {
        String path = texture.getPath();
        if (path.contains("critical") || path.contains("effective")) {
            return new TextureData(9, 9, 5, 5);
        } else if (path.contains("maximum")) {
            return new TextureData(7, 7, 4, 4);
        } else { // too_far_close
            return new TextureData(1, 1, 1, 1);
        }
    }

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
    private void stoneycore$renderStaminaBar(GuiGraphics guiGraphics, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null || player.isSpectator() || !ableStaminaOverlay(player)) return;
        double maxStamina = player.getAttributeValue(StoneyCore.MAX_STAMINA.get());
        if (maxStamina <= 0) return;

        int staminaBarX = client.getWindow().getGuiScaledWidth() / 2;
        int staminaBarY = getStaminaBarYPosition(player);
        double stamina = StaminaData.getStamina(player);
        boolean isStaminaBlocked = StaminaData.isStaminaBlocked((IEntityDataSaver) player);

        renderStaminaBar(guiGraphics, staminaBarX, staminaBarY, stamina, isStaminaBlocked);
    }

    @Unique
    private boolean ableStaminaOverlay(Player player) {
        if (player == null) return false;

        boolean hasSCWeapon = WeaponDefinitionsLoader.isMelee(player.getMainHandItem());
        for (ItemStack stack : player.getArmorSlots()) {
            if (ArmorDefinitionsLoader.containsItem(stack.getItem())) {
                return true;
            }
        }
        return hasSCWeapon;
    }

    @Unique
    private int getStaminaBarYPosition(LocalPlayer player) {
        Minecraft client = Minecraft.getInstance();

        int windowHeight = client.getWindow().getGuiScaledHeight();
        return player.getAirSupply() < player.getMaxAirSupply() ? windowHeight - 59 : windowHeight - 49;
    }

    @Unique
    private void renderStaminaBar(GuiGraphics guiGraphics, int staminaBarX, int staminaBarY, double stamina, boolean isStaminaBlocked) {
        int yOffset = StoneyCore.getConfig().visualOptions.getStaminaBarYOffset();
        int baseY = staminaBarY - yOffset;

        // Render empty stamina units
        for (int i = 0; i < 10; i++) {
            int x = staminaBarX + 82 - (i * STAMINA_UNIT_SIZE);
            renderStaminaUnit(guiGraphics, x, baseY, STAMINA_EMPTY, EMPTY_STAMINA_WIDTH, EMPTY_STAMINA_HEIGHT);
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
                renderStaminaUnit(guiGraphics, x, baseY, texture, STAMINA_BAR_WIDTH, STAMINA_BAR_HEIGHT);

                guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);

                if (!isStaminaBlocked) renderStaminaUnit(guiGraphics, x, baseY, STAMINA_OVERLAY, STAMINA_BAR_WIDTH, STAMINA_BAR_HEIGHT);
            }
        }
    }

    @Unique
    private void renderStaminaUnit(GuiGraphics guiGraphics, int x, int y, ResourceLocation texture, int width, int height) {
        guiGraphics.blit(texture, x, y, 0, 0, width, height, width, height);
    }
}