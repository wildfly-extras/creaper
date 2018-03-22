package org.wildfly.extras.creaper.commands.elytron;

import java.util.HashMap;
import java.util.Map;
import org.wildfly.extras.creaper.core.online.operations.Values;

public final class CredentialRef {
    private String alias;
    private String type;
    private String store;
    private String clearText;

    private CredentialRef(CredentialRefBuilder builder) {
        this.alias = builder.alias;
        this.type = builder.type;
        this.store = builder.store;
        this.clearText = builder.clearText;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStore() {
        return store;
    }

    public void setStore(String store) {
        this.store = store;
    }

    public String getClearText() {
        return clearText;
    }

    public void setClearText(String clearText) {
        this.clearText = clearText;
    }

    public Values toValues() {
        return Values.empty()
            .andOptional("alias", getAlias())
            .andOptional("type", getType())
            .andOptional("store", getStore())
            .andOptional("clear-text", getClearText());
    }

    public Map<String, Object> toParameters() {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("atrCredentialRefAlias", alias);
        parameters.put("atrCredentialRefType", type);
        parameters.put("atrCredentialRefStore", store);
        parameters.put("atrCredentialRefClearText", clearText);
        return parameters;
    }

    public static final class CredentialRefBuilder {
        private String alias;
        private String type;
        private String store;
        private String clearText;

        public CredentialRefBuilder alias(String alias) {
            this.alias = alias;
            return this;
        }

        public CredentialRefBuilder type(String type) {
            this.type = type;
            return this;
        }

        public CredentialRefBuilder store(String store) {
            this.store = store;
            return this;
        }

        public CredentialRefBuilder clearText(String clearText) {
            this.clearText = clearText;
            return this;
        }

        public CredentialRef build() {
            if (clearText == null || clearText.isEmpty()) {
                if ((alias == null || alias.isEmpty()) || (store == null || store.isEmpty())) {
                    throw new IllegalArgumentException("Either clear-text, or alias & store must be specified as non empty value");
                }
            }

            return new CredentialRef(this);
        }
    }
}
