package org.wildfly.extras.creaper.core.offline;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.wildfly.extras.creaper.core.ManagementVersion;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class OfflineManagementVersion {
    private static final Pattern ROOT_XMLNS = Pattern.compile("[\"']urn:jboss:domain:(\\d+)\\.(\\d+)[\"']");

    private OfflineManagementVersion() {} // avoid instantiation

    /**
     * Returns the management version of the server with given {@code configurationFile}.
     *
     * @throws IOException if an I/O error occurs during file operations
     */
    static ManagementVersion discover(File configurationFile) throws IOException {
        String content = Files.toString(configurationFile, Charsets.UTF_8);

        // this is not entirely cheap, but it only occurs once, during an operation that is supposed to be costly anyway
        Matcher matcher = ROOT_XMLNS.matcher(content);
        if (matcher.find()) {
            String major = matcher.group(1);
            String minor = matcher.group(2);

            return ManagementVersion.from(Integer.parseInt(major), Integer.parseInt(minor), 0);
        }

        throw new IllegalArgumentException("Unknown management version in configuration file " + configurationFile);
    }
}
