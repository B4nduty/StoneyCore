package banduty.stoneycore.mixin;

import banduty.stoneycore.items.custom.armor.underarmor.SCUnderArmor;
import banduty.stoneycore.items.custom.armor.underarmor.UnderArmorContents;
import banduty.stoneycore.util.data.itemdata.SCDataComponents;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public abstract class FLivingEntityMixin extends Entity {
    FLivingEntityMixin(final EntityType<?> type, final Level level) {
        super(type, level);
    }

    @WrapOperation(
            method = "doHurtEquipment",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;)V"
            )
    )
    private void stoneycore$modifyHurtAndBreak(ItemStack itemStack, int amount, LivingEntity entity, EquipmentSlot equipmentSlot, Operation<Void> original) {
        LivingEntity livingEntity = (LivingEntity) (Object) this;
        int durabilityDamage = Math.max(1, amount);
        if (itemStack.isEmpty()) return;

        if (!(itemStack.getItem() instanceof SCUnderArmor scUnderArmor)) {
            original.call(itemStack, amount, entity, equipmentSlot);
            return;
        }

        ArmorItem.Type slot = scUnderArmor.getType();
        boolean slotProtected;

        UnderArmorContents contents = itemStack.getOrDefault(SCDataComponents.UNDER_ARMOR_CONTENTS.get(), UnderArmorContents.EMPTY);

        if (!contents.isEmpty()) {
            UnderArmorContents.Mutable mutableContents = new UnderArmorContents.Mutable(contents);

            slotProtected = mutableContents.damageAttachment(slot, durabilityDamage, livingEntity);

            if (slotProtected) {
                UnderArmorContents newContents = mutableContents.toImmutable();

                if (newContents.isEmpty()) {
                    itemStack.remove(SCDataComponents.UNDER_ARMOR_CONTENTS.get());
                } else {
                    itemStack.set(
                            SCDataComponents.UNDER_ARMOR_CONTENTS.get(),
                            newContents
                    );
                }

                if (itemStack.getItem() instanceof SCUnderArmor underArmor) {
                    underArmor.rebuildAttachmentAttributes(itemStack);
                }
            }

            if (!slotProtected) {
                original.call(itemStack, amount, entity, equipmentSlot);
            }
        }
    }
}