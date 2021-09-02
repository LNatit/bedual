package lnatit.hr10.interfaces;

import javax.annotation.Nullable;

public interface IPlayerEntity extends IDuallableEntity
{
    void setSleepProperties(boolean doLastSleepValid, @Nullable SleeperInfo.SleepSide side);
}
