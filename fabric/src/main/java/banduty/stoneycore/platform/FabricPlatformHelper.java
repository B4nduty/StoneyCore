package banduty.stoneycore.platform;

import banduty.stoneycore.block.ModBlocks;
import banduty.stoneycore.config.ConfigImpl;
import banduty.stoneycore.config.FabricConfigImpl;
import banduty.stoneycore.event.StartTickHandler;
import banduty.stoneycore.lands.util.ClaimWorker;
import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.platform.services.IPlatformHelper;
import banduty.stoneycore.smithing.AnvilRecipe;
import banduty.stoneycore.smithing.ModRecipes;
import com.jamieswhiteshirt.reachentityattributes.ReachEntityAttributes;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.bettercombat.api.AttackHand;
import net.bettercombat.logic.PlayerAttackHelper;
import net.bettercombat.logic.PlayerAttackProperties;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class FabricPlatformHelper implements IPlatformHelper {
    private final ConfigImpl config;

    public FabricPlatformHelper() {
        this.config = new FabricConfigImpl();
    }

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return stack.getRecipeRemainder();
    }

    @Override
    public void sendTitle(ServerPlayer player, Component mainTitle) {
        FriendlyByteBuf buffer = PacketByteBufs.create();
        buffer.writeComponent(mainTitle);
        ServerPlayNetworking.send(player, ModMessages.LAND_TITLE_PACKET_ID, buffer);
    }

    @Override
    public BakedModel getBakedModel(ModelManager modelManager, ResourceLocation iModelPath) {
        return modelManager.getModel(iModelPath);
    }

    @Override
    public Queue<ClaimWorker> getClaimTasks() {
        return StartTickHandler.CLAIM_TASKS;
    }

    @Override
    public int comboCount(Player player) {
        return ((PlayerAttackProperties) player).getComboCount();
    }

    @Override
    public ItemStack getWeaponStack(Entity attacker, ItemStack defaultStack) {
        AttackHand hand = null;
        if (attacker instanceof Player player) {
            if (player instanceof PlayerAttackProperties props) {
                hand = PlayerAttackHelper.getCurrentAttack(player, props.getComboCount());
            }
        }
        ItemStack itemStack = defaultStack;
        if (hand != null) itemStack = hand.itemStack();
        return itemStack;
    }

    @Override
    public Attribute getAttackRange() {
        return ReachEntityAttributes.ATTACK_RANGE;
    }

    @Override
    public Attribute getReach() {
        return ReachEntityAttributes.REACH;
    }

    @Override
    public Block getCraftmanAnvil() {
        return ModBlocks.CRAFTMAN_ANVIL;
    }

    @Override
    public RecipeType<AnvilRecipe> getCraftmanAnvilRecipe() {
        return ModRecipes.ANVIL_RECIPE_TYPE;
    }

    @Override
    public RecipeSerializer<AnvilRecipe> getCraftmanAnvilRecipeSerializer() {
        return ModRecipes.ANVIL_RECIPE_SERIALIZER;
    }

    @Override
    public ConfigImpl getConfig() {
        return config;
    }

    @Override
    public List<ItemStack> getEquippedAccessories(LivingEntity livingEntity) {
        List<ItemStack> itemStacks = new ArrayList<>();
        if (AccessoriesCapability.getOptionally(livingEntity).isPresent()) {
            for (SlotEntryReference equipped : AccessoriesCapability.get(livingEntity).getAllEquipped()) {
                ItemStack itemStack = equipped.stack();
                if (itemStack.isEmpty()) continue;
                itemStacks.add(itemStack);
            }
        }
        return itemStacks;
    }
}
