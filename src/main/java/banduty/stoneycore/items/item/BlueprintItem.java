
package banduty.stoneycore.items.item;

import banduty.stoneycore.screen.BlueprintScreenHandler;
import banduty.stoneycore.structure.StructureSpawnRegistry;
import banduty.stoneycore.structure.StructureSpawner;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BlueprintItem extends Item {
    private final StructureSpawner structureSpawner;
    private final String backgroundTexture;

    public BlueprintItem(StructureSpawner structureSpawner, Settings settings) {
        super(settings);
        this.structureSpawner = structureSpawner;
        this.backgroundTexture = "";
    }

    public BlueprintItem(StructureSpawner structureSpawner, String backgroundTexture, Settings settings) {
        super(settings);
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
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        Identifier id = StructureSpawnRegistry.getId(getStructureSpawner());
        if (id != null) tooltip.add(Text.translatable("text.tooltip." + id.getNamespace() + ".blueprint." + id.getPath()).formatted(Formatting.AQUA));
        super.appendTooltip(stack, world, tooltip, context);
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (!world.isClient()) {
            user.openHandledScreen(new ExtendedScreenHandlerFactory() {

                @Override
                public @NotNull ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                    return new BlueprintScreenHandler(syncId, playerInventory, itemStack, StructureSpawnRegistry.getId(getStructureSpawner()));
                }

                @Override
                public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                    buf.writeItemStack(itemStack);
                    buf.writeIdentifier(StructureSpawnRegistry.getId(getStructureSpawner()));
                }

                @Override
                public Text getDisplayName() {
                    return Text.literal("Blueprint");
                }
            });
            // user.playSound(sound, 1f, 1f);
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }
}
