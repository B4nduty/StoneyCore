package banduty.stoneycore.mixin;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.StoneyCoreClient;
import banduty.stoneycore.items.armor.SCAccessoryItem;
import banduty.stoneycore.util.SCDamageCalculator;
import banduty.stoneycore.util.definitionsloader.SCArmorDefinitionsLoader;
import banduty.stoneycore.util.definitionsloader.SCWeaponDefinitionsLoader;
import banduty.stoneycore.util.itemdata.SCTags;
import banduty.stoneycore.util.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.playerdata.StaminaData;
import banduty.stoneycore.util.render.TextureData;
import banduty.stoneycore.util.weaponutil.SCWeaponUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import io.wispforest.owo.shader.BlurProgram;
import net.bettercombat.logic.PlayerAttackProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static banduty.stoneycore.util.weaponutil.SCWeaponUtil.getDamageValues;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Unique
    private static final Identifier TOO_FAR_CLOSE = new Identifier(StoneyCore.MOD_ID, "textures/overlay/too_far_close.png");

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void stoneycore$renderCrosshair(DrawContext context, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || player.getWorld() == null) return;

        ItemStack mainHandStack = player.getMainHandStack();
        Item item = mainHandStack.getItem();
        if (!SCWeaponDefinitionsLoader.isMelee(item)) return;

        Vec3d playerPos = player.getPos();
        double distance = client.targetedEntity == null
                ? 9999
                : playerPos.distanceTo(client.targetedEntity.getPos());

        SCWeaponDefinitionsLoader.DefinitionData weaponData = SCWeaponDefinitionsLoader.getData(item);

        String damageType = determineDamageType(mainHandStack, weaponData, (PlayerAttackProperties) player);
        renderCrosshair(item, context, distance, damageType);

        ci.cancel();
    }

    @Unique
    private String determineDamageType(ItemStack mainHandStack, SCWeaponDefinitionsLoader.DefinitionData weaponData, PlayerAttackProperties player) {
        Item item = mainHandStack.getItem();
        boolean isBludgeoning = mainHandStack.getNbt() != null && mainHandStack.getNbt().getBoolean("sc_bludgeoning");
        boolean isPiercing = isPiercing(player, item);
        boolean bludgeoningToPiercing = getDamageValues("SLASHING", item) == 0
                && getDamageValues("PIERCING", item) > 0
                && getDamageValues("BLUDGEONING", item) > 0;

        SCDamageCalculator.DamageType onlyType = weaponData.melee().onlyDamageType();

        if (isBludgeoning || onlyType == SCDamageCalculator.DamageType.BLUDGEONING) return "bludgeoning";
        if (isPiercing || bludgeoningToPiercing || onlyType == SCDamageCalculator.DamageType.PIERCING) return "piercing";
        return "slashing";
    }

    @Unique
    private static boolean isPiercing(PlayerAttackProperties player, Item item) {
        int comboCount = player.getComboCount();
        SCWeaponDefinitionsLoader.DefinitionData attributeData = SCWeaponDefinitionsLoader.getData(item);
        int[] piercingAnimations = attributeData.melee().piercingAnimation();
        int animation = attributeData.melee().animation();

        if (animation > 0) {
            for (int piercingAnimation : piercingAnimations) {
                if (comboCount % animation == piercingAnimation - 1) {
                    return true;
                }
            }
            return piercingAnimations.length == animation;
        }
        return false;
    }

    @Unique
    private void renderCrosshair(Item item, DrawContext context, double distance, String damageType) {
        MinecraftClient client = MinecraftClient.getInstance();
        int centerX = client.getWindow().getScaledWidth() / 2;
        int centerY = client.getWindow().getScaledHeight() / 2;

        Identifier[] textures = {
                TOO_FAR_CLOSE,
                getCrosshair(damageType, "effective"),
                getCrosshair(damageType, "critical"),
                getCrosshair(damageType, "effective"),
                getCrosshair(damageType, "maximum")
        };

        for (int i = 0; i < textures.length; i++) {
            double radius = SCWeaponUtil.getRadius(item, i) + 0.25F;
            if (distance <= radius) {
                renderCrosshairTexture(context, textures[i], centerX, centerY, getColorForIndex(i));
                return;
            }
        }

        renderCrosshairTexture(context, TOO_FAR_CLOSE, centerX, centerY, StoneyCore.getConfig().visualOptions.hexColorTooFarClose());
    }

    @Unique
    private void renderCrosshairTexture(DrawContext ctx, Identifier tex, int centerX, int centerY, int hexColor) {
        TextureData texData = getTextureData(tex);
        float[] rgb = hexToRGB(hexColor);

        RenderSystem.setShaderTexture(0, tex);
        RenderSystem.setShaderColor(rgb[0], rgb[1], rgb[2], 1.0f);
        ctx.drawTexture(tex, centerX - texData.offsetX(), centerY - texData.offsetY(), 0, 0, texData.width(), texData.height(), texData.width(), texData.height());
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
    private static Identifier getCrosshair(String damageType, String crosshairType) {
        return new Identifier(StoneyCore.MOD_ID, "textures/overlay/" + damageType + "_" + crosshairType + ".png");
    }

    @Unique
    private TextureData getTextureData(Identifier texture) {
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
    private static final Identifier STAMINA = new Identifier(StoneyCore.MOD_ID, "textures/overlay/stamina_bar.png");
    @Unique
    private static final Identifier STAMINA_OVERLAY = new Identifier(StoneyCore.MOD_ID, "textures/overlay/stamina_bar_overlay.png");
    @Unique
    private static final Identifier STAMINA_EMPTY = new Identifier(StoneyCore.MOD_ID, "textures/overlay/stamina_bar_empty.png");
    @Unique
    private static final Identifier STAMINA_BLOCKED = new Identifier(StoneyCore.MOD_ID, "textures/overlay/stamina_bar_blocked.png");
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

    @Inject(method = "renderStatusBars", at = @At("HEAD"))
    private void stoneycore$renderStaminaBar(DrawContext context, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;
        if (player == null || player.isSpectator() || !ableStaminaOverlay(player)) return;
        double maxStamina = player.getAttributeValue(StoneyCore.MAX_STAMINA.get());
        if (maxStamina <= 0) return;

        int staminaBarX = client.getWindow().getScaledWidth() / 2;
        int staminaBarY = getStaminaBarYPosition(player);
        double stamina = StaminaData.getStamina(player);
        boolean isStaminaBlocked = StaminaData.isStaminaBlocked((IEntityDataSaver) player);

        renderStaminaBar(context, staminaBarX, staminaBarY, stamina, isStaminaBlocked);
    }

    @Unique
    private boolean ableStaminaOverlay(PlayerEntity player) {
        if (player == null) return false;

        boolean hasSCWeapon = SCWeaponDefinitionsLoader.isMelee(player.getMainHandStack());
        for (ItemStack stack : player.getArmorItems()) {
            if (SCArmorDefinitionsLoader.containsItem(stack.getItem())) {
                return true;
            }
        }
        return hasSCWeapon;
    }

    @Unique
    private int getStaminaBarYPosition(ClientPlayerEntity player) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null) return 0;

        int windowHeight = client.getWindow().getScaledHeight();
        return player.getAir() < player.getMaxAir() ? windowHeight - 59 : windowHeight - 49;
    }

    @Unique
    private void renderStaminaBar(DrawContext drawContext, int staminaBarX, int staminaBarY, double stamina, boolean isStaminaBlocked) {
        int yOffset = StoneyCore.getConfig().visualOptions.getStaminaBarYOffset();
        int baseY = staminaBarY - yOffset;

        // Render empty stamina units
        for (int i = 0; i < 10; i++) {
            int x = staminaBarX + 82 - (i * STAMINA_UNIT_SIZE);
            renderStaminaUnit(drawContext, x, baseY, STAMINA_EMPTY, EMPTY_STAMINA_WIDTH, EMPTY_STAMINA_HEIGHT);
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

                if (!isStaminaBlocked) drawContext.setShaderColor(color[0], color[1], color[2], 1.0f);
                int x = staminaBarX + 82 - (i * STAMINA_UNIT_SIZE);
                Identifier texture = isStaminaBlocked ? STAMINA_BLOCKED : STAMINA;
                RenderSystem.setShaderTexture(0, texture);
                renderStaminaUnit(drawContext, x, baseY, texture, STAMINA_BAR_WIDTH, STAMINA_BAR_HEIGHT);

                drawContext.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

                if (!isStaminaBlocked) renderStaminaUnit(drawContext, x, baseY, STAMINA_OVERLAY, STAMINA_BAR_WIDTH, STAMINA_BAR_HEIGHT);
            }
        }
    }

    @Unique
    private void renderStaminaUnit(DrawContext drawContext, int x, int y, Identifier texture, int width, int height) {
        drawContext.drawTexture(texture, x, y, 0, 0, width, height, width, height);
    }

    @Unique
    private static final Identifier VISOR_HELMET = new Identifier(StoneyCore.MOD_ID, "textures/overlay/visor_helmet.png");

    @Inject(method = "render", at = @At("HEAD"))
    private void stoneycore$renderBackgroundOverlays(DrawContext context, float tickDelta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        StoneyCoreClient.LAND_TITLE_RENDERER.render(context);

        if (player == null || player.isCreative() || player.isSpectator()) return;

        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        if (AccessoriesCapability.getOptionally(player).isPresent()) {
            for (SlotEntryReference equipped : AccessoriesCapability.get(player).getAllEquipped()) {
                ItemStack itemStack = equipped.stack();
                if (itemStack.getItem() instanceof SCAccessoryItem && itemStack.isIn(SCTags.VISORED_HELMET.getTag()) && StoneyCore.getConfig().visualOptions.getVisoredHelmet()) {
                    RenderSystem.setShaderTexture(0, VISOR_HELMET);
                    context.drawTexture(VISOR_HELMET, 0, 0, 0, 0, width, height, width, height);
                }
            }
        }

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    @Unique
    private static final BlurProgram BLUR = new BlurProgram();

    @Inject(method = "render", at = @At("TAIL"))
    private void stoneycore$renderBackgroundOverlaysTail(DrawContext context, float tickDelta, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player == null || player.isCreative() || player.isSpectator()) return;

        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();

        double stamina = StaminaData.getStamina(player);
        double secondLevel = player.getAttributeBaseValue(StoneyCore.MAX_STAMINA.get()) * 0.15d;

        if (StaminaData.isStaminaBlocked((IEntityDataSaver) player) && StoneyCore.getConfig().visualOptions.getLowStaminaIndicator()) {
            double staminaPercentage = stamina / secondLevel;
            if (StoneyCore.getConfig().combatOptions.getRealisticCombat()) {
                if (noiseTextures == null) initNoiseTextures();
                renderBlurEffect(context, width, height, staminaPercentage);
            } else {
                int opacity = (int)((Math.max(0, 0.4f - staminaPercentage) * 255));
                int green = StaminaData.isStaminaBlocked((IEntityDataSaver) player) ? 0 : (int) (stamina / secondLevel);
                int gradientColorEnd = opacity << 24 | green | 0x00FF0000;

                context.fillGradient(0, 0, width, height, 0x00FFFFFF, gradientColorEnd);
            }
        }
    }

    @Unique
    private Identifier[] noiseTextures;
    @Unique
    private int currentNoiseTexture = 0;
    @Unique
    private int currentNoiseTextureTime = 0;

    @Unique
    private void initNoiseTextures() {
        noiseTextures = new Identifier[12];
        for (int i = 0; i < noiseTextures.length; i++) {
            noiseTextures[i] = new Identifier(StoneyCore.MOD_ID, "textures/overlay/noise/noise_" + i + ".png");
        }
    }

    @Unique
    private void renderBlurEffect(DrawContext context, int width, int height, double staminaPercentage) {
        renderBlur(context, width, height, staminaPercentage);
        renderTunnelVision(context, width, height, staminaPercentage);
        if (StoneyCore.getConfig().visualOptions.getNoiseEffect()) renderNoise(context, width, height, staminaPercentage);
    }

    @Unique
    private void renderBlur(DrawContext context, int width, int height, double staminaPercentage) {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION);
        buffer.vertex(matrix, 0, 0, 0).next();
        buffer.vertex(matrix, 0, height, 0).next();
        buffer.vertex(matrix, width, height, 0).next();
        buffer.vertex(matrix, width, 0, 0).next();

        float blur = (float) (Math.max(0.1f, 1.0f - staminaPercentage) * 12.0f);

        BLUR.setParameters(16, 12, blur);
        BLUR.use();

        tessellator.draw();

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    @Unique
    private void renderNoise(DrawContext context, int width, int height, double staminaPercentage) {
        Identifier noiseTexture = noiseTextures[currentNoiseTexture];
        float alpha = (float) (-0.001f * currentNoiseTextureTime * (currentNoiseTextureTime - 10) + Math.max(0, 1.0f - staminaPercentage) * 0.2f);
        if (currentNoiseTextureTime-- <= 0 && !MinecraftClient.getInstance().isPaused()) {
            currentNoiseTexture = (currentNoiseTexture + 1) % noiseTextures.length;
            currentNoiseTextureTime = 10;
        }

        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1.0f, 0.5f, 0.5f, alpha);
        context.drawTexture(noiseTexture, 0, 0, 0, 0, width, height, width, height);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    @Unique
    private void renderTunnelVision(DrawContext context, int width, int height, double staminaPercentage) {
        int opacity = (int)((Math.max(0, 0.2f - staminaPercentage) * 255));
        int gradientColor = opacity << 24;

        int opacity2 = (int)((Math.max(0, 0.6f - staminaPercentage) * 255));
        int gradientColor2 = opacity2 << 24;

        context.fillGradient(0, 0, width, height, gradientColor, gradientColor2);
    }
}