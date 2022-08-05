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

package com.exactpro.th2.infrarepo.repo;

import com.exactpro.th2.infrarepo.ResourceType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.fabric8.kubernetes.api.model.ObjectMeta;

public class GenericResource<T> {

    private String apiVersion;

    private String kind;

    private ObjectMeta metadata;

    private T spec;

    private String sourceHash;

    private String commitHash;

    private long detectionTime;

    public GenericResource() {
    }

    public GenericResource(ResourceType type) {
        this.kind = type.name();
    }

    public GenericResource(String apiVersion, String kind, ObjectMeta metadata, T spec) {
        this.apiVersion = apiVersion;
        this.kind = kind;
        this.metadata = metadata;
        this.spec = spec;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public ObjectMeta getMetadata() {
        return metadata;
    }

    public void setMetadata(ObjectMeta metadata) {
        this.metadata = metadata;
    }

    public T getSpec() {
        return spec;
    }

    public void setSpec(T spec) {
        this.spec = spec;
    }

    @JsonIgnore
    public String getApiGroup() {
        return GenericResource.getApiGroup(apiVersion);
    }

    @JsonIgnore
    public static String getApiGroup(String apiVersion) {
        return apiVersion.substring(0, apiVersion.indexOf("/"));
    }

    @JsonIgnore
    public String getVersion() {
        return GenericResource.getVersion(apiVersion);
    }

    @JsonIgnore
    public static String getVersion(String apiVersion) {
        return apiVersion.substring(apiVersion.indexOf("/") + 1);
    }

    @JsonIgnore
    public String getSourceHash() {
        return sourceHash;
    }

    @JsonIgnore
    public void setSourceHash(String hash) {
        this.sourceHash = hash;
    }

    @JsonIgnore
    public void stamp(String commitHash, long detectionTime) {
        this.commitHash = commitHash;
        this.detectionTime = detectionTime;
    }

    @JsonIgnore
    public String getCommitHash() {
        return commitHash;
    }

    @JsonIgnore
    public long getDetectionTime() {
        return detectionTime;
    }
}
