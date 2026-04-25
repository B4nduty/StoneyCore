package banduty.stoneycore.mixin;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.combat.melee.SCDamageType;
import banduty.stoneycore.util.data.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.data.playerdata.StaminaData;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
import banduty.stoneycore.util.weaponutil.SCWeaponUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Gui.class)
public class GuiMixin {
    @Unique
    private static final ResourceLocation TOO_FAR_CLOSE = new ResourceLocation(StoneyCore.MOD_ID, "textures/overlay/too_far_close.png");

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void stoneycore$renderCrosshair(GuiGraphics guiGraphics, CallbackInfo ci) {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) return;
        if (client.options.hideGui || StaminaData.isStaminaBlocked((IEntityDataSaver) Minecraft.getInstance().player)) {
            ci.cancel();
            return;
        }

        ItemStack mainHandStack = player.getMainHandItem();
        Item item = mainHandStack.getItem();
        if (!WeaponDefinitionsStorage.isMelee(item)) return;

        Vec3 playerPos = player.position();
        double distance = client.crosshairPickEntity == null
                ? 9999
                : playerPos.distanceTo(client.crosshairPickEntity.position());

        SCDamageType damageType = SCDamageType.determine(mainHandStack, player);
        stoneyCore$calculateCrosshair(item, guiGraphics, distance, damageType);

        ci.cancel();
    }

    @Unique
    private void stoneyCore$calculateCrosshair(Item item, GuiGraphics guiGraphics, double distance, SCDamageType damageType) {
        Minecraft client = Minecraft.getInstance();
        int centerX = client.getWindow().getGuiScaledWidth() / 2;
        int centerY = client.getWindow().getGuiScaledHeight() / 2;

        double currentDamage = SCWeaponUtil.calculateDamage(item, distance, damageType);

        List<Double> damageValues = SCWeaponUtil.getSortedDamageValues(damageType, item);

        ResourceLocation texture;
        int color;

        if (currentDamage <= 0 || damageValues.isEmpty()) {
            texture = TOO_FAR_CLOSE;
            color = StoneyCore.getConfig().visualOptions().hexColorTooFarClose();
        } else {
            int index = -1;

            for (int i = damageValues.size() - 1; i >= 0; i--) {
                if (currentDamage >= damageValues.get(i)) {
                    if (damageValues.get(i) <= 0) {
                        index = -2;
                    } else {
                        index = (damageValues.size() - 1) - i;
                    }
                    break;
                }
            }

            if (index == -2 || index == -1) {
                texture = TOO_FAR_CLOSE;
            } else {
                texture = stoneyCore$getCrosshair(damageType, String.valueOf(index));
            }
            color = 0xFFFFFF;
        }

        stoneyCore$renderCrosshairTexture(guiGraphics, texture, centerX, centerY, color);
    }

    @Unique
    private void stoneyCore$renderCrosshairTexture(GuiGraphics guiGraphics, ResourceLocation tex, int centerX, int centerY, int hexColor) {
        float[] rgb = stoneyCore$hexToRGB(hexColor);

        RenderSystem.setShaderTexture(0, tex);
        RenderSystem.setShaderColor(rgb[0], rgb[1], rgb[2], 1.0f);
        guiGraphics.blit(tex, centerX - 5, centerY - 5, 0, 0, 9, 9, 9, 9);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Unique
    private float[] stoneyCore$hexToRGB(int hex) {
        return new float[]{
                ((hex >> 16) & 0xFF) / 255.0f,
                ((hex >> 8) & 0xFF) / 255.0f,
                (hex & 0xFF) / 255.0f
        };
    }

    @Unique
    private static ResourceLocation stoneyCore$getCrosshair(SCDamageType damageType, String crosshairType) {
        return new ResourceLocation(StoneyCore.MOD_ID, "textures/overlay/" + damageType.name().toLowerCase() + "_" + crosshairType + ".png");
    }
}