package org.wildfly.extras.creaper.commands.foundation.offline.xml;

import groovy.lang.Script;
import groovy.util.slurpersupport.GPathResult;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;

/**
 * <p>This is the primary mechanism used for unifying access to standalone and domain configuration files. The trick is
 * that the transformation script doesn't work on the file, it doesn't even work on the entire XML document (unless
 * explicitly requested using {@link #root()}), it instead works on a set of subtrees. The kye here is that
 * for a vast majority of commonly used subtrees, it's possible to devise a single specification that can address them
 * correctly both in standalone and domain.</p>
 *
 * <p>For example, {@code Subtree.subsystem("infinispan")} is a unique location both in {@code standalone.xml} and
 * {@code domain.xml}, given that there is a concept of default profile for the domain use.</p>
 *
 * <p>Configuration files for domain have more concepts than the standalone ones, so there are domain-specific
 * features here, but their use is minor.</p>
 */
public final class Subtree {
    private final SubtreeLocator locator;
    private final SubtreeCreator creator;

    private Subtree(SubtreeLocator locator, SubtreeCreator creator) {
        this.locator = locator;
        this.creator = creator;
    }

    void addIfMissing(GPathResult root, OfflineOptions options) {
        if (creator != null) {
            creator.addIfMissing(root, options);
        }
    }

    GPathResult locate(GPathResult root, OfflineOptions options) throws Exception {
        return locator.locate(root, options);
    }

    // ---

    public static Subtree root() {
        // document root can't be missing, no point in trying to create it
        return new Subtree(SubtreeLocator.ROOT, null);
    }

    public static Subtree extensions() {
        return new Subtree(StaticSubtreeLocator.EXTENSIONS, SubtreeCreator.EXTENSIONS);
    }

    public static Subtree systemProperties() {
        return new Subtree(StaticSubtreeLocator.SYSTEM_PROPERTIES, SubtreeCreator.SYSTEM_PROPERTIES);
    }

    public static Subtree paths() {
        return new Subtree(StaticSubtreeLocator.PATHS, SubtreeCreator.PATHS);
    }

    public static Subtree management() {
        return new Subtree(StaticSubtreeLocator.MANAGEMENT, SubtreeCreator.MANAGEMENT);
    }

    // using default profile in domain.xml
    public static Subtree profile() {
        return new Subtree(ProfileSubtreeLocator.INSTANCE, SubtreeCreator.PROFILE);
    }

    // using default profile in domain.xml
    public static Subtree subsystem(String subsystemName) {
        // can't create subsystem, don't know the version
        return new Subtree(new SubsystemSubtreeLocator(subsystemName), null);
    }

    public static Subtree interfaces() {
        return new Subtree(StaticSubtreeLocator.INTERFACES, SubtreeCreator.INTERFACES);
    }

    // in domain.xml, tries to guess a corect name based on the default profile
    public static Subtree socketBindingGroup() {
        return new Subtree(SocketBindingGroupSubtreeLocator.INSTANCE, SubtreeCreator.SOCKET_BINDING_GROUP);
    }

    // ---
    // domain only

    public static Subtree profiles() {
        return new Subtree(StaticSubtreeLocator.PROFILES, SubtreeCreator.PROFILES);
    }

    public static Subtree subsystemInProfile(String profileName, String subsystemName) {
        // can't create subsystem, don't know the version
        return new Subtree(new SubsystemInProfileSubtreeLocator(profileName, subsystemName), null);
    }

    public static Subtree socketBindingGroups() {
        return new Subtree(StaticSubtreeLocator.SOCKET_BINDING_GROUPS, SubtreeCreator.SOCKET_BINDING_GROUPS);
    }

    public static Subtree serverGroups() {
        return new Subtree(StaticSubtreeLocator.SERVER_GROUPS, SubtreeCreator.SERVER_GROUPS);
    }

    public static Subtree domainController() {
        return new Subtree(StaticSubtreeLocator.DOMAIN_CONTROLLER, SubtreeCreator.DOMAIN_CONTROLLER);
    }

    public static Subtree jvms() {
        return new Subtree(StaticSubtreeLocator.JVMS, SubtreeCreator.JVMS);
    }

    public static Subtree servers() {
        return new Subtree(StaticSubtreeLocator.SERVERS, SubtreeCreator.SERVERS);
    }

    // ---

    private enum Type {
        DOMAIN("domain.xml"),
        HOST("host.xml"),
        SERVER("standalone.xml"),
        ;

        private final String description;

        Type(String description) {
            this.description = description;
        }

        @Override
        public String toString() {
            return description;
        }

        public static Type of(GPathResult root) {
            String rootElement = root.name();
            if ("domain".equals(rootElement)) {
                return Type.DOMAIN;
            } else if ("host".equals(rootElement)) {
                return Type.HOST;
            } else if ("server".equals(rootElement)) {
                return Type.SERVER;
            } else {
                throw new IllegalArgumentException("Unknown root node '" + rootElement + "'");
            }
        }
    }

    private interface SubtreeLocator {
        GPathResult locate(GPathResult root, OfflineOptions options) throws Exception;

        SubtreeLocator ROOT = new SubtreeLocator() {
            @Override
            public GPathResult locate(GPathResult root, OfflineOptions options) {
                return root;
            }
        };
    }

    private static final class StaticSubtreeLocator implements SubtreeLocator {
        static final SubtreeLocator EXTENSIONS = new StaticSubtreeLocator("extensions", null);
        static final SubtreeLocator SYSTEM_PROPERTIES = new StaticSubtreeLocator("system-properties", null);
        static final SubtreeLocator PATHS = new StaticSubtreeLocator("paths", null);
        static final SubtreeLocator MANAGEMENT = new StaticSubtreeLocator("management", null);
        static final SubtreeLocator INTERFACES = new StaticSubtreeLocator("interfaces", null);

        static final SubtreeLocator PROFILES = new StaticSubtreeLocator("profiles", Type.DOMAIN);
        static final SubtreeLocator SOCKET_BINDING_GROUPS = new StaticSubtreeLocator("socket-binding-groups",
                Type.DOMAIN);
        static final SubtreeLocator SERVER_GROUPS = new StaticSubtreeLocator("server-groups", Type.DOMAIN);
        static final SubtreeLocator DOMAIN_CONTROLLER = new StaticSubtreeLocator("domain-controller", Type.HOST);
        static final SubtreeLocator JVMS = new StaticSubtreeLocator("jvms", Type.HOST);
        static final SubtreeLocator SERVERS = new StaticSubtreeLocator("servers", Type.HOST);

        private final String tagName; // only for a possible exception message
        private final Class scriptClass;
        private final Type onlyForType;

        StaticSubtreeLocator(String tagName, Type onlyForType) {
            String script = "root.\"" + tagName + "\"";
            this.tagName = tagName;
            this.scriptClass = GroovyHolder.GROOVY.parseClass(script);
            this.onlyForType = onlyForType;
        }

        public GPathResult locate(GPathResult root, OfflineOptions options) throws Exception {
            if (onlyForType != null && onlyForType != Type.of(root)) {
                throw new IllegalArgumentException("Locating '" + tagName + "' is only possible in '" + onlyForType + "'");
            }
            Script script = (Script) scriptClass.newInstance();
            script.setProperty("root", root);
            return (GPathResult) script.run();
        }
    }

    private static final class ProfileSubtreeLocator implements SubtreeLocator {
        static final SubtreeLocator INSTANCE = new ProfileSubtreeLocator();

        private static final Class STANDALONE_OR_HOST_SCRIPT_CLASS = GroovyHolder.GROOVY.parseClass("root.profile");
        private static final Class DOMAIN_SCRIPT_CLASS = GroovyHolder.GROOVY.parseClass("root.profiles.profile.find { it.@name == defaultProfile }");

        @Override
        public GPathResult locate(GPathResult root, OfflineOptions options) throws Exception {
            boolean domain = Type.of(root) == Type.DOMAIN;

            Script script = (Script) (
                    domain ? DOMAIN_SCRIPT_CLASS.newInstance() : STANDALONE_OR_HOST_SCRIPT_CLASS.newInstance()
            );
            script.setProperty("root", root);
            if (domain) {
                script.setProperty("defaultProfile", options.defaultProfile);
            }
            return (GPathResult) script.run();
        }
    }

    private static final class SubsystemSubtreeLocator implements SubtreeLocator {
        private static final Class STANDALONE_OR_HOST_SCRIPT_CLASS = GroovyHolder.GROOVY.parseClass("root.profile.subsystem.find { it.@xmlns.toString()"
                + ".startsWith(\"urn:jboss:domain:${subsystemName}:\") || it.@xmlns.toString().startsWith(\"urn:wildfly:${subsystemName}:\")} ");
        private static final Class DOMAIN_SCRIPT_CLASS = GroovyHolder.GROOVY.parseClass("root.profiles.profile.find { it.@name == defaultProfile }.subsystem"
                + ".find { it.@xmlns.toString().startsWith(\"urn:jboss:domain:${subsystemName}:\") || it.@xmlns.toString().startsWith(\"urn:wildfly:${subsystemName}:\") }");

        private final String subsystemName;

        public SubsystemSubtreeLocator(String subsystemName) {
            this.subsystemName = subsystemName;
        }

        @Override
        public GPathResult locate(GPathResult root, OfflineOptions options) throws Exception {
            boolean domain = Type.of(root) == Type.DOMAIN;

            Script script = (Script) (
                    domain ? DOMAIN_SCRIPT_CLASS.newInstance() : STANDALONE_OR_HOST_SCRIPT_CLASS.newInstance()
            );
            script.setProperty("root", root);
            script.setProperty("subsystemName", subsystemName);
            if (domain) {
                script.setProperty("defaultProfile", options.defaultProfile);
            }
            return (GPathResult) script.run();
        }
    }

    private static final class SocketBindingGroupSubtreeLocator implements SubtreeLocator {
        static final SubtreeLocator INSTANCE = new SocketBindingGroupSubtreeLocator();

        private static final Class STANDALONE_OR_HOST_SCRIPT_CLASS = GroovyHolder.GROOVY.parseClass("root.\"socket-binding-group\"");
        private static final Class DOMAIN_SCRIPT_CLASS = GroovyHolder.GROOVY.parseClass("root.\"socket-binding-groups\".\"socket-binding-group\".find { it.@name == \"${defaultSocketBindingGroup}\" }");

        @Override
        public GPathResult locate(GPathResult root, OfflineOptions options) throws Exception {
            boolean domain = Type.of(root) == Type.DOMAIN;

            Script script = (Script) (
                    domain ? DOMAIN_SCRIPT_CLASS.newInstance() : STANDALONE_OR_HOST_SCRIPT_CLASS.newInstance()
            );
            script.setProperty("root", root);
            if (domain) {
                String defaultSocketBindingGroup = options.defaultProfile + "-sockets";
                if ("default".equals(options.defaultProfile)) {
                    defaultSocketBindingGroup = "standard-sockets";
                }
                script.setProperty("defaultSocketBindingGroup", defaultSocketBindingGroup);
            }
            return (GPathResult) script.run();
        }
    }

    private static final class SubsystemInProfileSubtreeLocator implements SubtreeLocator {
        private static final Class SCRIPT_CLASS = GroovyHolder.GROOVY.parseClass("root.profiles.profile.find { it.@name == profileName }.subsystem.find { it.@xmlns.toString().startsWith(\"urn:jboss:domain:${subsystemName}:\") }");

        private final String profileName;
        private final String subsystemName;

        public SubsystemInProfileSubtreeLocator(String profileName, String subsystemName) {
            this.profileName = profileName;
            this.subsystemName = subsystemName;
        }

        @Override
        public GPathResult locate(GPathResult root, OfflineOptions options) throws Exception {
            if (!options.isDomain) {
                throw new IllegalArgumentException("Locating a subsystem '" + subsystemName + "' in profile '"
                        + profileName + "' is only possible in domain");
            }

            Script script = (Script) SCRIPT_CLASS.newInstance();
            script.setProperty("root", root);
            script.setProperty("profileName", profileName);
            script.setProperty("subsystemName", subsystemName);
            return (GPathResult) script.run();
        }
    }

    // appends a new subtree to the end of the document, which is wrong (but simple)
    //
    // the location will be fixed later (which is easier and more efficient than doing it here),
    // see FirstLevelXmlElementOrder
    private static final class SubtreeCreator {
        static final SubtreeCreator EXTENSIONS = new SubtreeCreator("extensions");
        static final SubtreeCreator SYSTEM_PROPERTIES = new SubtreeCreator("system-properties");
        static final SubtreeCreator PATHS = new SubtreeCreator("paths");
        static final SubtreeCreator MANAGEMENT = new SubtreeCreator("management");
        static final SubtreeCreator PROFILE = new SubtreeCreator("profile", true);
        static final SubtreeCreator INTERFACES = new SubtreeCreator("interfaces");
        static final SubtreeCreator SOCKET_BINDING_GROUP = new SubtreeCreator("socket-binding-group", true);

        static final SubtreeCreator PROFILES = new SubtreeCreator("profiles");
        static final SubtreeCreator SOCKET_BINDING_GROUPS = new SubtreeCreator("socket-binding-groups");
        static final SubtreeCreator SERVER_GROUPS = new SubtreeCreator("server-groups");
        static final SubtreeCreator DOMAIN_CONTROLLER = new SubtreeCreator("domain-controller");
        static final SubtreeCreator JVMS = new SubtreeCreator("jvms");
        static final SubtreeCreator SERVERS = new SubtreeCreator("servers");

        private final Class scriptClass;
        private final boolean skipInDomain;

        private SubtreeCreator(String tagName) {
            this(tagName, false);
        }

        private SubtreeCreator(String tagName, boolean skipInDomain) {
            String script = "if (root.\"" + tagName + "\".isEmpty()) root << { \"" + tagName + "\"() }";
            this.scriptClass = GroovyHolder.GROOVY.parseClass(script);
            this.skipInDomain = skipInDomain;
        }

        void addIfMissing(GPathResult root, OfflineOptions options) {
            boolean domain = Type.of(root) == Type.DOMAIN;

            if (skipInDomain && domain) {
                return;
            }

            try {
                Script script = (Script) scriptClass.newInstance();
                script.setProperty("root", root);
                script.run();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
