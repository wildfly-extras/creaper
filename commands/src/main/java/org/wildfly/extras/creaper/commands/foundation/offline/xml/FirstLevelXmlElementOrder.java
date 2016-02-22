package org.wildfly.extras.creaper.commands.foundation.offline.xml;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import groovy.util.Node;
import groovy.util.XmlParser;
import groovy.xml.XmlUtil;

import java.util.Collections;
import java.util.List;

final class FirstLevelXmlElementOrder {
    private static final Function<Node, String> GET_NODE_NAME = new Function<Node, String>() {
        @Override
        public String apply(Node input) {
            return String.valueOf(input == null ? null : input.name());
        }
    };

    // see files [jboss-as|wildfly]-config_*.xsd

    private static final Ordering<Node> DOMAIN_ORDERING = Ordering.explicit(
            "extensions",
            "system-properties",
            "paths",
            "management",
            "profiles",
            "interfaces",
            "socket-binding-groups",
            "deployments",
            "deployment-overlays",
            "server-groups",
            "management-client-content"
    ).onResultOf(GET_NODE_NAME);

    private static final Ordering<Node> HOST_ORDERING = Ordering.explicit(
            "extensions",
            "system-properties",
            "paths",
            "vault",
            "management",
            "domain-controller",
            "interfaces",
            "jvms",
            "servers",
            "profile",
            "socket-binding-group"
    ).onResultOf(GET_NODE_NAME);

    private static final Ordering<Node> SERVER_ORDERING = Ordering.explicit(
            "extensions",
            "system-properties",
            "paths",
            "vault",
            "management",
            "profile",
            "interfaces",
            "socket-binding-group",
            "deployments",
            "deployment-overlays"
    ).onResultOf(GET_NODE_NAME);

    static String fix(String xml) {
        try {
            Node root = new XmlParser(false, false).parseText(xml);

            Ordering<Node> ordering;
            if ("domain".equals(root.name())) {
                ordering = DOMAIN_ORDERING;
            } else if ("host".equals(root.name())) {
                ordering = HOST_ORDERING;
            } else if ("server".equals(root.name())) {
                ordering = SERVER_ORDERING;
            } else {
                throw new IllegalArgumentException("Unknown root element '" + root.name() + "'");
            }

            Collections.sort((List<Node>) root.children(), ordering);
            return XmlUtil.serialize(root);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private FirstLevelXmlElementOrder() {} // avoid instantiation
}
