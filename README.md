# Saika

A sophisticated item forging and crafting system plugin for Paper/Spigot Minecraft servers.

## Overview

Saika provides a "Card-rolling Game System" that allows server administrators to create customizable random item generation pools with probability-based outcomes. The plugin features an interactive furnace-style UI for performing forge, enchant, recycle, and repulse operations on items.

## Features

- **Weighted Random Item Rolling**: Create item pools with configurable probability weights
- **Multi-Tier Crafting System**: Define levels (irons) and elements for tiered crafting
- **Four Core Operations**:
  - **Forge**: Combine materials to create random items from configured pools
  - **Enchant**: Apply enchantments from enchant books with probability-based outcomes
  - **Recycle**: Convert forged items back into materials
  - **Repulse**: Remove enchantments from items
- **Interactive GUI**: Furnace-styled 2-slot inventory interfaces
- **Bonus Item System**: Configure bonus drops with independent probabilities
- **Experience-Based Operations**: Enchant and repulse consume player XP
- **Comprehensive Logging**: Track all forge operations for auditing
- **Multi-Language Support**: English and Chinese localizations included

## Requirements

- Paper/Spigot 1.21.11+
- Java 21+
- [NyaaCore](https://github.com/NyaaCat/NyaaCore) v9.10+
- LangUtils

## Installation

1. Download the latest release from the releases page
2. Place `Saika.jar` in your server's `plugins/` folder
3. Ensure NyaaCore and LangUtils are installed
4. Restart your server
5. Configure the plugin in `plugins/Saika/config.yml`

## Commands

### Main Command: `/saika`

| Subcommand | Permission | Description |
|------------|------------|-------------|
| `open <type>` | `saika.open` | Open UI (forge/enchant/recycle/repulse) |
| `define <type> [args]` | `saika.admin.define` | Define forge levels, elements, enchants, repulses, recyclers |
| `add <level> <element> <cost> <weight>` | `saika.admin.add` | Add item in hand to forgeable pool |
| `remove <id>` | `saika.admin.remove` | Remove item from pool |
| `modify <id> <property> <value>` | `saika.admin.modify` | Modify item properties |
| `delete <type> <id>` | `saika.admin.delete` | Delete forge definition |
| `bonus add` | `saika.admin.bonus` | Create bonus item from held item |
| `bonus set <type> <id> <bonusId> <prob>` | `saika.admin.bonus` | Assign bonus to forge/recycle item |
| `inspect <id>` | `saika.command` | Show detailed item info |
| `list <level> <element> [amount]` | `saika.command` | List items in pool |
| `roll <element> <num> <iron> <num> [canRecycle] [times]` | `saika.roll` | Manual item rolling |
| `reload` | `saika.admin.define` | Reload configuration |

### List Command: `/saikal <level> <element> [amount]`

Shows possible items from a level/element combination with probability percentages.

## Permissions

| Permission | Default | Description |
|------------|---------|-------------|
| `saika.command` | true | Basic command access |
| `saika.open` | true | Open UI interfaces |
| `saika.list` | op | Use list command |
| `saika.roll` | op | Use roll command |
| `saika.admin.add` | op | Add forgeable items |
| `saika.admin.define` | op | Define levels/elements/etc |
| `saika.admin.delete` | op | Delete definitions |
| `saika.admin.modify` | op | Modify item properties |
| `saika.admin.bonus` | op | Manage bonus items |
| `saika.admin.remove` | op | Remove forgeable items |

## Configuration

### config.yml

```yaml
language: en_US                    # Language (en_US, zh_CN)

forge:
  roll:
    maxWeightMultiplier: 3.0       # Low efficiency warning threshold
  lowEfficiency:
    multiplier: 1.5                # Warning multiplier

enchant:
  exp: 100                         # XP cost per enchant
  maxLevel: 10                     # Maximum enchant level
  probability:
    success: 40                    # Full enchant chance (%)
    moderate: 50                   # Half-level enchant chance
    fail: 9                        # No effect chance
    destroy: 1                     # Item destruction chance

repulse:
  exp: 100                         # XP cost per repulse
  blacklist:                       # Enchantments that can't be removed
    - VANISHING_CURSE

position:
  forge:
    block: CRAFTING_TABLE          # Required nearby block
    distance: 3                    # Maximum distance
  enchant:
    block: ENCHANTING_TABLE
    distance: 3

sound:                             # Sound effects configuration
  forge:
    name: BLOCK_ANVIL_USE
    pitch: 1.0
  # ... (success, fail, bonus, recycle, repulse)

effect:                            # Particle effects configuration
  forge:
    particle: PORTAL
    offsetX: 0.0
    offsetY: 1.0
    offsetZ: 0.0
    speed: 0.0
    amount: 100
  # ... (success, fail, recycle)

directInteract:
  global: false                    # Enable direct block clicking
  worlds: []                       # Enabled worlds list

log:
  enabled: false                   # Enable operation logging
```

## How It Works

### Forge System

1. **Define Iron Levels**: Create tier materials using `/saika define iron <level>`
2. **Define Elements**: Create element types using `/saika define element <id>`
3. **Add Forgeable Items**: Hold an item and use `/saika add <level> <element> <cost> <weight>`
4. **Players Craft**: Players combine iron + element in forge UI to roll random items

Each forgeable item has:
- **Level**: Forge iron tier required
- **Element**: Element type required
- **Min Cost**: Minimum iron amount needed
- **Weight**: Probability weight for rolling
- **Forge Bonus**: Optional bonus items with drop chance

### Enchant System

Players place an item and enchant book in the enchant UI. Results are probability-based:
- **Success (40%)**: Full enchantment level applied
- **Moderate (50%)**: Half enchantment level applied
- **Fail (9%)**: No effect
- **Destroy (1%)**: Item is destroyed

### Recycle System

Players can break down forged items to recover materials. Configurable return percentages with optional bonus drops.

### Repulse System

Removes enchantments from items, consuming XP. Certain enchantments (like curses) can be blacklisted.

## Data Storage

All data is stored as YAML files in the plugin data folder:
- Forge materials (iron levels)
- Elements
- Forgeable items
- Enchant/repulse sources
- Bonus items

Items are serialized using NBT/Base64 encoding for persistence.

## API

Saika uses NBT tags via PersistentDataContainer to mark items, allowing other plugins to identify forged items.

## Building

```bash
./gradlew build
```

The compiled jar will be in `build/libs/`.

## License

This project is part of the NyaaCat plugin ecosystem.

## Links

- [NyaaCat GitHub](https://github.com/NyaaCat)
- [NyaaCore](https://github.com/NyaaCat/NyaaCore)
