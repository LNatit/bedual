package lnatit.hr10.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Optional;

import static lnatit.hr10.hr10.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class NetworkReg
{
    private static final String PROTOCOL_VERSION = "1.0.0";

    public static SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    @SubscribeEvent
    public static void onNetworkRegister(FMLCommonSetupEvent event)
    {
        registerPacket();
    }


    public static void registerPacket()
    {
        INSTANCE.registerMessage(0,
                                 SleepInfoPacket.class,
                                 SleepInfoPacket::encode,
                                 SleepInfoPacket::decode,
                                 SleepInfoPacket::handle,
                                 Optional.of(NetworkDirection.PLAY_TO_CLIENT)
        );
    }

    public static void playerSendToServer(SleepInfoPacket packet)
    {
        INSTANCE.sendToServer(packet);
    }

    public static void serverSendToPlayer(SleepInfoPacket packet, ServerPlayerEntity player)
    {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static void serverSendToAllPlayer(SleepInfoPacket packet)
    {
        INSTANCE.send(PacketDistributor.ALL.noArg(), packet);
    }
}