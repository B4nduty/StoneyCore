package banduty.stoneycore.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraftforge.common.ForgeConfigSpec;

import java.nio.file.Path;

public class SCVisualConfigs {

    public static final ForgeConfigSpec SPEC;

    // Visual settings
    public static final ForgeConfigSpec.BooleanValue damageIndicator;
    public static final ForgeConfigSpec.BooleanValue visoredHelmet;
    public static final ForgeConfigSpec.DoubleValue visoredHelmetAlphaCreative;
    public static final ForgeConfigSpec.DoubleValue visoredHelmetAlphaSurvival;
    public static final ForgeConfigSpec.BooleanValue overlayThirdPerson;
    public static final ForgeConfigSpec.BooleanValue lowStaminaIndicator;
    public static final ForgeConfigSpec.BooleanValue noiseEffect;

    public static final ForgeConfigSpec.IntValue muzzlesSmokeParticlesTime;
    public static final ForgeConfigSpec.IntValue staminaBarYOffset;

    public static final ForgeConfigSpec.ConfigValue<String> hexColorTooFarClose;

    public static final ForgeConfigSpec.ConfigValue<String> claimOutlineColor;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        damageIndicator = builder
                .comment("Damage Indicator | Default: false")
                .define("damageIndicator", false);

        visoredHelmet = builder
                .comment("Visored Helmet Overlay | Default: true")
                .define("visoredHelmet", true);

        visoredHelmetAlphaCreative = builder
                .comment("Visored Helmet Overlay Alpha in Creative | Default: 0.4")
                .defineInRange("visoredHelmetAlphaCreative", 0.4, 0.0, 1.0);

        visoredHelmetAlphaSurvival = builder
                .comment("Visored Helmet Overlay Alpha in Survival | Default: 1.0")
                .defineInRange("visoredHelmetAlphaSurvival", 1.0, 0.0, 1.0);

        overlayThirdPerson = builder
                .comment("Visor Overlay being visible in Third Person | Default: true")
                .define("overlayThirdPerson", true);

        lowStaminaIndicator = builder
                .comment("Low Stamina Indicator | Default: true")
                .define("lowStaminaIndicator", true);

        noiseEffect = builder
                .comment("Noise Effect in Low Stamina Indicator | Default: true")
                .define("noiseEffect", true);

        muzzlesSmokeParticlesTime = builder
                .comment("Muzzles Smoke Particles Time Active (Seconds) | Default: 60")
                .defineInRange("muzzlesSmokeParticlesTime", 60, 0, Integer.MAX_VALUE);

        staminaBarYOffset = builder
                .comment("Stamina Bar Y Offset | Default: 0")
                .defineInRange("staminaBarYOffset", 0, Integer.MIN_VALUE, Integer.MAX_VALUE);

        hexColorTooFarClose = builder
                .comment("Hex Color Too/Far Close")
                .define("hexColorTooFarClose", "0xFFFFFF");

        claimOutlineColor = builder
                .comment("Claim Outline Render Hex Color")
                .define("claimOutlineColor", "0x00FFFF80");

        SPEC = builder.build();
    }

    public static void loadConfig(ForgeConfigSpec spec, Path path) {
        final CommentedFileConfig configData = CommentedFileConfig.builder(path)
                .sync()
                .autosave()
                .preserveInsertionOrder()
                .build();
        configData.load();
        spec.setConfig(configData);
    }
}