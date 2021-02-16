package com.exactpro.th2.infrarepo;

public class Keyspace {
    private String schemaVersion;
    private String initializer;

    public String getSchemaVersion() {
        return schemaVersion == null ? "" : schemaVersion;
    }

    public void setSchemaVersion(String schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public String getInitializer() {
        return initializer == null ? "" : initializer;
    }

    public void setInitializer(String initializer) {
        this.initializer = initializer;
    }
}
