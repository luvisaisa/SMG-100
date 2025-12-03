# The Completionist - Architecture & OOP Principles

> note: this documentation file was ai-generated for presentation purposes

### A Super Mario Galaxy Progress Tracker

A terminal-based application for tracking 100% completion progress in Super Mario Galaxy, built with Java using object-oriented design principles.

---

## Features

- Track progress for both **Mario** and **Luigi** playthroughs (248 total stars)
- Visual ASCII art interface with star-filled backgrounds
- Automatic unlock notifications for galaxies, comets, and hidden content
- Profile management with save/load functionality
- 100% completion celebration screen
- Spoiler-free mode with progressive reveals

---

## Object-Oriented Programming Principles

### 1. **Abstraction**

Abstract classes define common behavior while hiding implementation details.

```java
// Star.java - Abstract base class for all star types
public abstract class Star {
    protected String id;
    protected String name;
    protected boolean isHiddenByDefault;
    protected UnlockCondition unlockCondition;
    
    public abstract String getStarType();  // Subclasses must implement
    
    public boolean isUnlocked(GameProgress progress) {
        if (unlockCondition == null) return true;
        return unlockCondition.isMet(progress);
    }
}
```

**Concrete implementations:**
- `MainStar` - Primary mission stars
- `CometStar` - Prankster comet challenge stars  
- `SecretStar` - Hidden/Luigi rescue stars
- `GreenStar` - Green power stars (unlock Planet of Trials)

---

### 2. **Encapsulation**

Data is protected within classes, accessed only through controlled interfaces.

```java
// PlayerProfile.java - Encapsulated player data
public class PlayerProfile {
    private String name;
    private CharacterMode currentMode;
    private GameProgress gameProgress;
    private long playTimeMinutes;
    
    // Controlled access through getters
    public int getMarioStarCount(GameProgress progress) {
        return (int) progress.getAllStarProgress().values().stream()
            .filter(sp -> !sp.getStarId().startsWith("luigi-"))
            .filter(sp -> !sp.getStarId().contains("green-star"))
            .filter(StarProgress::isCollected)
            .count();
    }
    
    // State changes through validated setters
    public void setCurrentMode(CharacterMode mode) {
        if (mode != null) {
            this.currentMode = mode;
        }
    }
}
```

---

### 3. **Inheritance**

Class hierarchies enable code reuse and specialization.

```
UnlockCondition (Abstract)
    ├── TotalStarsCondition      // Unlock after N stars collected
    ├── StarCollectedCondition   // Unlock after specific star collected
    ├── GreenStarsUnlockCondition // Unlock Planet of Trials
    └── GrandFinaleUnlockCondition // Unlock Grand Finale Galaxy
```

```java
// TotalStarsCondition.java - Inherits from UnlockCondition
public class TotalStarsCondition extends UnlockCondition {
    private final int requiredStars;
    
    @Override
    public boolean isMet(GameProgress progress) {
        return progress.getCollectedCount() >= requiredStars;
    }
    
    @Override
    public String getDescription() {
        return "Collect " + requiredStars + " stars";
    }
}
```

---

### 4. **Polymorphism**

Objects are treated uniformly through interfaces, with behavior varying by type.

```java
// ICompletionTrackable.java - Polymorphic interface
public interface ICompletionTrackable {
    int getTotalItems();
    int getCompletedItems(GameProgress progress);
    
    default double getCompletionPercentage(GameProgress progress) {
        int total = getTotalItems();
        if (total == 0) return 0.0;
        return (getCompletedItems(progress) * 100.0) / total;
    }
}

// Used polymorphically in ConsoleUI:
public void displayProgress(ICompletionTrackable trackable) {
    // Works with Game, Dome, or Galaxy objects!
    int completed = trackable.getCompletedItems(progress);
    int total = trackable.getTotalItems();
    System.out.println(completed + " / " + total);
}
```

**Implementers:** `Game`, `Dome`, `Galaxy`

---

### 5. **Composition**

Complex objects are built from simpler components.

```java
// Game.java - Composed of Domes, which contain Galaxies, which contain Stars
public class Game {
    private String name;
    private List<Dome> domes;  // Composition: Game HAS-A list of Domes
    
    public int getTotalItems() {
        return domes.stream()
            .mapToInt(Dome::getTotalItems)
            .sum();
    }
}

// Dome.java
public class Dome {
    private List<Galaxy> galaxies;  // Dome HAS-A list of Galaxies
}

// Galaxy.java  
public class Galaxy {
    private List<Star> stars;  // Galaxy HAS-A list of Stars
}
```

---

### 6. **Interface Segregation**

Small, focused interfaces for specific behaviors.

```java
// Separate interfaces for different concerns
public interface ICompletionTrackable {
    int getTotalItems();
    int getCompletedItems(GameProgress progress);
}

public interface UnlockCondition {
    boolean isMet(GameProgress progress);
    boolean isMetForMode(GameProgress progress, CharacterMode mode);
    String getDescription();
}
```

---

### 7. **Factory Pattern**

Centralized object creation with complex initialization logic.

```java
// GameFactory.java - Creates the entire game structure
public class GameFactory {
    public static Game createSuperMarioGalaxy() {
        List<Dome> domes = new ArrayList<>();
        domes.add(createTerraceDome());
        domes.add(createFountainDome());
        domes.add(createKitchenDome());
        // ... more domes
        return new Game("Super Mario Galaxy", domes);
    }
    
    private static Dome createTerraceDome() {
        List<Galaxy> galaxies = new ArrayList<>();
        galaxies.add(createGoodEggGalaxy());
        galaxies.add(createHoneyhiveGalaxy());
        // ...
        return new Dome("TERRACE", galaxies, null);
    }
}
```

---

### 8. **Enum for Type Safety**

Enums ensure type-safe mode switching.

```java
// CharacterMode.java
public enum CharacterMode {
    MARIO(""),
    LUIGI("luigi-");
    
    private final String prefix;
    
    public String getStarIdPrefix() {
        return prefix;
    }
    
    public String getPrefixedStarId(String baseId) {
        return prefix + baseId;
    }
}
```

---

## File Architecture

```
the-completionist/
├── app/
│   ├── build.gradle                 # Build configuration
│   ├── data/profiles/               # Save files (JSON)
│   │   └── *.json
│   └── src/
│       ├── main/
│       │   ├── java/com/completionist/
│       │   │   ├── App.java                    # Entry point
│       │   │   ├── model/                      # Domain models
│       │   │   │   ├── Game.java
│       │   │   │   ├── Dome.java
│       │   │   │   ├── Galaxy.java
│       │   │   │   ├── Star.java (abstract)
│       │   │   │   ├── MainStar.java
│       │   │   │   ├── CometStar.java
│       │   │   │   ├── SecretStar.java
│       │   │   │   ├── GreenStar.java
│       │   │   │   ├── CharacterMode.java (enum)
│       │   │   │   ├── GameFactory.java
│       │   │   │   ├── ICompletionTrackable.java
│       │   │   │   ├── UnlockCondition.java (abstract)
│       │   │   │   ├── TotalStarsCondition.java
│       │   │   │   ├── StarCollectedCondition.java
│       │   │   │   ├── GreenStarsUnlockCondition.java
│       │   │   │   └── GrandFinaleUnlockCondition.java
│       │   │   ├── progress/                   # Progress tracking
│       │   │   │   ├── GameProgress.java
│       │   │   │   ├── StarProgress.java
│       │   │   │   └── PlayerProfile.java
│       │   │   └── ui/                         # User interface
│       │   │       ├── ConsoleUI.java
│       │   │       ├── ConsoleUtils.java
│       │   │       ├── ConsoleColors.java
│       │   │       ├── ScreenBuffer.java
│       │   │       └── ProfileSelector.java
│       │   └── resources/games/
│       │       └── super-mario-galaxy.json     # Game data
│       └── test/java/com/completionist/
│           └── AppTest.java
├── gradle/
│   └── wrapper/
├── CLAUDE.md                        # AI assistant context
├── ARCHITECTURE.md                  # This file
├── README.md                        # Quick start guide
├── run.sh                           # Run script
├── gradlew                          # Gradle wrapper (Unix)
└── gradlew.bat                      # Gradle wrapper (Windows)
```

---

## Class Interaction Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                   App                                        │
│                              (Entry Point)                                   │
└─────────────────────────────────┬───────────────────────────────────────────┘
                                  │ creates
                                  ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              ConsoleUI                                       │
│                         (Main Controller)                                    │
│  - Handles user input                                                        │
│  - Manages game state                                                        │
│  - Coordinates all components                                                │
└───────┬────────────────┬────────────────┬────────────────┬──────────────────┘
        │                │                │                │
        ▼                ▼                ▼                ▼
┌───────────────┐ ┌─────────────┐ ┌──────────────┐ ┌───────────────────┐
│ ProfileSelector│ │   Game      │ │PlayerProfile │ │   ConsoleUtils    │
│               │ │ (from       │ │              │ │   ScreenBuffer    │
│ - Select/create│ │ GameFactory)│ │ - Save/Load  │ │   ConsoleColors   │
│   profiles    │ │             │ │ - Progress   │ │                   │
└───────────────┘ └──────┬──────┘ └──────┬───────┘ └───────────────────┘
                         │               │
        ┌────────────────┼───────────────┘
        │                │
        ▼                ▼
┌───────────────────────────────────────────────────────────────────┐
│                        Game Structure                              │
│                                                                    │
│  Game ◆────────────< Dome >────────────< Galaxy >────────< Star > │
│   │                   │                    │                 │     │
│   │ ICompletionTrackable                   │                 │     │
│   │    (interface)    │                    │                 │     │
│   └───────────────────┴────────────────────┘                 │     │
│                                                              │     │
│                                              ┌───────────────┴──┐  │
│                                              │   Star Types     │  │
│                                              │   (inheritance)  │  │
│                                              │                  │  │
│                                              │  ┌── MainStar    │  │
│                                              │  ├── CometStar   │  │
│                                              │  ├── SecretStar  │  │
│                                              │  └── GreenStar   │  │
│                                              └──────────────────┘  │
└───────────────────────────────────────────────────────────────────┘
                         │
                         ▼
┌───────────────────────────────────────────────────────────────────┐
│                     Unlock Conditions                              │
│                                                                    │
│  UnlockCondition (abstract)                                        │
│         △                                                          │
│         │                                                          │
│    ┌────┴────┬──────────────┬──────────────────┐                  │
│    │         │              │                  │                   │
│    ▼         ▼              ▼                  ▼                   │
│ TotalStars  StarCollected  GreenStars    GrandFinale              │
│ Condition   Condition      Condition     Condition                 │
└───────────────────────────────────────────────────────────────────┘
                         │
                         ▼
┌───────────────────────────────────────────────────────────────────┐
│                      Progress Tracking                             │
│                                                                    │
│  GameProgress ◆─────────< StarProgress >                          │
│       │                        │                                   │
│       │                        ├── starId                          │
│       │                        ├── collected (boolean)             │
│       │                        └── notes                           │
│       │                                                            │
│       └── getAllStarProgress(): Map<String, StarProgress>          │
│       └── isStarCollected(id): boolean                             │
│       └── getCollectedCount(): int                                 │
└───────────────────────────────────────────────────────────────────┘
```

---

## Data Flow

```
User Input
    │
    ▼
┌─────────────┐      ┌──────────────┐      ┌─────────────────┐
│  ConsoleUI  │ ──── │ PlayerProfile │ ──── │  JSON Storage   │
│  (control)  │      │   (state)    │      │  (persistence)  │
└─────────────┘      └──────────────┘      └─────────────────┘
    │                       │
    │                       ▼
    │               ┌──────────────┐
    │               │ GameProgress │
    │               │  (tracking)  │
    │               └──────────────┘
    │                       │
    ▼                       ▼
┌─────────────┐      ┌──────────────┐
│    Game     │ ──── │UnlockCondition│
│  (model)    │      │  (rules)     │
└─────────────┘      └──────────────┘
    │
    ▼
┌─────────────────────────────────────┐
│         Screen Rendering            │
│  ConsoleUtils → ScreenBuffer → TTY  │
└─────────────────────────────────────┘
```

---

## Running the Application

```bash
# Clone and navigate to project
cd the-completionist

# Run with Gradle
./gradlew run

# Or use the run script
./run.sh
```

---

## Project Statistics

| Component | Files | Lines (approx) |
|-----------|-------|----------------|
| Model | 14 | ~1,500 |
| Progress | 3 | ~300 |
| UI | 5 | ~2,000 |
| Total | 22+ | ~3,800+ |

---

## Learning Outcomes

This project demonstrates:

- **Clean Architecture**: Separation of concerns (Model/View/Controller)
- **SOLID Principles**: Single responsibility, Open/closed, Interface segregation
- **Design Patterns**: Factory, Strategy (unlock conditions), Composite (game structure)
- **Java Best Practices**: Streams, Enums, Generics, Default interface methods
- **File I/O**: JSON serialization/deserialization with Gson
- **Terminal UI**: ANSI escape codes, screen buffering, Unicode art

---

*Built with Java*
