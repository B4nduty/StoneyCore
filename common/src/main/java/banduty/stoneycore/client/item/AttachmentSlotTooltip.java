package banduty.stoneycore.client.item;

import banduty.stoneycore.definitions.ArmorAttachmentSlotDefinitionData;
import banduty.stoneycore.definitions.ArmorAttachmentSlotDefinitionsStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Adds the attachment slot an item occupies, and the slot that must be filled before it can be equipped.
 * Slot names are translatable via {@code attachment_slot.stoneycore.<slot>}, falling back to a prettified id.
 */
public final class AttachmentSlotTooltip {
    private AttachmentSlotTooltip() {}

    public static void append(ItemStack stack, List<Component> lines) {
        if (stack.isEmpty()) return;

        for (ArmorAttachmentSlotDefinitionData def : ArmorAttachmentSlotDefinitionsStorage.getSlotsForItem(stack.getItem())) {
            if (def.slot().isEmpty()) continue;

            lines.add(Component.translatable("component.tooltip.stoneycore.attachmentSlot",
                            slotName(def.slot()).withStyle(ChatFormatting.WHITE),
                            armorTypeName(ArmorAttachmentSlotDefinitionsStorage.getArmorType(def)))
                    .withStyle(ChatFormatting.GRAY));

            String required = def.requiredSlot();
            if (required != null && !required.isEmpty()) {
                lines.add(Component.translatable("component.tooltip.stoneycore.requiredSlot",
                                slotName(required).withStyle(ChatFormatting.WHITE))
                        .withStyle(ChatFormatting.RED));
            }
        }
    }

    private static MutableComponent slotName(String slot) {
        String key = "attachment_slot.stoneycore." + slot.toLowerCase(Locale.ROOT).replace(':', '.');
        return Component.translatableWithFallback(key, prettify(slot));
    }

    private static Component armorTypeName(ArmorItem.Type type) {
        String name = type.getName();
        return Component.translatableWithFallback("attachment_armor.stoneycore." + name, prettify(name));
    }

    private static String prettify(String id) {
        String path = id.contains(":") ? id.substring(id.indexOf(':') + 1) : id;
        return Arrays.stream(path.split("[_/]"))
                .filter(s -> !s.isEmpty())
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
                .collect(Collectors.joining(" "));
    }
}
