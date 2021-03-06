package lnatit.hr10.interfaces;

public interface IBedTileEntity
{
    SleeperInfo.DuallableSleeper getSleeper();

    void setSleeper(SleeperInfo.DuallableSleeper sleeper);

    /**
     * must called after each dedualWith();
     */
    void reinitSleeper();
}
