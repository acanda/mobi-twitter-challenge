package ch.mobi.emme.twitter.sparql.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class VersionUtil {

    public static String getVersion() {
        try {
            return VersionUtil.class.getPackage().getImplementationVersion();
        } catch (final Exception e) {
            return "0.0.0";
        }
    }

}
