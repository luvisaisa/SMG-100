package com.completionist.storage;

import com.completionist.progress.PlayerProfile;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

// saves to ./data/profiles/{id}.json
public class JsonStorageService implements StorageService {
    private final Path profilesDir;
    private final ObjectMapper mapper;

    public JsonStorageService() {
        this(Paths.get("data", "profiles"));
    }

    public JsonStorageService(Path profilesDir) {
        this.profilesDir = profilesDir;
        this.mapper = createObjectMapper();

        // make sure the folder exists
        try {
            Files.createDirectories(profilesDir);
        } catch (IOException e) {
            System.err.println("Warning: Could not create profiles directory: " + e.getMessage());
        }
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

    @Override
    public void saveProfile(PlayerProfile profile) throws StorageException {
        Path profilePath = getProfilePath(profile.getPlayerId());

        try {
            // backup existing file first
            if (Files.exists(profilePath)) {
                Path backupPath = Paths.get(profilePath.toString() + ".bak");
                Files.copy(profilePath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            }

            // write the json
            mapper.writeValue(profilePath.toFile(), profile);
        } catch (IOException e) {
            throw new StorageException("Failed to save profile: " + profile.getPlayerId(), e);
        }
    }

    @Override
    public PlayerProfile loadProfile(String playerId) throws StorageException {
        Path profilePath = getProfilePath(playerId);

        if (!Files.exists(profilePath)) {
            throw new StorageException("Profile not found: " + playerId);
        }

        try {
            return mapper.readValue(profilePath.toFile(), PlayerProfile.class);
        } catch (IOException e) {
            throw new StorageException("Failed to load profile: " + playerId, e);
        }
    }

    @Override
    public boolean profileExists(String playerId) {
        return Files.exists(getProfilePath(playerId));
    }

    @Override
    public List<String> listProfiles() {
        List<String> profiles = new ArrayList<>();

        if (!Files.exists(profilesDir)) {
            return profiles;
        }

        try (Stream<Path> paths = Files.list(profilesDir)) {
            paths.filter(path -> path.toString().endsWith(".json"))
                 .forEach(path -> {
                     String fileName = path.getFileName().toString();
                     String playerId = fileName.substring(0, fileName.length() - 5); // Remove .json
                     profiles.add(playerId);
                 });
        } catch (IOException e) {
            System.err.println("Warning: Could not list profiles: " + e.getMessage());
        }

        return profiles;
    }

    @Override
    public void deleteProfile(String playerId) throws StorageException {
        Path profilePath = getProfilePath(playerId);

        if (!Files.exists(profilePath)) {
            throw new StorageException("Profile not found: " + playerId);
        }

        try {
            Files.delete(profilePath);

            // also clean up backup
            Path backupPath = Paths.get(profilePath.toString() + ".bak");
            if (Files.exists(backupPath)) {
                Files.delete(backupPath);
            }
        } catch (IOException e) {
            throw new StorageException("Failed to delete profile: " + playerId, e);
        }
    }

    private Path getProfilePath(String playerId) {
        return profilesDir.resolve(playerId + ".json");
    }
}
