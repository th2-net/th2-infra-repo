/*
 * Copyright 2020-2022 Exactpro (Exactpro Systems Limited)
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

public class SchemaUtils {
    public static Map<String, Map<String, RepositoryResource>> convertToRepositoryMap(
            Set<RepositoryResource> repositoryResources
    ) {
        Map<String, Map<String, RepositoryResource>> repositoryMap = new HashMap<>();
        for (ResourceType t : ResourceType.values()) {
            if (t.isK8sResource()) {
                repositoryMap.put(t.kind(), new HashMap<>());
            }
        }

        for (RepositoryResource resource : repositoryResources) {
            if (ResourceType.forKind(resource.getKind()).isK8sResource()) {
                Map<String, RepositoryResource> typeMap = repositoryMap.get(resource.getKind());
                typeMap.put(resource.getMetadata().getName(), resource);
            }
        }
        return repositoryMap;
    }
}
