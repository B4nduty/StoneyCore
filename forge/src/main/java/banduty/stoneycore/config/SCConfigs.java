package banduty.stoneycore.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.minecraftforge.common.ForgeConfigSpec;

import java.nio.file.Path;

public class SCConfigs {
    public static final ForgeConfigSpec SPEC;

    // Combat settings
    public static final ForgeConfigSpec.BooleanValue realisticCombat;
    public static final ForgeConfigSpec.IntValue toggleVisorTime;
    public static final ForgeConfigSpec.BooleanValue parry;
    public static final ForgeConfigSpec.DoubleValue maxBaseStamina;
    public static final ForgeConfigSpec.ConfigValue<String> staminaRecoveryFormula;

    public static final ForgeConfigSpec.DoubleValue blockingStaminaConstant;
    public static final ForgeConfigSpec.DoubleValue onBlockStaminaConstant;
    public static final ForgeConfigSpec.DoubleValue onParryStaminaConstant;
    public static final ForgeConfigSpec.DoubleValue sprintingStaminaConstant;
    public static final ForgeConfigSpec.DoubleValue jumpingStaminaConstant;
    public static final ForgeConfigSpec.DoubleValue swimmingStaminaConstant;
    public static final ForgeConfigSpec.DoubleValue attackStaminaConstant;

    public static final ForgeConfigSpec.IntValue staminaRecoverTime;

    // Technical settings
    public static final ForgeConfigSpec.IntValue maxWorkPerTick;
    public static final ForgeConfigSpec.IntValue maxLandExpandRadius;
    public static final ForgeConfigSpec.BooleanValue breakOrRemoveSiegeDestroy;

    // Land settings
    public static final ForgeConfigSpec.BooleanValue claimLand;
    public static final ForgeConfigSpec.BooleanValue removeClaimedSiege;
    public static final ForgeConfigSpec.BooleanValue hungerSiege;
    public static final ForgeConfigSpec.BooleanValue landVisitors;

    static {
        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        // Combat Section
        builder.push("combat");

        realisticCombat = builder
                .comment("Realistic Combat | Default: true")
                .define("realisticCombat", true);

        toggleVisorTime = builder
                .comment("Time (in ticks) to Toggle Visor | Default: 10")
                .defineInRange("toggleVisorTime", 10, 0, 99999);

        parry = builder
                .comment("Parry | Default: true")
                .define("parry", true);

        maxBaseStamina = builder
                .comment("Max Base Stamina (0 or less disables) | Default: 20")
                .defineInRange("maxBaseStamina", 20.0, -100000.0, 100000.0);

        staminaRecoveryFormula = builder
                .comment("Stamina Recovery Formula")
                .define("staminaRecoveryFormula", "10 - (foodLevel + health) / 5");

        blockingStaminaConstant = builder
                .comment("Blocking Stamina Constant")
                .defineInRange("blockingStaminaConstant", 0.01, 0.0, 1.0);

        onBlockStaminaConstant = builder
                .comment("On Block Stamina Constant")
                .defineInRange("onBlockStaminaConstant", 0.03, 0.0, 1.0);

        onParryStaminaConstant = builder
                .comment("On Parry Stamina Constant")
                .defineInRange("onParryStaminaConstant", 0.025, 0.0, 1.0);

        sprintingStaminaConstant = builder
                .comment("Sprinting Stamina Constant")
                .defineInRange("sprintingStaminaConstant", 0.04, 0.0, 1.0);

        jumpingStaminaConstant = builder
                .comment("Jumping Stamina Constant")
                .defineInRange("jumpingStaminaConstant", 0.01, 0.0, 1.0);

        swimmingStaminaConstant = builder
                .comment("Swimming Stamina Constant")
                .defineInRange("swimmingStaminaConstant", 0.02, 0.0, 1.0);

        attackStaminaConstant = builder
                .comment("Attack Stamina Constant")
                .defineInRange("attackStaminaConstant", 0.02, 0.0, 1.0);

        staminaRecoverTime = builder
                .comment("Stamina Time to Replenish after Using | Default: 60")
                .defineInRange("staminaRecoverTime", 60, 0, 99999);

        builder.pop();

        // Technical Section
        builder.push("technical");

        maxWorkPerTick = builder
                .comment(
                        "Max Work for Claim Worker per tick",
                        "Lower numbers will slow the claim,",
                        "but improve performance."
                )
                .defineInRange("maxWorkPerTick", 5000, 0, Integer.MAX_VALUE);

        maxLandExpandRadius = builder
                .comment("Max Land Expand Radius | Default: -1 (Unlimited)")
                .defineInRange("maxLandExpandRadius", -1, -1, Integer.MAX_VALUE);

        breakOrRemoveSiegeDestroy = builder
                .comment(
                        "Break or Remove Blocks on siege_engine destroy",
                        "false = Break",
                        "true = Remove"
                )
                .define("breakOrRemoveSiegeDestroy", false);

        builder.pop();

        // Land Section
        builder.push("land");

        claimLand = builder
                .comment("Claim Land | Default: true")
                .define("claimLand", true);

        removeClaimedSiege = builder
                .comment("Remove radius claim on defender death | Default: true")
                .define("removeClaimedSiege", true);

        hungerSiege = builder
                .comment("Hunger inside a Siege | Default: true")
                .define("hungerSiege", true);

        landVisitors = builder
                .comment("[Beta] Villagers can spawn on your land. Improve their mood so they stay there")
                .define("landVisitors", false);

        builder.pop();
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