package banduty.stoneycore.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class DefaultSquareModel extends Model {

    // Define ModelLayerLocation to bake in LayerDefinitions
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("stoneycore", "square_model"), "main");

    private final ModelPart root;

    public DefaultSquareModel(ModelPart root) {
        super(RenderType::entitySolid);
        this.root = root;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Standard square box (16x16x2 unit cube placed in origin, adjustable via UV offsets and box bounds)
        partdefinition.addOrReplaceChild(
                "square",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-8.0F, -8.0F, -1.0F, 16.0F, 16.0F, 2.0F),
                PartPose.ZERO
        );

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}