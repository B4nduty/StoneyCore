package banduty.stoneycore.event;

import banduty.stoneycore.networking.ModMessages;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class KeyInputHandler {
    public static final String STONEYCORE = "key.category.stoneycore";
    public static final String KEY_RELOAD = "key.stoneycore.reload";

    public static KeyBinding reload;

    public static void registerKeyInputs() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if (reload.isPressed()) {
                ClientPlayNetworking.send(ModMessages.RELOAD_PACKET_ID, PacketByteBufs.empty());
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

        registerKeyInputs();
    }
}
