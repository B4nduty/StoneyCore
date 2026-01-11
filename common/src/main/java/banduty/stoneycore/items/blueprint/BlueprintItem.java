package banduty.stoneycore.items.blueprint;

import banduty.stoneycore.platform.Services;
import banduty.stoneycore.structure.StructureSpawnRegistry;
import banduty.stoneycore.structure.StructureSpawner;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class BlueprintItem extends Item {
    private final StructureSpawner structureSpawner;
    private final String backgroundTexture;

    public BlueprintItem(StructureSpawner structureSpawner, Item.Properties properties) {
        super(properties);
        this.structureSpawner = structureSpawner;
        this.backgroundTexture = "";
    }

    public BlueprintItem(StructureSpawner structureSpawner, String backgroundTexture, Item.Properties properties) {
        super(properties);
        this.structureSpawner = structureSpawner;
        this.backgroundTexture = backgroundTexture;
    }

    public String getBackgroundTexture() {
        return backgroundTexture;
    }

    public StructureSpawner getStructureSpawner() {
        return structureSpawner;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag tooltipFlag) {
        ResourceLocation id = StructureSpawnRegistry.getId(getStructureSpawner());
        if (id != null) {
            tooltip.add(Component.translatable("component.tooltip." + id.getNamespace() + ".blueprint." + id.getPath())
                    .withStyle(ChatFormatting.AQUA));
        }
        super.appendHoverText(stack, level, tooltip, tooltipFlag);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        if (!world.isClientSide() && user instanceof ServerPlayer serverPlayer) {
            ResourceLocation structureId = StructureSpawnRegistry.getId(getStructureSpawner());

            Services.BLUEPRINT.openBlueprintScreen(serverPlayer, itemStack, structureId);
            
            // user.playSound(sound, 1f, 1f);
        }
        return InteractionResultHolder.success(user.getItemInHand(hand));
    }
}