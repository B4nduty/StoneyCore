package banduty.stoneycore.mixin;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.armor.SCTrinketsItem;
import banduty.stoneycore.util.SCDamageCalculator;
import banduty.stoneycore.util.definitionsloader.SCMeleeWeaponDefinitionsLoader;
import banduty.stoneycore.util.definitionsloader.SCArmorDefinitionsLoader;
import banduty.stoneycore.util.itemdata.SCTags;
import banduty.stoneycore.util.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.playerdata.StaminaData;
import banduty.stoneycore.util.render.TextureData;
import banduty.stoneycore.util.weaponutil.SCWeaponUtil;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import dev.emi.trinkets.api.TrinketsApi;
import net.bettercombat.logic.PlayerAttackProperties;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
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
        if (!SCMeleeWeaponDefinitionsLoader.containsItem(item)) return;

        Vec3d playerPos = player.getPos();
        double distance = client.targetedEntity == null
                ? 9999
                : playerPos.distanceTo(client.targetedEntity.getPos());

        SCMeleeWeaponDefinitionsLoader.DefinitionData weaponData = SCMeleeWeaponDefinitionsLoader.getData(item);

        String damageType = determineDamageType(mainHandStack, weaponData, (PlayerAttackProperties) player);
        renderOverlay(item, context, distance, damageType);

        ci.cancel();
    }

    @Unique
    private String determineDamageType(ItemStack mainHandStack, SCMeleeWeaponDefinitionsLoader.DefinitionData weaponData, PlayerAttackProperties player) {
        boolean bludgeoning = mainHandStack.getOrCreateNbt().getBoolean("sc_bludgeoning");
        boolean bludgeoningToPiercing = getDamageValues(SCDamageCalculator.DamageType.SLASHING.name(), mainHandStack.getItem()) == 0
                && getDamageValues(SCDamageCalculator.DamageType.PIERCING.name(), mainHandStack.getItem()) > 0
                && getDamageValues(SCDamageCalculator.DamageType.BLUDGEONING.name(), mainHandStack.getItem()) > 0;
        boolean piercing = isPiercing(player, mainHandStack.getItem());

        if (bludgeoning || weaponData.onlyDamageType() == SCDamageCalculator.DamageType.BLUDGEONING) {
            return "bludgeoning";
        }
        if (piercing || bludgeoningToPiercing || weaponData.onlyDamageType() == SCDamageCalculator.DamageType.PIERCING) {
            return "piercing";
        }
        return "slashing";
    }

    @Unique
    private static boolean isPiercing(PlayerAttackProperties player, Item item) {
        int comboCount = player.getComboCount();
        SCMeleeWeaponDefinitionsLoader.DefinitionData attributeData = SCMeleeWeaponDefinitionsLoader.getData(item);
        int[] piercingAnimations = attributeData.piercingAnimation();
        int animation = attributeData.animation();

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
    private void renderOverlay(Item item, DrawContext drawContext, double distance, String damageType) {
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
            if (distance <= SCWeaponUtil.getRadius(item, i) + 0.25F) {
                Identifier texture = textures[i];
                TextureData textureData = getTextureData(texture);
                int color = switch (i) {
                    case 0 -> StoneyCore.getConfig().hexColorTooFarClose();
                    case 1, 3 -> StoneyCore.getConfig().hexColorEffective();
                    case 2 -> StoneyCore.getConfig().hexColorCritical();
                    case 4 -> StoneyCore.getConfig().hexColorMaximum();
                    default -> 0xFFFFFF;
                };

                float red = ((color >> 16) & 0xFF) / 255.0f;
                float green = ((color >> 8) & 0xFF) / 255.0f;
                float blue = (color & 0xFF) / 255.0f;

                RenderSystem.setShaderTexture(0, texture);
                RenderSystem.setShaderColor(red, green, blue, 1);
                drawContext.drawTexture(texture, centerX - textureData.offsetX(), centerY - textureData.offsetY(), 0, 0, textureData.width(), textureData.height(), textureData.width(), textureData.height());
                RenderSystem.setShaderColor(1, 1, 1, 1);
                return;
            }
        }
        int color = StoneyCore.getConfig().hexColorTooFarClose();
        float red = ((color >> 16) & 0xFF) / 255.0f;
        float green = ((color >> 8) & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;

        RenderSystem.setShaderTexture(0, TOO_FAR_CLOSE);
        RenderSystem.setShaderColor(red, green, blue, 1);
        drawContext.drawTexture(TOO_FAR_CLOSE, centerX - 1, centerY - 1, 0, 0, 1, 1, 1, 1);
        RenderSystem.setShaderColor(1, 1, 1, 1);
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
        if (player == null || player.isSpectator() || !ableStamina(player) || StoneyCore.getConfig().maxStamina() <= 0) return;

        int staminaBarX = client.getWindow().getScaledWidth() / 2;
        int staminaBarY = getStaminaBarYPosition(player);
        float stamina = StaminaData.getStamina((IEntityDataSaver) player);
        boolean isStaminaBlocked = StaminaData.isStaminaBlocked((IEntityDataSaver) player);

        renderStaminaBar(context, staminaBarX, staminaBarY, stamina, isStaminaBlocked);
    }

    @Unique
    private boolean ableStamina(PlayerEntity player) {
        if (player == null) return false;

        boolean hasSCWeapon = SCMeleeWeaponDefinitionsLoader.containsItem(player.getMainHandStack().getItem());
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
    private void renderStaminaBar(DrawContext drawContext, int staminaBarX, int staminaBarY, float stamina, boolean isStaminaBlocked) {
        int yOffset = StoneyCore.getConfig().getStaminaBarYOffset();
        int y = staminaBarY - yOffset;

        // Render empty stamina units
        for (int i = 0; i < 10; i++) {
            int x = staminaBarX + 82 - (i * STAMINA_UNIT_SIZE);
            renderStaminaUnit(drawContext, x, y, STAMINA_EMPTY, EMPTY_STAMINA_WIDTH, EMPTY_STAMINA_HEIGHT);
        }

        // Render filled or blocked stamina units
        float maxStamina = StoneyCore.getConfig().maxStamina();
        for (int i = 0; i < maxStamina; i++) {
            if (stamina < i) break;

            int x = staminaBarX + 82 - Math.absExact((int) (i / (maxStamina / 10)) * STAMINA_UNIT_SIZE);
            Identifier texture = isStaminaBlocked ? STAMINA_BLOCKED : STAMINA;
            renderStaminaUnit(drawContext, x, y, texture, STAMINA_BAR_WIDTH, STAMINA_BAR_HEIGHT);
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
        if (player == null || player.isCreative() || player.isSpectator()) return;

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();

        // Render visor helmet overlay if equipped
        TrinketsApi.getTrinketComponent(player).ifPresent(trinketComponent -> {
            trinketComponent.getAllEquipped().forEach(pair -> {
                ItemStack trinketStack = pair.getRight();
                if (trinketStack.getItem() instanceof SCTrinketsItem && trinketStack.isIn(SCTags.VISORED_HELMET.getTag()) && StoneyCore.getConfig().getVisoredHelmet()) {
                    RenderSystem.setShaderTexture(0, VISOR_HELMET);
                    context.drawTexture(VISOR_HELMET, 0, 0, 0, 0, width, height, width, height);
                }
            });
        });

        // Render low stamina overlay if applicable
        float stamina = StaminaData.getStamina((IEntityDataSaver) player);
        int lowStaminaThreshold = (int) (StoneyCore.getConfig().maxStamina() * 0.3f);

        if (stamina <= lowStaminaThreshold && StoneyCore.getConfig().getLowStaminaIndicator()) {
            if (StoneyCore.getConfig().getRealisticCombat()) {
                renderBlurEffect(context, width, height);
            } else {
                float staminaPercentage = stamina / lowStaminaThreshold;
                int opacity = (int) ((int)((Math.max(0, 0.4f - staminaPercentage) * 255)));
                int green = StaminaData.isStaminaBlocked((IEntityDataSaver) player) ? 0 : (int) (stamina / lowStaminaThreshold);
                int gradientColorEnd = opacity << 24 | green | 0x00FF0000;

                context.fillGradient(0, 0, width, height, 0x00000000, gradientColorEnd);
            }
        }

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    @Unique
    private int frameCapturedTime = 0;
    @Unique
    private int blurTextureId = -1;
    @Unique
    private static final int BLUR_DURATION_TICKS = 5 * 20;

    @Unique
    private void captureScreenToTexture(MinecraftClient client) {
        if (blurTextureId != -1) {
            RenderSystem.deleteTexture(blurTextureId);
            blurTextureId = -1;
        }

        Framebuffer mainFb = client.getFramebuffer();

        blurTextureId = GlStateManager._genTexture();
        RenderSystem.bindTexture(blurTextureId);

        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        GlStateManager._texImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8,
                mainFb.textureWidth, mainFb.textureHeight,
                0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, null);

        GL30.glCopyTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, 0, 0,
                mainFb.textureWidth, mainFb.textureHeight);

        frameCapturedTime = BLUR_DURATION_TICKS;
    }

    @Unique
    private void renderBlurEffect(DrawContext context, int width, int height) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (frameCapturedTime <= 0) {
            captureScreenToTexture(client);
        }

        if (blurTextureId == -1) {
            return;
        }

        if (frameCapturedTime > 0) {
            frameCapturedTime--;
        }

        MatrixStack matrices = context.getMatrices();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        RenderSystem.setShaderTexture(0, blurTextureId);
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);

        BufferBuilder buffer = Tessellator.getInstance().getBuffer();

        float alpha = (float) frameCapturedTime / BLUR_DURATION_TICKS;
        alpha = MathHelper.clamp(alpha, 0.0f, 1.0f);

        RenderSystem.setShaderColor(1f, 1f, 1f, alpha);
        renderTexturedQuad(buffer, matrices.peek().getPositionMatrix(),
                0, 0, width, height, 0, 1, 1, 0);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();

        if (frameCapturedTime <= 0) {
            RenderSystem.deleteTexture(blurTextureId);
            blurTextureId = -1;
        }
    }


    @Unique
    private void renderTexturedQuad(BufferBuilder buffer, Matrix4f matrix, int x, int y, int width, int height, float u1, float u2, float v1, float v2) {
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE);
        buffer.vertex(matrix, x, y, 0).texture(u1, v1).next();
        buffer.vertex(matrix, x, y + height, 0).texture(u1, v2).next();
        buffer.vertex(matrix, x + width, y + height, 0).texture(u2, v2).next();
        buffer.vertex(matrix, x + width, y, 0).texture(u2, v1).next();
        BufferRenderer.drawWithGlobalProgram(buffer.end());
    }
}