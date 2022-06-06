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

package com.exactpro.th2.infrarepo.settings;

import java.util.Map;

public class Th2BoxConfig {

    private Map<String, Object> mqRouter;

    private Map<String, Object> grpcRouter;

    private Map<String, Object> cradleManager;

    private Logging logging = new Logging();

    public Map<String, Object> getMqRouter() {
        return mqRouter;
    }

    public Map<String, Object> getGrpcRouter() {
        return grpcRouter;
    }

    public Map<String, Object> getCradleManager() {
        return cradleManager;
    }

    public Logging getLogging() {
        return logging;
    }

    static class Logging {
        public static final String LOG_LEVEL_FATAL = "FATAL";

        public static final String LOG_LEVEL_ERROR = "ERROR";

        public static final String LOG_LEVEL_WARNING = "WARNING";

        public static final String LOG_LEVEL_INFO = "INFO";

        public static final String LOG_LEVEL_DEBUG = "DEBUG";

        public static final String LOG_LEVEL_TRACE = "TRACE";

        public static final String LOG_LEVEL_ALL = "ALL";

        public static final String LOG_LEVEL_OFF = "OFF";

        private String logLevelTh2 = LOG_LEVEL_INFO;

        private String logLevelRoot = LOG_LEVEL_INFO;

        public String getLogLevelTh2() {
            return logLevelTh2;
        }

        public String getLogLevelRoot() {
            return logLevelRoot;
        }

        public void setLogLevelTh2(String logLevel) {

            if (logLevel == null || logLevel.equals("")) {
                this.logLevelTh2 = LOG_LEVEL_DEBUG;
                return;
            }

            if (logLevel.equals(LOG_LEVEL_ERROR) ||
                    logLevel.equals(LOG_LEVEL_WARNING) ||
                    logLevel.equals(LOG_LEVEL_INFO) ||
                    logLevel.equals(LOG_LEVEL_OFF) ||
                    logLevel.equals(LOG_LEVEL_TRACE) ||
                    logLevel.equals(LOG_LEVEL_ALL) ||
                    logLevel.equals(LOG_LEVEL_FATAL) ||
                    logLevel.equals(LOG_LEVEL_DEBUG)) {
                this.logLevelTh2 = logLevel;
            } else {
                throw new IllegalArgumentException(String.format("Unknown value (%s)", logLevel));
            }
        }

        public void setLogLevelRoot(String logLevel) {

            if (logLevel == null || logLevel.equals("")) {
                this.logLevelRoot = LOG_LEVEL_INFO;
                return;
            }

            if (logLevel.equals(LOG_LEVEL_ERROR) ||
                    logLevel.equals(LOG_LEVEL_WARNING) ||
                    logLevel.equals(LOG_LEVEL_INFO) ||
                    logLevel.equals(LOG_LEVEL_OFF) ||
                    logLevel.equals(LOG_LEVEL_TRACE) ||
                    logLevel.equals(LOG_LEVEL_ALL) ||
                    logLevel.equals(LOG_LEVEL_FATAL) ||
                    logLevel.equals(LOG_LEVEL_DEBUG)) {
                this.logLevelRoot = logLevel;
            } else {
                throw new IllegalArgumentException(String.format("Unknown value (%s)", logLevel));
            }
        }
    }
}
