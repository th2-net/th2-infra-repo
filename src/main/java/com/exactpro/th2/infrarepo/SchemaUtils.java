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
