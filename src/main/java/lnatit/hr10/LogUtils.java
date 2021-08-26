package lnatit.hr10;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LogUtils
{
    public static final Logger LOGGER = LogManager.getLogger();

    public static void warn(String str)
    {
        if (Config.SHOW_LOG.get() & Config.SHOW_WARN.get())
            LOGGER.warn(str);
    }

    public static void warn(String str, Object... args)
    {
        if (Config.SHOW_LOG.get() & Config.SHOW_WARN.get())
            LOGGER.warn(String.format(str, args));
    }

    public static void info(String str)
    {
        if (Config.SHOW_LOG.get())
            LOGGER.info(str);
    }

    public static void info(String str, Object... args)
    {
        if (Config.SHOW_LOG.get())
            LOGGER.info(String.format(str, args));
    }
}
