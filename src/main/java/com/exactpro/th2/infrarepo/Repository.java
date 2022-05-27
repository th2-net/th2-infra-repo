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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class Repository {

    public static final int RESOURCE_NAME_MAX_LENGTH = 64;

    public static final String YML_EXTENSION = ".yml";

    public static final String YAML_EXTENSION = ".yaml";

    private static final ObjectMapper generalMapper = new ObjectMapper();

    private static RepositoryResource loadYAML(File file) throws IOException {

        String contents = Files.readString(file.toPath());
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory())
                .enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
        RepositoryResource resource = mapper.readValue(contents, RepositoryResource.class);
        resource.setSourceHash(Repository.digest(contents));

        return resource;
    }

    private static void saveYAML(File file, RepositoryResource resource) throws IOException {

        file.getParentFile().mkdir();
        ObjectMapper mapper = new ObjectMapper(
                new YAMLFactory()
                        .disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
                        .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
        ).setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String contents = mapper.writeValueAsString(resource);
        resource.setSourceHash(Repository.digest(contents));
        Files.writeString(file.toPath(), contents);
    }

    private static Set<RepositoryResource> loadKind(File repositoryRoot, ResourceType kind) {
        Logger logger = LoggerFactory.getLogger(Repository.class);

        Set<RepositoryResource> resources = new HashSet<>();
        Map<String, RepositoryResource> firstOccurrences = new HashMap<>();

        if (kind.isRepositoryResource()) {
            File dir = new File(repositoryRoot.getAbsolutePath() + "/" + kind.path());
            if (dir.exists()) {

                if (!dir.isDirectory()) {
                    logger.error("entry expected to be a directory: \"{}\"", dir.getAbsoluteFile());
                    return Collections.emptySet();
                }

                File[] files = dir.listFiles();
                if (files == null) {
                    return resources;
                }

                for (File f : files) {
                    if (f.isFile() && (f.getAbsolutePath().endsWith(".yml") || f.getAbsolutePath().endsWith(".yaml"))) {
                        try {
                            RepositoryResource resource = Repository.loadYAML(f);
                            RepositoryResource.Metadata meta = resource.getMetadata();

                            if (meta == null || !extractName(f.getName()).equals(meta.getName())) {
                                logger.warn("skipping \"{}\" | resource name does not match filename",
                                        f.getAbsolutePath());
                                continue;
                            }

                            if (!isNameLengthValid(meta.getName())) {
                                logger.warn("skipping \"{}\" | resource name must be less than {} characters",
                                        meta.getName(), RESOURCE_NAME_MAX_LENGTH);
                                continue;
                            }

                            if (!ResourceType.forKind(resource.getKind()).path().equals(kind.path())) {
                                logger.error("skipping \"{}\" | resource is located in wrong directory. kind" +
                                        ": {}, dir:" + " {}", f.getAbsolutePath(), resource.getKind(), kind.path());
                                continue;
                            }

                            // some directories might contain multiple resource kinds
                            // skip other kinds as they will be checked on their iteration
                            if (!resource.getKind().equals(kind.kind())) {
                                continue;
                            }

                            String name = meta.getName();
                            RepositoryResource sameNameResource = firstOccurrences.get(name);
                            if (sameNameResource != null) {
                                // we already encountered resource with same name
                                // ignore both of them
                                logger.error("\"{}/{}\" has the same name as \"{}/{}\". " +
                                                "skipping both of them. this may cause \"{}\" to be undeployed",
                                        resource.getKind(), name,
                                        sameNameResource.getKind(), name,
                                        name);
                                resources.remove(firstOccurrences.get(name));
                                continue;
                            }

                            resources.add(resource);
                            firstOccurrences.put(name, resource);
                        } catch (Exception e) {
                            logger.error("skipping \"{}\" | exception loading resource", f.getAbsolutePath(), e);
                        }
                    }
                }
            }
        }
        return resources;
    }

    private static Set<RepositoryResource> loadBranch(File repositoryRoot) {

        Set<RepositoryResource> resources = new HashSet<>();
        for (ResourceType t : ResourceType.values()) {
            resources.addAll(loadKind(repositoryRoot, t));
        }

        return resources;
    }

    private static File fileFor(Gitter gitter, RepositoryResource resource, String extension) {

        return new File(
                gitter.getConfig().getLocalRepositoryRoot()
                        + "/" + gitter.getBranch()
                        + "/" + ResourceType.forKind(resource.getKind()).path()
                        + "/" + resource.getMetadata().getName()
                        + extension);
    }

    private static String extractName(String fileName) {

        int index = fileName.lastIndexOf(".");
        if (index < 0) {
            return fileName;
        } else {
            return fileName.substring(0, index);
        }
    }

    private static boolean isNameLengthValid(String resourceName) {
        return resourceName.length() < RESOURCE_NAME_MAX_LENGTH;
    }

    private static String digest(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(data.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method will checkout latest version from the repository
     * and will create RepositorySnapshot from it.
     *
     * @param gitter Gitter object that will be used to checkout data from the repository.
     *               Must be locked externally as this method does not lock repository by itself
     * @param kind   what kind of resources to load
     * @return Latest versions of resources for given kind
     * @throws IOException     If repository IO operation fails
     * @throws GitAPIException If git checkout operation fails
     */
    public static Set<RepositoryResource> getResourcesByKind(Gitter gitter, ResourceType kind)
            throws IOException, GitAPIException {

        String path = gitter.getConfig().getLocalRepositoryRoot() + "/" + gitter.getBranch();
        gitter.checkout();

        return Repository.loadKind(new File(path), kind);
    }

    /**
     * This method will checkout latest version from the repository
     * and will create RepositorySnapshot from it.
     *
     * @param gitter Gitter object that will be used to checkout data from the repository.
     *               Must be locked externally as this method does not lock repository by itself
     * @return Latest versions of resources for Th2Box, Th2CoreBox, Th2Estore and Th2Mstore kind
     * @throws IOException     If repository IO operation fails
     * @throws GitAPIException If git checkout operation fails
     */
    public static Set<RepositoryResource> getAllBoxesAndStores(Gitter gitter)
            throws IOException, GitAPIException {

        String path = gitter.getConfig().getLocalRepositoryRoot() + "/" + gitter.getBranch();
        gitter.checkout();

        Set<RepositoryResource> resources = new HashSet<>(Repository.loadKind(new File(path), ResourceType.Th2Box));
        resources.addAll(Repository.loadKind(new File(path), ResourceType.Th2CoreBox));
        resources.addAll(Repository.loadKind(new File(path), ResourceType.Th2Estore));
        resources.addAll(Repository.loadKind(new File(path), ResourceType.Th2Mstore));

        return resources;
    }

    /**
     * This method will checkout latest version from the repository
     * and will create RepositorySnapshot from it.
     *
     * @param gitter Gitter object that will be used to checkout data from the repository.
     *               Must be locked externally as this method does not lock repository by itself
     * @return Latest snapshot of repository
     * @throws IOException     If repository IO operation fails
     * @throws GitAPIException If git checkout operation fails
     */
    public static RepositorySnapshot getSnapshot(Gitter gitter) throws IOException, GitAPIException {

        String path = gitter.getConfig().getLocalRepositoryRoot() + "/" + gitter.getBranch();
        String commitRef = gitter.checkout();
        Set<RepositoryResource> resources = Repository.loadBranch(new File(path));

        RepositorySnapshot snapshot = new RepositorySnapshot(commitRef);
        snapshot.setResources(resources);
        return snapshot;
    }

    /**
     * This method will checkout latest version from the repository
     * and will return settings file for it.
     *
     * @param gitter Gitter object that will be used to checkout data from the repository.
     *               Must be locked externally as this method does not lock repository by itself
     * @return Settings file
     * @throws IOException     If repository IO operation fails
     * @throws GitAPIException If git checkout operation fails
     */
    public static RepositorySettings getSettings(Gitter gitter) throws IOException, GitAPIException {
        gitter.checkout();
        String settingsFileName = "infra-mgr-config";
        String pathYml = String.format("%s/%s/%s",
                gitter.getConfig().getLocalRepositoryRoot(), gitter.getBranch(), settingsFileName + YML_EXTENSION);
        try {
            return generalMapper.readValue(
                    generalMapper.writeValueAsString(loadYAML(new File(pathYml)).getSpec()), RepositorySettings.class
            );
        } catch (NoSuchFileException e) {
            String pathYaml = String.format("%s/%s/%s",
                    gitter.getConfig().getLocalRepositoryRoot(), gitter.getBranch(), settingsFileName + YAML_EXTENSION);
            return generalMapper.readValue(
                    generalMapper.writeValueAsString(loadYAML(new File(pathYaml)).getSpec()), RepositorySettings.class
            );
        }
    }

    /**
     * This method is called on already checkout version of the repository
     * and will return file for given name parameter.
     *
     * @param gitter       Gitter object that will be used to checkout data from the repository.
     *                     Must be locked externally as this method does not lock repository by itself
     * @param kind         Kind of resource
     * @param resourceName name of the RepositoryResource that will be loaded from the the repository
     * @return loaded RepositoryResource
     * @throws IOException If repository IO operation fails
     */
    public static RepositoryResource getResource(Gitter gitter, String kind, String resourceName) throws IOException {
        String kindPath = ResourceType.forKind(kind).path();
        String pathYml = String.format("%s/%s/%s/%s", gitter.getConfig().getLocalRepositoryRoot(),
                gitter.getBranch(), kindPath, resourceName + YML_EXTENSION);
        try {
            return loadYAML(new File(pathYml));
        } catch (Exception e) {
            String pathYaml = String.format("%s/%s/%s/%s", gitter.getConfig().getLocalRepositoryRoot(),
                    gitter.getBranch(), kindPath, resourceName + YAML_EXTENSION);
            return loadYAML(new File(pathYaml));
        }
    }

    /**
     * Adds resource to the local repository, but does not commit or push changes.
     * Throws an IllegalArgumentException if resource with same name and kind already exists
     *
     * @param gitter   Gitter object for which repository will be updated.
     *                 Must be locked externally as this method does not lock repository by itself
     * @param resource RepositoryResource that will be added to the repository
     * @throws IOException              If repository IO operation fails
     * @throws IllegalArgumentException If resource already exists in the repository
     */
    public static void add(Gitter gitter, RepositoryResource resource) throws IOException {

        File file = fileFor(gitter, resource, YML_EXTENSION);
        if (file.exists()) {
            throw new IllegalArgumentException("resource already exist");
        }
        Repository.saveYAML(file, resource);
    }

    /**
     * Updates resource in the local repository, but does not commit or push changes.
     * Throws an IllegalArgumentException if resource does not exists
     *
     * @param gitter   Gitter object for which repository will be updated.
     *                 Must be locked externally as this method does not lock repository by itself
     * @param resource RepositoryResource that will be updated in the repository
     * @throws IOException              If repository IO operation fails
     * @throws IllegalArgumentException If resource does not exists in the repository
     */
    public static void update(Gitter gitter, RepositoryResource resource) throws IOException {

        String yamlExtension = ".yaml";
        File fileYml = fileFor(gitter, resource, YML_EXTENSION);
        File fileYaml = fileFor(gitter, resource, yamlExtension);
        if (fileYml.exists() && fileYml.isFile()) {
            Repository.saveYAML(fileYml, resource);
            return;
        }
        if (fileYaml.exists() && fileYaml.isFile()) {
            Repository.saveYAML(fileYaml, resource);
            return;
        }
        throw new IllegalArgumentException("resource does not exist");
    }

    /**
     * Removes resource from the local repository, but does not commit or push changes.
     * Throws an IllegalArgumentException if resource does not exists
     *
     * @param gitter   Gitter object for which repository will be updated.
     *                 Must be locked externally as this method does not lock repository by itself
     * @param resource RepositoryResource that will be removed from the repository
     * @throws IllegalArgumentException If resource does not exists in the repository
     */
    public static void remove(Gitter gitter, RepositoryResource resource) {

        File file = fileFor(gitter, resource, YML_EXTENSION);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("resource does not exist");
        }
        file.delete();
    }

}
