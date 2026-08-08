package banduty.stoneycore.platform;

import banduty.stoneycore.platform.services.HumanoidModelSetupAnimHelper;
import banduty.stoneycore.platform.services.IClientPlatformHelper;
import banduty.stoneycore.platform.services.KeyInputHelper;

public class ClientPlatform {
    private static HumanoidModelSetupAnimHelper HUMANOID_MODEL_SETUP_ANIM;
    private static KeyInputHelper KEY_INPUT;
    private static IClientPlatformHelper ICLIENT_PLATFORM_HELPER;

    public static void setHumanoidModelSetupAnimHelper(HumanoidModelSetupAnimHelper helper) {
        HUMANOID_MODEL_SETUP_ANIM = helper;
    }

    public static HumanoidModelSetupAnimHelper getHumanoidModelSetupAnimHelper() {
        return HUMANOID_MODEL_SETUP_ANIM;
    }

    public static IClientPlatformHelper getIclientPlatformHelper() {
        return ICLIENT_PLATFORM_HELPER;
    }

    public static void setIclientPlatformHelper(IClientPlatformHelper helper) {
        ICLIENT_PLATFORM_HELPER = helper;
    }

    public static void setKeyInputHelper(KeyInputHelper helper) {
        KEY_INPUT = helper;
    }

    public static KeyInputHelper getKeyInputHelper() {
        return KEY_INPUT;
    }
}
