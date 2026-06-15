package banduty.stoneycore.platform;

import banduty.stoneycore.platform.services.HumanoidModelSetupAnimHelper;
import banduty.stoneycore.platform.services.KeyInputHelper;

public class ClientPlatform {
    private static HumanoidModelSetupAnimHelper HUMANOID_MODEL_SETUP_ANIM;
    private static KeyInputHelper KEY_INPUT;

    public static void setHumanoidModelSetupAnimHelper(HumanoidModelSetupAnimHelper helper) {
        HUMANOID_MODEL_SETUP_ANIM = helper;
    }

    public static HumanoidModelSetupAnimHelper getHumanoidModelSetupAnimHelper() {
        return HUMANOID_MODEL_SETUP_ANIM;
    }

    public static void setKeyInputHelper(KeyInputHelper helper) {
        KEY_INPUT = helper;
    }

    public static KeyInputHelper getKeyInputHelper() {
        return KEY_INPUT;
    }
}
