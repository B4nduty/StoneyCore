package banduty.stoneycore.mixin;

import banduty.stoneycore.client.MinecraftS4S;
import banduty.stoneycore.combat.damagetype.SCDamageType;
import banduty.stoneycore.items.custom.hotiron.QuenchItem;
import banduty.stoneycore.items.custom.Tongs;
import banduty.stoneycore.platform.ClientPlatform;
import banduty.stoneycore.data.SCTags;
import banduty.stoneycore.definitions.WeaponDefinitionsStorage;
import banduty.stoneycore.combat.weapon.SCRangeWeaponUtil;
import banduty.stoneycore.combat.weapon.SCWeaponUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(Item.class)
public class ItemClientMixin {
    @Inject(method = "appendHoverText", at = @At("HEAD"))
    public void stoneycore$appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag tooltipFlag, CallbackInfo ci) {
        Item item = stack.getItem();
        if (WeaponDefinitionsStorage.isMelee(stack)) {
            boolean hasSlashing = SCWeaponUtil.hasDamageType(SCDamageType.SLASHING, item);
            boolean hasPiercing = SCWeaponUtil.hasDamageType(SCDamageType.PIERCING, item);
            boolean hasBludgeoning = SCWeaponUtil.hasDamageType(SCDamageType.BLUDGEONING, item);

            if (hasSlashing && hasBludgeoning) {
                tooltip.add(Component.translatable("component.tooltip.stoneycore.shift-right_click-bludgeoning"));
            }

            if (!hasSlashing && hasBludgeoning && hasPiercing) {
                tooltip.add(Component.translatable("component.tooltip.stoneycore.shift-right_click-bludgeoning-piercing"));
            }

            if (stack.is(SCTags.WEAPONS_HARVEST.getTag())) {
                tooltip.add(Component.translatable("component.tooltip.stoneycore.right_click-replant"));
            }

            double slashing = SCWeaponUtil.getMaxDamage(SCDamageType.SLASHING, item);
            double bludgeoning = SCWeaponUtil.getMaxDamage(SCDamageType.BLUDGEONING, item);
            double piercing = SCWeaponUtil.getMaxDamage(SCDamageType.PIERCING, item);

            if (stack.is(SCTags.BROKEN_WEAPONS.getTag()) && stack.getDamageValue() >= stack.getMaxDamage() * 0.9f) {
                slashing *= 0.25f;
                bludgeoning *= 0.25f;
                piercing *= 0.25f;
            }

            if (hasSlashing)
                tooltip.add(Component.translatable("component.tooltip.stoneycore.slashingDamage", slashing).withStyle(ChatFormatting.GREEN));
            if (hasBludgeoning)
                tooltip.add(Component.translatable("component.tooltip.stoneycore.bludgeoningDamage", bludgeoning).withStyle(ChatFormatting.GREEN));
            if (hasPiercing)
                tooltip.add(Component.translatable("component.tooltip.stoneycore.piercingDamage", piercing).withStyle(ChatFormatting.GREEN));
        }

        if (WeaponDefinitionsStorage.isRanged(stack) && SCRangeWeaponUtil.getAmmoRequirement(stack) != SCRangeWeaponUtil.AmmoRequirement.EMPTY) {
            tooltip.add(Component.translatable("component.tooltip.stoneycore.need_to_hold", ClientPlatform.getKeyInputHelper().getTranslatedKeyMessage()));
        }

        if (stack.getItem() instanceof QuenchItem quenchItem
                && quenchItem.isIgnited(stack)) {

            Long igniteTime = quenchItem.getIgniteTime(stack);

            if (igniteTime != null) {
                Level clientLevel = MinecraftS4S.minecraft().level;

                if (clientLevel != null) {
                    long currentTime = clientLevel.getGameTime();

                    long remainingTicks =
                            quenchItem.getIgniteDuration()
                                    - (currentTime - igniteTime);

                    remainingTicks = Math.max(0, remainingTicks);

                    long totalSeconds = remainingTicks / 20;

                    long hours = totalSeconds / 3600;
                    long minutes = (totalSeconds % 3600) / 60;
                    long seconds = totalSeconds % 60;

                    tooltip.add(
                            Component.translatable(
                                    "component.tooltip.stoneycore.ignitedtime",
                                    String.format(
                                            "%02d:%02d:%02d",
                                            hours,
                                            minutes,
                                            seconds
                                    )
                            )
                    );
                }
            }
        }
        if (stack.getItem() instanceof Tongs tongs) {

            ItemStack capturedItem = tongs.getCapturedItem(stack);

            if (!capturedItem.isEmpty()
                    && capturedItem.getItem() instanceof QuenchItem quenchItem
                    && quenchItem.isIgnited(capturedItem)) {

                Long igniteTime = quenchItem.getIgniteTime(capturedItem);

                if (igniteTime != null) {
                    Level clientLevel = MinecraftS4S.minecraft().level;

                    if (clientLevel != null) {
                        long currentTime = clientLevel.getGameTime();

                        long remainingTicks =
                                quenchItem.getIgniteDuration()
                                        - (currentTime - igniteTime);

                        remainingTicks = Math.max(0, remainingTicks);

                        long totalSeconds = remainingTicks / 20;

                        long hours = totalSeconds / 3600;
                        long minutes = (totalSeconds % 3600) / 60;
                        long seconds = totalSeconds % 60;

                        tooltip.add(
                                Component.translatable(
                                        "component.tooltip.stoneycore.tong_ignitedtime",
                                        String.format(
                                                "%02d:%02d:%02d",
                                                hours,
                                                minutes,
                                                seconds
                                        )
                                )
                        );
                    }
                }
            }
        }
    }
}
