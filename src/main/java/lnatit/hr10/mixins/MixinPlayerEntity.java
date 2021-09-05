package lnatit.hr10.mixins;

import com.mojang.datafixers.util.Either;
import lnatit.hr10.interfaces.*;
import lnatit.hr10.network.NetworkReg;
import lnatit.hr10.network.SleepInfoPacket;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static lnatit.hr10.interfaces.IBedBlock.PARTLY;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity implements IDuallableEntity
{
    private boolean doLastSleepValid;
    private long lastSleepStartTime;
    @Nullable
    private SleeperInfo.SleepSide sleepSide;

    protected MixinPlayerEntity(EntityType<? extends LivingEntity> type, World worldIn) throws IllegalAccessException
    {
        super(type, worldIn);
        throw new IllegalAccessException("Trying to create an instance of MixinPlayerEntity.class");
    }

    @Override
    public void startSleeping(@Nonnull BlockPos pos)
    {
        //TODO move this part to SleepInfo.class
//        {
//            this.doLastSleepValid = this.isSneaking();
//            this.lastSleepStartTime = this.world.getDayTime();
//            if (doLastSleepValid)
//                this.sleepSide = SleeperInfo.getSleeperSide(this.world, pos, this);
//        }
        BlockState blockstate = this.world.getBlockState(pos);
        Block block = blockstate.getBlock();
        super.startSleeping(pos);
        if (blockstate.isBed(world, pos, this))
            if (doLastSleepValid)
                ((IBedBlock) block).setBedPartly(blockstate, world, pos, this, false);
            else
                ((IBedBlock) block).setBedPartly(blockstate, world, pos, this, !blockstate.get(PARTLY));
    }

    @Inject(
            method = "trySleep",
            at = @At("HEAD"),
            cancellable = true
    )
    private void $trySleep(BlockPos at, CallbackInfoReturnable<Either<PlayerEntity.SleepResult, Unit>> cir)
    {
        this.doLastSleepValid = !this.isSneaking();
        SleeperInfo.SleepSide sleepSide = doLastSleepValid ? null : SleeperInfo.getSleeperSide(this.world, at, this);
        TileEntity tileEntity = this.world.getTileEntity(at);
        if (tileEntity instanceof IBedTileEntity)
        {
            SleeperInfo.DuallableSleeper sleeper = ((IBedTileEntity) tileEntity).getSleeper();
            if (sleeper == null)
                ((IBedTileEntity) tileEntity).setSleeper(new SleeperInfo.DuallableSleeper(this, sleepSide));
            else sleeper.dualWith(this, sleepSide);
        }
        this.lastSleepStartTime = this.world.getDayTime();
    }

    @Inject(
            method = "stopSleepInBed",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;wakeUp()V",
                    remap = false
            )
    )
    private void $stopSleepInBed(boolean updateSleepingFlag, boolean displayGuiShadow, CallbackInfo ci)
    {
        if (this.world instanceof ServerWorld)
            this.getBedPosition().ifPresent(this::resetSpawnPoints);
    }

    @Nullable
    @Override
    public SleeperInfo.SleepSide getSleepSide()
    {
        return this.sleepSide;
    }

    @Override
    public void setSleepSide(@Nullable SleeperInfo.SleepSide side)
    {
        this.sleepSide = side;
        if (this.world instanceof ServerWorld)
            NetworkReg.serverSendToPlayer(new SleepInfoPacket(side), (ServerPlayerEntity) (Object) this);
    }

    private void resetSpawnPoints(BlockPos pos)
    {
        //TODO transfer to config settings
        if (this.doLastSleepValid)
        {
            long sleepTime = this.world.getDayTime() - lastSleepStartTime;
            if (sleepTime >= 6000)
            {
                ((ServerPlayerEntity) (Object) this).func_242111_a(this.world.getDimensionKey(),
                                                                   pos,
                                                                   this.rotationYaw,
                                                                   false,
                                                                   true
                );
//                this.sendMessage(
//                        new StringTextComponent("your spawnpoint was reset due to your efficent sleep, good job!"),
//                        Util.DUMMY_UUID
//                );
            }
        }
        else
        {
            BlockState blockstate = this.world.getBlockState(pos);
            Block block = blockstate.getBlock();
            if (blockstate.isBed(world, pos, this))
                ((IBedBlock) block).setBedPartly(blockstate, world, pos, this, !blockstate.get(PARTLY));
        }

        TileEntity tileEntity = this.world.getTileEntity(pos);
        if (tileEntity instanceof IBedTileEntity)
        {
            ((IBedTileEntity) tileEntity).getSleeper().dedualWith(this);
            ((IBedTileEntity) tileEntity).reinitSleeper();
        }
    }
}
