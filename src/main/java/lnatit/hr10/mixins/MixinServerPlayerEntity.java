package lnatit.hr10.mixins;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Either;
import lnatit.hr10.interfaces.IBedBlock;
import lnatit.hr10.interfaces.SleeperInfo;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.stats.Stats;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.List;

import static lnatit.hr10.interfaces.IBedBlock.PARTLY;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity extends PlayerEntity
{
    private boolean doLastSleepVaild;
    private long lastSleepStartTime;
    @Nullable
    private SleeperInfo.SleepSide sleepSide = null;

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


//            if (this.isSneaking())

            Either<PlayerEntity.SleepResult, Unit> either = super.trySleep(at).ifRight((p_241144_1_) ->
                                                                                       {
                                                                                           this.addStat(
                                                                                                   Stats.SLEEP_IN_BED);
                                                                                           CriteriaTriggers.SLEPT_IN_BED.trigger(
                                                                                                   (ServerPlayerEntity) (Object) this);
                                                                                       });
            this.lastSleepStartTime = this.world.getDayTime();
            ((ServerWorld) this.world).updateAllPlayersSleepingFlag();
            cir.setReturnValue(either);
//                    return either;
        }
    }

    @Inject(
            method = "startSleeping",
            at = @At("RETURN")
    )
    private void $startSleeping(BlockPos pos, CallbackInfo ci)
    {
        BlockState blockstate = this.world.getBlockState(pos);
        Block block = blockstate.getBlock();
        if (this.isSneaking())
        {
            this.doLastSleepVaild = false;
            if (blockstate.isBed(world, pos, this))
                ((IBedBlock) block).setBedPartly(blockstate, world, pos, this, !blockstate.get(PARTLY));
        }
        else
        {
            this.doLastSleepVaild = true;
            ((IBedBlock) block).setBedPartly(blockstate, world, pos, this, false);
        }
    }

    @Inject(
            method = "stopSleepInBed",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;stopSleepInBed(ZZ)V",
                    remap = false
            )
    )
    private void $stopSleepInBed(boolean updateSleepingFlag, boolean displayGuiShadow, CallbackInfo ci)
    {
        this.sleepSide = null;
        long sleepTime = this.world.getDayTime() - lastSleepStartTime;
        BlockPos pos = this.getBedPosition().get();
        //TODO transfer to config settings
        if (doLastSleepVaild)
        {
            if (sleepTime >= 6000)
            {
                this.func_242111_a(this.world.getDimensionKey(), pos, this.rotationYaw, false,
                                   true
                );
                this.sendMessage(
                        new StringTextComponent("your spawnpoint was reset due to your efficent sleep, good job!"),
                        Util.DUMMY_UUID
                );
            }
        }
        else
        {
            BlockState blockstate = this.world.getBlockState(pos);
            Block block = blockstate.getBlock();
            if (blockstate.isBed(world, pos, this))
                ((IBedBlock) block).setBedPartly(blockstate, world, pos, this, !blockstate.get(PARTLY));
        }
    }

    @Shadow
    public void func_242111_a(RegistryKey<World> p_242111_1_, @Nullable BlockPos p_242111_2_, float p_242111_3_, boolean p_242111_4_, boolean p_242111_5_)
    {
        throw new IllegalStateException("Mixin failed to shadow func_242111_a(RegistryKey<World>, BlockPos, F, Z, Z)");
    }
}
