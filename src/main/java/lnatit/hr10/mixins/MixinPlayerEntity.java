package lnatit.hr10.mixins;

import com.mojang.authlib.GameProfile;
import lnatit.hr10.interfaces.IPlayerEntity;
import lnatit.hr10.interfaces.SleeperInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nullable;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity implements IPlayerEntity
{
    @Nullable
    SleeperInfo.SleepSide sleepSide = null;
}
