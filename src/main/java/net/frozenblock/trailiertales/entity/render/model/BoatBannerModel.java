package net.frozenblock.trailiertales.entity.render.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.frozenblock.trailiertales.TrailierConstants;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class BoatBannerModel extends EntityModel<Boat> {
	private final ModelPart flag;
	private final ModelPart pole;
	private final ModelPart bar;

	public BoatBannerModel(@NotNull ModelPart root) {
		super();
		this.flag = root.getChild("flag");
		this.pole = root.getChild("pole");
		this.bar = root.getChild("bar");
	}

	@Contract("_ -> new")
	public static @NotNull ResourceLocation getTextureLocation(Boat.@NotNull Type type) {
		return TrailierConstants.id("textures/entity/boat/banner/" + type.getName() + ".png");
	}

	public static @NotNull LayerDefinition createBodyLayer() {
		MeshDefinition meshDefinition = new MeshDefinition();
		PartDefinition partDefinition = meshDefinition.getRoot();
		partDefinition.addOrReplaceChild("flag", CubeListBuilder.create().texOffs(0, 0).addBox(-10F, 0F, -2F, 20F, 40F, 1F), PartPose.ZERO);
		partDefinition.addOrReplaceChild("pole", CubeListBuilder.create().texOffs(44, 0).addBox(-1F, -30F, -1F, 2F, 42F, 2F), PartPose.ZERO);
		partDefinition.addOrReplaceChild("bar", CubeListBuilder.create().texOffs(0, 42).addBox(-10F, -32F, -1F, 20F, 2F, 2F), PartPose.ZERO);
		return LayerDefinition.create(meshDefinition, 64, 64);
	}

	@Override
	public void setupAnim(Boat entity, float limbAngle, float walkDistance, float animationProgress, float headYaw, float headPitch) {
		this.flag.xRot = Mth.cos(animationProgress * 0.2F) * Mth.PI;
		this.flag.y = -32F;
	}

	private static void setupPoseStack(@NotNull PoseStack matrices) {
		matrices.pushPose();
		matrices.translate(-0.94F, -0.25F, 0F);

		float h = -Direction.WEST.toYRot();
		matrices.mulPose(Axis.YN.rotationDegrees(h));
		matrices.translate(0F, -0.3125F, 0F);
		matrices.mulPose(Axis.XP.rotation(Mth.PI));

		matrices.pushPose();
		matrices.scale(0.6666667F, -0.6666667F, -0.6666667F);
	}

	private static void popPoseStack(@NotNull PoseStack matrices) {
		matrices.popPose();
		matrices.popPose();
	}

	public void renderFlag(
		@NotNull PoseStack matrices,
		MultiBufferSource vertexConsumers,
		int light,
		int overlay,
		DyeColor dyeColor,
		BannerPatternLayers bannerPatternLayers
	) {
		setupPoseStack(matrices);
		BannerRenderer.renderPatterns(matrices, vertexConsumers, light, overlay, this.flag, ModelBakery.BANNER_BASE, true, dyeColor, bannerPatternLayers, false);
		popPoseStack(matrices);
	}

	@Override
	public void renderToBuffer(@NotNull PoseStack matrices, VertexConsumer vertexConsumer, int i, int j, int k) {
		setupPoseStack(matrices);
		this.pole.render(matrices, vertexConsumer, i, j);
		this.bar.render(matrices, vertexConsumer, i, j);
		popPoseStack(matrices);
	}
}
