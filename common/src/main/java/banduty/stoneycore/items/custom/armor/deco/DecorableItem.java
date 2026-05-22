package banduty.stoneycore.items.custom.armor.deco;

import banduty.stoneycore.items.custom.armor.SCAccessory;
import banduty.stoneycore.util.data.itemdata.SCDataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import java.util.Optional;

public class DecorableItem extends Item {
    public DecorableItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack itemStack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY) return false;

        ItemStack slotItem = slot.getItem();
        DecoContents contents = itemStack.getOrDefault(SCDataComponents.DECO_CONTENTS.get(), DecoContents.EMPTY);
        DecoContents.Mutable mutable = new DecoContents.Mutable(contents);

        if (slotItem.isEmpty()) {
            ItemStack extracted = mutable.removeLast();
            if (!extracted.isEmpty()) {
                slot.safeInsert(extracted);
                itemStack.set(SCDataComponents.DECO_CONTENTS.get(), mutable.toImmutable());
                playRemoveSound(player);
                return true;
            }
        } else {
            int inserted = mutable.tryInsert(slotItem, itemStack.getItem());
            if (inserted > 0) {
                slotItem.shrink(inserted);
                itemStack.set(SCDataComponents.DECO_CONTENTS.get(), mutable.toImmutable());
                playInsertSound(player);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack helmetStack, ItemStack incomingStack, Slot slot, ClickAction action, Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) return false;

        DecoContents contents = helmetStack.getOrDefault(SCDataComponents.DECO_CONTENTS.get(), DecoContents.EMPTY);
        DecoContents.Mutable mutable = new DecoContents.Mutable(contents);

        if (incomingStack.isEmpty()) {
            ItemStack extracted = mutable.removeLast();
            if (!extracted.isEmpty()) {
                access.set(extracted);
                helmetStack.set(SCDataComponents.DECO_CONTENTS.get(), mutable.toImmutable());
                playRemoveSound(player);
                return true;
            }
        } else {
            if (Deco.getFromItem(incomingStack.getItem()).isEmpty()) return false;
            if (!(helmetStack.getItem() instanceof SCAccessory scAccessory && Deco.getFromItem(incomingStack.getItem()).get().allowedArmorTypes().contains(scAccessory.getArmorSlot()))) return false;
            int inserted = mutable.tryInsert(incomingStack, helmetStack.getItem());
            if (inserted > 0) {
                incomingStack.shrink(inserted);
                helmetStack.set(SCDataComponents.DECO_CONTENTS.get(), mutable.toImmutable());
                playInsertSound(player);
                return true;
            }
        }
        return false;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        DecoContents contents = stack.get(SCDataComponents.DECO_CONTENTS.get());
        if (contents == null || contents.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new DecoTooltip(contents));
    }

    private void playInsertSound(Player player) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BUNDLE_INSERT, SoundSource.PLAYERS, 0.8F, 0.8F + player.getRandom().nextFloat() * 0.4F);
    }

    private void playRemoveSound(Player player) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BUNDLE_REMOVE_ONE, SoundSource.PLAYERS, 0.8F, 0.8F + player.getRandom().nextFloat() * 0.4F);
    }
}