package com.completionist.ui;

import com.completionist.progress.PlayerProfile;
import com.completionist.storage.StorageException;
import com.completionist.storage.StorageService;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

import java.io.IOException;
import java.util.List;

import static com.completionist.ui.ConsoleColors.*;
import static com.completionist.ui.ConsoleUtils.*;

// startup screen - pick or create profile
public class ProfileSelector {
    private final StorageService storage;
    private final Terminal terminal;
    private final LineReader lineReader;

    public ProfileSelector(StorageService storage) {
        this.storage = storage;
        
        try {
            this.terminal = TerminalBuilder.builder()
                .system(true)
                .jna(true)
                .jansi(true)
                .dumb(false)
                .build();

            this.lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize terminal", e);
        }
    }

    // show profile selection and return the chosen profile
    public PlayerProfile selectProfile() {
        List<String> existingProfiles = storage.listProfiles();
        
        if (existingProfiles.isEmpty()) {
            // no profiles - make a new one
            return promptNewProfile();
        } else {
            // show profile picker
            return showProfileMenu(existingProfiles);
        }
    }

    // ask for a name for new profile
    private PlayerProfile promptNewProfile() {
        clearScreen();
        printTitle();
        System.out.println();
        System.out.println(colored("Welcome to The Completionist!", CYAN));
        System.out.println();
        System.out.println("No saved profiles found. Let's create one!");
        System.out.println();
        
        String name = "";
        while (name.isEmpty()) {
            try {
                name = lineReader.readLine("Enter your name: ").trim();
                if (name.isEmpty()) {
                    System.out.println(colored("Name cannot be empty. Please try again.", "\033[31m"));
                }
            } catch (Exception e) {
                System.out.println("Error reading input, using default name.");
                name = "Player";
            }
        }
        
        // create profile id from name (lowercase, no spaces)
        String playerId = name.toLowerCase().replaceAll("\\s+", "-").replaceAll("[^a-z0-9-]", "");
        if (playerId.isEmpty()) {
            playerId = "player";
        }
        
        // make id unique
        int counter = 1;
        String baseId = playerId;
        while (storage.profileExists(playerId)) {
            playerId = baseId + "-" + counter;
            counter++;
        }
        
        PlayerProfile newProfile = new PlayerProfile(playerId, name);
        // New profiles start with 0 star bits and 0 play time (default)
        
        try {
            storage.saveProfile(newProfile);
            System.out.println();
            System.out.println(colored("✓ Created profile: " + name, CYAN));
            Thread.sleep(1000);
        } catch (StorageException e) {
            System.err.println("Warning: Could not save new profile: " + e.getMessage());
        } catch (InterruptedException ignored) {
        }
        
        return newProfile;
    }

    // show menu to select or create profile
    private PlayerProfile showProfileMenu(List<String> existingProfiles) {
        int selectedIndex = 0;
        int totalOptions = existingProfiles.size() + 2; // +1 for "New Profile", +1 for "Delete Profile"
        
        var savedAttributes = terminal.enterRawMode();
        var attrs = terminal.getAttributes();
        attrs.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ECHO, false);
        attrs.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ECHONL, false);
        attrs.setLocalFlag(org.jline.terminal.Attributes.LocalFlag.ICANON, false);
        terminal.setAttributes(attrs);
        hideCursor();
        
        try {
            while (true) {
                // Refresh the profile list in case of deletion
                List<String> profiles = storage.listProfiles();
                if (profiles.isEmpty()) {
                    terminal.setAttributes(savedAttributes);
                    showCursor();
                    return promptNewProfile();
                }
                totalOptions = profiles.size() + 2;
                if (selectedIndex >= totalOptions) {
                    selectedIndex = 0;
                }
                
                ScreenBuffer buffer = new ScreenBuffer();
                addTitleToBuffer(buffer);
                buffer.addLine("");
                buffer.addLine(colored("SELECT PROFILE", CYAN));
                addDividerToBuffer(buffer);
                
                // list existing profiles
                for (int i = 0; i < profiles.size(); i++) {
                    String profileId = profiles.get(i);
                    String prefix = (i == selectedIndex) ? colored("→ ", CYAN) : "  ";
                    
                    // try to get display name from profile
                    String displayName = profileId;
                    String starIcon = colored("★", CYAN);
                    try {
                        PlayerProfile p = storage.loadProfile(profileId);
                        displayName = p.getDisplayName();
                        // add some profile info
                        int stars = p.getAllGameProgress().values().stream()
                            .mapToInt(gp -> (int) gp.getAllStarProgress().values().stream()
                                .filter(sp -> sp.isCollected())
                                .count())
                            .sum();
                        // 248 stars = complete (yellow star icon)
                        if (stars >= 248) {
                            starIcon = colored("★", BRIGHT_YELLOW);
                        }
                        displayName += colored(" (" + stars + " stars)", BRIGHT_CYAN);
                    } catch (StorageException ignored) {
                    }
                    
                    buffer.addLine(prefix + starIcon + " " + displayName);
                }
                
                addDividerToBuffer(buffer);
                
                // "New Profile" option
                int newProfileIndex = profiles.size();
                String newPrefix = (selectedIndex == newProfileIndex) ? colored("→ ", CYAN) : "  ";
                buffer.addLine(newPrefix + colored("+", CYAN) + " Create New Profile");
                
                // "Delete Profile" option
                int deleteProfileIndex = profiles.size() + 1;
                String deletePrefix = (selectedIndex == deleteProfileIndex) ? colored("→ ", CYAN) : "  ";
                buffer.addLine(deletePrefix + colored("✕", "\033[31m") + " Delete Profile");
                
                addDividerToBuffer(buffer);
                buffer.addLine("↑↓ navigate · enter select");
                addStarBufferLines(buffer);
                
                buffer.printWithArt();
                terminal.flush();
                
                int key = readKey();
                
                if (key == 27) { // ESC sequence
                    int next1 = readKey();
                    if (next1 == 91) { // '[' - arrow key
                        int next2 = readKey();
                        if (next2 == 65) { // Up arrow
                            selectedIndex = (selectedIndex - 1 + totalOptions) % totalOptions;
                        } else if (next2 == 66) { // Down arrow
                            selectedIndex = (selectedIndex + 1) % totalOptions;
                        }
                    }
                } else if (key == 10 || key == 13) { // Enter
                    if (selectedIndex == newProfileIndex) {
                        // Create new profile
                        terminal.setAttributes(savedAttributes);
                        showCursor();
                        return promptNewProfile();
                    } else if (selectedIndex == deleteProfileIndex) {
                        // Show delete profile submenu
                        showDeleteProfileMenu(profiles);
                        // After deletion, stay in menu (loop continues)
                    } else {
                        // Load selected profile
                        String selectedId = profiles.get(selectedIndex);
                        terminal.setAttributes(savedAttributes);
                        showCursor();
                        try {
                            PlayerProfile profile = storage.loadProfile(selectedId);
                            System.out.println();
                            System.out.println(colored("✓ Loaded profile: " + profile.getDisplayName(), CYAN));
                            return profile;
                        } catch (StorageException e) {
                            System.err.println("Error loading profile: " + e.getMessage());
                            return promptNewProfile();
                        }
                    }
                } else if (key == 'q' || key == 'Q') {
                    terminal.setAttributes(savedAttributes);
                    showCursor();
                    System.exit(0);
                }
            }
        } finally {
            try {
                terminal.setAttributes(savedAttributes);
            } catch (Exception ignored) {
            }
            showCursor();
        }
    }
    
    // submenu for deleting profiles
    private void showDeleteProfileMenu(List<String> profiles) {
        int selectedIndex = 0;
        int totalOptions = profiles.size() + 1; // +1 for "Cancel" option
        
        while (true) {
            ScreenBuffer buffer = new ScreenBuffer();
            addTitleToBuffer(buffer);
            buffer.addLine("");
            buffer.addLine(colored("DELETE PROFILE", "\033[31m"));
            addDividerToBuffer(buffer);
            
            // List profiles that can be deleted
            for (int i = 0; i < profiles.size(); i++) {
                String profileId = profiles.get(i);
                String prefix = (i == selectedIndex) ? colored("→ ", "\033[31m") : "  ";
                
                String displayName = profileId;
                try {
                    PlayerProfile p = storage.loadProfile(profileId);
                    displayName = p.getDisplayName();
                    int stars = p.getAllGameProgress().values().stream()
                        .mapToInt(gp -> (int) gp.getAllStarProgress().values().stream()
                            .filter(sp -> sp.isCollected())
                            .count())
                        .sum();
                    displayName += colored(" (" + stars + " stars)", DIM);
                } catch (StorageException ignored) {
                }
                
                buffer.addLine(prefix + colored("✕", "\033[31m") + " " + displayName);
            }
            
            addDividerToBuffer(buffer);
            
            // Cancel option
            int cancelIndex = profiles.size();
            String cancelPrefix = (selectedIndex == cancelIndex) ? colored("→ ", CYAN) : "  ";
            buffer.addLine(cancelPrefix + colored("←", CYAN) + " Cancel");
            
            addDividerToBuffer(buffer);
            buffer.addLine("↑↓ navigate · enter select");
            addStarBufferLines(buffer);
            
            buffer.printWithArt();
            terminal.flush();
            
            int key = readKey();
            
            if (key == 27) { // ESC sequence
                int next1 = readKey();
                if (next1 == 91) { // '[' - arrow key
                    int next2 = readKey();
                    if (next2 == 65) { // Up arrow
                        selectedIndex = (selectedIndex - 1 + totalOptions) % totalOptions;
                    } else if (next2 == 66) { // Down arrow
                        selectedIndex = (selectedIndex + 1) % totalOptions;
                    }
                }
            } else if (key == 10 || key == 13) { // Enter
                if (selectedIndex == cancelIndex) {
                    return; // Go back to main menu
                } else {
                    // Confirm deletion
                    String profileId = profiles.get(selectedIndex);
                    if (confirmDelete(profileId)) {
                        try {
                            storage.deleteProfile(profileId);
                        } catch (StorageException e) {
                            // Ignore deletion errors, will refresh list anyway
                        }
                    }
                    return; // Go back to main menu
                }
            } else if (key == 'q' || key == 'Q') {
                return; // Go back to main menu
            }
        }
    }
    
    // confirmation dialog - are you sure?
    private boolean confirmDelete(String profileId) {
        String displayName = profileId;
        try {
            PlayerProfile p = storage.loadProfile(profileId);
            displayName = p.getDisplayName();
        } catch (StorageException ignored) {
        }
        
        int selectedIndex = 1; // Default to "No"
        
        while (true) {
            ScreenBuffer buffer = new ScreenBuffer();
            addTitleToBuffer(buffer);
            buffer.addLine("");
            buffer.addLine(colored("CONFIRM DELETE", "\033[31m"));
            addDividerToBuffer(buffer);
            buffer.addLine("");
            buffer.addLine("  Delete profile \"" + displayName + "\"?");
            buffer.addLine(colored("  This cannot be undone!", "\033[31m"));
            buffer.addLine("");
            addDividerToBuffer(buffer);
            
            String yesPrefix = (selectedIndex == 0) ? colored("→ ", "\033[31m") : "  ";
            String noPrefix = (selectedIndex == 1) ? colored("→ ", CYAN) : "  ";
            buffer.addLine(yesPrefix + colored("Yes, delete", "\033[31m"));
            buffer.addLine(noPrefix + colored("No, cancel", CYAN));
            
            addDividerToBuffer(buffer);
            buffer.addLine("↑↓ navigate · enter select");
            addStarBufferLines(buffer);
            
            buffer.printWithArt();
            terminal.flush();
            
            int key = readKey();
            
            if (key == 27) { // ESC sequence
                int next1 = readKey();
                if (next1 == 91) { // '[' - arrow key
                    int next2 = readKey();
                    if (next2 == 65 || next2 == 66) { // Up or Down arrow
                        selectedIndex = 1 - selectedIndex; // Toggle between 0 and 1
                    }
                }
            } else if (key == 10 || key == 13) { // Enter
                return selectedIndex == 0;
            } else if (key == 'q' || key == 'Q' || key == 'n' || key == 'N') {
                return false;
            } else if (key == 'y' || key == 'Y') {
                return true;
            }
        }
    }
    
    private void printTitle() {
        System.out.println(colored("  ╭──────────────────────────────╮", CYAN));
        System.out.println(colored("  │  ★ THE COMPLETIONIST ★      │", CYAN));
        System.out.println(colored("  ╰──────────────────────────────╯", CYAN));
    }
    
    private void addTitleToBuffer(ScreenBuffer buffer) {
        buffer.addLine(colored("  ╭──────────────────────────────╮", CYAN));
        buffer.addLine(colored("  │  ★ THE COMPLETIONIST ★      │", CYAN));
        buffer.addLine(colored("  ╰──────────────────────────────╯", CYAN));
    }
    
    private void addDividerToBuffer(ScreenBuffer buffer) {
        buffer.addLine(colored("  " + "─".repeat(30), DIM));
    }
    
    private void addStarBufferLines(ScreenBuffer buffer) {
        // Star fill is now handled by ScreenBuffer.printWithArt()
        // No additional lines needed here
    }
    
    private int readKey() {
        try {
            return terminal.reader().read();
        } catch (IOException e) {
            return -1;
        }
    }
    
    // cleanup when done
    public void close() {
        try {
            terminal.close();
        } catch (IOException ignored) {
        }
    }
}
