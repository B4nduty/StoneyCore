package banduty.stoneycore.client.render;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

public interface ArmorAttachmentPosition {
    default Vector3f getRotation(ItemStack stack, LivingEntity entity) {return new Vector3f();}
    default Vector3f getOffset(ItemStack stack, LivingEntity entity) {return new Vector3f();}
}