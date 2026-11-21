package banduty.stoneycore.effect;

import banduty.stoneycore.StoneyCore;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class WindedEffect extends MobEffect {
    public WindedEffect(MobEffectCategory category, int color) {
        super(category, color);
        addAttributeModifier(Attributes.ATTACK_SPEED,
                ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "winded_attack_speed"),
                -0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.MOVEMENT_SPEED,
                ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "winded_movement_speed"),
                -0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        addAttributeModifier(Attributes.BLOCK_BREAK_SPEED,
                ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "winded_block_break_speed"),
                -0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }

    @Override
    public boolean applyEffectTick(LivingEntity livingEntity, int amplifier) {
        return true;
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return true;
    }
}