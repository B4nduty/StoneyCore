package banduty.stoneycore.mixin;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.util.SCDamageCalculator;
import banduty.stoneycore.util.SCBetterCombat;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionData;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
import banduty.stoneycore.util.render.TextureData;
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
        if (!WeaponDefinitionsStorage.isMelee(item)) return;

        Vec3 playerPos = player.position();
        double distance = client.crosshairPickEntity == null
                ? 9999
                : playerPos.distanceTo(client.crosshairPickEntity.position());

        WeaponDefinitionData weaponData = WeaponDefinitionsStorage.getData(item);

        SCDamageCalculator.DamageType damageType = SCBetterCombat.determineDamageType(mainHandStack, weaponData, player);
        stoneyCore$renderCrosshair(item, guiGraphics, distance, damageType);

        ci.cancel();
    }

    @Unique
    private void stoneyCore$renderCrosshair(Item item, GuiGraphics guiGraphics, double distance, SCDamageCalculator.DamageType damageType) {
        Minecraft client = Minecraft.getInstance();
        int centerX = client.getWindow().getGuiScaledWidth() / 2;
        int centerY = client.getWindow().getGuiScaledHeight() / 2;

        ResourceLocation[] textures = {
                TOO_FAR_CLOSE,
                stoneyCore$getCrosshair(damageType, "effective"),
                stoneyCore$getCrosshair(damageType, "critical"),
                stoneyCore$getCrosshair(damageType, "effective"),
                stoneyCore$getCrosshair(damageType, "maximum")
        };

        for (int i = 0; i < textures.length; i++) {
            double radius = SCWeaponUtil.getRadius(item, i) + 0.25F;
            if (distance <= radius) {
                stoneyCore$renderCrosshairTexture(guiGraphics, textures[i], centerX, centerY, stoneyCore$getColorForIndex(i));
                return;
            }
        }

        stoneyCore$renderCrosshairTexture(guiGraphics, TOO_FAR_CLOSE, centerX, centerY, StoneyCore.getConfig().visualOptions().hexColorTooFarClose());
    }

    @Unique
    private void stoneyCore$renderCrosshairTexture(GuiGraphics guiGraphics, ResourceLocation tex, int centerX, int centerY, int hexColor) {
        TextureData texData = stoneyCore$getTextureData(tex);
        float[] rgb = stoneyCore$hexToRGB(hexColor);

        RenderSystem.setShaderTexture(0, tex);
        RenderSystem.setShaderColor(rgb[0], rgb[1], rgb[2], 1.0f);
        guiGraphics.blit(tex, centerX - texData.offsetX(), centerY - texData.offsetY(), 0, 0, texData.width(), texData.height(), texData.width(), texData.height());
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Unique
    private int stoneyCore$getColorForIndex(int i) {
        return switch (i) {
            case 0 -> StoneyCore.getConfig().visualOptions().hexColorTooFarClose();
            case 1, 3 -> StoneyCore.getConfig().visualOptions().hexColorEffective();
            case 2 -> StoneyCore.getConfig().visualOptions().hexColorCritical();
            case 4 -> StoneyCore.getConfig().visualOptions().hexColorMaximum();
            default -> 0xFFFFFF;
        };
    }

    @Unique
    private float[] stoneyCore$hexToRGB(int hex) {
        return new float[] {
                ((hex >> 16) & 0xFF) / 255.0f,
                ((hex >> 8) & 0xFF) / 255.0f,
                (hex & 0xFF) / 255.0f
        };
    }

    @Unique
    private static ResourceLocation stoneyCore$getCrosshair(SCDamageCalculator.DamageType damageType, String crosshairType) {
        return new ResourceLocation(StoneyCore.MOD_ID, "textures/overlay/" + damageType.name().toLowerCase() + "_" + crosshairType + ".png");
    }

    @Unique
    private TextureData stoneyCore$getTextureData(ResourceLocation texture) {
        String path = texture.getPath();
        if (path.contains("critical") || path.contains("effective")) {
            return new TextureData(9, 9, 5, 5);
        } else if (path.contains("maximum")) {
            return new TextureData(7, 7, 4, 4);
        } else { // too_far_close
            return new TextureData(1, 1, 1, 1);
        }
    }
}