package com.completionist.storage;

import com.completionist.progress.PlayerProfile;
import java.util.List;

public interface StorageService {
    void saveProfile(PlayerProfile profile) throws StorageException;
    PlayerProfile loadProfile(String playerId) throws StorageException;
    boolean profileExists(String playerId);
    List<String> listProfiles();
    void deleteProfile(String playerId) throws StorageException;
}
