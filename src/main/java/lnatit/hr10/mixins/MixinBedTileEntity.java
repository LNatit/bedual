package lnatit.hr10.mixins;

import lnatit.hr10.interfaces.IBedTileEntity;
import lnatit.hr10.interfaces.SleeperInfo;
import net.minecraft.tileentity.BedTileEntity;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;

@Mixin(BedTileEntity.class)
public abstract class MixinBedTileEntity implements IBedTileEntity
{
    @Nullable
    private SleeperInfo.DuallableSleeper sleeper = null;

    @Nullable
    @Override
    public SleeperInfo.DuallableSleeper getSleeper()
    {
        return this.sleeper;
    }

    @Override
    public void setSleeper(@Nullable SleeperInfo.DuallableSleeper sleeper)
    {
        this.sleeper = sleeper;
    }

    @Override
    public void reinitSleeper()
    {
        if (this.sleeper.isSleeperInvalid())
            this.sleeper = null;
    }
}
