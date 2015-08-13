package org.wildfly.extras.creaper.commands.auth;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.wildfly.extras.creaper.core.CommandFailedException;
import org.wildfly.extras.creaper.core.offline.OfflineCommand;
import org.wildfly.extras.creaper.core.offline.OfflineCommandContext;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Provides a set of offline commands to manipulate authentication and authorization {@code .properties} files.</p>
 *
 * <p>The generic factory methods ({@link #inConfigurationDirectory(String)} or {@link #of(java.io.File)}}) return
 * an instance of this class, so that methods for working with users and methods for working with user mappings
 * are both available.</p>
 *
 * <p>The configuration file specific factory methods ({@link #applicationUsers()}, {@link #applicationRoles()},
 * {@link #mgmtUsers()} and {@link #mgmtGroups()}) return an instance of a view class that only exposes methods for
 * working with users ({@link Users}) or methods for working with user mappings ({@link UserMappings}). This is only
 * for convenience (static checking), there's no change in functionality.</p>
 */
public final class PropertiesFileAuth {
    private static final String APPLICATION_USERS = "application-users.properties";
    private static final String APPLICATION_ROLES = "application-roles.properties";
    private static final String MGMT_USERS = "mgmt-users.properties";
    private static final String MGMT_GROUPS = "mgmt-groups.properties";

    // exactly one is always null and the other is always non-null
    // see the method "thePropertiesFile" below
    private final String fileName;
    private final File file;

    /**
     * Creates a {@link PropertiesFileAuth.Users} object for the {@code application-users.properties} file
     * in the configuration directory of the {@code OfflineManagementClient}.
     */
    public static PropertiesFileAuth.Users applicationUsers() {
        return new Users(inConfigurationDirectory(APPLICATION_USERS));
    }

    /**
     * Creates a {@link PropertiesFileAuth.UserMappings} object for the {@code application-roles.properties} file
     * in the configuration directory of the {@code OfflineManagementClient}.
     */
    public static PropertiesFileAuth.UserMappings applicationRoles() {
        return new UserMappings(inConfigurationDirectory(APPLICATION_ROLES));
    }

    /**
     * Creates a {@link PropertiesFileAuth.Users} object for the {@code mgmt-users.properties} file
     * in the configuration directory of the {@code OfflineManagementClient}.
     */
    public static PropertiesFileAuth.Users mgmtUsers() {
        return new Users(inConfigurationDirectory(MGMT_USERS));
    }

    /**
     * Creates a {@link PropertiesFileAuth.UserMappings} object for the {@code mgmt-groups.properties} file
     * in the configuration directory of the {@code OfflineManagementClient}.
     */
    public static PropertiesFileAuth.UserMappings mgmtGroups() {
        return new UserMappings(inConfigurationDirectory(MGMT_GROUPS));
    }

    /**
     * Creates a {@code PropertiesFileAuth} object for the file named {@code fileName} that resides
     * in the configuration directory of the {@code OfflineManagementClient}.
     */
    public static PropertiesFileAuth inConfigurationDirectory(String fileName) {
        return new PropertiesFileAuth(fileName, null);
    }

    /** Creates a {@code PropertiesFileAuth} object for given {@code file}. */
    public static PropertiesFileAuth of(File file) {
        return new PropertiesFileAuth(null, file);
    }

    private PropertiesFileAuth(String fileName, File file) {
        this.fileName = fileName;
        this.file = file;
    }

    // ---

    private File thePropertiesFile(OfflineCommandContext ctx) {
        if (file != null) {
            return file;
        }

        return new File(ctx.options.configurationDirectory(), fileName);
    }

    private static final Pattern REALM = Pattern.compile("\\$REALM_NAME=(.*?)\\$");

    private final class DefineUser implements OfflineCommand {
        private final String username;
        private final String password;

        DefineUser(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
            File file = thePropertiesFile(ctx);
            List<String> lines = Files.readLines(file, Charsets.UTF_8);

            Optional<String> realmLine = Iterables.tryFind(lines, Predicates.contains(REALM));
            if (!realmLine.isPresent()) {
                throw new CommandFailedException("The $REALM_NAME=...$ directive is missing: " + file);
            }
            Matcher matcher = REALM.matcher(realmLine.get());
            matcher.find();
            String realm = matcher.group(1);

            Iterables.removeIf(lines, Predicates.containsPattern("^" + username + "="));

            String authString = username + ":" + realm + ":" + password;
            String hashedAuthString = Hashing.md5().hashString(authString, Charsets.UTF_8).toString();
            lines.add(username + "=" + hashedAuthString);

            Files.asCharSink(file, Charsets.UTF_8).writeLines(lines, "\n");
        }

        @Override
        public String toString() {
            return "DefineUser " + username;
        }
    }

    private final class UndefineUser implements OfflineCommand {
        private final String username;

        UndefineUser(String username) {
            this.username = username;
        }

        @Override
        public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
            File file = thePropertiesFile(ctx);
            List<String> lines = Files.readLines(file, Charsets.UTF_8);

            Iterables.removeIf(lines, Predicates.containsPattern("^" + Pattern.quote(username) + "="));

            Files.asCharSink(file, Charsets.UTF_8).writeLines(lines, "\n");
        }

        @Override
        public String toString() {
            return "UndefineUser " + username;
        }
    }

    private abstract class AbstractUserMapping implements OfflineCommand {
        private final String username;
        private final String roleOrGroup;
        private final boolean trueToAdd_falseToRemove;

        public AbstractUserMapping(String username, String roleOrGroup, boolean trueToAdd_falseToRemove) {
            this.username = username;
            this.roleOrGroup = roleOrGroup;
            this.trueToAdd_falseToRemove = trueToAdd_falseToRemove;
        }

        @Override
        public void apply(OfflineCommandContext ctx) throws CommandFailedException, IOException {
            File file = thePropertiesFile(ctx);
            List<String> lines = Files.readLines(file, Charsets.UTF_8);

            Set<String> allRolesOrGroupsForUser = Sets.newHashSet();
            Iterable<String> linesForUser = Iterables.filter(lines,
                    Predicates.containsPattern("^" + Pattern.quote(username) + "="));
            Splitter splitter = Splitter.on(',').omitEmptyStrings().trimResults();
            for (String line : linesForUser) {
                String rolesOrGroups = line.replaceFirst("^" + Pattern.quote(username) + "=", "");
                allRolesOrGroupsForUser.addAll(splitter.splitToList(rolesOrGroups));
            }
            if (trueToAdd_falseToRemove) {
                allRolesOrGroupsForUser.add(roleOrGroup);
            } else {
                allRolesOrGroupsForUser.remove(roleOrGroup);
            }

            Iterables.removeIf(lines, Predicates.containsPattern("^" + Pattern.quote(username) + "="));
            lines.add(username + "=" + Joiner.on(',').join(allRolesOrGroupsForUser));

            Files.asCharSink(file, Charsets.UTF_8).writeLines(lines, "\n");
        }

        @Override
        public String toString() {
            return this.getClass().getSimpleName() + " " + username + " -> " + roleOrGroup;
        }
    }

    private final class DefineUserMapping extends AbstractUserMapping {
        DefineUserMapping(String username, String roleOrGroup) {
            super(username, roleOrGroup, true);
        }
    }

    private final class UndefineUserMapping extends AbstractUserMapping {
        UndefineUserMapping(String username, String roleOrGroup) {
            super(username, roleOrGroup, false);
        }
    }

    /**
     * Ensures that the {@code .properties} file, which must be a {@code *-users.properties} file, contains
     * a definition of a user with given {@code username} and given {@code password}.
     */
    public OfflineCommand defineUser(String username, String password) {
        return new DefineUser(username, password);
    }

    /**
     * Ensures that the {@code .properties} file, which must be a {@code *-users.properties} file, <i>doesn't </i>
     * contain a definition of a user with given {@code username}.
     */
    public OfflineCommand undefineUser(String username) {
        return new UndefineUser(username);
    }

    /**
     * Ensures that the {@code .properties} file, which must be a {@code *-<groups|roles>.properties} file, contains
     * a mapping of a user with given {@code username} to given role/group ({@code roleOrGroup}).
     */
    public OfflineCommand defineUserMapping(String username, String roleOrGroup) {
        return new DefineUserMapping(username, roleOrGroup);
    }

    /**
     * Ensures that the {@code .properties} file, which must be a {@code *-<groups|roles>.properties} file,
     * <i>doesn't</i> contain a mapping of a user with given {@code username} to given role/group ({@code roleOrGroup}).
     */
    public OfflineCommand undefineUserMapping(String username, String roleOrGroup) {
        return new UndefineUserMapping(username, roleOrGroup);
    }

    // ---
    // following classes are only used when the kind of the .properties file is known

    /**
     * Convenience view on {@link PropertiesFileAuth} that only exposes methods for working with users.
     */
    public static final class Users {
        private final PropertiesFileAuth delegate;

        private Users(PropertiesFileAuth delegate) {
            this.delegate = delegate;
        }

        /**
         * Ensures that the {@code *-users.properties} file contains a definition of a user
         * with given {@code username} and given {@code password}.
         */
        public OfflineCommand defineUser(String username, String password) {
            return delegate.defineUser(username, password);
        }

        /**
         * Ensures that the {@code *-users.properties} file <i>doesn't </i> contain a definition of a user
         * with given {@code username}.
         */
        public OfflineCommand undefineUser(String username) {
            return delegate.undefineUser(username);
        }
    }

    /** Convenience view on {@link PropertiesFileAuth} that only exposes methods for working with user mappings. */
    public static final class UserMappings {
        private final PropertiesFileAuth delegate;

        private UserMappings(PropertiesFileAuth delegate) {
            this.delegate = delegate;
        }

        /**
         * Ensures that the {@code *-<groups|roles>.properties} file contains a mapping of a user
         * with given {@code username} to given role/group ({@code roleOrGroup}).
         */
        public OfflineCommand defineUserMapping(String username, String roleOrGroup) {
            return delegate.defineUserMapping(username, roleOrGroup);
        }

        /**
         * Ensures that the {@code *-<groups|roles>.properties} file <i>doesn't</i> contain a mapping of a user
         * with given {@code username} to given role/group ({@code roleOrGroup}).
         */
        public OfflineCommand undefineUserMapping(String username, String roleOrGroup) {
            return delegate.undefineUserMapping(username, roleOrGroup);
        }
    }
}
