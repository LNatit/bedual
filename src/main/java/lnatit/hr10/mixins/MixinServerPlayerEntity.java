package lnatit.hr10.mixins;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.stats.Stats;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity
{
    public MixinServerPlayerEntity(World world, BlockPos at, float yaw, GameProfile profile) throws IllegalAccessException
    {
        super(world, at, yaw, profile);
        throw new IllegalAccessException("Trying to create an instance of MixinServerPlayerEntity.class");
    }

    @Inject(
            method = "trySleep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/ServerPlayerEntity;func_242111_a(Lnet/minecraft/util/RegistryKey;Lnet/minecraft/util/math/BlockPos;FZZ)V",
                    remap = false
            ),
            cancellable = true
    )
    private void $trySleep(BlockPos at, CallbackInfoReturnable<Either<PlayerEntity.SleepResult, Unit>> cir)
    {
        java.util.Optional<BlockPos> optAt = java.util.Optional.of(at);
        this.sendMessage(new StringTextComponent("no more spawnpoints hahaha~"), Util.DUMMY_UUID);
        if (!net.minecraftforge.event.ForgeEventFactory.fireSleepingTimeCheck(this, optAt))
            cir.setReturnValue(Either.left(PlayerEntity.SleepResult.NOT_POSSIBLE_NOW));
//                    return Either.left(PlayerEntity.SleepResult.NOT_POSSIBLE_NOW);
        else
        {
            if (!this.isCreative())
            {
                Vector3d vector3d = Vector3d.copyCenteredHorizontally(at);
                List<MonsterEntity> list = this.world.getEntitiesWithinAABB(MonsterEntity.class,
                                                                            new AxisAlignedBB(
                                                                                    vector3d.getX() - 8.0D,
                                                                                    vector3d.getY() - 5.0D,
                                                                                    vector3d.getZ() - 8.0D,
                                                                                    vector3d.getX() + 8.0D,
                                                                                    vector3d.getY() + 5.0D,
                                                                                    vector3d.getZ() + 8.0D
                                                                            ),
                                                                            p_241146_1_ ->
                                                                                    p_241146_1_.func_230292_f_(this)
                );
                if (!list.isEmpty())
                    cir.setReturnValue(Either.left(PlayerEntity.SleepResult.NOT_SAFE));
//                            return Either.left(PlayerEntity.SleepResult.NOT_SAFE);
            }

            Either<PlayerEntity.SleepResult, Unit> either = super.trySleep(at).ifRight((p_241144_1_) ->
                                                                                       {
                                                                                           this.addStat(
                                                                                                   Stats.SLEEP_IN_BED);
                                                                                           CriteriaTriggers.SLEPT_IN_BED.trigger(
                                                                                                   (ServerPlayerEntity) (Object) this);
                                                                                       });
            ((ServerWorld) this.world).updateAllPlayersSleepingFlag();
            cir.setReturnValue(either);
//                    return either;
        }
    }
}
