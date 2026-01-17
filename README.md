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
- **Complete Bilingual Translation**: 249 English + 249 Chinese entries, all creative tabs and items fully translated
- **Creative Mode Tabs**: 8 civilization tabs, all items categorized
- **Decorative Blocks**: Stone, wood, earth, and more, with full data generation
- **Magic Items**: Dynamic amulets, wands, and more

#### Living Villages

- **Dynamic Growth**: Villages expand organically based on population and resources
- **Cultural Diversity**: Multiple civilizations with unique building styles and customs
- **Economic Systems**: Complex trade networks and currency exchange
- **Social Interactions**: Deep villager relationships and reputation systems

#### Cultural Systems

- **Norman, Byzantine, Hindi, Japanese, Mayan, Seljuk, Inuit, Generic**: All cultures implemented in creative tabs and item registration

#### Magic & Technology

- **Dynamic Amulets**: Magical items that change appearance based on environment
- **Advanced Crafting**: Culture-specific recipes and materials
- **Unique Tools**: Specialized equipment for each civilization

### Development Status

**Current Version**: `0.2.0-alpha` (NeoForge 1.21.1 Port)
**Last Updated**: January 17, 2026

#### Completed Systems

| System                 | Items        | Status      | Description                                          |
| ---------------------- | ------------ | ----------- | ---------------------------------------------------- |
| **Item System**        | 100+ items   | Complete | All cultural items, tools, food, currency registered |
| **Currency System**    | 6 currencies | Complete | Multi-cultural monetary system                       |
| **Food System**        | 30+ foods    | Complete | Culture-specific cuisine with proper nutrition       |
| **Decorative Blocks**  | 9+ variants  | Complete | Stone, wood, and earth decorative building materials |
| **Magic Items**        | 5+ amulets   | Complete | Dynamic color-changing magical amulets               |
| **Core Architecture**  | -            | Complete | Modern NeoForge 1.21.1 foundation                    |
| **Translation System** | 249+ entries | Complete | English/Chinese, all creative tabs/items translated  |
| **Creative Tabs**      | 8 cultures   | Complete | All tabs and categorization working                  |

#### In Progress

| System                  | Progress | Notes                    |
| ----------------------- | -------- | ------------------------ |
| **Village Core System** | 30%      | Core village logic       |
| **Building Framework**  | 15%      | Construction system      |
| **Entity System**       | 10%      | NPC villagers            |
| **Mill Chest System**   | 10%      | Secure storage           |

#### Planned Features

- **Mill Chest System**: Secure storage with village integration
- **Path & Road System**: Cultural building connections
- **Sign System**: Multi-language village signage
- **Crop System**: Culture-specific agriculture
- **AI Villagers**: Smart NPCs with complex behaviors
- **Building Generator**: Procedural architecture system

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

- **Java Classes**: 80+ modern implementations
- **Texture Assets**: 120+ high-quality PNG files
- **Generated Resources**: 200+ automatically created files
- **Registered Items**: 100+ unique items with proper integration
- **Registered Blocks**: 10+ foundational blocks with entity support

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
4. **Cultural Expansion** - Additional civilizations and features

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
- **完整双语翻译**：249 条英文+249 条中文，所有创造标签和物品均已翻译
- **创造模式标签页**：8 个文明分类，所有物品已归类
- **装饰方块系统**：石材、木材、土质等，数据自动生成
- **魔法物品**：动态护符、法杖等

#### 活跃村庄

- **动态增长**：村庄根据人口和资源有机扩张
- **文化多样性**：诺曼、拜占庭、印度、日本、玛雅、塞尔柱、因纽特、通用
- **经济系统**：复杂的贸易网络和货币交换
- **社交互动**：深度的村民关系和声望系统

#### 文化系统

- 所有文化已在创造标签和物品注册中实现

#### 魔法与科技

- **动态护符**：根据环境改变外观的魔法物品
- **高级制作**：文化特定的配方和材料
- **独特工具**：每个文明的专用装备

### 开发状态

**当前版本**: `0.2.0-alpha` (NeoForge 1.21.1 移植版)
**最后更新**: 2026 年 1 月 17 日

#### 已完成系统

| 系统         | 数量       | 状态 | 描述                                  |
| ------------ | ---------- | ---- | ------------------------------------- |
| **物品系统** | 100+个物品 | 完成 | 所有文化物品、工具、食物、货币已注册  |
| **货币系统** | 6 种货币   | 完成 | 多文化货币体系                        |
| **食物系统** | 30+ 种食物 | 完成 | 文化特色料理，具有合适的营养值        |
| **装饰方块** | 9+ 个变体  | 完成 | 石材、木材和土质装饰建筑材料          |
| **魔法物品** | 5+ 个护符  | 完成 | 动态变色魔法护符                      |
| **核心架构** | -          | 完成 | 现代 NeoForge 1.21.1 基础             |
| **翻译系统** | 249+条     | 完成 | 英文/中文，所有创造标签和物品均已翻译 |
| **创造标签** | 8 个文明   | 完成 | 所有标签和分类均已实现                |

#### 开发中

| 系统             | 进度 | 备注         |
| ---------------- | ---- | ------------ |
| **村庄核心系统** | 30%  | 核心村庄逻辑 |
| **建筑框架**     | 15%  | 建造系统     |
| **实体系统**     | 10%  | NPC 村民     |
| **千年箱系统**   | 10%  | 安全储存     |

#### 计划功能

- **千年箱系统**: 与村庄集成的安全储存
- **道路系统**: 文化建筑连接
- **标志系统**: 多语言村庄标识
- **作物系统**: 文化特色农业
- **AI 村民**: 具有复杂行为的智能 NPC
- **建筑生成器**: 程序化建筑系统

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

- **Java 类**: 80+ 个现代实现
- **材质资产**: 120+ 个高质量 PNG 文件
- **生成资源**: 200+ 个自动创建的文件
- **注册物品**: 100+ 个独特物品，完整集成
- **注册方块**: 10+ 个基础方块，支持实体

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
4. **文化扩展** - 额外的文明和功能

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
