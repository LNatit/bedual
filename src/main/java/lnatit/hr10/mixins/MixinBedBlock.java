package lnatit.hr10.mixins;

import lnatit.hr10.interfaces.IBedBlock;
import lnatit.hr10.interfaces.IBedTileEntity;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BedPart;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.Explosion;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nonnull;

import static net.minecraft.block.BedBlock.doesBedWork;
import static net.minecraft.block.HorizontalBlock.HORIZONTAL_FACING;

@Mixin(BedBlock.class)
public abstract class MixinBedBlock extends Block implements IBedBlock
{
    @Final
    @Shadow
    public static EnumProperty<BedPart> PART;
    @Final
    @Shadow
    public static BooleanProperty OCCUPIED;

    private MixinBedBlock(Properties properties) throws IllegalAccessException
    {
        super(properties);
        throw new IllegalAccessException("Trying to create an instance of MixinBedBlock.class");
    }

    @Inject(
            method = "<init>",
            at = @At("RETURN")
    )
    private void $BedBlock(DyeColor colorIn, AbstractBlock.Properties properties, CallbackInfo ci)
    {
        BlockState state = this.getDefaultState().with(PARTLY, false);
        this.setDefaultState(state);
    }

    /**
     * @author Locus_Natit
     * @reason there are more than one part of the method need to @Inject
     */
    @Nonnull
    @Overwrite
    public ActionResultType onBlockActivated(@Nonnull BlockState state, World worldIn, @Nonnull BlockPos pos, @Nonnull PlayerEntity player, @Nonnull Hand handIn, @Nonnull BlockRayTraceResult hit)
    {
        if (worldIn.isRemote)
        {
            //TODO finish clientside logic
            return ActionResultType.CONSUME;
        }
        else
        {
//            player.sendMessage(new StringTextComponent("This is a fucking shitty bed!!!"), Util.DUMMY_UUID);
            if (state.get(PART) != BedPart.HEAD)
            {
                pos = pos.offset(state.get(HORIZONTAL_FACING));
                state = worldIn.getBlockState(pos);
                if (!state.matchesBlock(this))
                {
//                    cir.setReturnValue(ActionResultType.CONSUME);
                    return ActionResultType.CONSUME;
                }
            }

            if (!doesBedWork(worldIn))
            {
                worldIn.removeBlock(pos, false);
                BlockPos blockpos = pos.offset(state.get(HORIZONTAL_FACING).getOpposite());
                if (worldIn.getBlockState(blockpos).matchesBlock(this))
                {
                    worldIn.removeBlock(blockpos, false);
                }

                worldIn.createExplosion(null, DamageSource.causeBedExplosionDamage(), null,
                                        (double) pos.getX() + 0.5D, (double) pos.getY() + 0.5D,
                                        (double) pos.getZ() + 0.5D, 5.0F, true, Explosion.Mode.DESTROY
                );
//                cir.setReturnValue(ActionResultType.SUCCESS);
                return ActionResultType.SUCCESS;
            }
            else if (state.get(OCCUPIED))
            {
                if (!this.tryWakeUpVillager(worldIn, pos))
                {
                    if (state.get(PARTLY))
                        tryPlayerToSleep(player, pos);
                    else player.sendStatusMessage(new TranslationTextComponent("block.minecraft.bed.occupied"), true);
                }
//                cir.setReturnValue(ActionResultType.SUCCESS);
                return ActionResultType.SUCCESS;
            }
            else
            {
                tryPlayerToSleep(player, pos);
//                cir.setReturnValue(ActionResultType.SUCCESS);
                return ActionResultType.SUCCESS;
            }
        }
    }

    @Inject(
            method = "updatePostPlacement",
            at = @At(value = "RETURN", ordinal = 0),
            cancellable = true
    )
    private void $updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos, CallbackInfoReturnable<BlockState> cir)
    {
        if (facingState.matchesBlock(this) && facingState.get(PART) != stateIn.get(PART))
        {
            BlockState state = stateIn.with(OCCUPIED, facingState.get(OCCUPIED)).with(PARTLY, facingState.get(PARTLY));
            cir.setReturnValue(state);
        }
        else cir.setReturnValue(Blocks.AIR.getDefaultState());
    }

    @Inject(
            method = "fillStateContainer",
            at = @At("HEAD"),
            cancellable = true
    )
    private void $fillStateContainer(StateContainer.Builder<Block, BlockState> builder, CallbackInfo ci)
    {
        builder.add(HORIZONTAL_FACING, PART, OCCUPIED, PARTLY);
        ci.cancel();
    }

    //TODO wakeup sleeper & hurt sleeper
    @Inject(
            method = "onFallenUpon",
            at = @At("HEAD"),
            cancellable = true
    )
    private void $onFallenUpon(World worldIn, BlockPos pos, Entity entityIn, float fallDistance, CallbackInfo ci)
    {
        if (worldIn.getBlockState(pos).get(OCCUPIED))
        {
            super.onFallenUpon(worldIn, pos, entityIn, fallDistance * 0.4F);
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if (tileEntity instanceof IBedTileEntity)
                ((IBedTileEntity) tileEntity).getSleeper().sleeperExecute(
                        sleeper -> sleeperDmgedByFallenEntity(sleeper, fallDistance * 0.2F));
            ci.cancel();
        }
    }

    //TODO store sleeper info for wakeup when entity fallen upon
    @Override
    public void setBedOccupied(BlockState state, World world, BlockPos pos, LivingEntity sleeper, boolean occupied)
    {
        super.setBedOccupied(state, world, pos, sleeper, occupied);

    }

    @Shadow
    private boolean tryWakeUpVillager(World world, BlockPos pos)
    {
        throw new IllegalStateException("Mixin failed to shadow tryWakeUpVillager()");
    }

    private void tryPlayerToSleep(PlayerEntity player, BlockPos pos)
    {
        player.trySleep(pos).ifLeft((result) ->
                                    {
                                        if (result != null)
                                        {
                                            player.sendStatusMessage(result.getMessage(), true);
                                        }
                                    });
    }
}
