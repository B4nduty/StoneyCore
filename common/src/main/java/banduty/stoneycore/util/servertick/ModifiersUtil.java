package banduty.stoneycore.util.servertick;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.combat.melee.CombatSelect;
import banduty.stoneycore.util.definitionsloader.WeaponDefinitionsStorage;
import banduty.stoneycore.util.weaponutil.SCWeaponUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public class ModifiersUtil {
    private static final ResourceLocation ATTACK_RANGE_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "attack_range");
    private static final ResourceLocation RANGE_MODIFIER_ID =
            ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID, "interaction_range");

    public static void updatePlayerReachAttributes(ServerPlayer player) {
        if (player == null) return;

        ItemStack itemStack = CombatSelect.getWeaponStack(player, player.getMainHandItem());

        var attackRangeAttribute = player.getAttribute(Attributes.ENTITY_INTERACTION_RANGE);
        var rangeAttribute = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);

        boolean shouldHaveModifier = !itemStack.isEmpty() &&
                WeaponDefinitionsStorage.isMelee(itemStack);

        if (shouldHaveModifier) {
            double extraReach = SCWeaponUtil.getMaxDistance(itemStack.getItem());

            updateModifier(attackRangeAttribute, ATTACK_RANGE_MODIFIER_ID, extraReach);
            updateModifier(rangeAttribute, RANGE_MODIFIER_ID, extraReach);
        } else {
            removeModifierIfPresent(attackRangeAttribute, ATTACK_RANGE_MODIFIER_ID);
            removeModifierIfPresent(rangeAttribute, RANGE_MODIFIER_ID);
        }
    }

    private static void updateModifier(AttributeInstance attribute, ResourceLocation id, double value) {
        if (attribute == null) return;

        AttributeModifier existingModifier = attribute.getModifier(id);
        if (existingModifier == null || existingModifier.amount() != value) {
            attribute.removeModifier(id);
            attribute.addTransientModifier(
                    new AttributeModifier(id, value,
                            AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private static void removeModifierIfPresent(AttributeInstance attribute, ResourceLocation id) {
        if (attribute != null && attribute.hasModifier(id)) {
            attribute.removeModifier(id);
        }
    }
}
