package lnatit.hr10.network;

import lnatit.hr10.interfaces.IDuallableEntity;
import lnatit.hr10.interfaces.SleeperInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class SleepInfoPacket
{
    private final SleeperInfo.SleepSide side;
    private final int target;

    public SleepInfoPacket(SleeperInfo.SleepSide side, int target)
    {
        this.side = side;
        this.target = target;
    }

    public static void encode(SleepInfoPacket packet, PacketBuffer buffer)
    {
        buffer.writeByte(SleeperInfo.SleepSide.getIndex(packet.side));
        buffer.writeInt(packet.target);
    }

    public static SleepInfoPacket decode(PacketBuffer buffer)
    {
        return new SleepInfoPacket(SleeperInfo.SleepSide.getSideByIndex(buffer.readByte()), buffer.readInt());
    }

    public static void handle(SleepInfoPacket packet, Supplier<NetworkEvent.Context> contextSupplier)
    {
        contextSupplier.get().enqueueWork(() ->
                                          {
                                              Entity target = Minecraft.getInstance().world.getEntityByID(packet.target);
                                              if (target instanceof PlayerEntity)
                                                  ((IDuallableEntity) target).setSleepSide(packet.side);
                                          });

        contextSupplier.get().setPacketHandled(true);
    }
}
