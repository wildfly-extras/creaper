package org.wildfly.extras.creaper.core;

import java.util.Locale;

/**
 * <p>The management version in the management model is stored in 3 parts:</p>
 *
 * <ul>
 * <li>the {@code management-major-version} attribute</li>
 * <li>the {@code management-minor-version} attribute</li>
 * <li>the {@code management-micro-version} attribute</li>
 * </ul>
 *
 * @see org.wildfly.extras.creaper.core.ManagementVersion
 */
public enum ManagementVersionPart {
    MAJOR,
    MINOR,
    MICRO;

    /**
     * Returns the name of the attribute in the management model that contains the value of this part of the version
     * number.
     */
    public String attributeName() {
        return "management-" + this.name().toLowerCase(Locale.US) + "-version";
    }
}
