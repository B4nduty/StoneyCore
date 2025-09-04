package banduty.stoneycore.util.servertick;

import banduty.stoneycore.items.armor.SCAccessoryItem;
import banduty.stoneycore.util.definitionsloader.AccessoriesDefinitionsLoader;
import banduty.stoneycore.util.definitionsloader.ArmorDefinitionsLoader;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.EnumSet;
import java.util.Set;

public class ArmorUtil {

    private static final Set<EquipmentSlot> ARMOR_SLOTS = EnumSet.of(
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET
    );

    public static void startArmorCheck(ServerPlayerEntity player) {
        AccessoriesCapability.getOptionally(player).ifPresent(accessories -> {
            for (SlotEntryReference equipped : accessories.getAllEquipped()) {
                ItemStack stack = equipped.stack();
                if (!(stack.getItem() instanceof SCAccessoryItem)) continue;

                // Get the armor slot this accessory is related to
                String slotFromJson = AccessoriesDefinitionsLoader.getData(stack.getItem()).armorSlot();
                if (slotFromJson.isBlank()) continue;

                // Convert to EquipmentSlot
                EquipmentSlot targetSlot = EquipmentSlot.valueOf(slotFromJson);

                // Check if the related armor piece is equipped
                ItemStack armorPiece = player.getEquippedStack(targetSlot);
                if (!armorPiece.isEmpty() && ArmorDefinitionsLoader.containsItem(armorPiece.getItem())) {
                    // Armor is present → accessory can stay equipped
                    continue;
                }

                // Armor is missing → remove the accessory
                player.giveItemStack(stack.copy());
                stack.setCount(0);


                player.sendMessage(
                        Text.translatable(
                                "text.warning.stoneycore.armor_needed_for_accessory",
                                stack.getName(),
                                targetSlot
                        ).formatted(Formatting.DARK_RED),
                        true
                );
            }
        });
    }
}