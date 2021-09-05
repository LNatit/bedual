package lnatit.hr10.network;

import lnatit.hr10.interfaces.IDuallableEntity;
import lnatit.hr10.interfaces.SleeperInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SleepInfoPacket
{
    private final SleeperInfo.SleepSide side;

    public SleepInfoPacket(SleeperInfo.SleepSide side)
    {
        this.side = side;
    }

    public static void encode(SleepInfoPacket packet, PacketBuffer buffer)
    {
        buffer.writeByte(SleeperInfo.SleepSide.getIndex(packet.side));
    }

    public static SleepInfoPacket decode(PacketBuffer buffer)
    {
        return new SleepInfoPacket(SleeperInfo.SleepSide.getSideByIndex(buffer.readByte()));
    }

    public static void handle(SleepInfoPacket packet, Supplier<NetworkEvent.Context> contextSupplier)
    {
        contextSupplier.get().enqueueWork(() ->
                                          {
                                              if (Minecraft.getInstance().player != null)
                                              {
                                                  ((IDuallableEntity) (Object) Minecraft.getInstance().player).setSleepSide(packet.side);
                                              }
                                          });

        contextSupplier.get().setPacketHandled(true);
    }
}
