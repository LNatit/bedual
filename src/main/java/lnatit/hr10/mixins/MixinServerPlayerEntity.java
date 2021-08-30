package lnatit.hr10.mixins;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.stats.Stats;
import net.minecraft.util.Direction;
import net.minecraft.util.Unit;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.UUID;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity
{
    @Shadow public abstract void sendMessage(ITextComponent component, UUID senderUUID);

    public MixinServerPlayerEntity(World world, BlockPos at, float yaw, GameProfile profile) throws IllegalAccessException
    {
        super(world, at, yaw, profile);
        throw new IllegalAccessException("Trying to create an instance of MixinServerPlayerEntity.class");
    }

    @Inject(
            method = "trySleep",
            at = @At("HEAD"),
            cancellable = true
    )
    private void $trySleep(BlockPos at, CallbackInfoReturnable<Either<PlayerEntity.SleepResult, Unit>> cir)
    {
        java.util.Optional<BlockPos> optAt = java.util.Optional.of(at);
        PlayerEntity.SleepResult ret = net.minecraftforge.event.ForgeEventFactory.onPlayerSleepInBed(this, optAt);
        if (ret != null)
            cir.setReturnValue(Either.left(ret));
//            return Either.left(ret);
        Direction direction = this.world.getBlockState(at).get(HorizontalBlock.HORIZONTAL_FACING);
        if (!this.isSleeping() && this.isAlive())
        {
            if (!this.world.getDimensionType().isNatural())
                cir.setReturnValue(Either.left(PlayerEntity.SleepResult.NOT_POSSIBLE_HERE));
//                return Either.left(PlayerEntity.SleepResult.NOT_POSSIBLE_HERE);
            else if (!this.func_241147_a_(at, direction))
                cir.setReturnValue(Either.left(PlayerEntity.SleepResult.TOO_FAR_AWAY));
//                return Either.left(PlayerEntity.SleepResult.TOO_FAR_AWAY);
            else if (this.func_241156_b_(at, direction))
                cir.setReturnValue(Either.left(PlayerEntity.SleepResult.OBSTRUCTED));
//                return Either.left(PlayerEntity.SleepResult.OBSTRUCTED);
            else
            {
//                this.func_242111_a(this.world.getDimensionKey(), at, this.rotationYaw, false, true);
                this.sendMessage(new StringTextComponent("no more spawnpoints hahaha~"), null);
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
                                                                                    ), p_241146_1_ ->
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
        else
            cir.setReturnValue(Either.left(PlayerEntity.SleepResult.OTHER_PROBLEM));
//            return Either.left(PlayerEntity.SleepResult.OTHER_PROBLEM);
    }

    @Shadow
    private boolean func_241147_a_(BlockPos at, Direction direction)
    {
        throw new IllegalStateException("Mixin failed to shadow func_241147_a_(BlockPos, Direction)");
    }

    @Shadow
    private boolean func_241156_b_(BlockPos at, Direction direction)
    {
        throw new IllegalStateException("Mixin failed to shadow func_241156_b_(BlockPos, Direction)");
    }
}
