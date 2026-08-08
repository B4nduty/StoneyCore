package banduty.stoneycore.client.render.armor;

import banduty.stoneycore.items.custom.armor.underarmor.UnderArmorContents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

import java.util.function.Consumer;

/**
 * Provides positional offset, rotation, and priority data describing how an attachment
 * influences the placement of a related render model.
 * <p>
 * This interface does not move the attachment's own {@link ItemStack}. implementors
 * describe data that a caller applies to its own model via {@link #applyPositionAndRotation}.
 */
public interface ArmorAttachmentPosition {
    default Vector3f getRotation(ItemStack stack, LivingEntity entity, UnderArmorContents contents) {return new Vector3f();}
    default Vector3f getOffset(ItemStack stack, LivingEntity entity, UnderArmorContents contents) {return new Vector3f();}
    default int getPriority(ItemStack stack, LivingEntity entity, UnderArmorContents contents) {return 0;}

    static void applyPositionAndRotation(UnderArmorContents contents, ItemStack itemStack, LivingEntity entity,
                                         Item excluded, Consumer<Vector3f> moveModel, Consumer<Vector3f> rotateModel) {
        ArmorAttachmentPosition best = resolveBest(contents, itemStack, entity, excluded);
        if (best == null) return;

        Vector3f offset = best.getOffset(itemStack, entity, contents);
        Vector3f rotation = best.getRotation(itemStack, entity, contents);

        if (!isZero(offset)) moveModel.accept(offset);
        if (!isZero(rotation)) rotateModel.accept(rotation);
    }

    static ArmorAttachmentPosition resolveBest(UnderArmorContents contents, ItemStack itemStack,
                                               LivingEntity entity, Item excluded) {
        ArmorAttachmentPosition best = null;
        int bestPriority = Integer.MIN_VALUE;

        for (ItemStack item : contents.getAttachments()) {
            if (item.getItem() == excluded) continue;
            if (!(item.getItem() instanceof ArmorAttachmentPosition position)) continue;

            int actualPriority = position.getPriority(itemStack, entity, contents);
            if (actualPriority <= bestPriority) continue;

            best = position;
            bestPriority = actualPriority;
        }

        return best;
    }

    static boolean isZero(Vector3f v) {
        return v.x == 0 && v.y == 0 && v.z == 0;
    }
}