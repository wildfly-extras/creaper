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

    // using default profile in domain
    public static Subtree profile() {
        return new Subtree(ProfileSubtreeLocator.INSTANCE, SubtreeCreator.PROFILE);
    }

    // using default profile in domain
    public static Subtree subsystem(String subsystemName) {
        // can't create subsystem, don't know the version
        return new Subtree(new SubsystemSubtreeLocator(subsystemName), null);
    }

    public static Subtree interfaces() {
        return new Subtree(StaticSubtreeLocator.INTERFACES, SubtreeCreator.INTERFACES);
    }

    // in domain, tries to guess a corect name based on the default profile
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
        static final SubtreeLocator EXTENSIONS = new StaticSubtreeLocator("extensions", false);
        static final SubtreeLocator SYSTEM_PROPERTIES = new StaticSubtreeLocator("system-properties", false);
        static final SubtreeLocator PATHS = new StaticSubtreeLocator("paths", false);
        static final SubtreeLocator MANAGEMENT = new StaticSubtreeLocator("management", false);
        static final SubtreeLocator INTERFACES = new StaticSubtreeLocator("interfaces", false);

        static final SubtreeLocator PROFILES = new StaticSubtreeLocator("profiles", true);
        static final SubtreeLocator SOCKET_BINDING_GROUPS = new StaticSubtreeLocator("socket-binding-groups", true);
        static final SubtreeLocator SERVER_GROUPS = new StaticSubtreeLocator("server-groups", true);
        static final SubtreeLocator DOMAIN_CONTROLLER = new StaticSubtreeLocator("domain-controller", true);
        static final SubtreeLocator JVMS = new StaticSubtreeLocator("jvms", true);
        static final SubtreeLocator SERVERS = new StaticSubtreeLocator("servers", true);

        private final String tagName; // only for a possible exception message
        private final Class scriptClass;
        private final boolean domainOnly;

        StaticSubtreeLocator(String tagName, boolean domainOnly) {
            String script = "root.\"" + tagName + "\"";
            this.tagName = tagName;
            this.scriptClass = (GroovyHolder.GROOVY.parseClass(script));
            this.domainOnly = domainOnly;
        }

        public GPathResult locate(GPathResult root, OfflineOptions options) throws Exception {
            if (domainOnly && !options.isDomain) {
                throw new IllegalArgumentException("Locating '" + tagName + "' is only possible in domain");
            }
            Script script = (Script) scriptClass.newInstance();
            script.setProperty("root", root);
            return (GPathResult) script.run();
        }
    }

    private static final class ProfileSubtreeLocator implements SubtreeLocator {
        static final SubtreeLocator INSTANCE = new ProfileSubtreeLocator();

        private static final Class STANDALONE_SCRIPT_CLASS = GroovyHolder.GROOVY.parseClass("root.profile");
        private static final Class DOMAIN_SCRIPT_CLASS = GroovyHolder.GROOVY.parseClass("root.profiles.profile.find { it.@name == defaultProfile }");

        @Override
        public GPathResult locate(GPathResult root, OfflineOptions options) throws Exception {
            Script script = (Script) (
                    options.isDomain ? DOMAIN_SCRIPT_CLASS.newInstance() : STANDALONE_SCRIPT_CLASS.newInstance()
            );
            script.setProperty("root", root);
            if (options.isDomain) {
                script.setProperty("defaultProfile", options.defaultProfile);
            }
            return (GPathResult) script.run();
        }
    }

    private static final class SubsystemSubtreeLocator implements SubtreeLocator {
        private static final Class STANDALONE_SCRIPT_CLASS = GroovyHolder.GROOVY.parseClass("root.profile.subsystem.find { it.@xmlns.toString().startsWith(\"urn:jboss:domain:${subsystemName}:\") }");
        private static final Class DOMAIN_SCRIPT_CLASS = GroovyHolder.GROOVY.parseClass("root.profiles.profile.find { it.@name == defaultProfile }.subsystem.find { it.@xmlns.toString().startsWith(\"urn:jboss:domain:${subsystemName}:\") }");

        private final String subsystemName;

        public SubsystemSubtreeLocator(String subsystemName) {
            this.subsystemName = subsystemName;
        }

        @Override
        public GPathResult locate(GPathResult root, OfflineOptions options) throws Exception {
            Script script = (Script) (
                    options.isDomain ? DOMAIN_SCRIPT_CLASS.newInstance() : STANDALONE_SCRIPT_CLASS.newInstance()
            );
            script.setProperty("root", root);
            script.setProperty("subsystemName", subsystemName);
            if (options.isDomain) {
                script.setProperty("defaultProfile", options.defaultProfile);
            }
            return (GPathResult) script.run();
        }
    }

    private static final class SocketBindingGroupSubtreeLocator implements SubtreeLocator {
        static final SubtreeLocator INSTANCE = new SocketBindingGroupSubtreeLocator();

        private static final Class STANDALONE_SCRIPT_CLASS = GroovyHolder.GROOVY.parseClass("root.\"socket-binding-group\"");
        private static final Class DOMAIN_SCRIPT_CLASS = GroovyHolder.GROOVY.parseClass("root.\"socket-binding-groups\".\"socket-binding-group\".find { it.@name == \"${defaultSocketBindingGroup}\" }");

        @Override
        public GPathResult locate(GPathResult root, OfflineOptions options) throws Exception {
            Script script = (Script) (
                    options.isDomain ? DOMAIN_SCRIPT_CLASS.newInstance() : STANDALONE_SCRIPT_CLASS.newInstance()
            );
            script.setProperty("root", root);
            if (options.isDomain) {
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
            if (skipInDomain && options.isDomain) {
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
