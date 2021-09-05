package lnatit.hr10.mixins;

import com.mojang.blaze3d.matrix.MatrixStack;
import lnatit.hr10.interfaces.IDuallableEntity;
import lnatit.hr10.interfaces.SleeperInfo;
import net.minecraft.client.entity.player.RemoteClientPlayerEntity;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.util.math.vector.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingRenderer.class)
public abstract class MixinLivingRenderer<T extends LivingEntity, M extends EntityModel<T>>
{
    @Inject(
            method = "applyRotations",
            at = @At("RETURN")
    )
    private void $applyRotations(T entityLiving, MatrixStack matrixStackIn, float ageInTicks, float rotationYaw, float partialTicks, CallbackInfo ci)
    {
        if (entityLiving.getPose() == Pose.SLEEPING)
        {
            matrixStackIn.translate(0.0D, 0.04D, 0.0D);
            if (entityLiving instanceof IDuallableEntity)
            {
                SleeperInfo.SleepSide side = ((IDuallableEntity) entityLiving).getSleepSide();
                if (side == SleeperInfo.SleepSide.LEFT)
                {
                    matrixStackIn.rotate(Vector3f.YP.rotationDegrees(90.0F));
                    matrixStackIn.translate((entityLiving instanceof RemoteClientPlayerEntity) ? 0.08D : 0.2D, 0.0D, -0.25D);          //left!!!!!
                }
                else if (side == SleeperInfo.SleepSide.RIGHT)
                {
                    matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-90.0F));
                    matrixStackIn.translate((entityLiving instanceof RemoteClientPlayerEntity) ? -0.08D : -0.2D, 0.0D, -0.25D);
                }
            }
        }
    }
}
