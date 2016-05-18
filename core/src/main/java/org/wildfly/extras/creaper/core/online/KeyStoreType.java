package org.wildfly.extras.creaper.core.online;

import java.security.KeyStore;

/**
 * See http://docs.oracle.com/javase/8/docs/technotes/guides/security/StandardNames.html#KeyStore
 */
public enum KeyStoreType {
    /** The proprietary keystore implementation provided by the SunJCE provider. */
    JCEKS("jceks"),
    /** The proprietary keystore implementation provided by the SUN provider. */
    JKS("jks"),
    /**
     * A keystore backed by a PKCS #11 token.
     * See http://docs.oracle.com/javase/8/docs/technotes/guides/security/p11guide.html
     */
    PKCS11("pkcs11"),
    /** The transfer syntax for personal identity information as defined in PKCS #12. */
    PKCS12("pkcs12"),
    /** The second version of PKCS12 type keystore. */
    PKCS12S2("pkcs12s2"),
    ;

    /**
     * Default type of keystore as specified by the {@link KeyStore#getDefaultType},
     * or JKS if no matching type found.
     */
    public static final KeyStoreType DEFAULT_TYPE;

    private final String typeName;

    KeyStoreType(String typeName) {
        this.typeName = typeName;
    }

    /** The name of the type as expected by the client libraries. */
    public String typeName() {
        return this.typeName;
    }

    static {
        final String jdkDefaultTypeName = KeyStore.getDefaultType();
        KeyStoreType defaultType = null;

        for (KeyStoreType type : KeyStoreType.values()) {
            if (type.typeName.equalsIgnoreCase(jdkDefaultTypeName)) {
                defaultType = type;
                break;
            }
        }

        DEFAULT_TYPE = defaultType != null ? defaultType : JKS;
    }
}
