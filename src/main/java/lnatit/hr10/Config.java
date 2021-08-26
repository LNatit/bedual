package lnatit.hr10;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config
{
    public static ForgeConfigSpec.BooleanValue SHOW_LOG;
    public static ForgeConfigSpec.BooleanValue SHOW_WARN;

    public static ForgeConfigSpec init()
    {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("Universal Settings");
        {
            builder.comment("Should logger output mod log:");
            SHOW_LOG = builder.define("SHOW_LOG", true);

            builder.comment("Should logger output mod warn:");
            SHOW_WARN = builder.define("SHOW_WARN", true);
        }

        return builder.build();
    }
}
