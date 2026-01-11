package banduty.stoneycore.platform;

import banduty.stoneycore.event.KeyInputHandler;
import banduty.stoneycore.platform.services.KeyInputHelper;
import net.minecraft.network.chat.Component;

public class FabricKeyInputHelper implements KeyInputHelper {
    @Override
    public boolean isTogglingVisor() {
        return KeyInputHandler.isTogglingVisor;
    }

    @Override
    public boolean isVisorToggled() {
        return KeyInputHandler.visorToggled;
    }

    @Override
    public long toggleVisorTicks() {
        return KeyInputHandler.toggleVisorTicks;
    }

    @Override
    public float toggleProgress() {
        return KeyInputHandler.toggleProgress;
    }

    @Override
    public Component getTranslatedKeyMessage() {
        return KeyInputHandler.reload.getTranslatedKeyMessage();
    }
}
