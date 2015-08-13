package org.wildfly.extras.creaper.commands.patching;

import java.util.List;

final class PatchingConversions {
    private PatchingConversions() {} // avoid instantiation

    /**
     * Converts list to single string using comma as delimiter.
     */
    public static String flatten(List<String> list) {
        if (list == null) {
            throw new IllegalArgumentException("Argument list must be provided");
        }
        StringBuilder sb = new StringBuilder();
        for (String p : list) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(p);
        }
        return sb.toString();
    }

    /**
     * Converts list of paths to string with comma as delimiter, with escaping of backslashes.
     */
    public static String flattenAndEscape(List<String> paths) {
        if (paths == null) {
            throw new IllegalArgumentException("Argument paths must be provided");
        }
        StringBuilder sb = new StringBuilder();
        for (String p : paths) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(p.replaceAll("\\\\", "\\\\\\\\")); //FIXME correct path escaping on Windows?
        }
        return sb.toString();
    }
}
