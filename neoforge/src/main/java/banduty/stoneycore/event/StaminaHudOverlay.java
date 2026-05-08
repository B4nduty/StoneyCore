package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.util.data.entitydata.IEntityDataSaver;
import banduty.stoneycore.util.data.entitydata.SCAttributes;
import banduty.stoneycore.util.data.entitydata.StaminaData;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsStorage;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

@EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class StaminaHudOverlay {
    private static final ResourceLocation STAMINA = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "textures/overlay/stamina_bar.png");
    private static final ResourceLocation STAMINA_OVERLAY = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "textures/overlay/stamina_bar_overlay.png");
    private static final ResourceLocation STAMINA_EMPTY = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "textures/overlay/stamina_bar_empty.png");
    private static final ResourceLocation STAMINA_BLOCKED = ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "textures/overlay/stamina_bar_blocked.png");
    private static final int STAMINA_UNIT_SIZE = 8;
    private static final int EMPTY_STAMINA_WIDTH = 9;
    private static final int EMPTY_STAMINA_HEIGHT = 9;
    private static final int STAMINA_BAR_WIDTH = 9;

    private static final int STAMINA_BAR_HEIGHT = 9;

    public static final LayeredDraw.Layer STAMINA_LAYER = (guiGraphics, deltaTracker) -> {
        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;

        if (player == null || player.isSpectator() || player.isCreative() || client.options.hideGui || !stoneyCore$ableStaminaOverlay(player))
            return;

        double maxStamina = player.getAttributeValue(SCAttributes.MAX_STAMINA);
        if (maxStamina <= 0) return;

        int staminaBarX = guiGraphics.guiWidth() / 2;
        int staminaBarY = stoneyCore$getStaminaBarYPosition(player);
        double stamina = StaminaData.getStamina(player);
        boolean isStaminaBlocked = StaminaData.isStaminaBlocked((IEntityDataSaver) player);

        stoneyCore$renderStaminaBar(guiGraphics, staminaBarX, staminaBarY, stamina, isStaminaBlocked);
    };

    @SubscribeEvent
    public static void onRegisterLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(
                VanillaGuiLayers.PLAYER_HEALTH,
                ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "stamina_bar"),
                STAMINA_LAYER
        );
    }

    private static boolean stoneyCore$ableStaminaOverlay(Player player) {
        if (player == null) return false;
        boolean hasSCWeapon = WeaponDefinitionsStorage.isMelee(player.getMainHandItem());
        if (hasSCWeapon) return true;
        for (ItemStack stack : player.getArmorSlots()) {
            if (ArmorDefinitionsStorage.containsItem(stack.getItem())) return true;
        }
        return false;
    }
    private static int stoneyCore$getStaminaBarYPosition(LocalPlayer player) {
        int windowHeight = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        int baseY = windowHeight - 49;
        if (player.getAirSupply() < player.getMaxAirSupply()) baseY -= 10;
        if (player.getVehicle() instanceof net.minecraft.world.entity.LivingEntity vehicle && vehicle.showVehicleHealth()) {
            int rows = (int) Math.ceil((vehicle.getMaxHealth() / 2.0) / 10.0) - 1;
            baseY -= Math.max(0, rows) * 10;
        }
        return baseY;
    }

    private static void stoneyCore$renderStaminaBar(GuiGraphics guiGraphics, int staminaBarX, int staminaBarY, double stamina, boolean isStaminaBlocked) {
        int yOffset = StoneyCore.getConfig().visualOptions().getStaminaBarYOffset();
        int baseY = staminaBarY - yOffset;

        for (int i = 0; i < 10; i++) {
            int x = staminaBarX + 82 - (i * STAMINA_UNIT_SIZE);
            stoneyCore$renderStaminaUnit(guiGraphics, x, baseY, STAMINA_EMPTY, EMPTY_STAMINA_WIDTH, EMPTY_STAMINA_HEIGHT);
        }

        float[][] rowColors = { {1.0f, 1.0f, 0.0f}, {0.0f, 1.0f, 0.0f}, {0.0f, 0.5f, 1.0f}, {1.0f, 0.0f, 1.0f}, {1.0f, 0.5f, 0.0f}, {0.5f, 0.0f, 0.5f}, {1.0f, 1.0f, 1.0f} };
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
                stoneyCore$renderStaminaUnit(guiGraphics, x, baseY, texture, STAMINA_BAR_WIDTH, STAMINA_BAR_HEIGHT);
                guiGraphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
                if (!isStaminaBlocked) stoneyCore$renderStaminaUnit(guiGraphics, x, baseY, STAMINA_OVERLAY, STAMINA_BAR_WIDTH, STAMINA_BAR_HEIGHT);
            }
        }
    }

    private static void stoneyCore$renderStaminaUnit(GuiGraphics guiGraphics, int x, int y, ResourceLocation texture, int width, int height) {
        guiGraphics.blit(texture, x, y, 0, 0, width, height, width, height);
    }
}