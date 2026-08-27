# AutoFarmer

Minecraft Forge 1.20.1 mod — fully automatic crop & tree farming machine.

Place the Auto Farmer block, fill input slots with seeds/saplings, and it will automatically plant, grow, harvest, and replant surrounding crops and trees — including **modded trees and crops** via runtime registry name detection.

## Features

### Crops (6 input slots)

| Crop | Seed Item | Harvest Output |
|------|-----------|---------------|
| Wheat | Wheat Seeds | Wheat |
| Carrots | Carrot | Carrots |
| Potatoes | Potato | Potatoes |
| Beetroot | Beetroot Seeds | Beetroots |
| Pumpkin | Pumpkin Seeds | Pumpkin Stem → Pumpkin blocks |
| Melon | Melon Seeds | Melon Stem → Melon blocks |
| Bamboo | Bamboo Shoot | Bamboo (auto-replants from bottom) |
| Sugar Cane | Sugar Cane | Sugar Cane (auto-replants from bottom) |
| Cactus | Cactus | Cactus (auto-replants from bottom) |
| Nether Wart | Nether Wart | Nether Wart |
| Sweet Berries | Sweet Berries | Sweet Berries (auto-replants) |

### Trees — fully mod-compatible

The mod uses **runtime registry name detection** to identify saplings, logs, and leaves. This means it works with:
- All 12 vanilla tree types (Oak, Spruce, Birch, Jungle, Acacia, Dark Oak, Mangrove, Cherry, Azalea, Flowering Azalea, Crimson, Warped)
- Any mod that follows naming conventions (registry path containing `sapling`, `fungus`, `log`, `stem`, `wood`, `trunk`, `leaf`, `leaves`, `foliage`)
- Custom tree mods (e.g., Botania, Mekanism, Twilight Forest)

**Tree auto-replant:** When a tree is harvested, the sapling item type is automatically returned to the input slot, creating an infinite farming loop.

### Cocoa Beans

Cocoa beans on adjacent log blocks are harvested at maturity (AGE_2 ≥ 2).

### Melon / Pumpkin Fruit

Adjacent melon and pumpkin fruit blocks are harvested when the stem is mature.

## Configuration

Edit `config/autofarmer-common.toml` to adjust:

| Config | Default | Range | Description |
|--------|---------|-------|-------------|
| `cooldownTicks` | 40 | 10–200 | Ticks between operations (40 = 2 seconds) |
| `treeSearchRadius` | 2 | 1–5 | Search radius around sapling to find grown tree (for offset-growing trees) |
| `treeHarvestRadius` | 2 | 1–5 | Extra radius to harvest leaves beyond connected logs |

## How It Works

1. **Place** the Auto Farmer block on the ground
2. **Right-click** to open the GUI
3. **Put seeds/saplings** in the 5 input slots (left side)
4. **Ensure proper terrain** around the block:
   - Seeds → Farmland or tillable dirt
   - Saplings → Dirt, grass, podzol
   - Nether Wart → Soul Sand
   - Sugar Cane → Sand (with adjacent water)
   - Cactus → Sand
   - Crimson/Warped Fungi → Netherrack or Nylium
   - Sweet Berries → Grass block, podzol, or mycelium
5. The machine automatically cycles:
   - **Phase 1 — Tree:** Check if planted sapling has grown → BFS harvest all connected logs + leaves → auto-replant sapling
   - **Phase 2 — Crops:** Check adjacent blocks for mature crops/fruit → harvest via loot tables → collect in output
   - **Phase 3 — Plant:** Plant next seed/sapling from input → track sapling position for tree harvest

### Full inventory fallback

If the output slot is full, harvested items drop on the ground. If input is full when auto-replanting, the sapling drops on the ground.

### Sound effects

- **Planting:** Bone meal sound (0.3 volume, 1.2 pitch)
- **Harvesting crops:** Sheep shearing sound (0.5 volume, 1.0 pitch)

## GUI Layout

```
┌──────────────────────────────────────┐
│            Auto Farmer               │
├──────────────────────────────────────┤
│  INPUT                →    OUTPUT    │
│  [S] [S] [S]                    [W] │
│  [S] [S] [S]                       │
│  ● Active                         │
├──────────────────────────────────────┤
│ [ ] [ ] [ ] [ ] [ ] [ ] [ ] [ ] [ ] │
│ [ ] [ ] [ ] [ ] [ ] [ ] [ ] [ ] [ ] │
│ [ ] [ ] [ ] [ ] [ ] [ ] [ ] [ ] [ ] │
├──────────────────────────────────────┤
│ [H] [H] [H] [H] [H] [H] [H] [H] [H] │
└──────────────────────────────────────┘
```

## Building

```bash
# Requires Java 17, Gradle
./gradlew build
# Output: build/libs/autofarmer-1.0.0.jar
```

## Dependencies

- Minecraft Forge 1.20.1 (47.4.0)
- Java 17

## License

MIT
