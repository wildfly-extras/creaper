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

    private static final Ordering<Node> ORDERING = Ordering.explicit(
            // this is a blend of "domain", "host" and "server" schemas that satisfies all ordering constraints
            "extensions",
            "system-properties",
            "paths",
            "vault",
            "management",
            "profile",
            "domain-controller",
            "profiles",
            "interfaces",
            "socket-binding-groups",
            "socket-binding-group",
            "deployments",
            "deployment-overlays",
            "server-groups",
            "jvms",
            "servers",
            "management-client-content"
    ).onResultOf(GET_NODE_NAME);

    static String fix(String xml) {
        try {
            Node root = new XmlParser(false, false).parseText(xml);
            Collections.sort((List<Node>) root.children(), ORDERING);
            return XmlUtil.serialize(root);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private FirstLevelXmlElementOrder() {} // avoid instantiation
}
