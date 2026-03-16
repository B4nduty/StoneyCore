package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.items.armor.SCAccessoryItem;
import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.networking.packet.ReloadC2SPacket;
import banduty.stoneycore.networking.packet.ToggleVisorC2SPacket;
import banduty.stoneycore.sounds.ModSounds;
import banduty.stoneycore.util.data.itemdata.INBTKeys;
import banduty.stoneycore.util.data.keys.NBTDataHelper;
import com.mojang.blaze3d.platform.InputConstants;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
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

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        reload = new KeyMapping(
                KEY_RELOAD,
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                STONEYCORE
        );

        toggleVisor = new KeyMapping(
                KEY_TOGGLE_VISOR,
                KeyConflictContext.IN_GAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_LEFT_ALT,
                STONEYCORE
        );
        hideVisor = new KeyMapping(
                KEY_HIDE_VISOR,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_V,
                STONEYCORE
        );

        event.register(reload);
        event.register(toggleVisor);
        event.register(hideVisor);
    }

    @Mod.EventBusSubscriber(modid = StoneyCore.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ClientForgeEvents {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (reload.isDown()) {
                ModMessages.CHANNEL.send(PacketDistributor.SERVER.noArg(), new ReloadC2SPacket());
            }
            LocalPlayer localPlayer = Minecraft.getInstance().player;
            if (localPlayer != null) {
                if (hideVisor.isDown()) {
                    visorHidden = hideVisor.isDown();
                } else visorHidden = false;
                if (toggleVisor.isDown()) {
                    if (ModList.get().isLoaded("accessories")) {
                        if (!isTogglingVisor) {
                            if (AccessoriesCapability.getOptionally(localPlayer).isPresent()) {
                                for (SlotEntryReference equipped : AccessoriesCapability.get(localPlayer).getAllEquipped()) {
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

                        toggleProgress = Math.min(1.0f, (float) toggleVisorTicks / (StoneyCore.getConfig().combatOptions().getToggleVisorTime() - 1));

                        if (!visorToggled && toggleVisorTicks >= StoneyCore.getConfig().combatOptions().getToggleVisorTime()) {

                            // Determine visor state BEFORE toggle
                            boolean isCurrentlyOpen = false;

                            if (AccessoriesCapability.getOptionally(localPlayer).isPresent()) {
                                for (SlotEntryReference equipped : AccessoriesCapability.get(localPlayer).getAllEquipped()) {
                                    ItemStack stack = equipped.stack();
                                    if (!stack.isEmpty() && stack.getItem() instanceof SCAccessoryItem accessory) {
                                        if (accessory.hasOpenVisor(stack)) {
                                            isCurrentlyOpen = NBTDataHelper.get(stack, INBTKeys.VISOR_OPEN, false);
                                            break;
                                        }
                                    }
                                }
                            }

                            // Play appropriate sound
                            if (isCurrentlyOpen) {
                                localPlayer.playSound(ModSounds.VISOR_CLOSE.get(), 1.0F, 1.0F);
                            } else {
                                localPlayer.playSound(ModSounds.VISOR_OPEN.get(), 1.0F, 1.0F);
                            }

                            ModMessages.CHANNEL.send(PacketDistributor.SERVER.noArg(), new ToggleVisorC2SPacket());
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
        }
    }
}