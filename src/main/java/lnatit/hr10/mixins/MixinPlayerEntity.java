package lnatit.hr10.mixins;

import lnatit.hr10.interfaces.IBedBlock;
import lnatit.hr10.interfaces.IPlayerEntity;
import lnatit.hr10.interfaces.SleeperInfo;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static lnatit.hr10.interfaces.IBedBlock.PARTLY;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity extends LivingEntity implements IPlayerEntity
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
        this.doLastSleepValid = this.isSneaking();
        this.lastSleepStartTime = this.world.getDayTime();
        if (doLastSleepValid)
            this.sleepSide = SleeperInfo.getSleeperSide(this.world, pos, this);
//        setSleepProperties(, this.isSneaking() ? SleeperInfo.getSleeperSide(this.world, pos, this) : null);
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
    }

    @Override
    public void setSleepProperties(boolean doLastSleepValid, @Nullable SleeperInfo.SleepSide side)
    {
        this.doLastSleepValid = doLastSleepValid;
        this.lastSleepStartTime = this.world.getDayTime();
        this.sleepSide = side;
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
}
