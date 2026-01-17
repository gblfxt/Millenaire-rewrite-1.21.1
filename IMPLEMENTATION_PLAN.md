# Millenaire Feature Parity Implementation Plan

## Overview

This document outlines the plan to bring Millenaire-rewrite (NeoForge 1.21.1) to feature parity with the original Millenaire mod, using Millenaire-Reborn and OldSource (1.12) as references.

**Base Project:** `/home/critic/minecraft/modding/Millenaire-rewrite-1.21.1`
**Reference (Fabric):** `/home/critic/minecraft/modding/Millenaire-Reborn`
**Reference (1.12):** `/home/critic/minecraft/modding/Millenaire-Reborn/OldSource`

---

## Current Status

| Phase | Status | Progress |
|-------|--------|----------|
| Phase 1: Block System | ✅ COMPLETE | 100% (code complete, textures pending) |
| Phase 2: Villager Entity | ✅ COMPLETE | 100% (entity, rendering, inventory, animations) |
| Phase 3: AI System | 🔲 Not Started | 0% |
| Phase 4: Village System | 🔲 Not Started | 0% |
| Phase 5: Trading | 🔲 Not Started | 0% |
| Phase 6: Quests | 🔲 Not Started | 0% |
| Phase 7: World Gen | 🔲 Not Started | 0% |
| Phase 8: Polish | 🔲 Not Started | 0% |

**Last Updated:** January 17, 2026

---

## Phase 1: Block System Foundation ✅ COMPLETE

**Goal:** Port all 66 blocks from OldSource with proper NeoForge 1.21.1 patterns

### 1.1 Decorative Blocks ✅ COMPLETE

| Block | Status | Implementation |
|-------|--------|----------------|
| Byzantine Mosaic (Red/Blue) | ✅ | `BasicBuildingMaterial.BYZANTINE_MOSAIC_*` |
| Light Blue Brick | ✅ | `BasicBuildingMaterial.LIGHT_BLUE_BRICK` |
| Mayan Gold | ✅ | `BasicBuildingMaterial.MAYAN_GOLD_BLOCK` |
| DecorativeStone variants | ✅ | Integrated into `BuildingBlockRegistry` |
| DecorativeWood variants | ✅ | Integrated into `BuildingBlockRegistry` |
| DecorativeEarth variants | ✅ | Integrated into `BuildingBlockRegistry` |

**Files Created:**
- `BuildingMaterial.java` - Added BYZANTINE_MOSAIC, LIGHT_BLUE_BRICK, MAYAN_GOLD, HONEY
- `BasicBuildingMaterial.java` - Added all new material entries with variants

### 1.2 Structural Blocks ✅ COMPLETE

| Block | Status | Implementation |
|-------|--------|----------------|
| Path Blocks | ✅ | `PathBlock.java` with 15/16 height |
| Stairs/Slabs/Walls | ✅ | Auto-generated via `BuildingBlockRegistry` |

**Files Created:**
- `blocks/building/PathBlock.java` - Special height path blocks with STABLE property

### 1.3 Functional Blocks with Block Entities ✅ COMPLETE

| Block | Status | Files |
|-------|--------|-------|
| FirePit | ✅ | `FirePitBlock.java`, `FirePitBlockEntity.java`, `FirePitMenu.java`, `FirePitScreen.java` |
| LockedChest | ✅ | `LockedChestBlock.java`, `LockedChestBlockEntity.java`, `LockedChestMenu.java`, `LockedChestScreen.java` |
| ImportTable | ✅ | `ImportTableBlock.java`, `ImportTableBlockEntity.java`, `ImportTableMenu.java`, `ImportTableScreen.java` |

**Implementation Details:**
- **FirePit:** 7-slot container (3 input, 1 fuel, 3 output), cooking progress, burn time tracking
- **LockedChest:** 27 slots, lid animation, lock state, permission checking, visual lock overlay
- **ImportTable:** Building config data (dimensions, variation, level, orientation), export options, GUI

### 1.4 Agricultural Blocks ✅ COMPLETE

| Block | Status | Implementation |
|-------|--------|----------------|
| MillCropBlock | ✅ | Base crop class with 8 stages, irrigation, slow growth |
| Rice Crop | ✅ | `CROP_RICE` - requires irrigation |
| Turmeric Crop | ✅ | `CROP_TURMERIC` - Indian culture |
| Maize Crop | ✅ | `CROP_MAIZE` - slow growth, Mayan culture |
| Cotton Crop | ✅ | `CROP_COTTON` - requires irrigation |
| GrapeVine | ✅ | Double-height crop with HALF property |
| FruitLeaves | ✅ | Time-based fruit growth (diurnal cycle) |
| SilkWorm | ✅ | 4-stage progress, requires low light |

**Files Created:**
- `blocks/agriculture/MillCropBlock.java` - Base crop with 8 stages
- `blocks/agriculture/GrapeVineBlock.java` - Double-height vine
- `blocks/agriculture/FruitLeavesBlock.java` - Fruit-bearing leaves
- `blocks/agriculture/SilkWormBlock.java` - Silk production

**Registered Fruit Leaves:**
- Apple Tree (Norman) → Cider Apples
- Olive Tree (Byzantine) → Olives
- Pistachio (Seljuk) → Pistachios
- Cherry (Japanese) → Cherries
- Sakura (Japanese) → Cherry Blossoms

### 1.5 Assets & Datagen

- [x] Block registration in `ModBlocks.java`
- [x] Block entity registration in `ModBlockEntities.java`
- [x] Menu registration in `ModMenuTypes.java`
- [x] Item registration in `ModItems.java`
- [x] Screen registration in `MillenaireRewrite.java`
- [x] Localization (EN/ZH) in `ModLanguageProvider.java`
- [ ] Block textures (~120 PNG files) - **PENDING**
- [ ] Block models (JSON) - **Auto-generated via datagen**
- [ ] Loot tables - **Auto-generated via datagen**

---

## Phase 2: Villager Entity System ✅ COMPLETE

**Goal:** Port MillVillager entity with rendering and basic functionality

### 2.1 Entity Registration ✅ COMPLETE

**Files Created:**
```
src/main/java/com/jasoncian/millenaire_rewrite/entity/
├── MillVillager.java                    # Main villager entity ✅
├── culture/Culture.java                 # Culture enum (7 cultures) ✅
└── villager/VillagerProfession.java     # Profession enum (24+ professions) ✅
└── villager/VillagerAnimationState.java # Animation states enum ✅

src/main/java/com/jasoncian/millenaire_rewrite/item/
└── InvItem.java                         # Inventory item wrapper ✅

src/main/java/com/jasoncian/millenaire_rewrite/client/
├── model/MillVillagerModel.java         # Entity model with animations ✅
├── renderer/MillVillagerRenderer.java   # Entity renderer ✅
└── renderer/layer/VillagerClothesLayer.java # Clothing overlay layer ✅
```

**Reference:** `OldSource/java/org/millenaire/common/entity/MillVillager.java`

**Core Features:**
- [x] Entity registration with spawn egg
- [x] Basic movement and pathfinding (PathfinderMob base)
- [x] Synced data accessors (culture, profession, gender, name, isChild)
- [x] Health and damage handling (20 HP, 2.0 attack damage)
- [x] Cultural affiliation enum (7 cultures)
- [x] NBT save/load for persistence
- [x] Full inventory system (InvItem class, add/take/count methods)

### 2.2 Villager Professions ✅ COMPLETE

**VillagerProfession.java** implements 24+ professions:

| Culture | Professions |
|---------|-------------|
| Generic | Farmer, Miner, Lumberjack, Guard, Merchant, Wife, Child |
| Norman | Knight, Priest, Blacksmith |
| Byzantine | Silk Farmer, Orthodox Priest |
| Japanese | Samurai, Monk |
| Mayan | Hunter, Shaman |
| Indian | Brick Maker, Sadhu |
| Seljuk | Shepherd, Imam |
| Inuit | Fisher |
| Special | Chief, Visitor, Foreign Merchant, Raider |

**Features:**
- `isCombatProfession()` - Guard, Knight, Samurai, Hunter, Raider
- `isLeaderProfession()` - Chief, Priest, Monk, Shaman, Sadhu, Imam
- `isMerchantProfession()` - Merchant, Foreign Merchant
- `canBeMale()` - Gender restrictions (Wife is female-only)

### 2.3 Villager Rendering ✅ COMPLETE

**Files Created:**
- `MillVillagerModel.java` - HumanoidModel-based with animation states
- `MillVillagerRenderer.java` - HumanoidMobRenderer with all layers
- `VillagerClothesLayer.java` - Two-layer clothing overlay system

**Implementation Details:**
- Uses vanilla ModelLayers.PLAYER for humanoid skeleton
- Texture path: `textures/entity/villager/{culture}/{gender}_{profession}.png`
- Default texture: `norman/male_farmer.png`
- Child scaling (0.5x) with proper head proportion handling
- Armor layer support via HumanoidArmorLayer
- ItemInHandLayer for held item rendering
- VillagerClothesLayer (2 layers) for clothing overlays

**Features Complete:**
- [x] Clothing texture layers per culture
- [x] Tool/item held rendering via ItemInHandLayer
- [x] Animation states (idle, walking, running, working, attacking, etc.)

### 2.4 Villager Interaction ✅ COMPLETE

**Files Created:**
- `VillagerInteractionMenu.java` - Menu container for interaction
- `VillagerInteractionScreen.java` - GUI screen with info display

**Features Implemented:**
- [x] Right-click opens interaction menu
- [x] Displays villager name, culture, profession, gender, status
- [x] Trade button (UI ready, logic pending)
- [x] Hire button (UI ready, logic pending)
- [x] Close button
- [x] EN/ZH translations

**Pending:**
- [ ] Trading interface trigger (link to Phase 5)
- [ ] Quest interface trigger (link to Phase 6)
- [ ] Reputation display system
- [ ] Gift giving mechanics

---

## Phase 3: AI & Goal System

**Goal:** Port the goal-based AI system for villager behaviors

### 3.1 Goal Framework (Week 5)

**Files to create:**
```
src/main/java/com/jasoncian/millenaire_rewrite/entity/ai/
├── Goal.java                   # Abstract base goal
├── GoalManager.java            # Goal selection/priority
├── GoalState.java              # Goal state enum
└── goals/                      # Individual goal implementations
```

**Reference:** `OldSource/java/org/millenaire/common/goal/Goal.java`

**Base Goal System:**
- [ ] Goal priority system
- [ ] Goal state machine (INACTIVE, ACTIVE, COMPLETED, FAILED)
- [ ] Goal interruption handling
- [ ] Goal persistence (save/load)

### 3.2 Generic Goals (Week 5-6)

| Goal | Description | Priority |
|------|-------------|----------|
| GoalIdle | Default idle behavior | Low |
| GoalSleep | Find bed and sleep | High (night) |
| GoalEat | Find and consume food | High (hungry) |
| GoalSocialize | Talk to other villagers | Low |
| GoalWander | Random movement | Very Low |

### 3.3 Work Goals (Week 6)

| Goal | Description | Profession |
|------|-------------|------------|
| GoalFarm | Plant/harvest crops | Farmer |
| GoalMine | Mine resources | Miner |
| GoalChopWood | Harvest trees | Lumberjack |
| GoalCraft | Craft items | Various |
| GoalCook | Cook food at fire pit | Wife/Cook |
| GoalFish | Fish at water | Fisher |
| GoalHunt | Hunt animals | Hunter |
| GoalGatherSilk | Collect silk (Byzantine) | Silk Farmer |
| GoalDryBricks | Make bricks (Indian) | Brick Maker |

### 3.4 Building Goals (Week 6-7)

| Goal | Description |
|------|-------------|
| GoalConstructionStepByStep | Build structures block by block |
| GoalBuildPath | Construct roads between buildings |
| GoalDeliverGoods | Transport items between buildings |
| GoalGetGoods | Retrieve items from storage |

### 3.5 Combat Goals (Week 7)

| Goal | Description |
|------|-------------|
| GoalDefendVillage | Protect village from threats |
| GoalHuntMonster | Actively hunt hostile mobs |
| GoalPatrol | Guard patrol routes |
| GoalRaidVillage | Enemy raid behavior |

---

## Phase 4: Village System

**Goal:** Implement village generation, management, and growth

### 4.1 Village Data Structure (Week 7)

**Files to create:**
```
src/main/java/com/jasoncian/millenaire_rewrite/village/
├── Village.java                # Main village class
├── VillageManager.java         # World village tracking
├── VillageType.java            # Village type definitions
└── VillageData.java            # Saved data component
```

**Reference:** `OldSource/java/org/millenaire/common/village/`

**Core Features:**
- [ ] Village center point
- [ ] Village bounds calculation
- [ ] Villager population tracking
- [ ] Resource inventory
- [ ] Reputation system per player

### 4.2 Building System (Week 8)

**Files to create:**
```
src/main/java/com/jasoncian/millenaire_rewrite/building/
├── Building.java               # Building definition
├── BuildingLocation.java       # Placed building instance
├── BuildingProject.java        # Construction in progress
├── BuildingPlan.java           # Structure template
└── BuildingRegistry.java       # All building definitions
```

**Reference:** `OldSource/java/org/millenaire/common/building/Building.java`

**Core Features:**
- [ ] Building template loading (NBT/schematic)
- [ ] Building placement validation
- [ ] Construction progress tracking
- [ ] Resource requirements
- [ ] Upgrade paths

### 4.3 Village Generation (Week 8-9)

**Reference:** `OldSource/java/org/millenaire/common/WorldGenVillage.java`

- [ ] Village spawn conditions (biome, terrain)
- [ ] Initial building placement
- [ ] Road network generation
- [ ] Wall generation (optional)
- [ ] Villager spawning

### 4.4 Village Growth (Week 9)

- [ ] Population growth triggers
- [ ] New building construction triggers
- [ ] Resource accumulation
- [ ] Building upgrades
- [ ] Village level system

---

## Phase 5: Trading & Economy

**Goal:** Implement the trading system and economy

### 5.1 Trade System (Week 9)

**Files to create:**
```
src/main/java/com/jasoncian/millenaire_rewrite/trade/
├── TradeGood.java              # Tradeable item definition
├── TradeOffer.java             # Buy/sell offer
├── MerchantInventory.java      # Merchant stock
└── TradeRegistry.java          # All trade definitions
```

**Reference:** `OldSource/java/org/millenaire/common/TradeGood.java`

### 5.2 Trading GUI (Week 9-10)

- [ ] Trading screen (buy/sell interface)
- [ ] Price display
- [ ] Reputation effects on prices
- [ ] Bulk trading

### 5.3 Currency System

Already implemented in ModItems:
- DENIER (copper)
- DENIER_ARGENT (silver)
- DENIER_OR (gold)

Need to add:
- [ ] Currency conversion
- [ ] Villager payment handling
- [ ] Village treasury

---

## Phase 6: Quest System

**Goal:** Implement the quest/mission system

### 6.1 Quest Framework (Week 10)

**Files to create:**
```
src/main/java/com/jasoncian/millenaire_rewrite/quest/
├── Quest.java                  # Quest definition
├── QuestStep.java              # Quest step/objective
├── QuestInstance.java          # Active quest tracking
├── QuestReward.java            # Reward definitions
└── QuestRegistry.java          # All quest definitions
```

**Reference:** `OldSource/java/org/millenaire/common/quest/Quest.java`

### 6.2 Quest Types

| Type | Description |
|------|-------------|
| Fetch | Bring items to villager |
| Craft | Create specific items |
| Explore | Visit locations |
| Kill | Defeat enemies |
| Build | Help construct buildings |
| Escort | Protect villager |

### 6.3 Quest GUI (Week 10)

- [ ] Quest log screen
- [ ] Quest giver dialogue
- [ ] Objective tracking
- [ ] Reward preview

---

## Phase 7: World Generation Extras

**Goal:** Add culture-specific world generation

### 7.1 Trees (Week 10-11)

| Tree | Culture | Drops |
|------|---------|-------|
| Apple Tree | Norman | Cider Apples |
| Cherry Tree | Japanese | Cherries |
| Sakura | Japanese | Cherry Blossoms |
| Olive Tree | Byzantine | Olives |
| Pistachio | Seljuk | Pistachios |

**Reference:** `OldSource/java/org/millenaire/common/worldgen/`

### 7.2 Structures

- [ ] Standalone ruins
- [ ] Resource nodes
- [ ] Cultural landmarks

---

## Phase 8: Polish & Integration

### 8.1 GUI Polish (Week 11)

- [ ] Village info screen
- [ ] Building info screen
- [ ] Villager info screen
- [ ] Reputation display

### 8.2 Sound System

- [ ] Villager voices (culture-specific)
- [ ] Ambient village sounds
- [ ] Work sounds
- [ ] Combat sounds

### 8.3 Achievements/Advancements

- [ ] First village discovery
- [ ] Trading milestones
- [ ] Quest completion
- [ ] Village growth

### 8.4 Config Options

- [ ] Village spawn frequency
- [ ] Villager AI difficulty
- [ ] Resource multipliers
- [ ] Combat settings

---

## Technical Considerations

### NeoForge 1.21.1 Patterns

**Entity Registration:**
```java
public static final DeferredHolder<EntityType<?>, EntityType<MillVillager>> MILL_VILLAGER =
    ENTITIES.register("mill_villager", () -> EntityType.Builder
        .of(MillVillager::new, MobCategory.MISC)
        .sized(0.6F, 1.95F)
        .build("mill_villager"));
```

**Block Entity Registration:**
```java
public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FirePitBlockEntity>> FIRE_PIT =
    BLOCK_ENTITIES.register("fire_pit", () -> BlockEntityType.Builder
        .of(FirePitBlockEntity::new, ModBlocks.FIRE_PIT.get())
        .build(null));
```

**Menu/Screen Registration:**
```java
public static final DeferredHolder<MenuType<?>, MenuType<TradingMenu>> TRADING_MENU =
    MENUS.register("trading", () -> IMenuTypeExtension.create(TradingMenu::new));
```

### Data Storage

- Use `DataComponents` for item data (1.21+ pattern)
- Use `SavedData` for world-level village data
- Use entity data accessors for synced villager data

### Networking

- Use NeoForge's `PayloadRegistrar` for custom packets
- Sync village data on player join
- Sync villager actions for rendering

---

## File Structure (Target)

```
src/main/java/com/jasoncian/millenaire_rewrite/
├── MillenaireRewrite.java
├── block/
│   ├── decorative/
│   ├── functional/
│   └── agricultural/
├── blockentity/
│   ├── FirePitBlockEntity.java
│   ├── LockedChestBlockEntity.java
│   └── ...
├── entity/
│   ├── MillVillager.java
│   ├── ai/
│   │   ├── Goal.java
│   │   └── goals/
│   └── render/
├── village/
│   ├── Village.java
│   ├── VillageManager.java
│   └── ...
├── building/
│   ├── Building.java
│   └── ...
├── trade/
│   ├── TradeGood.java
│   └── ...
├── quest/
│   ├── Quest.java
│   └── ...
├── gui/
│   ├── TradingScreen.java
│   ├── QuestScreen.java
│   └── ...
├── network/
│   └── packets/
├── core/
│   ├── ModBlocks.java
│   ├── ModItems.java
│   ├── ModBlockEntities.java
│   ├── ModEntities.java
│   └── ...
├── datagen/
└── util/
```

---

## Timeline Summary

| Phase | Estimated | Actual | Status | Deliverables |
|-------|-----------|--------|--------|--------------|
| 1. Blocks | 3 weeks | 1 day | ✅ DONE | 66 blocks, block entities, GUIs |
| 2. Villager Entity | 2 weeks | - | 🔲 NEXT | MillVillager, rendering, interaction |
| 3. AI System | 3 weeks | - | 🔲 | Goal framework, 40+ goals |
| 4. Village System | 2 weeks | - | 🔲 | Village management, generation |
| 5. Trading | 1 week | - | 🔲 | Trade system, GUI |
| 6. Quests | 1 week | - | 🔲 | Quest system, GUI |
| 7. World Gen | 1 week | - | 🔲 | Trees, structures |
| 8. Polish | 1 week | - | 🔲 | GUI, sounds, config |

**Original Estimate: 14 weeks**
**Progress: Phase 1 complete (code), textures pending**

---

## Next Steps

### Immediate (Phase 2 Preparation)
1. ~~Begin Phase 1: Block System Foundation~~ ✅ COMPLETE
2. Create block textures for Phase 1 blocks (~120 PNG files)
3. Begin Phase 2: Villager Entity System
   - Create `MillVillager.java` entity class
   - Set up entity renderer and model
   - Implement basic movement and pathfinding

### Phase 2 Priorities
1. Entity registration with spawn egg
2. Basic villager model (male/female variants)
3. Cultural clothing layers
4. Right-click interaction menu
5. Basic inventory system

---

## Changelog

### January 17, 2026 - Phase 2 Complete
- ✅ Created MillVillager entity with synced data accessors
- ✅ Created Culture enum (7 cultures: Norman, Byzantine, Indian, Japanese, Mayan, Inuit, Seljuk)
- ✅ Created VillagerProfession enum (24+ professions with culture-specific roles)
- ✅ Created VillagerAnimationState enum (15 animation states)
- ✅ Created InvItem class for efficient inventory item caching
- ✅ Implemented full inventory system (add, take, count, NBT persistence, death drops)
- ✅ Created MillVillagerModel with animation state support
- ✅ Created MillVillagerRenderer with all rendering layers
- ✅ Created VillagerClothesLayer for clothing texture overlays
- ✅ Added ItemInHandLayer for held item rendering
- ✅ Implemented equipment slot methods (getItemBySlot, setItemSlot)
- ✅ Registered entity with spawn egg
- ✅ Created VillagerInteractionMenu and VillagerInteractionScreen
- ✅ Added EN/ZH translations for entity and interaction GUI
- ✅ Added default villager texture
- Build verified successful - **Phase 2 Complete!**

### January 17, 2026 - Phase 1 Complete
- ✅ Added decorative blocks (Byzantine mosaic, Mayan gold, light blue brick)
- ✅ Added path blocks with 15/16 height
- ✅ Implemented FirePit with 7-slot cooking GUI
- ✅ Implemented LockedChest with lock state and permissions
- ✅ Implemented ImportTable with building config GUI
- ✅ Implemented agricultural blocks (crops, grape vine, fruit leaves, silkworm)
- ✅ Added EN/ZH translations for all new content
- Build verified successful

---

*Plan created: January 17, 2026*
*Last updated: January 17, 2026*
*Target Minecraft Version: 1.21.1*
*Target Mod Loader: NeoForge 21.1.215+*
