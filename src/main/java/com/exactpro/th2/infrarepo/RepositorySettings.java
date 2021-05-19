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

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSetter;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RepositorySettings {
    // no action will be taken regarding the kubernetes
    private static final String PROPAGATE_OFF = "off";

    // no synchronization, namespace will be deleted when this value is set
    private static final String PROPAGATE_DENY = "deny";

    // git->k8s synchronization on repository changes
    private static final String PROPAGATE_SYNC = "sync";
    private static final String PROPAGATE_TRUE = "true";

    // git->k8s synchronization on repository or kubernetes change
    private static final String PROPAGATE_RULE = "rule";

    private String k8sPropagation = PROPAGATE_OFF;

    private Th2BoxConfig th2BoxConfig = new Th2BoxConfig();

    @JsonGetter("k8s-propagation")
    public String getK8sPropagation() {
        return k8sPropagation;
    }

    @JsonSetter("k8s-propagation")
    public void setK8sPropagation(String k8sPropagation) {
        if (k8sPropagation != null)
            this.k8sPropagation = k8sPropagation;
    }

    public Map<String, String> getMqRouter() {
        return th2BoxConfig.getMqRouter();
    }

    public Map<String, String> getGrpcRouter() {
        return th2BoxConfig.getGrpcRouter();
    }

    public Map<String, String> getCradleManager() {
        return th2BoxConfig.getCradleManager();
    }

    public String getLogLevelTh2() {
        return th2BoxConfig.getLogging().getLogLevelTh2();
    }

    public String getLogLevelRoot() {
        return th2BoxConfig.getLogging().getLogLevelRoot();
    }

    public Th2BoxConfig getTh2BoxConfig() {
        return th2BoxConfig;
    }

    @JsonIgnore
    public boolean isK8sSynchronizationRequired() {
        return k8sPropagation.equals(PROPAGATE_SYNC)
                || k8sPropagation.equals(PROPAGATE_RULE)
                || k8sPropagation.equals(PROPAGATE_TRUE);
    }

    @JsonIgnore
    public Boolean isK8sGovernanceRequired() {
        return k8sPropagation.equals(PROPAGATE_RULE);
    }

    @JsonIgnore
    public Boolean isK8sPropagationDenied() {
        return k8sPropagation.equals(PROPAGATE_DENY);
    }
}
