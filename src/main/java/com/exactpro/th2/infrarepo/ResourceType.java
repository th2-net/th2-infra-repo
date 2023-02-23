/*
 * Copyright 2020-2021 Exactpro (Exactpro Systems Limited)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.exactpro.th2.infrarepo;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public enum ResourceType {
    SettingsFile("SettingsFile", "", null, false),

    HelmRelease("HelmRelease", null, null, false),

    @Deprecated
    Th2Link("Th2Link", "links", "th2.exactpro.com/v1", false),

    Th2Dictionary("Th2Dictionary", "dictionaries", "th2.exactpro.com/v1"),

    Th2CoreBox("Th2CoreBox", "core", "th2.exactpro.com/v2"),

    Th2Mstore("Th2Mstore", "core", "th2.exactpro.com/v2"),

    Th2Estore("Th2Estore", "core", "th2.exactpro.com/v2"),

    Th2Box("Th2Box", "boxes", "th2.exactpro.com/v2"),

    Th2Job("Th2Job", "jobs", "th2.exactpro.com/v2");

    private final String kind;

    private final String path;

    private final String apiVersion;

    private final boolean isMangedResource;

    ResourceType(String kind, String path, String apiVersion, boolean isMangedResource) {
        this.kind = kind;
        this.path = path;
        this.apiVersion = apiVersion;
        this.isMangedResource = isMangedResource;
    }

    ResourceType(String kind, String path, String apiVersion) {
        this.kind = kind;
        this.path = path;
        this.apiVersion = apiVersion;
        this.isMangedResource = true;
    }

    public String kind() {
        return kind;
    }

    public String path() {
        return path;
    }

    public static ResourceType forKind(String kind) {
        return kinds.get(kind);
    }

    public static Set<String> knownKinds() {
        return kinds.keySet();
    }

    public boolean isRepositoryResource() {
        return path != null;
    }

    public String k8sApiVersion() {
        return apiVersion;
    }

    public boolean isMangedResource() {
        return isMangedResource;
    }

    private static final Map<String, ResourceType> kinds = new HashMap<>();

    static {
        for (ResourceType t : ResourceType.values()) {
            kinds.put(t.kind(), t);
        }
    }
}
