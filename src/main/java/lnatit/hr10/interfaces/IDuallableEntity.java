package lnatit.hr10.interfaces;

import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;

public interface IDuallableEntity
{
    SleeperInfo.SleepSide getSleepSide();

    void setSleepSide(@Nullable SleeperInfo.SleepSide side);
}
