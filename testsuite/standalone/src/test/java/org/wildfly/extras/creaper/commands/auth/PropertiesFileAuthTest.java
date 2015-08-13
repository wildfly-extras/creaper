package org.wildfly.extras.creaper.commands.auth;

import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import org.wildfly.extras.creaper.core.ManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineManagementClient;
import org.wildfly.extras.creaper.core.offline.OfflineOptions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class PropertiesFileAuthTest {
    @Rule
    public final TemporaryFolder tmp = new TemporaryFolder();

    private File usersProperties;
    private File groupsProperties;

    private OfflineManagementClient client;

    @Before
    public void setUp() throws IOException {
        File configuration = tmp.newFolder("configuration");
        new File(configuration, "standalone.xml").createNewFile();
        usersProperties = new File(configuration, "mgmt-users.properties");
        groupsProperties = new File(configuration, "mgmt-groups.properties");
        Resources.copy(Resources.getResource(PropertiesFileAuthTest.class, "mgmt-users.properties"),
                new FileOutputStream(usersProperties));
        Resources.copy(Resources.getResource(PropertiesFileAuthTest.class, "mgmt-groups.properties"),
                new FileOutputStream(groupsProperties));

        client = ManagementClient.offline(
                OfflineOptions.standalone().baseDirectory(tmp.getRoot()).configurationFile("standalone.xml").build()
        );
    }

    // this only tests the test code, nothing else
    @Test
    public void readExisting() throws Exception {
        String hash = firstLineContentForUser(usersProperties, "admin");
        assertEquals("c22052286cd5d72239a90fe193737253", hash);

        String groups = firstLineContentForUser(groupsProperties, "admin");
        assertEquals("Administrator", groups);
    }

    @Test
    public void defineUser() throws Exception {
        client.apply(PropertiesFileAuth.mgmtUsers().defineUser("foo", "bar"));

        String hash = firstLineContentForUser(usersProperties, "foo");
        assertEquals("b44c2a300af96cbefc6e42e55e010b29", hash);
    }

    @Test
    public void undefineUser() throws Exception {
        client.apply(PropertiesFileAuth.mgmtUsers().defineUser("foo", "bar"));

        String hash = firstLineContentForUser(usersProperties, "foo");
        assertNotNull(hash);

        client.apply(PropertiesFileAuth.mgmtUsers().undefineUser("foo"));

        hash = firstLineContentForUser(usersProperties, "foo");
        assertNull(hash);
    }

    @Test
    public void defineUserMapping() throws Exception {
        client.apply(PropertiesFileAuth.mgmtUsers().defineUser("foo", "bar"));
        client.apply(PropertiesFileAuth.mgmtGroups().defineUserMapping("foo", "Monitor"));

        String hash = firstLineContentForUser(usersProperties, "foo");
        assertNotNull(hash);
        String groups = firstLineContentForUser(groupsProperties, "foo");
        assertEquals("Monitor", groups);

        client.apply(PropertiesFileAuth.mgmtGroups().defineUserMapping("foo", "Maintainer"));
        groups = firstLineContentForUser(groupsProperties, "foo");
        assertTrue(groups.contains("Monitor"));
        assertTrue(groups.contains("Maintainer"));
    }

    @Test
    public void undefineUserMapping() throws Exception {
        client.apply(PropertiesFileAuth.mgmtUsers().defineUser("foo", "bar"));
        client.apply(PropertiesFileAuth.mgmtGroups().defineUserMapping("foo", "Monitor"));
        client.apply(PropertiesFileAuth.mgmtGroups().defineUserMapping("foo", "Maintainer"));

        String hash = firstLineContentForUser(usersProperties, "foo");
        assertNotNull(hash);
        String groups = firstLineContentForUser(groupsProperties, "foo");
        assertTrue(groups.contains("Monitor"));
        assertTrue(groups.contains("Maintainer"));

        client.apply(PropertiesFileAuth.mgmtGroups().undefineUserMapping("foo", "Monitor"));

        groups = firstLineContentForUser(groupsProperties, "foo");
        assertEquals("Maintainer", groups);
    }

    private static String firstLineContentForUser(File file, String username) throws IOException {
        String pattern = "^" + Pattern.quote(username) + "=";
        List<String> lines = Files.readLines(file, Charsets.UTF_8);
        Optional<String> line = Iterables.tryFind(lines, Predicates.containsPattern(pattern));

        if (line.isPresent()) {
            return line.get().replaceFirst(pattern, "");
        }
        return null;
    }
}
