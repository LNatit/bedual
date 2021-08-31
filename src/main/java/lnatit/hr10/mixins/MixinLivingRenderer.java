package lnatit.hr10.mixins;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingRenderer.class)
public abstract class MixinLivingRenderer<T extends LivingEntity, M extends EntityModel<T>>
{
    @Inject(
            method = "render",
            at = @At(
                    value = "FIELD",
                    target = "Lnet/minecraft/entity/Pose;SLEEPING:Lnet/minecraft/entity/Pose;",
                    shift = At.Shift.AFTER,
                    remap = false
            )
    )
    private void $render(T entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, CallbackInfo ci)
    {
        Direction direction = entityIn.getBedDirection();
        if (direction != null)
        {
            float f4 = entityIn.getEyeHeight(Pose.STANDING) - 0.1F;
            matrixStackIn.translate((double) ((float) (-direction.getXOffset()) * f4), 0.0D,
                                    (double) ((float) (-direction.getZOffset()) * f4)
            );
        }
    }

    @Inject(
            method = "applyRotations",
            at = @At("RETURN")
    )
    private void $applyRotations(T entityLiving, MatrixStack matrixStackIn, float ageInTicks, float rotationYaw, float partialTicks, CallbackInfo ci)
    {
        if (entityLiving.getPose() == Pose.SLEEPING)
        {
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(90.0F));
            matrixStackIn.translate(0.2D, -0.02D, 0.0D);
        }
    }
}
