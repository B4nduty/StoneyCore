package banduty.stoneycore.mixin;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.StoneyCoreClient;
import banduty.stoneycore.event.KeyInputHandler;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.itemdata.SCTags;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import banduty.stoneycore.util.data.playerdata.IEntityDataSaver;
import banduty.stoneycore.util.data.playerdata.StaminaData;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.systems.VertexSorter;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import io.wispforest.owo.shader.BlurProgram;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Unique
    private static final Identifier VISOR_HELMET = new Identifier(StoneyCore.MOD_ID, "textures/overlay/visor_helmet.png");

    // Surely there's a better way to do this, but it works, so I don't care
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/render/DiffuseLighting;enableGuiDepthLighting()V",
                    shift = At.Shift.AFTER
            )
    )
    private void renderSCThings(float tickDelta, long startTime, boolean tick, CallbackInfo ci) {
        GameRenderer gameRenderer = (GameRenderer) (Object) this;

        if (!tick || gameRenderer.getClient().world == null) return;

        BufferBuilderStorage buffers = ((GameRendererAccessor) gameRenderer).getBuffers();
        DrawContext context = new DrawContext(gameRenderer.getClient(), buffers.getEntityVertexConsumers());

        Window window = gameRenderer.getClient().getWindow();
        RenderSystem.clear(256, MinecraftClient.IS_SYSTEM_MAC);
        Matrix4f matrix4f = (new Matrix4f()).setOrtho(0.0F, (float)((double)window.getFramebufferWidth() / window.getScaleFactor()), (float)((double)window.getFramebufferHeight() / window.getScaleFactor()), 0.0F, 1000.0F, 21000.0F);
        RenderSystem.setProjectionMatrix(matrix4f, VertexSorter.BY_Z);
        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.loadIdentity();
        matrixStack.translate(0.0F, 0.0F, -11000.0F);
        RenderSystem.applyModelViewMatrix();
        DiffuseLighting.enableGuiDepthLighting();

        // Just move outside the code, because I know I will somehow make this thing crash the NASA
        dontMessItNow(context, tickDelta);

        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        context.draw();
        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();
    }

    @Unique
    private void dontMessItNow(DrawContext context, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        ClientPlayerEntity player = client.player;

        if (player == null || player.isSpectator()) return;

        int width = context.getScaledWindowWidth();
        int height = context.getScaledWindowHeight();

        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        if (AccessoriesCapability.getOptionally(player).isPresent()) {
            for (SlotEntryReference equipped : AccessoriesCapability.get(player).getAllEquipped()) {
                ItemStack itemStack = equipped.stack();
                if (!(NBTDataHelper.get(itemStack, INBTKeys.VISOR_OPEN, false)) && itemStack.isIn(SCTags.VISORED_HELMET.getTag()) && StoneyCore.getConfig().visualOptions.getVisoredHelmet()) {
                    RenderSystem.setShaderTexture(0, VISOR_HELMET);
                    context.setShaderColor(1f, 1f, 1f, player.isCreative() ? StoneyCore.getConfig().visualOptions.getVisoredHelmetAlpha() : 1f);
                    context.drawTexture(VISOR_HELMET, 0, 0, 0, 0, width, height, width, height);
                    break;
                }
            }
        }
        context.setShaderColor(1f, 1f, 1f, 1f);

        double stamina = StaminaData.getStamina(player);
        double secondLevel = player.getAttributeBaseValue(StoneyCore.MAX_STAMINA.get()) * 0.15d;

        if (!player.isCreative() && StaminaData.isStaminaBlocked((IEntityDataSaver) player) && StoneyCore.getConfig().visualOptions.getLowStaminaIndicator()) {
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

        renderVisorToggleProgress(context, tickDelta);

        StoneyCoreClient.LAND_TITLE_RENDERER.render(context);
    }

    @Unique
    private static final BlurProgram BLUR = new BlurProgram();

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

    @Unique
    private static final Identifier VISOR_PROGRESS_BACKGROUND = new Identifier(StoneyCore.MOD_ID, "textures/overlay/visor_progress_background.png");
    @Unique
    private static final Identifier VISOR_PROGRESS_BAR = new Identifier(StoneyCore.MOD_ID, "textures/overlay/visor_progress_bar.png");
    @Unique
    private float lastRenderedProgress = 0.0f;

    @Unique
    private void renderVisorToggleProgress(DrawContext context, float tickDelta) {
        if (StoneyCore.getConfig().combatOptions.getToggleVisorTime() == 0
                || !KeyInputHandler.isTogglingVisor
                || KeyInputHandler.visorToggled
                || KeyInputHandler.toggleVisorTicks <= 0.0f) {
            lastRenderedProgress = 0.0f;
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        int yOffset = 50;

        int bgWidth = 128;
        int bgHeight = 16;
        int barWidth = 124;
        int barHeight = 12;

        float targetProgress = KeyInputHandler.toggleProgress;
        float interpolationSpeed = 20.0f;

        float smoothProgress = lastRenderedProgress + (targetProgress - lastRenderedProgress) * Math.min(1.0f, interpolationSpeed * tickDelta);
        lastRenderedProgress = smoothProgress;

        int progressWidth = (int) (barWidth * smoothProgress);

        int bgX = centerX - bgWidth / 2;
        int bgY = centerY + yOffset - bgHeight / 2;

        context.drawTexture(VISOR_PROGRESS_BACKGROUND, bgX, bgY, 0, 0, bgWidth, bgHeight, bgWidth, bgHeight);

        if (progressWidth > 0) {
            int barX = centerX - barWidth / 2;
            int barY = centerY + yOffset - barHeight / 2;
            context.drawTexture(VISOR_PROGRESS_BAR, barX, barY, 0, 0, progressWidth, barHeight, barWidth, barHeight);
        }
    }
}
