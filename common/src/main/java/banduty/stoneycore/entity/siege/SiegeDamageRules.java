package banduty.stoneycore.entity.siege;

import banduty.stoneycore.definitions.DamageSourceConfig;
import banduty.stoneycore.items.custom.SmithingHammer;
import banduty.stoneycore.lands.LandState;
import banduty.stoneycore.siege.SiegeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;

import java.util.UUID;

/** Who may use, repair and damage an engine. */
public final class SiegeDamageRules {

    private SiegeDamageRules() {}

    public static boolean isDisabled(ServerLevel level, Player player) {
        UUID id = player.getUUID();
        return SiegeManager.getPlayerSiege(level, id).map(siege -> siege.isDisabled(id)).orElse(false);
    }

    public static boolean canOperate(ServerLevel level, Player player, BlockPos enginePos) {
        if (isDisabled(level, player)) return false;
        UUID id = player.getUUID();
        return LandState.get(level).getLandAt(enginePos)
                .map(land -> land.getOwnerUUID().equals(id) || land.isAlly(id) || player.isCreative())
                .orElse(true);
    }

    /** Heals the engine by one hammer swing. Returns true if the swing was consumed as a repair. */
    public static boolean tryRepair(AbstractSiegeEntity siege, Player player, ServerLevel level) {
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof SmithingHammer)) return false;
        if (siege.getCooldown() != 0 || siege.getHealth() >= siege.getMaxHealth()) return false;

        siege.setHealth(player.isCreative()
                ? siege.getMaxHealth()
                : Math.min(siege.getMaxHealth(), siege.getHealth() + 1F));

        if (!player.isCreative()) held.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);

        level.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                siege.getX(), siege.getY() + 1.0, siege.getZ(), 5, 0.3, 0.5, 0.3, 0.1);
        return true;
    }

    public static boolean isAllowed(Entity attacker, DamageSource source, DamageSourceConfig config) {
        String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(attacker.getType()).toString();
        if (config.canEntityDamage(entityId)) return true;

        if (attacker instanceof AbstractArrow && config.canDamageTypeDamage("projectile")) return true;

        if ((source.is(DamageTypes.EXPLOSION) || source.is(DamageTypes.PLAYER_EXPLOSION))
                && config.canDamageTypeDamage("explosion")) return true;

        if (attacker instanceof LivingEntity living) {
            ItemStack held = living.getMainHandItem();
            if (!held.isEmpty()) {
                return config.canItemDamage(BuiltInRegistries.ITEM.getKey(held.getItem()).toString());
            }
        }
        return false;
    }
}
