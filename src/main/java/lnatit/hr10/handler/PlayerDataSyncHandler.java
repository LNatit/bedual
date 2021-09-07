package lnatit.hr10.handler;

import lnatit.hr10.interfaces.IDuallableEntity;
import lnatit.hr10.network.NetworkReg;
import lnatit.hr10.network.SleepInfoPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

import static lnatit.hr10.hr10.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID)
public class PlayerDataSyncHandler
{
    //TODO untested!!!
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event)
    {
        PlayerEntity player = event.getPlayer();

        if (player instanceof ServerPlayerEntity)
        {
            ServerWorld world = ((ServerPlayerEntity) player).getServerWorld();
            List<ServerPlayerEntity> playerList = world.getPlayers();

            for (ServerPlayerEntity serverPlayer : playerList)
            {
                NetworkReg.serverSendToPlayer(new SleepInfoPacket(((IDuallableEntity) serverPlayer).getSleepSide(),
                                                                  serverPlayer.getEntityId()
                ), (ServerPlayerEntity) player);
            }
        }
    }
}
