package org.wildfly.extras.creaper.core.online;

import org.jboss.as.controller.client.helpers.ClientConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class extends {@link ClientConstants} and adds some more useful constants that are not present there.
 * It's meant to be the only set of constants that is needed.
 */
public final class Constants extends ClientConstants {
    private Constants() {} // avoid instantiation

    public static final String ALLOW_RESOURCE_SERVICE_RESTART = "allow-resource-service-restart";
    public static final String ATTRIBUTES_ONLY = "attributes-only";
    public static final String COMPOSITE = "composite";
    public static final String CORE_SERVICE = "core-service";
    public static final String DOMAIN_FAILURE_DESCRIPTION = "domain-failure-description";
    public static final String FAILED = "failed";
    public static final String HOST_STATE = "host-state";
    public static final String INCLUDE_DEFAULTS = "include-defaults";
    public static final String INTERFACE = "interface";
    public static final String PROCESS_STATE = "process-state";
    public static final String PROFILE = "profile";
    public static final String READ_CHILDREN_TYPES = "read-children-types";
    public static final String RECURSIVE_DEPTH = "recursive-depth";
    public static final String RELOAD = "reload";
    public static final String RESPONSE = "response";
    public static final String RESPONSE_HEADERS = "response-headers";
    public static final String RESTART = "restart";
    public static final String SERVER_GROUPS = "server-groups";
    public static final String SERVER_STATE = "server-state";
    public static final String SHUTDOWN = "shutdown";
    public static final String STEPS = "steps";
    public static final String WHOAMI = "whoami";

    public static final List<String> RESULT_CODES_FOR_UNKNOWN_OR_NOT_FOUND = Collections.unmodifiableList(Arrays.asList(
            "JBAS010850",                // no handler for operation at address
            "JBAS014739", "WFLYCTL0148", // no handler at address
            "JBAS014792", "WFLYCTL0201", // unknown attribute
            "JBAS014793", "WFLYCTL0202", // no known child type
            "JBAS014807", "WFLYCTL0216", // management resource not found
            "JBAS014883", "WFLYCTL0030"  // no resource definition is registered for address
    ));
}
