# The Completionist

A terminal-based tracker I built to help me 100% Super Mario Galaxy. It keeps track of all 242 stars across both Mario and Luigi playthroughs.

## Why I made this

I wanted a way to track my progress without having to use a spreadsheet or check off items on a wiki. This app knows all 121 stars per character, which galaxies are in which domes, and what unlocks what — so I can just focus on playing.

## Features

The main thing is star tracking. You can mark stars as collected, and it'll show your progress per galaxy, per dome, and overall. It handles both Mario and Luigi separately since the game requires you to beat it twice for full completion.

The unlock system mirrors the actual game — domes unlock at specific star counts, prankster comets show up after you hit 13 stars and beat the main missions in a galaxy, purple comets don't appear until you've beaten Bowser and grabbed the Gateway purple coins, etc. I looked all this up on the wiki and implemented each condition.

You can also add notes to any star (helpful for the tricky ones) and rate the difficulty 1-5. Everything saves automatically to JSON files, and it keeps backups in case something goes wrong.

The UI is all terminal-based with arrow key navigation. I used JLine3 to make the keyboard input work properly on macOS since the default Java input handling doesn't play nice with arrow keys.

## Star types

The game has different kinds of stars and I color-code them in the UI:

- ⭐ Main stars — the regular missions you'd expect
- 🌟 Secret stars — hidden ones you might miss
- ☄️ Comet stars — the prankster comet challenges (speed runs, daredevil, etc)
- 💚 Green stars — there are 3 of these hidden around, they unlock Planet of Trials and are not counted as a part of the 121 stars
- 🌠 Grand stars — the ones you get from beating Bowser/Bowser Jr

## Running it

You need Java 17 or higher. Then either:

```bash
./gradlew run
```

Or build a standalone jar:

```bash
./gradlew fatJar
java -jar app/build/libs/app-all.jar
```

I also put together a macOS app bundle in the `dist/` folder if you want something double-clickable.

## Controls

Pretty simple — arrow keys to move around, enter to select or toggle a star. Hit `n` to add a note to whatever star you're on, `d` to set difficulty. Press `m` to switch between Mario and Luigi mode (Luigi unlocks after you get 120 Mario stars). `q` quits and saves.

## File structure

*(ai-generated for documentation)*

```
the-completionist/
├── app/src/main/java/com/completionist/
│   ├── App.java                 # Entry point
│   ├── model/                   # Game data structures
│   │   ├── Game.java
│   │   ├── Dome.java
│   │   ├── Galaxy.java
│   │   ├── Star.java            # Abstract base
│   │   ├── MainStar.java
│   │   ├── SecretStar.java
│   │   ├── CometStar.java
│   │   ├── GreenStar.java
│   │   ├── GrandStar.java
│   │   ├── GameFactory.java     # Builds all 121 stars
│   │   └── *Condition.java      # Unlock logic
│   ├── progress/                # Save data classes
│   │   ├── PlayerProfile.java
│   │   ├── GameProgress.java
│   │   └── StarProgress.java
│   ├── storage/                 # File I/O
│   │   ├── StorageService.java
│   │   └── JsonStorageService.java
│   └── ui/                      # Terminal UI
│       ├── ConsoleUI.java
│       ├── ConsoleUtils.java
│       ├── ConsoleColors.java
│       ├── ProfileSelector.java
│       └── ScreenBuffer.java
└── app/src/test/java/com/completionist/
    └── AppTest.java
```

## How the classes interact

*(ai-generated for documentation)*

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                                  App.java                                   │
│                              (entry point)                                  │
└─────────────────┬───────────────────┬───────────────────┬───────────────────┘
                  │                   │                   │
                  ▼                   ▼                   ▼
┌─────────────────────┐   ┌───────────────────┐   ┌───────────────────────────┐
│    GameFactory      │   │  ProfileSelector  │   │       ConsoleUI           │
│  (builds game data) │   │ (pick/create user)│   │   (main navigation)       │
└──────────┬──────────┘   └─────────┬─────────┘   └─────────────┬─────────────┘
           │                        │                           │
           ▼                        │                           │
┌──────────────────────────────┐    │         ┌─────────────────┴─────────────┐
│           Game               │    │         │                               │
│  ┌──────────────────────┐    │    │         ▼                               ▼
│  │        Dome          │    │    │   ┌───────────┐               ┌─────────────────┐
│  │  ┌────────────────┐  │    │    │   │ConsoleUtil│               │  ScreenBuffer   │
│  │  │    Galaxy      │  │    │    │   │(formatting)│               │(flicker-free)   │
│  │  │  ┌──────────┐  │  │    │    │   └───────────┘               └─────────────────┘
│  │  │  │  Star    │  │  │    │    │
│  │  │  │(abstract)│  │  │    │    │
│  │  │  └────┬─────┘  │  │    │    │
│  │  │       │        │  │    │    │
│  │  └───────┼────────┘  │    │    │
│  └──────────┼───────────┘    │    │
└─────────────┼────────────────┘    │
              │                     │
              ▼                     ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                            Star Subclasses                                  │
│  ┌──────────┐ ┌───────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐         │
│  │ MainStar │ │SecretStar │ │CometStar │ │GreenStar │ │GrandStar │         │
│  └──────────┘ └───────────┘ └──────────┘ └──────────┘ └──────────┘         │
│                                  │                                          │
│                                  ▼                                          │
│                         UnlockCondition                                     │
│           (CometCondition, TotalStarsCondition, etc.)                       │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                           Progress Tracking                                 │
│                                                                             │
│   PlayerProfile ──────────┐                                                 │
│   (one per user)          │ has many                                        │
│                           ▼                                                 │
│                     GameProgress ──────────┐                                │
│                     (one per game)         │ has many                       │
│                                            ▼                                │
│                                      StarProgress                           │
│                                   (collected, notes,                        │
│                                    difficulty rating)                       │
└─────────────────────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────────────────────┐
│                              Storage Layer                                  │
│                                                                             │
│   StorageService (interface)                                                │
│         │                                                                   │
│         ▼                                                                   │
│   JsonStorageService ─────────────▶ data/profiles/*.json                   │
│   (save/load profiles)                    │                                 │
│                                           ▼                                 │
│                                    *.json.bak (backups)                     │
└─────────────────────────────────────────────────────────────────────────────┘
```

## How saves work

Everything gets stored in `data/profiles/` as JSON. Each profile has its own file. Here's roughly what one looks like:

```json
{
  "playerId": "isa",
  "displayName": "Isa",
  "characterMode": "MARIO",
  "starBits": 1250,
  "allGameProgress": {
    "super-mario-galaxy": {
      "allStarProgress": {
        "good-egg-dino-piranha": {
          "collected": true,
          "note": "Jump on tail 3x",
          "difficultyRating": 2
        }
      }
    }
  }
}
```

## The 9 domes

For reference, here's the unlock progression:

1. Gateway — always open, it's the tutorial (2 stars)
2. Terrace — starting dome (25 stars)
3. Fountain — need 7 stars
4. Kitchen — need 18 stars
5. Bedroom — need 33 stars
6. Engine Room — need 45 stars
7. Garden — need 50 stars
8. Planet of Trials — need all 3 green stars
9. Grand Finale — need 120 stars (the final one!)

## Building from source

```bash
git clone https://github.com/luvisaisa/SMG-100.git
cd SMG-100
./gradlew build
./gradlew test
```

## License

All rights reserved — this is a school project. Super Mario Galaxy is Nintendo's property, obviously.
