package lnatit.hr10.mixins;

import lnatit.hr10.interfaces.SleeperInfo;
import net.minecraft.tileentity.BedTileEntity;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nonnull;

@Mixin(BedTileEntity.class)
public class MixinBedTileEntity
{
    @Nonnull
    public SleeperInfo.DuallableSleeper sleeper;
}
