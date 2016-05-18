package org.wildfly.extras.creaper.test;

/** Category marker for tests that need to control application server lifecycle manually. */
public interface ManualTests {
    String ARQUILLIAN_CONTAINER = "jboss-manual";
    String ARQUILLIAN_CONTAINER_MGMT_PROTOCOL_REMOTE = "jboss-manual-with-remote-mgmt-protocol";
}
