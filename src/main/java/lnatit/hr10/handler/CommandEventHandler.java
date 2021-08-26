package lnatit.hr10.handler;

import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static lnatit.hr10.hr10.MOD_ID;

@Mod.EventBusSubscriber(modid = MOD_ID)
public class CommandEventHandler
{
    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event)
    {
    }
}
