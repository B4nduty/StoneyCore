package banduty.stoneycore.platform;

import banduty.stoneycore.block.ModBlocks;
import banduty.stoneycore.config.ConfigImpl;
import banduty.stoneycore.config.ForgeConfigImpl;
import banduty.stoneycore.event.StartTickHandler;
import banduty.stoneycore.lands.util.ClaimWorker;
import banduty.stoneycore.networking.ModMessages;
import banduty.stoneycore.networking.packet.LandTitleS2CPacket;
import banduty.stoneycore.platform.services.IPlatformHelper;
import banduty.stoneycore.recipes.BannerPatternRecipe;
import banduty.stoneycore.recipes.ManuscriptCraftingRecipe;
import banduty.stoneycore.recipes.AnvilRecipe;
import banduty.stoneycore.recipes.ModRecipes;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.slot.SlotEntryReference;
import net.bettercombat.api.AttackHand;
import net.bettercombat.logic.PlayerAttackHelper;
import net.bettercombat.logic.PlayerAttackProperties;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelManager;
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
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class ForgePlatformHelper implements IPlatformHelper {
    private final ConfigImpl config;

    public ForgePlatformHelper() {
        this.config = new ForgeConfigImpl();
    }

    @Override
    public String getPlatformName() {

        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return stack.getCraftingRemainingItem();
    }

    @Override
    public void sendTitle(ServerPlayer player, Component mainTitle) {
        ModMessages.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new LandTitleS2CPacket(mainTitle));
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
        return ForgeMod.ENTITY_REACH.get();
    }

    @Override
    public Attribute getReach() {
        return ForgeMod.ENTITY_REACH.get();
    }

    @Override
    public Block getCraftmanAnvil() {
        return ModBlocks.CRAFTMAN_ANVIL.get();
    }

    @Override
    public RecipeType<AnvilRecipe> getCraftmanAnvilRecipe() {
        return ModRecipes.ANVIL_RECIPE_TYPE.get();
    }

    @Override
    public RecipeSerializer<AnvilRecipe> getCraftmanAnvilRecipeSerializer() {
        return ModRecipes.ANVIL_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeSerializer<ManuscriptCraftingRecipe> getManuscriptRecipeSerializer() {
        return ModRecipes.MANUSCRIPT_SERIALIZER.get();
    }

    @Override
    public RecipeSerializer<BannerPatternRecipe> getBannerRecipeSerializer() {
        return ModRecipes.BANNER_SERIALIZER.get();
    }

    @Override
    public RecipeType<ManuscriptCraftingRecipe> getManuscriptRecipeType() {
        return ModRecipes.MANUSCRIPT_RECIPE_TYPE.get();
    }

    @Override
    public RecipeType<BannerPatternRecipe> getBannerRecipeType() {
        return ModRecipes.BANNER_RECIPE_TYPE.get();
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