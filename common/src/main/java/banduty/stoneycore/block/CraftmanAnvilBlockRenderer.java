package banduty.stoneycore.block;

import banduty.stoneycore.smithing.AnvilRecipe;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.*;

public class CraftmanAnvilBlockRenderer implements BlockEntityRenderer<CraftmanAnvilBlockEntity> {

    public CraftmanAnvilBlockRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(CraftmanAnvilBlockEntity entity, float tickDelta, PoseStack poseStack,
                       MultiBufferSource vertexConsumers, int light, int overlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        NonNullList<ItemStack> itemStacks = entity.getItems();

        BlockState blockState = entity.getBlockState();
        Direction facing = blockState.getValue(CraftmanAnvilBlock.FACING);

        for (int i = 0; i < itemStacks.size(); i++) {
            ItemStack itemStack = itemStacks.get(i);
            if (itemStack.isEmpty()) continue;

            poseStack.pushPose();

            poseStack.translate(0.5f, 0.66f, 0.5f);

            switch (facing) {
                case NORTH -> poseStack.translate(-0.1f, 0f, 0f);
                case SOUTH -> {
                    poseStack.mulPose(Axis.YP.rotationDegrees(180));
                    poseStack.translate(-0.1f, 0f, 0f);
                }
                case WEST -> {
                    poseStack.mulPose(Axis.YP.rotationDegrees(90));
                    poseStack.translate(-0.1f, 0f, 0f);
                }
                case EAST -> {
                    poseStack.mulPose(Axis.YP.rotationDegrees(270));
                    poseStack.translate(-0.1f, 0f, 0f);
                }
            }

            boolean twoRows = itemStacks.stream().filter(stack -> !stack.isEmpty()).count() > 3;
            int row = (twoRows && i >= 3) ? 1 : 0;

            if (twoRows && row == 0) {
                poseStack.translate(0f, 0f, -0.12f);
            } else if (twoRows) {
                poseStack.translate(0f, 0f, 0.12f);
            }

            int indexInRow = (twoRows ? i % 3 : i);
            switch (indexInRow) {
                case 1 -> poseStack.translate(0.3f, 0f, 0f);
                case 2 -> poseStack.translate(-0.3f, 0f, 0f);
                default -> {}
            }

            long seed = (long) BuiltInRegistries.ITEM.getKey(itemStack.getItem()).hashCode() + (row * 101) + indexInRow * 37L;
            Random rand = new Random(seed);

            float offsetX = -0.06f + rand.nextFloat() * 0.12f;
            float offsetZ = -0.02f + rand.nextFloat() * 0.04f;

            float rotY = -50f + rand.nextFloat() * 100f;

            poseStack.translate(offsetX, 0f, offsetZ);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotY));

            poseStack.scale(0.25f, 0.25f, 0.25f);
            poseStack.mulPose(Axis.XP.rotationDegrees(270));

            itemRenderer.renderStatic(itemStack, ItemDisplayContext.GUI, getLightLevel(entity.getLevel(),
                    entity.getBlockPos()), OverlayTexture.NO_OVERLAY, poseStack, vertexConsumers, entity.getLevel(), 1);
            poseStack.popPose();
        }

        Optional<AnvilRecipe> recipe = entity.getRecipe();
        recipe.ifPresent(anvilRecipe -> renderHitSquares(entity, poseStack, vertexConsumers, recipe.get().hitTimes(), facing));
    }

    private void renderHitSquares(CraftmanAnvilBlockEntity entity, PoseStack poseStack, MultiBufferSource vertexConsumers, int totalHits, Direction facing) {
        int hitsDone = entity.getHitCount();
        if (totalHits <= 0) return;

        poseStack.pushPose();

        poseStack.translate(0.55f, 0.655f, 0.3f);

        switch (facing) {
            case NORTH -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
                poseStack.translate(0.1f, 0.0f, -0.4f);
            }
            case WEST -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(270));
                poseStack.translate(0.25f, 0.0f, -0.15f);
            }
            case EAST -> {
                poseStack.mulPose(Axis.YP.rotationDegrees(90));
                poseStack.translate(-0.15f, 0.0f, -0.25f);
            }
        }

        poseStack.mulPose(Axis.XP.rotationDegrees(90));
        poseStack.scale(-0.005f, -0.005f, 0.005f);

        float squareSize = 8f;
        float spacing = 4f;

        int rows = (int) Math.ceil(totalHits / 10.0f) - 1;

        int squaresInLastRow = totalHits % 10;
        if (squaresInLastRow == 0) squaresInLastRow = 10;

        VertexConsumer bufferBuilder = vertexConsumers.getBuffer(RenderType.gui());

        List<SquareData> squaresToDraw = new ArrayList<>();
        Set<Integer> usedIndices = new HashSet<>();

        for (int colorGroup = rows; colorGroup >= 0; colorGroup--) {
            int squaresInThisRow = (colorGroup == rows) ? squaresInLastRow : 10;
            int a1 = Math.min(totalHits, 10);

            float totalWidth = (a1 * squareSize) + ((a1 - 1) * spacing);
            float startX = -totalWidth / 2f;

            for (int i = 0; i < squaresInThisRow; i++) {
                float x = startX + ((i + (10 - squaresInThisRow)) * (squareSize + spacing));
                float y = 0;

                float r, g, b, a = 1.0f;

                int absoluteHitIndex = ((rows - colorGroup) * 10) + i;

                if (usedIndices.contains(i)) {
                    continue;
                }

                int silr = 10 - squaresInLastRow;
                if (squaresInLastRow == 10 || squaresInLastRow == squaresInThisRow) silr = 0;
                if (absoluteHitIndex < hitsDone + silr) {
                    if (rows - colorGroup != rows) continue;
                    r = g = b = 0.3f;
                } else {
                    if (colorGroup == 0) {
                        r = g = b = 1.0f;
                    } else {
                        Random rand = new Random((long) totalHits * 101L + colorGroup * 37L);

                        float hue = (colorGroup * 0.618033988749895f) % 1.0f;
                        float saturation = 0.7f + rand.nextFloat() * 0.2f;
                        float value = 0.8f + rand.nextFloat() * 0.15f;

                        float[] color = hsvToRgb(hue, saturation, value);
                        r = color[0];
                        g = color[1];
                        b = color[2];

                        float variation = 0.08f;
                        r = clampColor(r + (rand.nextFloat() * variation * 2 - variation));
                        g = clampColor(g + (rand.nextFloat() * variation * 2 - variation));
                        b = clampColor(b + (rand.nextFloat() * variation * 2 - variation));
                    }
                }

                int i2 = i;
                if (squaresInThisRow < 10) i2 += (10 - squaresInThisRow);
                squaresToDraw.add(new SquareData(x, y, r, g, b, a, i2));
                usedIndices.add(i2);
            }
        }

        for (int i = squaresToDraw.size() - 1; i >= 0; i--) {
            SquareData square = squaresToDraw.get(i);

            Matrix4f matrix = poseStack.last().pose();
            Matrix3f normalMatrix = poseStack.last().normal();
            renderSingleSquare(bufferBuilder, matrix, normalMatrix,
                    square.x, square.y, squareSize,
                    square.r, square.g, square.b, square.a);
        }

        poseStack.popPose();
    }

    private record SquareData(float x, float y, float r, float g, float b, float a, int index) { }

    private float[] hsvToRgb(float hue, float saturation, float value) {
        float[] rgb = new float[3];

        int h = (int)(hue * 6);
        float f = hue * 6 - h;
        float p = value * (1 - saturation);
        float q = value * (1 - f * saturation);
        float t = value * (1 - (1 - f) * saturation);

        switch (h % 6) {
            case 0 -> { rgb[0] = value; rgb[1] = t; rgb[2] = p; }
            case 1 -> { rgb[0] = q; rgb[1] = value; rgb[2] = p; }
            case 2 -> { rgb[0] = p; rgb[1] = value; rgb[2] = t; }
            case 3 -> { rgb[0] = p; rgb[1] = q; rgb[2] = value; }
            case 4 -> { rgb[0] = t; rgb[1] = p; rgb[2] = value; }
            case 5 -> { rgb[0] = value; rgb[1] = p; rgb[2] = q; }
        }

        return rgb;
    }

    private float clampColor(float value) {
        return Math.max(0, Math.min(1, value));
    }

    private void renderSingleSquare(VertexConsumer bufferBuilder, Matrix4f matrix, Matrix3f normalMatrix,
                                    float x, float y, float squareSize, float r, float g, float b, float a) {
        bufferBuilder.vertex(matrix, x, y, 0)
                .color(r, g, b, a)
                .uv(0, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normalMatrix, 0, 0, 1)
                .endVertex();

        bufferBuilder.vertex(matrix, x, y + squareSize, 0)
                .color(r, g, b, a)
                .uv(0, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normalMatrix, 0, 0, 1)
                .endVertex();

        bufferBuilder.vertex(matrix, x + squareSize, y + squareSize, 0)
                .color(r, g, b, a)
                .uv(1, 1)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normalMatrix, 0, 0, 1)
                .endVertex();

        bufferBuilder.vertex(matrix, x + squareSize, y, 0)
                .color(r, g, b, a)
                .uv(1, 0)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normalMatrix, 0, 0, 1)
                .endVertex();
    }

    private int getLightLevel(Level level, BlockPos pos) {
        if (level == null) {
            return LightTexture.pack(15, 15);
        }
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}