# Saika

一个功能丰富的物品锻造与合成系统插件，适用于 Paper/Spigot Minecraft 服务器。

## 概述

Saika 提供了一个"抽卡游戏系统"，允许服务器管理员创建可自定义的随机物品生成池，并支持基于概率的结果。该插件具有类似熔炉风格的交互式 UI，用于执行锻造、附魔、回收和祛魔操作。

## 功能特性

- **加权随机物品抽取**：创建带有可配置概率权重的物品池
- **多层级合成系统**：定义等级（铁锭）和元素，实现分层合成
- **四大核心操作**：
  - **锻造**：组合材料从配置的物品池中随机生成物品
  - **附魔**：使用附魔书为物品附魔，结果基于概率
  - **回收**：将锻造的物品分解回材料
  - **祛魔**：移除物品上的附魔
- **交互式 GUI**：熔炉风格的双格物品栏界面
- **奖励物品系统**：配置具有独立概率的奖励掉落
- **基于经验的操作**：附魔和祛魔消耗玩家经验值
- **完整的日志记录**：追踪所有锻造操作以便审计
- **多语言支持**：内置英文和中文本地化

## 系统要求

- Paper/Spigot 1.21.11+
- Java 21+
- [NyaaCore](https://github.com/NyaaCat/NyaaCore) v9.10+
- LangUtils

## 安装

1. 从发布页面下载最新版本
2. 将 `Saika.jar` 放入服务器的 `plugins/` 文件夹
3. 确保已安装 NyaaCore 和 LangUtils
4. 重启服务器
5. 在 `plugins/Saika/config.yml` 中配置插件

## 命令

### 主命令：`/saika`

| 子命令 | 权限 | 描述 |
|--------|------|------|
| `open <类型>` | `saika.open` | 打开 UI（forge/enchant/recycle/repulse） |
| `define <类型> [参数]` | `saika.admin.define` | 定义锻造等级、元素、附魔书、祛魔石、回收器 |
| `add <等级> <元素> <消耗> <权重>` | `saika.admin.add` | 将手持物品添加到可锻造物品池 |
| `remove <ID>` | `saika.admin.remove` | 从物品池移除物品 |
| `modify <ID> <属性> <值>` | `saika.admin.modify` | 修改物品属性 |
| `delete <类型> <ID>` | `saika.admin.delete` | 删除锻造定义 |
| `bonus add` | `saika.admin.bonus` | 从手持物品创建奖励物品 |
| `bonus set <类型> <ID> <奖励ID> <概率>` | `saika.admin.bonus` | 为锻造/回收物品分配奖励 |
| `inspect <ID>` | `saika.command` | 显示详细物品信息 |
| `list <等级> <元素> [数量]` | `saika.command` | 列出物品池中的物品 |
| `roll <元素> <数量> <铁锭> <数量> [可回收] [次数]` | `saika.roll` | 手动抽取物品 |
| `reload` | `saika.admin.define` | 重新加载配置 |

### 列表命令：`/saikal <等级> <元素> [数量]`

显示指定等级/元素组合可能产出的物品及其概率百分比。

## 权限

| 权限 | 默认值 | 描述 |
|------|--------|------|
| `saika.command` | true | 基本命令访问 |
| `saika.open` | true | 打开 UI 界面 |
| `saika.list` | op | 使用列表命令 |
| `saika.roll` | op | 使用抽取命令 |
| `saika.admin.add` | op | 添加可锻造物品 |
| `saika.admin.define` | op | 定义等级/元素等 |
| `saika.admin.delete` | op | 删除定义 |
| `saika.admin.modify` | op | 修改物品属性 |
| `saika.admin.bonus` | op | 管理奖励物品 |
| `saika.admin.remove` | op | 移除可锻造物品 |

## 配置

### config.yml

```yaml
language: zh_CN                    # 语言设置（en_US, zh_CN）

forge:
  roll:
    maxWeightMultiplier: 3.0       # 低效率警告阈值
  lowEfficiency:
    multiplier: 1.5                # 警告倍数

enchant:
  exp: 100                         # 每次附魔消耗的经验值
  maxLevel: 10                     # 最大附魔等级
  probability:
    success: 40                    # 完整附魔概率 (%)
    moderate: 50                   # 半级附魔概率
    fail: 9                        # 无效果概率
    destroy: 1                     # 物品销毁概率

repulse:
  exp: 100                         # 每次祛魔消耗的经验值
  blacklist:                       # 无法移除的附魔
    - VANISHING_CURSE

position:
  forge:
    block: CRAFTING_TABLE          # 需要附近的方块类型
    distance: 3                    # 最大距离
  enchant:
    block: ENCHANTING_TABLE
    distance: 3

sound:                             # 音效配置
  forge:
    name: BLOCK_ANVIL_USE
    pitch: 1.0
  # ... (success, fail, bonus, recycle, repulse)

effect:                            # 粒子效果配置
  forge:
    particle: PORTAL
    offsetX: 0.0
    offsetY: 1.0
    offsetZ: 0.0
    speed: 0.0
    amount: 100
  # ... (success, fail, recycle)

directInteract:
  global: false                    # 启用直接方块点击
  worlds: []                       # 启用的世界列表

log:
  enabled: false                   # 启用操作日志
```

## 工作原理

### 锻造系统

1. **定义铁锭等级**：使用 `/saika define iron <等级>` 创建材料层级
2. **定义元素**：使用 `/saika define element <ID>` 创建元素类型
3. **添加可锻造物品**：手持物品并使用 `/saika add <等级> <元素> <消耗> <权重>`
4. **玩家合成**：玩家在锻造 UI 中组合铁锭和元素来随机抽取物品

每个可锻造物品具有：
- **等级**：所需的锻造铁锭层级
- **元素**：所需的元素类型
- **最小消耗**：所需的最小铁锭数量
- **权重**：抽取时的概率权重
- **锻造奖励**：可选的带掉落概率的奖励物品

### 附魔系统

玩家将物品和附魔书放入附魔 UI。结果基于概率：
- **成功（40%）**：应用完整附魔等级
- **中等（50%）**：应用一半附魔等级
- **失败（9%）**：无效果
- **销毁（1%）**：物品被销毁

### 回收系统

玩家可以分解锻造的物品以回收材料。可配置返还百分比，并支持可选的奖励掉落。

### 祛魔系统

从物品上移除附魔，消耗经验值。某些附魔（如诅咒）可以被列入黑名单。

## 数据存储

所有数据以 YAML 文件形式存储在插件数据文件夹中：
- 锻造材料（铁锭等级）
- 元素
- 可锻造物品
- 附魔/祛魔来源
- 奖励物品

物品使用 NBT/Base64 编码进行序列化以实现持久化。

## API

Saika 使用 PersistentDataContainer 通过 NBT 标签标记物品，允许其他插件识别锻造的物品。

## 构建

```bash
./gradlew build
```

编译后的 jar 文件将位于 `build/libs/` 目录。

## 许可证

本项目是 NyaaCat 插件生态系统的一部分。

## 链接

- [NyaaCat GitHub](https://github.com/NyaaCat)
- [NyaaCore](https://github.com/NyaaCat/NyaaCore)
