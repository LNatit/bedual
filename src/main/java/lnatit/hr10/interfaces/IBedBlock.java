package lnatit.hr10.interfaces;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.state.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IBedBlock
{
    BooleanProperty PARTLY = BooleanProperty.create("partly");

    default void setBedPartly(BlockState state, World world, BlockPos pos, LivingEntity sleeper, boolean partly)
    {
        world.setBlockState(pos, state.with(PARTLY, partly), 3);
    }
}
