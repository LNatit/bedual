package lnatit.hr10.interfaces;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;

import static net.minecraft.block.HorizontalBlock.HORIZONTAL_FACING;

public class SleeperInfo
{
    public static final Vector3d VEC_OFFSET = new Vector3d(0.5, 0.5, 0.5);

    public static SleepSide getSleeperSide(World world, BlockPos pos, LivingEntity sleeper)
    {
        SleepSide side = getSleeperSideBySleeper(world, pos, sleeper);
        return side == null ? getSleeperSideByDirection(world, pos, sleeper) : side;
    }

    private static SleepSide getSleeperSideByDirection(World world, BlockPos pos, LivingEntity sleeper)
    {
        BlockState blockState = world.getBlockState(pos);
        Direction direction = blockState.get(HORIZONTAL_FACING).rotateYCCW();
        //in this direction, the difference between their positions tells the side: positive for Left and negative for Right.
        Vector3d sleeperVec = sleeper.getPositionVec();
        Vector3d bedVec = new Vector3d(pos.getX(), pos.getY(), pos.getZ()).add(VEC_OFFSET);
        Vector3d deltaVec = sleeperVec.subtract(bedVec);
        Vector3d dirVec = new Vector3d(direction.getXOffset(), direction.getYOffset(), direction.getZOffset());
        if (deltaVec.dotProduct(dirVec) >= 0)
        {
//            if (sleeper instanceof ServerPlayerEntity)
//                sleeper.sendMessage(new StringTextComponent("sleep on RIGHT side"), Util.DUMMY_UUID);
            return SleeperInfo.SleepSide.RIGHT;
        }
        else
        {
//            if (sleeper instanceof ServerPlayerEntity)
//                sleeper.sendMessage(new StringTextComponent("sleep on LEFT side"), Util.DUMMY_UUID);
            return SleeperInfo.SleepSide.LEFT;
        }
    }

    private static SleepSide getSleeperSideBySleeper(World world, BlockPos pos, LivingEntity sleeper)
    {
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IBedTileEntity)
        {
            DuallableSleeper duallableSleeper = ((IBedTileEntity) tileEntity).getSleeper();
            if (duallableSleeper != null)
            {
                return duallableSleeper.getEmptySide();
            }
        }
        return null;
    }

    //TODO unfinished!!!
    public static class DuallableSleeper
    {
        private final IDuallableEntity[] sleeper = {null, null};
        private final boolean canDual;

        @Deprecated
        public DuallableSleeper(IDuallableEntity sleeper)
        {
            SleepSide side = sleeper.getSleepSide();
            this.sleeper[side.index] = sleeper;
            this.canDual = false;
        }

        @Deprecated
        public DuallableSleeper(IDuallableEntity sleeper, boolean canDual)
        {
            this.sleeper[0] = sleeper;
            this.canDual = canDual;
        }

        public DuallableSleeper(IDuallableEntity sleeper, @Nullable SleepSide side)
        {
            if (side != null)
            {
                this.sleeper[side.index] = sleeper;
                sleeper.setSleepSide(side);
                this.canDual = true;
            }
            else
            {
                this.sleeper[0] = sleeper;
                sleeper.setSleepSide(null);
                this.canDual = false;
            }
        }

        /**
         * check this.canDual & SleepSide before execute this method!!!
         *
         * @param sleeper sleeper entity
         * @param side    sleeper side
         */
        private void dualWithUnsafe(IDuallableEntity sleeper, @Nonnull SleepSide side)
        {
            this.sleeper[side.index] = sleeper;
            sleeper.setSleepSide(side);
        }

        public boolean dualWith(IDuallableEntity sleeper, @Nonnull SleepSide side)
        {
            if (this.canDual)
            {
                this.dualWithUnsafe(sleeper, side);
                return true;
            }
            return false;
        }

        //TODO unfinished
        public boolean dedualWith(IDuallableEntity sleeper)
        {
            SleepSide side = sleeper.getSleepSide();
            if (this.canDual && side != null)
            {
                this.sleeper[side.index] = null;
                sleeper.setSleepSide(null);
                return true;
            }
            else
            {
                this.sleeper[0] = null;
                sleeper.setSleepSide(null);
                return false;
            }
        }

        public SleepSide getEmptySide()
        {
            boolean flag = false;
            SleepSide side = null;
            for (int i = 0; i < 2; i++)
            {
                if (this.sleeper[i] == null)
                {
                    side = SleepSide.getSideByIndex(i);
                    flag = !flag;
                }
            }
            if (flag)
                return side;
            else return null;
        }

        public void sleeperExecute(Consumer<LivingEntity> function)
        {
            for (IDuallableEntity sleeperEntity : this.sleeper)
                function.accept((LivingEntity) sleeperEntity);
        }

        public void playerSleeperExecute(Consumer<PlayerEntity> function)
        {
            for (IDuallableEntity sleeperEntity : this.sleeper)
            {
                if (sleeperEntity instanceof PlayerEntity)
                    function.accept((PlayerEntity) sleeperEntity);
            }
        }

        public boolean isSleeperInvalid()
        {
            return (sleeper[0] == null && sleeper[1] == null);
        }
    }

    public enum SleepSide implements IStringSerializable
    {
        LEFT("ss_left", 0),    //0
        RIGHT("ss_right", 1);  //1

        private final String name;
        private final int index;

        SleepSide(String name, int index)
        {
            this.name = name;
            this.index = index;
        }

        public static int getIndex(SleepSide side)
        {
            if (side != null)
            return side.index;
            else return 2;
        }

        public static SleepSide getSideByIndex(int index)
        {
            switch (index)
            {
                case 0:
                    return LEFT;
                case 1:
                    return RIGHT;
                case 2:
                    return null;
                default:
                    throw new IndexOutOfBoundsException("trying to get sleepSide by index: " + index);
            }
        }

        @Nonnull
        @Override
        public String getString()
        {
            return this.name;
        }
    }
}
