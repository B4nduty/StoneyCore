package banduty.stoneycore.platform;

import banduty.stoneycore.platform.services.HumanoidModelSetupAnimHelper;
import banduty.stoneycore.platform.services.KeyInputHelper;
import banduty.stoneycore.platform.services.RenderFirstPersonAccessoryArmorHelper;
import banduty.stoneycore.platform.services.IClientPlatformHelper;
import banduty.stoneycore.util.render.SCRenderTypeHelper;

public class ClientPlatform {
    private static IClientPlatformHelper CLIENT_PLATFORM;
    private static SCRenderTypeHelper SC_RENDER_TYPE;
    private static HumanoidModelSetupAnimHelper HUMANOID_MODEL_SETUP_ANIM;
    private static KeyInputHelper KEY_INPUT;
    private static RenderFirstPersonAccessoryArmorHelper RENDER_FIRST_PERSON_ACCESSORY_ARMOR;

    public static void setClientPlaformHelper(IClientPlatformHelper helper) {
        CLIENT_PLATFORM = helper;
    }

    public static IClientPlatformHelper getClientPlaformHelper() {
        return CLIENT_PLATFORM;
    }

    public static void setSCRenderTypeHelper(SCRenderTypeHelper helper) {
        SC_RENDER_TYPE = helper;
    }

    public static SCRenderTypeHelper getSCRenderTypeHelper() {
        return SC_RENDER_TYPE;
    }

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

    public static void setRenderFirstPersonAccessoryArmorHelper(RenderFirstPersonAccessoryArmorHelper helper) {
        RENDER_FIRST_PERSON_ACCESSORY_ARMOR = helper;
    }

    public static RenderFirstPersonAccessoryArmorHelper getRenderFirstPersonAccessoryArmorHelper() {
        return RENDER_FIRST_PERSON_ACCESSORY_ARMOR;
    }
}
