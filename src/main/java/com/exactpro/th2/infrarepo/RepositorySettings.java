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

    public static final String LOG_LEVEL_ERROR = "ERROR";
    public static final String LOG_LEVEL_WARNING = "WARNING";
    public static final String LOG_LEVEL_INFO = "INFO";
    public static final String LOG_LEVEL_DEBUG = "DEBUG";

    private String logLevel = LOG_LEVEL_INFO;
    private String k8sPropagation = PROPAGATE_OFF;

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {

        if (logLevel == null || logLevel.equals("")) {
            this.logLevel = LOG_LEVEL_INFO;
            return;
        }

        if (logLevel.equals(LOG_LEVEL_ERROR) ||
                logLevel.equals(LOG_LEVEL_WARNING) ||
                logLevel.equals(LOG_LEVEL_INFO) ||
                logLevel.equals(LOG_LEVEL_DEBUG))
            this.logLevel = logLevel;
        else
            throw new IllegalArgumentException(String.format("Unknown value (%s)", logLevel));
    }

    @JsonGetter("k8s-propagation")
    public String getK8sPropagation() {
        return k8sPropagation;
    }

    @JsonSetter("k8s-propagation")
    public void setK8sPropagation(String k8sPropagation) {
        if (k8sPropagation != null)
            this.k8sPropagation = k8sPropagation;
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
