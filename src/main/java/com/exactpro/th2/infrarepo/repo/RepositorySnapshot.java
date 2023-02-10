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
import com.exactpro.th2.infrarepo.settings.RepositorySettingsSpec;

import java.util.Set;

import static com.exactpro.th2.infrarepo.SchemaUtils.JSON_MAPPER;

public class RepositorySnapshot {

    private final String commitRef;

    private final Set<RepositoryResource> resources;

    private RepositorySettingsSpec repositorySettingsSpec;

    public RepositorySnapshot(String commitRef, Set<RepositoryResource> resources) {
        this.commitRef = commitRef;
        this.resources = resources;
        for (RepositoryResource resource : resources) {
            if (resource.getKind().equals(ResourceType.SettingsFile.kind())) {
                repositorySettingsSpec = JSON_MAPPER.convertValue(resource.getSpec(), RepositorySettingsSpec.class);
            }
        }
    }

    public String getCommitRef() {
        return commitRef;
    }

    public Set<RepositoryResource> getResources() {
        return resources;
    }

    public RepositorySettingsSpec getRepositorySettingsSpec() {
        return repositorySettingsSpec;
    }
}
