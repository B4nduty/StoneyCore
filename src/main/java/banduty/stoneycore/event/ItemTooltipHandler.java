package banduty.stoneycore.event;

import banduty.stoneycore.StoneyCore;
import banduty.stoneycore.lands.LandType;
import banduty.stoneycore.lands.LandTypeRegistry;
import banduty.stoneycore.util.SCDamageCalculator;
import banduty.stoneycore.util.definitionsloader.SCAccessoriesDefinitionsLoader;
import banduty.stoneycore.util.definitionsloader.SCArmorDefinitionsLoader;
import banduty.stoneycore.util.definitionsloader.SCWeaponDefinitionsLoader;
import banduty.stoneycore.util.itemdata.SCTags;
import banduty.stoneycore.util.weaponutil.SCArmorUtil;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;

public class ItemTooltipHandler implements ItemTooltipCallback {
    @Override
    public void getTooltip(ItemStack stack, TooltipContext context, List<Text> lines) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) return;
        Text attackDamage = Text.translatable("attribute.name.generic.attack_damage");
        if (SCWeaponDefinitionsLoader.containsItem(stack)) lines.removeIf(line -> line.contains(attackDamage));

        for (LandType landType : LandTypeRegistry.getAll()) {
            if (stack.getItem() == landType.coreItem()) {
                lines.add(Text.translatable("text.tooltip.stoneycore.coreItem").formatted(Formatting.GOLD));
                break;
            }
        }

        if (stack.isIn(SCTags.HIDE_NAME_TAG.getTag())) lines.add(Text.translatable("text.tooltip.stoneycore.hideNameTag").formatted(Formatting.BLUE));
        if (stack.isIn(ItemTags.FREEZE_IMMUNE_WEARABLES)) lines.add(Text.translatable("text.tooltip.stoneycore.freezing").formatted(Formatting.BLUE));

        if (SCWeaponDefinitionsLoader.isRanged(stack)) {
            double baseDamage = SCWeaponDefinitionsLoader.getData(stack).ranged().baseDamage();
            lines.add(Text.translatable("text.tooltip.stoneycore.baseDamage", baseDamage).formatted(Formatting.GREEN));
        }

        if (stack.getItem() instanceof ArmorItem armorItem && SCArmorDefinitionsLoader.containsItem(armorItem)) {
            double slashing = SCArmorUtil.getResistance(SCDamageCalculator.DamageType.SLASHING, armorItem) * 100;
            double bludgeoning = SCArmorUtil.getResistance(SCDamageCalculator.DamageType.BLUDGEONING, armorItem) * 100;
            double piercing = SCArmorUtil.getResistance(SCDamageCalculator.DamageType.PIERCING, armorItem) * 100;

            if (slashing != 0) lines.add(Text.translatable("text.tooltip.stoneycore.slashingResistance", slashing).formatted(Formatting.BLUE));
            if (bludgeoning != 0) lines.add(Text.translatable("text.tooltip.stoneycore.bludgeoningResistance", bludgeoning).formatted(Formatting.BLUE));
            if (piercing != 0) lines.add(Text.translatable("text.tooltip.stoneycore.piercingResistance", (int) piercing).formatted(Formatting.BLUE));
        }

        if (SCWeaponDefinitionsLoader.isMelee(stack)) {
            double deflectChance = SCWeaponDefinitionsLoader.getData(stack).melee().deflectChance();
            if (deflectChance > 0) {
                lines.add(Text.translatable("text.tooltip.stoneycore.deflectChance", (int) (deflectChance * 100)).formatted(Formatting.AQUA));
            }
        }

        if (SCWeaponDefinitionsLoader.isAmmo(stack)) {
            double deflectChance = SCWeaponDefinitionsLoader.getData(stack).ammo().deflectChance();
            lines.add(Text.translatable("text.tooltip.stoneycore.deflectChance", (int) (deflectChance * 100)).formatted(Formatting.AQUA));
        }

        if (SCArmorDefinitionsLoader.containsItem(stack)) {
            Map<String, Double> deflectMap = SCArmorDefinitionsLoader.getData(stack).deflectChance();
            if (context.isAdvanced()) {
                deflectMap.forEach((key, value) -> {
                    try {
                        Identifier id = new Identifier(key);
                        if (value != 0) {
                            if (Registries.ITEM.containsId(id)) {
                                lines.add(Text.translatable("text.tooltip.stoneycore.deflectChanceType",
                                        Registries.ITEM.get(id).getName(), (int) (value * 100)).formatted(Formatting.AQUA));
                            } else if (Registries.ENTITY_TYPE.containsId(id)) {
                                lines.add(Text.translatable("text.tooltip.stoneycore.deflectChanceType",
                                        Registries.ENTITY_TYPE.get(id).getName(), (int) (value * 100)).formatted(Formatting.AQUA));
                            }
                        }
                    } catch (Exception e) {
                        StoneyCore.LOGGER.warn("Invalid identifier in armor deflectChance map: {}", key);
                    }
                });
            } else {
                if (!deflectMap.isEmpty()) {
                    double avg = deflectMap.values().stream()
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0.0);
                    if (avg > 0) {
                        lines.add(Text.translatable("text.tooltip.stoneycore.deflectChance", (int) (avg * 100))
                                .formatted(Formatting.AQUA));
                    }
                }
            }
        }

        if (SCAccessoriesDefinitionsLoader.containsItem(stack)) {
            Map<String, Double> deflectMap = SCAccessoriesDefinitionsLoader.getData(stack).deflectChance();
            if (context.isAdvanced()) {
                deflectMap.forEach((key, value) -> {
                    try {
                        Identifier id = new Identifier(key);
                        if (value != 0) {
                            if (Registries.ITEM.containsId(id)) {
                                lines.add(Text.translatable("text.tooltip.stoneycore.deflectChanceType",
                                        Registries.ITEM.get(id).getName(), (int) (value * 100)).formatted(Formatting.AQUA));
                            } else if (Registries.ENTITY_TYPE.containsId(id)) {
                                lines.add(Text.translatable("text.tooltip.stoneycore.deflectChanceType",
                                        Registries.ENTITY_TYPE.get(id).getName(), (int) (value * 100)).formatted(Formatting.AQUA));
                            }
                        }
                    } catch (Exception e) {
                        StoneyCore.LOGGER.warn("Invalid identifier in accessories deflectChance map: {}", key);
                    }
                });
            } else {
                if (!deflectMap.isEmpty()) {
                    double avg = deflectMap.values().stream()
                            .mapToDouble(Double::doubleValue)
                            .average()
                            .orElse(0.0);
                    if (avg > 0) {
                        lines.add(Text.translatable("text.tooltip.stoneycore.deflectChance", (int) (avg * 100))
                                .formatted(Formatting.AQUA));
                    }
                }
            }
        }
    }
}
