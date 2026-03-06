package banduty.stoneycore.platform.services;

import net.minecraft.network.chat.Component;

public interface KeyInputHelper {
    boolean isTogglingVisor();

    boolean isVisorToggled();

    boolean isHidingVisor();

    long toggleVisorTicks();

    float toggleProgress();

    Component getTranslatedKeyMessage();
}
