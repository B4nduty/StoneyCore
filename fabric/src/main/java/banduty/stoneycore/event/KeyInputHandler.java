package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.custom.armor.SCAccessory;
import banduty.stoneycore.items.custom.armor.underarmor.SCUnderArmor;
import banduty.stoneycore.networking.payload.ReloadC2SPacket;
import banduty.stoneycore.networking.payload.ToggleVisorC2SPacket;
import banduty.stoneycore.sounds.SCSounds;
import banduty.stoneycore.util.data.itemdata.SCDataComponents;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class KeyInputHandler {
    public static final String STONEYCORE = "key.category.stoneycore";
    public static final String KEY_RELOAD = "key.stoneycore.reload";
    public static final String KEY_TOGGLE_VISOR = "key.stoneycore.toggle_visor";
    public static final String KEY_HIDE_VISOR = "key.stoneycore.hide_visor";

    public static KeyMapping reload;
    public static KeyMapping toggleVisor;
    public static KeyMapping hideVisor;
    public static long toggleVisorTicks = 0;
    public static boolean isTogglingVisor = false;
    public static float toggleProgress = 0.0f;
    public static boolean visorToggled = false;
    public static boolean visorHidden = false;

    public static void registerKeyInputs() {

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (reload.isDown()) {
                ClientPlayNetworking.send(new ReloadC2SPacket());
            }
            if (hideVisor.isDown()) {
                visorHidden = hideVisor.isDown();
            } else visorHidden = false;
            LocalPlayer localPlayer = Minecraft.getInstance().player;
            if (localPlayer != null) {
                if (toggleVisor.isDown()) {
                    if (FabricLoader.getInstance().isModLoaded("accessories")) {
                        if (!isTogglingVisor) {
                            ItemStack itemStack = localPlayer.getItemBySlot(EquipmentSlot.HEAD);
                            for (ItemStack accessoryStack : SCUnderArmor.getAccessories(itemStack)) {
                                if (accessoryStack.getItem() instanceof SCAccessory scAccessory && scAccessory.hasOpenVisor(accessoryStack)) {
                                    isTogglingVisor = true;
                                    break;
                                }
                            }
                            if (!isTogglingVisor) return;
                        }

                        toggleVisorTicks++;

                        toggleProgress = Math.min(1.0f, (float) toggleVisorTicks / (StoneyCore.getConfig().combatOptions().getToggleVisorTime() - 1));

                        if (!visorToggled && toggleVisorTicks >= StoneyCore.getConfig().combatOptions().getToggleVisorTime()) {

                            // Determine visor state BEFORE toggle
                            boolean isCurrentlyOpen = false;

                            ItemStack itemStack = localPlayer.getItemBySlot(EquipmentSlot.HEAD);
                            for (ItemStack accessoryStack : SCUnderArmor.getAccessories(itemStack)) {
                                if (accessoryStack.getItem() instanceof SCAccessory scAccessory && scAccessory.hasOpenVisor(accessoryStack)) {
                                    isCurrentlyOpen = Boolean.TRUE.equals(accessoryStack.get(SCDataComponents.VISOR_OPEN.get()));
                                    break;
                                }
                            }

                            // Play appropriate sound
                            if (isCurrentlyOpen) {
                                localPlayer.playSound(SCSounds.VISOR_CLOSE.get(), 1.0F, 1.0F);
                            } else {
                                localPlayer.playSound(SCSounds.VISOR_OPEN.get(), 1.0F, 1.0F);
                            }

                            ClientPlayNetworking.send(new ToggleVisorC2SPacket());
                            visorToggled = true;
                            isTogglingVisor = false;
                            toggleProgress = 0.0f;
                        }
                    }
                } else {
                    isTogglingVisor = false;
                    toggleProgress = 0.0f;
                    visorToggled = false;
                    toggleVisorTicks = 0;
                }
            }
        });
    }

    public static void register() {
        reload = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                KEY_RELOAD,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                STONEYCORE
        ));

        toggleVisor = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                KEY_TOGGLE_VISOR,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,
                STONEYCORE
        ));

        hideVisor = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                KEY_HIDE_VISOR,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                STONEYCORE
        ));
        registerKeyInputs();
    }
}