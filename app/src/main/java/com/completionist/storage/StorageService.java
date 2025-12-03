package com.completionist.storage;

import com.completionist.progress.PlayerProfile;
import java.util.List;

// interface for saving/loading player profiles
public interface StorageService {
    // save a profile
    void saveProfile(PlayerProfile profile) throws StorageException;

    // load a profile by id
    PlayerProfile loadProfile(String playerId) throws StorageException;

    // check if profile exists
    boolean profileExists(String playerId);

    // get all profile ids
    List<String> listProfiles();

    // delete a profile
    void deleteProfile(String playerId) throws StorageException;
}
