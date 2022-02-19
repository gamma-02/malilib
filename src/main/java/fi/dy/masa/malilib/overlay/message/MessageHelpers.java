package fi.dy.masa.malilib.overlay.message;

import java.util.Locale;
import java.util.function.Function;
import javax.annotation.Nullable;
import fi.dy.masa.malilib.config.option.BooleanConfig;
import fi.dy.masa.malilib.util.StringUtils;

public class MessageHelpers
{
    public static String getOnOff(boolean value, boolean capitalize)
    {
        String key;

        if (capitalize)
        {
            key = value ? "malilib.label.misc.on.caps" : "malilib.label.misc.off.caps";
        }
        else
        {
            key = value ? "malilib.label.misc.on" : "malilib.label.misc.off";
        }

        return StringUtils.translate(key);
    }

    public static String getOnOffColored(boolean value, boolean capitalize)
    {
        String key;

        if (capitalize)
        {
            key = value ? "malilib.label.misc.on.caps_colored" : "malilib.label.misc.off.caps_colored";
        }
        else
        {
            key = value ? "malilib.label.misc.on.colored" : "malilib.label.misc.off.colored";
        }

        return StringUtils.translate(key);
    }

    public static String getTrueFalse(boolean value, boolean capitalize)
    {
        String key = value ? "malilib.label.misc.true.lower_case" : "malilib.label.misc.false.lower_case";
        return translateAndCapitalize(key, capitalize);
    }

    public static String getTrueFalseColored(boolean value, boolean capitalize)
    {
        String key = value ? "malilib.label.misc.true.lower_case.colored" : "malilib.label.misc.false.lower_case.colored";
        return translateAndCapitalize(key, capitalize);
    }

    public static String getYesNo(boolean value, boolean capitalize)
    {
        String key = value ? "malilib.label.misc.yes" : "malilib.label.misc.no";
        return translateAndCapitalize(key, capitalize);
    }

    public static String getYesNoColored(boolean value, boolean capitalize)
    {
        String key = value ? "malilib.label.misc.yes.colored" : "malilib.label.misc.no.colored";
        return translateAndCapitalize(key, capitalize);
    }

    public static String translateAndCapitalize(String key, boolean capitalize)
    {
        String str = StringUtils.translate(key);
        return capitalize ? str.toUpperCase(Locale.ROOT) : str;
    }

    public static String getBooleanConfigToggleMessage(BooleanConfig config,
                                                       @Nullable Function<BooleanConfig, String> messageFactory)
    {
        boolean newValue = config.getBooleanValue();
        String message;

        if (config.hasOverride())
        {
            String msgKey = newValue ? "malilib.message.info.config_overridden_on" :
                                       "malilib.message.info.config_overridden_off";
            message = StringUtils.translate(msgKey, config.getPrettyName());
        }
        else if (config.isLocked())
        {
            String msgKey = newValue ? "malilib.message.info.config_locked_on" :
                                       "malilib.message.info.config_locked_off";
            message = StringUtils.translate(msgKey, config.getPrettyName());
        }
        else if (messageFactory != null)
        {
            message = messageFactory.apply(config);
        }
        else
        {
            message = getBasicBooleanConfigToggleMessage(config);
        }

        return message;
    }

    public static String getBasicBooleanConfigToggleMessage(BooleanConfig config)
    {
        String msgKey = config.getBooleanValue() ? "malilib.message.info.toggled_config_on" :
                                                   "malilib.message.info.toggled_config_off";
        return StringUtils.translate(msgKey, config.getPrettyName());
    }
}
