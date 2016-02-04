package org.wildfly.extras.creaper.core.offline;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.wildfly.extras.creaper.core.ServerVersion;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class OfflineServerVersion {
    private static final Pattern ROOT_XMLNS = Pattern.compile("[\"']urn:jboss:domain:(\\d+)\\.(\\d+)[\"']");

    private OfflineServerVersion() {} // avoid instantiation

    /**
     * Returns the management version of the server with given {@code configurationFile}.
     *
     * @throws IOException if an I/O error occurs during file operations
     */
    static ServerVersion discover(File configurationFile) throws IOException {
        // this is not entirely cheap, but it only occurs once, during an operation that is supposed to be costly anyway
        String content = Files.toString(configurationFile, Charsets.UTF_8);
        Matcher matcher = ROOT_XMLNS.matcher(content);
        if (matcher.find()) {
            String majorStr = matcher.group(1);
            String minorStr = matcher.group(2);

            int major = Integer.parseInt(majorStr);
            int minor = Integer.parseInt(minorStr);

            // normalize the schema version numbers to management version numbers (see ServerVersion)
            if (major == 1 && minor == 0) {
                // AS 7.0.x
                major = 0;
                minor = 0;
            } else if (major == 1 && minor == 1) {
                // AS 7.1.0
                major = 1;
                minor = 0;
            } else if (major == 1 && minor == 2) {
                // AS 7.1.1
                major = 1;
                minor = 1;
            } else if (major == 1 && minor == 3) {
                // EAP 6.0.x
                // can't really recognize EAP 6.0.0 and EAP 6.0.1 here, too bad :-(
                // will pretend that it's 6.0.0
                major = 1;
                minor = 2;
            }
            // in other cases, schema version number and management version number are the same
            // with a bit of luck, noone will ever use this with AS7/EAP6 so old that the code above will matter

            return ServerVersion.from(major, minor, 0);
        }

        throw new IllegalArgumentException("Missing or bad schema version in configuration file " + configurationFile);
    }
}
