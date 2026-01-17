# Millenaire Feature Parity Implementation Plan

## Overview

This document outlines the plan to bring Millenaire-rewrite (NeoForge 1.21.1) to feature parity with the original Millenaire mod, using Millenaire-Reborn and OldSource (1.12) as references.

**Base Project:** `/home/critic/minecraft/modding/Millenaire-rewrite-1.21.1`
**Reference (Fabric):** `/home/critic/minecraft/modding/Millenaire-Reborn`
**Reference (1.12):** `/home/critic/minecraft/modding/Millenaire-Reborn/OldSource`

---

## Phase 1: Block System Foundation

**Goal:** Port all 66 blocks from OldSource with proper NeoForge 1.21.1 patterns

### 1.1 Decorative Blocks (Week 1)

| Block | OldSource File | Notes |
|-------|----------------|-------|
| DecorativeStone (3 variants) | `BlockDecorativeStone.java` | Stone, cobble, brick |
| DecorativeWood (timber frames) | `BlockDecorativeWood.java` | Multiple wood styles |
| DecorativeEarth (3 variants) | `BlockDecorativeEarth.java` | Mud, adobe, thatch |
| Rosette | `BlockRosette.java` | Wall decoration |
| Panel | `BlockPanel.java` | Wall panels |
| OrientedBrick | `BlockOrientedBrick.java` | Directional placement |
| PaintedBricks | `BlockPaintedBricks.java` | 16 color variants |

**Implementation Notes:**
- Use existing `BuildingBlockRegistry` for auto-generating variants (stairs, slabs, walls)
- Create `DecorativeBlockType` enum for variant management
- Add blockstate JSON generation to datagen

### 1.2 Structural Blocks (Week 1-2)

| Block | OldSource File | Notes |
|-------|----------------|-------|
| MillStairs | `BlockMillStairs.java` | Custom stair logic |
| SlabStone | `BlockSlabStone.java` | Stone slabs |
| SlabWood | `BlockSlabWood.java` | Wood slabs |
| MillWall | `BlockMillWall.java` | Custom walls |
| Path | `BlockPath.java` | Road system blocks |

**Implementation Notes:**
- Extend vanilla `StairBlock`, `SlabBlock`, `WallBlock`
- Path blocks need special walkable properties

### 1.3 Functional Blocks with Block Entities (Week 2)

| Block | OldSource Files | Block Entity |
|-------|-----------------|--------------|
| FirePit | `BlockFirePit.java`, `TileEntityFirePit.java` | Cooking/smelting |
| LockedChest | `BlockLockedChest.java`, `TileEntityLockedChest.java` | Village storage |
| ImportTable | `BlockImportTable.java`, `TileEntityImportTable.java` | Building import/export |
| MillBed | `BlockMillBed.java`, `TileEntityMillBed.java` | Cultural beds |
| MockBanner | N/A, `TileEntityMockBanner.java` | Banner decoration |

**Implementation Notes:**
- Register block entities in `ModBlockEntities.java`
- FirePit needs recipe system integration
- LockedChest needs permission system (village ownership)
- ImportTable is critical for building system

### 1.4 Agricultural Blocks (Week 2-3)

| Block | OldSource File | Notes |
|-------|----------------|-------|
| MillCrops | `BlockMillCrops.java` | Culture-specific crops |
| GrapeVine | `BlockGrapeVine.java` | Wine production |
| FruitLeaves | `BlockFruitLeaves.java` | Fruit trees |
| SilkWorm | `BlockSilkWorm.java` | Silk production (Byzantine) |

**Implementation Notes:**
- Extend `CropBlock` for growth mechanics
- Integrate with villager farming AI
- Add harvest loot tables

### 1.5 Assets & Datagen

- [ ] Block models (JSON)
- [ ] Blockstate definitions
- [ ] Loot tables
- [ ] Block tags
- [ ] Recipe integration
- [ ] Localization entries

---

## Phase 2: Villager Entity System

**Goal:** Port MillVillager entity with rendering and basic functionality

### 2.1 Entity Registration (Week 3)

**Files to create:**
```
src/main/java/com/jasoncian/millenaire_rewrite/entity/
├── MillVillager.java           # Main villager entity
├── MillVillagerRenderer.java   # Entity renderer
├── MillVillagerModel.java      # Entity model
└── VillagerProfession.java     # Profession enum/registry
```

**Reference:** `OldSource/java/org/millenaire/common/entity/MillVillager.java`

**Core Features:**
- [ ] Entity registration with spawn egg
- [ ] Basic movement and pathfinding
- [ ] Inventory system (27 slots like villager)
- [ ] Health and damage handling
- [ ] Cultural affiliation

### 2.2 Villager Professions (Week 3)

| Culture | Professions |
|---------|-------------|
| Norman | Farmer, Miner, Lumberjack, Guard, Merchant, Wife |
| Byzantine | Farmer, Silk Farmer, Guard, Merchant, Wife |
| Japanese | Farmer, Guard, Samurai, Merchant, Wife |
| Mayan | Farmer, Hunter, Guard, Shaman, Wife |
| Indian | Farmer, Brick Maker, Guard, Sadhu, Wife |
| Seljuk | Farmer, Shepherd, Guard, Merchant, Wife |
| Inuit | Hunter, Fisher, Guard, Shaman, Wife |

### 2.3 Villager Rendering (Week 4)

**Reference:** `OldSource/java/org/millenaire/client/entity/`

- [ ] Base villager model (male/female variants)
- [ ] Clothing layers per culture
- [ ] Armor overlay rendering
- [ ] Tool/item held rendering
- [ ] Animation states (walking, working, idle)

### 2.4 Villager Interaction (Week 4)

- [ ] Right-click interaction menu
- [ ] Trading interface trigger
- [ ] Quest interface trigger
- [ ] Reputation display
- [ ] Gift giving

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

| Phase | Duration | Deliverables |
|-------|----------|--------------|
| 1. Blocks | 3 weeks | 66 blocks, block entities, assets |
| 2. Villager Entity | 2 weeks | MillVillager, rendering, interaction |
| 3. AI System | 3 weeks | Goal framework, 40+ goals |
| 4. Village System | 2 weeks | Village management, generation |
| 5. Trading | 1 week | Trade system, GUI |
| 6. Quests | 1 week | Quest system, GUI |
| 7. World Gen | 1 week | Trees, structures |
| 8. Polish | 1 week | GUI, sounds, config |

**Total Estimated Time: 14 weeks** (accounting for complexity)

---

## Next Steps

1. Review this plan and adjust scope as needed
2. Set up project tracking (GitHub issues/milestones)
3. Begin Phase 1: Block System Foundation
4. Create test world for iterative development

---

*Plan created: January 17, 2026*
*Target Minecraft Version: 1.21.1*
*Target Mod Loader: NeoForge 21.1.215+*
