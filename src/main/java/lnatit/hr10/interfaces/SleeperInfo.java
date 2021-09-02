package lnatit.hr10.interfaces;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.swing.text.html.parser.Entity;
import java.util.function.Consumer;

import static net.minecraft.block.HorizontalBlock.HORIZONTAL_FACING;

public class SleeperInfo
{
    public static final Vector3d VEC_OFFSET = new Vector3d(0.5, 0.5, 0.5);

    public static SleeperInfo.SleepSide getSleeperSide(World world, BlockPos pos, LivingEntity sleeper)
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
            if (sleeper instanceof ServerPlayerEntity)
                sleeper.sendMessage(new StringTextComponent("sleep on RIGHT side"), Util.DUMMY_UUID);
            return SleeperInfo.SleepSide.RIGHT;
        }
        else
        {
            if (sleeper instanceof ServerPlayerEntity)
                sleeper.sendMessage(new StringTextComponent("sleep on LEFT side"), Util.DUMMY_UUID);
            return SleeperInfo.SleepSide.LEFT;
        }
    }

    public class DuallableSleeper
    {
        private IDuallableEntity[] sleeper = {null, null};
        private final boolean canDual;

        public DuallableSleeper(IDuallableEntity sleeper)
        {
            this.sleeper[0] = sleeper;
            this.canDual = false;
        }

        public DuallableSleeper(IDuallableEntity sleeper, boolean canDual)
        {
            this.sleeper[0] = sleeper;
            this.canDual = canDual;
        }

        public DuallableSleeper(IDuallableEntity sleeper, @Nonnull SleepSide side)
        {
            if (side == SleepSide.LEFT)
                this.sleeper[0] = sleeper;
            else this.sleeper[1] = sleeper;
            sleeper.setSleepSide(side);
            this.canDual = true;
        }

        public boolean dualWith(IDuallableEntity sleeper)
        {
            if (this.canDual)
            {
                this.sleeper[1] = sleeper;
                return true;
            }
            else return false;
        }

        //TODO unfinished
        public boolean dedualWith(IDuallableEntity sleeper)
        {
            return true;
        }

        public void sleeperExecute(Consumer<Entity> function)
        {
            for (IDuallableEntity sleeperEntity : this.sleeper)
                function.accept((Entity) (Object) sleeperEntity);
        }

        public void playerSleeperExecute(Consumer<PlayerEntity> function)
        {

        }
    }

    public enum SleepSide implements IStringSerializable
    {
        LEFT("ss_left"),    //0
        RIGHT("ss_right");  //1

        private final String name;

        SleepSide(String name)
        {
            this.name = name;
        }

        @Override
        public String getString()
        {
            return this.name;
        }
    }
}
