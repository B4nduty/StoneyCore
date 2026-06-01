package banduty.stoneycore;

import banduty.stoneycore.block.CraftmanAnvilBlockRenderer;
import banduty.stoneycore.block.SCBlocks;
import banduty.stoneycore.client.*;
import banduty.stoneycore.entity.SCEntities;
import banduty.stoneycore.event.AttackCancelHandler;
import banduty.stoneycore.event.ClientTickHandler;
import banduty.stoneycore.event.ItemTooltipHandler;
import banduty.stoneycore.event.KeyInputHandler;
import banduty.stoneycore.items.SCItems;
import banduty.stoneycore.items.custom.armor.deco.DecoContents;
import banduty.stoneycore.items.custom.armor.deco.DecoTooltip;
import banduty.stoneycore.items.custom.armor.underarmor.UnderArmorContents;
import banduty.stoneycore.items.custom.armor.underarmor.UnderArmorTooltip;
import banduty.stoneycore.items.custom.hotiron.HotIron;
import banduty.stoneycore.items.custom.tongs.Tongs;
import banduty.stoneycore.networking.SCS2CNetworking;
import banduty.stoneycore.particle.MuzzlesFlashParticle;
import banduty.stoneycore.particle.MuzzlesSmokeParticle;
import banduty.stoneycore.particle.SCParticles;
import banduty.stoneycore.platform.*;
import banduty.stoneycore.screen.BlueprintScreen;
import banduty.stoneycore.screen.SCScreenHandlers;
import banduty.stoneycore.util.data.itemdata.SCTags;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ArmorRenderer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.TooltipComponentCallback;
import net.fabricmc.fabric.api.event.client.player.ClientPreAttackCallback;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class StoneyCoreFabricClient implements ClientModInitializer {
    private static final ResourceLocation BACKGROUND_SPRITE = ResourceLocation.withDefaultNamespace("container/bundle/background");

    @Override
    public void onInitializeClient() {
        ClientPlatform.setClientPlaformHelper(new FabricClientPlatformHelper());
        ClientPlatform.setHumanoidModelSetupAnimHelper(new FabricHumanoidModelSetupAnimHelper());
        ClientPlatform.setKeyInputHelper(new FabricKeyInputHelper());
        SCS2CNetworking.registerS2CNetworking();
        ClientPreAttackCallback.EVENT.register(new AttackCancelHandler());
        ItemTooltipCallback.EVENT.register(new ItemTooltipHandler());
        ClientTickEvents.END_CLIENT_TICK.register(new ClientTickHandler());
        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof UnderArmorTooltip(UnderArmorContents contents)) {
                return new ClientTooltipComponent() {
                    private final List<ItemStack> items = contents.attachments();

                    private int gridSizeX() {
                        return Math.max(2, (int) Math.ceil(Math.sqrt((double) items.size() + 1.0D)));
                    }

                    private int gridSizeY() {
                        return (int) Math.ceil(((double) items.size() + 1.0D) / (double) this.gridSizeX());
                    }

                    @Override
                    public int getHeight() {
                        return this.gridSizeY() * 20 + 2 + 4;
                    }

                    @Override
                    public int getWidth(Font font) {
                        return this.gridSizeX() * 18 + 2;
                    }

                    @Override
                    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
                        int columns = this.gridSizeX();
                        int rows = this.gridSizeY();
                        guiGraphics.blitSprite(BACKGROUND_SPRITE, x, y, this.getWidth(font), this.getHeight() - 4);

                        int itemIndex = 0;
                        for (int r = 0; r < rows; ++r) {
                            for (int c = 0; c < columns; ++c) {
                                int slotX = x + c * 18 + 1;
                                int slotY = y + r * 20 + 1;
                                ItemStack stack = itemIndex < items.size() ? items.get(itemIndex) : ItemStack.EMPTY;
                                renderSlot(slotX, slotY, itemIndex++, false, guiGraphics, font, items.size(), stack);
                            }
                        }
                    }
                };
            }
            return null;
        });

        TooltipComponentCallback.EVENT.register(data -> {
            if (data instanceof DecoTooltip(DecoContents contents)) {
                return new ClientTooltipComponent() {private final List<ItemStack> items = contents.items();

                    private int gridSizeX() {
                        return Math.max(2, (int) Math.ceil(Math.sqrt((double) items.size() + 1.0D)));
                    }

                    private int gridSizeY() {
                        return (int) Math.ceil(((double) items.size() + 1.0D) / (double) this.gridSizeX());
                    }

                    @Override
                    public int getHeight() {
                        return this.gridSizeY() * 20 + 2 + 4;
                    }

                    @Override
                    public int getWidth(Font font) {
                        return this.gridSizeX() * 18 + 2;
                    }

                    @Override
                    public void renderImage(Font font, int x, int y, GuiGraphics guiGraphics) {
                        int columns = this.gridSizeX();
                        int rows = this.gridSizeY();
                        guiGraphics.blitSprite(BACKGROUND_SPRITE, x, y, this.getWidth(font), this.getHeight() - 4);

                        int itemIndex = 0;
                        for (int r = 0; r < rows; ++r) {
                            for (int c = 0; c < columns; ++c) {
                                int slotX = x + c * 18 + 1;
                                int slotY = y + r * 20 + 1;
                                ItemStack stack = itemIndex < items.size() ? items.get(itemIndex) : ItemStack.EMPTY;
                                renderSlot(slotX, slotY, itemIndex++, false, guiGraphics, font, items.size(), stack);
                            }
                        }
                    }
                };
            }
            return null;
        });

        KeyInputHandler.register();
        EntityRendererRegistry.register(SCEntities.SC_BULLET.get(), SCBulletEntityRenderer::new);
        ClientOutlineRenderer.register();
        for (Item item : BuiltInRegistries.ITEM) {
            ItemProperties.register(item, ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID,"broken"),
                    (stack, world, entity, seed) ->
                            stack.is(SCTags.BROKEN_WEAPONS.getTag()) && stack.getDamageValue() >= stack.getMaxDamage() * 0.9f ? 1.0F : 0.0F);

            if (item instanceof HotIron hotIron) {
                ItemProperties.register(item, ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID,"finished"),
                        (stack, world, entity, seed) ->
                                hotIron.isFinished(stack) ? 1.0F : 0.0F);
            }

            if (item instanceof Tongs) {
                ItemProperties.register(item, ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID,"hotiron"),
                        (stack, world, entity, seed) ->
                                Tongs.getTargetStack(stack).getItem() instanceof HotIron ? 1.0F : 0.0F);
                ItemProperties.register(item, ResourceLocation.fromNamespaceAndPath(StoneyCore.MOD_ID,"finished"),
                        (stack, world, entity, seed) ->
                                !(HotIron.getTargetStack(Tongs.getTargetStack(stack)).isEmpty()) ? 1.0F : 0.0F);
            }
        }
        ArmorRenderer.register(new CrownRenderer(), SCItems.CROWN.get());

        ParticleFactoryRegistry.getInstance().register(SCParticles.MUZZLES_SMOKE_PARTICLE.get(), MuzzlesSmokeParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(SCParticles.MUZZLES_FLASH_PARTICLE.get(), MuzzlesFlashParticle.Factory::new);

        BlockEntityRenderers.register(SCBlocks.CRAFTMAN_ANVIL_BLOCK_ENTITY.get(), CraftmanAnvilBlockRenderer::new);

        MenuScreens.register(SCScreenHandlers.BLUEPRINT_SCREEN_HANDLER.get(), BlueprintScreen::new);
    }

    private void renderSlot(int x, int y, int itemIndex, boolean isBundleFull, GuiGraphics guiGraphics, Font font, int size, ItemStack itemStack) {
        if (itemIndex >= size) {
            this.blit(guiGraphics, x, y, isBundleFull ? Texture.BLOCKED_SLOT : Texture.SLOT);
        } else {
            this.blit(guiGraphics, x, y, Texture.SLOT);
            guiGraphics.renderItem(itemStack, x + 1, y + 1, itemIndex);
            guiGraphics.renderItemDecorations(font, itemStack, x + 1, y + 1);
            if (itemIndex == 0) {
                AbstractContainerScreen.renderSlotHighlight(guiGraphics, x + 1, y + 1, 0);
            }
        }

    }

    private void blit(GuiGraphics guiGraphics, int x, int y, Texture texture) {
        guiGraphics.blitSprite(texture.sprite, x, y, 0, texture.w, texture.h);
    }

    enum Texture {
        BLOCKED_SLOT(ResourceLocation.withDefaultNamespace("container/bundle/blocked_slot"), 18, 20),
        SLOT(ResourceLocation.withDefaultNamespace("container/bundle/slot"), 18, 20);

        public final ResourceLocation sprite;
        public final int w;
        public final int h;

        Texture(ResourceLocation sprite, int w, int h) {
            this.sprite = sprite;
            this.w = w;
            this.h = h;
        }
    }
}
