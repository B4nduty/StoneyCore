package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.armor.SCAccessoryItem;
import banduty.stoneycore.networking.ModMessages;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class KeyInputHandler {
    public static final String STONEYCORE = "key.category.stoneycore";
    public static final String KEY_RELOAD = "key.stoneycore.reload";
    public static final String KEY_TOGGLE_VISOR = "key.stoneycore.toggle_visor";

    public static KeyBinding reload;
    public static KeyBinding toggleVisor;
    public static long toggleVisorTicks = 0;
    public static boolean isTogglingVisor = false;
    public static float toggleProgress = 0.0f;
    public static boolean visorToggled = false;

    public static void registerKeyInputs() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (reload.isPressed()) {
                ClientPlayNetworking.send(ModMessages.RELOAD_PACKET_ID, PacketByteBufs.empty());
            }

            PlayerEntity playerEntity = MinecraftClient.getInstance().player;
            if (playerEntity != null) {
                if (toggleVisor.isPressed()) {

                    if (!isTogglingVisor) {
                        if (AccessoriesCapability.getOptionally(playerEntity).isPresent()) {
                            for (SlotEntryReference equipped : AccessoriesCapability.get(playerEntity).getAllEquipped()) {
                                ItemStack itemStack = equipped.stack();
                                if (!itemStack.isEmpty() && itemStack.getItem() instanceof SCAccessoryItem scAccessoryItem &&
                                        scAccessoryItem.hasOpenVisor(itemStack)) {
                                    isTogglingVisor = true;
                                    break;
                                }
                            }
                        }

                        if (!isTogglingVisor) return;
                    }

                    toggleVisorTicks++;

                    toggleProgress = Math.min(1.0f, (float) toggleVisorTicks / (StoneyCore.getConfig().combatOptions.getToggleVisorTime() - 1));

                    if (!visorToggled && toggleVisorTicks >= StoneyCore.getConfig().combatOptions.getToggleVisorTime()) {
                        ClientPlayNetworking.send(ModMessages.TOGGLE_VISOR_ID, PacketByteBufs.create());
                        visorToggled = true;
                        isTogglingVisor = false;
                        toggleProgress = 0.0f;
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
        reload = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_RELOAD,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                STONEYCORE
        ));

        toggleVisor = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_TOGGLE_VISOR,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,
                STONEYCORE
        ));

        registerKeyInputs();
    }
}