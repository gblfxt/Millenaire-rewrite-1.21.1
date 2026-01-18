# Millenaire Rewrite | 千年村庄重制版

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-green.svg)](https://minecraft.net)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.215-orange.svg)](https://neoforged.net)
[![Java](https://img.shields.io/badge/Java-21+-blue.svg)](https://openjdk.java.net)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE.txt)
[![Development](https://img.shields.io/badge/Status-Alpha%20Development-red.svg)](#development-status)

[English](#english) | [中文](#中文)

---

## English

### About

**Millenaire Rewrite** is a complete modernization of the classic Millenaire mod, now ported to NeoForge 1.21.1. This project brings the beloved village-building and cultural experience to modern Minecraft with enhanced performance, updated mechanics, and a cleaner codebase.

Originally created as one of Minecraft's most ambitious civilization mods, Millenaire adds living, breathing villages with unique cultures that grow and evolve over time. This rewrite preserves the core spirit while embracing modern modding standards.

### Port History

- **Original**: Based on [JasonCian/Millenaire-rewrite](https://github.com/JasonCian/Millenaire-rewrite) for Forge 1.20.1
- **1.21.1 Port**: Ported to NeoForge 1.21.1 by [gblfxt](https://github.com/gblfxt)
- **Reference**: Legacy code from [Millenaire-Reborn](https://github.com/Leviaria/Millenaire-Reborn) (1.12 source)

### Key Features

#### Core Systems

- **Advanced Currency System**: Three-tier currency with copper, silver, and gold deniers
- **Complete Bilingual Translation**: 350+ English + 350+ Chinese entries, all creative tabs and items fully translated
- **Creative Mode Tabs**: 9 civilization tabs (Norman, Byzantine, Indian, Japanese, Mayan, Seljuk, Inuit, + Blocks, Food, Misc)
- **Decorative Blocks**: Stone, wood, earth, and culture-specific building materials
- **Magic Items**: Dynamic amulets, wands, and the legendary Amulet of Creation

#### Item Systems (240+ Items!)

| Category | Count | Description |
|----------|-------|-------------|
| **Building Materials** | 30 | Culture-specific blocks and crafting materials |
| **Quest Items** | 8 | Creation Quest storyline items + Galianite Ore |
| **Trade Goods** | 14 | Dyes, spices, and luxury items for each culture |
| **Loot Items** | 8 | Bandit camp and ruins exploration loot |
| **Tools & Weapons** | 40+ | Culture-specific equipment sets |
| **Food & Crops** | 45+ | Unique cuisine for each civilization |
| **Magic & Amulets** | 10+ | Dynamic magical items with special abilities |
| **Parchments** | 26 | Knowledge scrolls for each culture |

#### Living Villages

- **Dynamic Growth**: Villages expand organically based on population and resources
- **Cultural Diversity**: 7 unique civilizations with distinct building styles
- **Economic Systems**: Complex trade networks and currency exchange
- **Social Interactions**: Deep villager relationships and reputation systems

#### Cultural Systems

All 7 cultures fully implemented with complete item sets:

| Culture | Building Materials | Trade Goods | Food | Tools/Armor |
|---------|-------------------|-------------|------|-------------|
| **Norman** | Timber Frame, Wattle & Daub, Plaster | Woad Dye | Cider, Calvados, Tripes | Full set |
| **Byzantine** | Byzantine Tile, Marble | Tyrian Purple, Murex Shell | Wine, Feta, Souvlaki | Full set |
| **Indian** | Mud Brick, Cooked Brick | Indigo, Saffron | Curry, Rasgulla | Brick Mould |
| **Japanese** | Thatch, Washi Paper | - | Sake, Udon, Ikayaki | 3 armor tiers! |
| **Mayan** | Limestone, Obsidian | Cochineal Dye | Cacauhaa, Masa, Wah | Obsidian tools |
| **Seljuk** | Glazed Tile, Carved Stone | Sumac, Rose Water | Pide, Helva, Lokum | Scimitar, Bow |
| **Inuit** | Whale Bone, Packed Snow | - | Bear/Wolf meat, Stews | Fur armor, Ulu |

#### Magic & Technology

- **Dynamic Amulets**: Color-changing items that respond to environment
  - Alchemist Amulet: Detects nearby ores
  - Vishnu Amulet: Senses nearby creatures
  - Yggdrasil Amulet: Shows altitude information
  - Skoll & Hati Amulet: Controls day/night cycle
- **Amulet of Creation**: Legendary quest reward with passive regen and powerful buffs
- **Wands**: Summoning, Negation, Creative, and Tuning Fork
- **Advanced Crafting**: Culture-specific recipes and materials

### Development Status

**Current Version**: `0.3.0-alpha` (NeoForge 1.21.1 Port)
**Last Updated**: January 17, 2026

#### Completed Systems

| System | Items | Status | Description |
|--------|-------|--------|-------------|
| **Item System** | 240+ items | ✅ Complete | All cultural items, tools, food, materials registered |
| **Building Materials** | 30 items | ✅ Complete | Culture-specific building blocks and materials |
| **Quest System** | 8 items | ✅ Complete | Creation Quest items with special Amulet of Creation |
| **Trade Goods** | 14 items | ✅ Complete | Dyes, spices, luxury items for trading |
| **Loot System** | 8 items | ✅ Complete | Exploration rewards for lone structures |
| **Currency System** | 3 tiers | ✅ Complete | Copper, Silver, Gold deniers |
| **Food System** | 45+ foods | ✅ Complete | Culture-specific cuisine with effects |
| **Magic Items** | 10+ items | ✅ Complete | Amulets, wands with special abilities |
| **Block System** | 25+ blocks | ✅ Complete | Building blocks, ores, functional blocks |
| **Translation** | 350+ entries | ✅ Complete | English/Chinese for all content |
| **Creative Tabs** | 9 tabs | ✅ Complete | All cultures + utility tabs |

#### In Progress

| System | Progress | Notes |
|--------|----------|-------|
| **Village Core System** | 30% | Core village logic |
| **Building Framework** | 15% | Construction system |
| **Entity System** | 10% | NPC villagers |
| **Loot Tables** | 0% | Integration with loot items |

#### Planned Features

- **Mill Chest System**: Secure storage with village integration
- **Path & Road System**: Cultural building connections
- **Quest Storyline**: Full Creation Quest implementation
- **AI Villagers**: Smart NPCs with complex behaviors
- **Building Generator**: Procedural architecture system
- **World Generation**: Galianite ore spawning, lone structures

### Technical Details

#### 1.21.1 Port Changes

The port from Forge 1.20.1 to NeoForge 1.21.1 included:

- **Build System**: Migrated to NeoGradle 2.0.42-beta
- **Registry System**: `RegistryObject` → `DeferredHolder`
- **Armor Materials**: Converted to registry-based `Holder<ArmorMaterial>`
- **Tool Materials**: Custom `SimpleTier` implementation (ForgeTier removed)
- **Item Data**: NBT → DataComponents compatibility layer
- **Food Properties**: Updated for 1.21 API changes
- **Tool Items**: New attribute-based constructor pattern

#### Project Statistics

- **Java Classes**: 90+ modern implementations
- **Texture Assets**: 150+ high-quality PNG files
- **Generated Resources**: 250+ automatically created files
- **Registered Items**: 240+ unique items with proper integration
- **Registered Blocks**: 25+ blocks with full variant support
- **Translation Keys**: 350+ bilingual entries

#### Code Quality

- **Modern Java 21**: Latest language features and best practices
- **NeoForge 21.1.215**: Current stable API implementation
- **Clean Architecture**: Modular design for easy expansion
- **Comprehensive Documentation**: Full JavaDoc coverage

### Installation & Development

#### Prerequisites

- **Minecraft**: 1.21.1
- **NeoForge**: 21.1.215+
- **Java Development Kit**: 21+
- **IDE**: IntelliJ IDEA or Eclipse with Minecraft Development Kit

#### Quick Start

```bash
# Clone the repository
git clone https://github.com/gblfxt/Millenaire-rewrite-1.21.1.git
cd Millenaire-rewrite-1.21.1

# Build the mod
./gradlew build

# Run in development
./gradlew runClient
```

#### Building

```bash
# Build mod JAR
./gradlew build

# Generate data files
./gradlew runData
```

### Contributing

We welcome contributions! Please see our development roadmap and pick an area that interests you.

#### Current Priorities

1. **Village Management System** - Core village logic and data structures
2. **Building Generation** - Automated construction system
3. **NPC AI System** - Smart villager behaviors
4. **Loot Table Integration** - Connect loot items to world generation

### License

This project is licensed under the MIT License - see [LICENSE.txt](LICENSE.txt) for details.

### Acknowledgments

- **Original Millenaire Team** - For creating the beloved original mod
- **JasonCian** - For the Forge 1.20.1 rewrite foundation
- **NeoForged Team** - For providing the modding framework
- **Community Contributors** - For feedback and suggestions

---

## 中文

### 关于项目

**千年村庄重制版**是经典千年村庄（Millenaire）模组的完全现代化重制，现已移植到 NeoForge 1.21.1。本项目将备受喜爱的村庄建设和文化体验带到现代 Minecraft 中，具有增强的性能、更新的机制和更清洁的代码库。

作为 Minecraft 最具雄心的文明类模组之一，千年村庄添加了具有独特文化的活跃村庄，这些村庄会随时间增长和演化。这次重制保留了核心精神，同时拥抱现代模组开发标准。

### 移植历史

- **原版**: 基于 [JasonCian/Millenaire-rewrite](https://github.com/JasonCian/Millenaire-rewrite) (Forge 1.20.1)
- **1.21.1 移植**: 由 [gblfxt](https://github.com/gblfxt) 移植到 NeoForge 1.21.1
- **参考**: [Millenaire-Reborn](https://github.com/Leviaria/Millenaire-Reborn) 的 1.12 源码

### 核心特性

- **三层货币系统**：铜、银、金第纳尔
- **完整双语翻译**：350+ 条英文+350+ 条中文，所有创造标签和物品均已翻译
- **创造模式标签页**：9 个文明分类（诺曼、拜占庭、印度、日本、玛雅、塞尔柱、因纽特 + 方块、食物、杂项）
- **装饰方块系统**：石材、木材、土质及文化特色建筑材料
- **魔法物品**：动态护符、法杖及传奇创世护身符

#### 物品系统（240+ 物品！）

| 类别 | 数量 | 描述 |
|------|------|------|
| **建筑材料** | 30 | 文化特色方块和制作材料 |
| **任务物品** | 8 | 创世任务线物品 + 加里亚奈特矿石 |
| **贸易品** | 14 | 各文化的染料、香料和奢侈品 |
| **战利品** | 8 | 强盗营地和遗迹探索奖励 |
| **工具武器** | 40+ | 文化特色装备套装 |
| **食物作物** | 45+ | 各文明独特料理 |
| **魔法护符** | 10+ | 具有特殊能力的动态魔法物品 |
| **羊皮纸** | 26 | 各文化知识卷轴 |

#### 活跃村庄

- **动态增长**：村庄根据人口和资源有机扩张
- **文化多样性**：7 个独特文明，各具特色建筑风格
- **经济系统**：复杂的贸易网络和货币交换
- **社交互动**：深度的村民关系和声望系统

#### 文化系统

全部 7 种文化已完整实现：

| 文化 | 建筑材料 | 贸易品 | 食物 | 工具/盔甲 |
|------|----------|--------|------|-----------|
| **诺曼** | 木框架、编条夯土、灰泥 | 菘蓝染料 | 苹果酒、卡尔瓦多斯 | 完整套装 |
| **拜占庭** | 拜占庭瓷砖、大理石 | 泰尔紫、骨螺壳 | 葡萄酒、羊奶酪 | 完整套装 |
| **印度** | 泥砖、烧制砖 | 蓝靛、藏红花 | 咖喱、甜点 | 砖模 |
| **日本** | 茅草、和纸 | - | 清酒、乌冬面 | 3 级盔甲！ |
| **玛雅** | 石灰石、黑曜石 | 胭脂红染料 | 可可饮品、玉米饼 | 黑曜石工具 |
| **塞尔柱** | 琉璃瓦、雕刻石 | 漆树果、玫瑰水 | 土耳其薄饼、软糖 | 弯刀、弓 |
| **因纽特** | 鲸骨、压实雪 | - | 熊肉、狼肉、炖菜 | 毛皮盔甲、乌卢刀 |

#### 魔法与科技

- **动态护符**：根据环境改变颜色的物品
  - 炼金术士护符：探测附近矿石
  - 毗湿奴护符：感知附近生物
  - 世界树护符：显示高度信息
  - 斯库尔与哈提护符：控制昼夜循环
- **创世护身符**：传奇任务奖励，被动回复 + 强力增益
- **法杖**：召唤、否定、创造、音叉
- **高级制作**：文化特定的配方和材料

### 开发状态

**当前版本**: `0.3.0-alpha` (NeoForge 1.21.1 移植版)
**最后更新**: 2026 年 1 月 17 日

#### 已完成系统

| 系统 | 数量 | 状态 | 描述 |
|------|------|------|------|
| **物品系统** | 240+ 物品 | ✅ 完成 | 所有文化物品、工具、食物、材料已注册 |
| **建筑材料** | 30 物品 | ✅ 完成 | 文化特色建筑方块和材料 |
| **任务系统** | 8 物品 | ✅ 完成 | 创世任务物品及特殊创世护身符 |
| **贸易品** | 14 物品 | ✅ 完成 | 染料、香料、贸易奢侈品 |
| **战利品系统** | 8 物品 | ✅ 完成 | 孤立建筑探索奖励 |
| **货币系统** | 3 层 | ✅ 完成 | 铜、银、金第纳尔 |
| **食物系统** | 45+ 食物 | ✅ 完成 | 文化特色料理及效果 |
| **魔法物品** | 10+ 物品 | ✅ 完成 | 护符、法杖及特殊能力 |
| **方块系统** | 25+ 方块 | ✅ 完成 | 建筑方块、矿石、功能方块 |
| **翻译系统** | 350+ 条 | ✅ 完成 | 英文/中文全内容翻译 |
| **创造标签** | 9 个 | ✅ 完成 | 所有文化 + 实用标签 |

#### 开发中

| 系统 | 进度 | 备注 |
|------|------|------|
| **村庄核心系统** | 30% | 核心村庄逻辑 |
| **建筑框架** | 15% | 建造系统 |
| **实体系统** | 10% | NPC 村民 |
| **战利品表** | 0% | 与战利品物品集成 |

#### 计划功能

- **千年箱系统**: 与村庄集成的安全储存
- **道路系统**: 文化建筑连接
- **任务故事线**: 完整创世任务实现
- **AI 村民**: 具有复杂行为的智能 NPC
- **建筑生成器**: 程序化建筑系统
- **世界生成**: 加里亚奈特矿石生成、孤立建筑

### 技术细节

#### 1.21.1 移植变更

从 Forge 1.20.1 到 NeoForge 1.21.1 的移植包括：

- **构建系统**: 迁移到 NeoGradle 2.0.42-beta
- **注册系统**: `RegistryObject` → `DeferredHolder`
- **护甲材料**: 转换为基于注册表的 `Holder<ArmorMaterial>`
- **工具材料**: 自定义 `SimpleTier` 实现（ForgeTier 已移除）
- **物品数据**: NBT → DataComponents 兼容层
- **食物属性**: 更新以适应 1.21 API 变更
- **工具物品**: 新的基于属性的构造器模式

#### 项目统计

- **Java 类**: 90+ 个现代实现
- **材质资产**: 150+ 个高质量 PNG 文件
- **生成资源**: 250+ 个自动创建的文件
- **注册物品**: 240+ 个独特物品，完整集成
- **注册方块**: 25+ 个方块，支持完整变体
- **翻译键**: 350+ 条双语条目

#### 代码质量

- **现代 Java 21**: 最新语言特性和最佳实践
- **NeoForge 21.1.215**: 当前稳定 API 实现
- **清洁架构**: 模块化设计，易于扩展
- **全面文档**: 完整 JavaDoc 覆盖

### 安装与开发

#### 前置条件

- **Minecraft**: 1.21.1
- **NeoForge**: 21.1.215+
- **Java 开发工具包**: 21+
- **IDE**: IntelliJ IDEA 或 Eclipse，配合 Minecraft 开发工具包

#### 快速开始

```bash
# 克隆仓库
git clone https://github.com/gblfxt/Millenaire-rewrite-1.21.1.git
cd Millenaire-rewrite-1.21.1

# 构建模组
./gradlew build

# 开发运行
./gradlew runClient
```

#### 构建

```bash
# 构建模组JAR
./gradlew build

# 生成数据文件
./gradlew runData
```

### 贡献

我们欢迎贡献！请查看我们的开发路线图，选择您感兴趣的领域。

#### 当前优先级

1. **村庄管理系统** - 核心村庄逻辑和数据结构
2. **建筑生成** - 自动化建造系统
3. **NPC AI 系统** - 智能村民行为
4. **战利品表集成** - 将战利品物品连接到世界生成

### 许可证

本项目采用 MIT 许可证 - 详见[LICENSE.txt](LICENSE.txt)。

### 致谢

- **原版千年村庄团队** - 创造了备受喜爱的原版模组
- **JasonCian** - Forge 1.20.1 重制版基础
- **NeoForged 团队** - 提供模组开发框架
- **社区贡献者** - 提供反馈和建议

---

## Links | 链接

- **Issues** | **问题报告**: [GitHub Issues](https://github.com/gblfxt/Millenaire-rewrite-1.21.1/issues)
- **Original Mod** | **原版模组**: [Millenaire Legacy](https://millenaire.org)
- **Upstream** | **上游项目**: [JasonCian/Millenaire-rewrite](https://github.com/JasonCian/Millenaire-rewrite)

---

_Last updated: January 17, 2026 | 最后更新：2026 年 1 月 17 日_
