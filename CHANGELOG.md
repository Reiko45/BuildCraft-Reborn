# Changelog

## 1.1.3

- The mod has a logo, shown in the mods list and on the GitHub page.

## 1.1.2

Oil and fuel work with other mods.

- BuildCraft's oil is in the common fluid tags `c:oil` and `c:crude_oil`, light fuel is in `c:fuel` and `c:light_fuel`, and gaseous fuel is in `c:gaseous`. These are the names other 1.21.1 mods use: PneumaticCraft's refinery and Ad Astra's fuel refinery take BuildCraft's oil, and Ad Astra's rockets fly on BuildCraft's fuel.
- Other mods' fluids in those tags work in BuildCraft's machines: oil burns in the combustion engine, heats in the heat exchanger and distils in the distiller like BuildCraft's oil, and fuel burns like light fuel. Data packs can change these values in the fuel, distillation and heatable data maps.
- New GameTests, including tests with PneumaticCraft's oil (`gradlew runGameTestServer -Pinterop_runtime=true`).
- The mods list links to the GitHub page and issue tracker, says when a new version is out (from `update.json` in the repository), and has a description that includes the robots.
- First release on GitHub.

## 1.1.1

Fixes found in the first play test.

- Pickaxes work again with BuildCraft Reborn installed. The released jar's `minecraft:mineable/pickaxe` tag listed a development-only test block that only exists in development builds, so Minecraft threw the whole tag away: stone and ores mined slowly and dropped nothing. Development-only content no longer gets tags or a loot table, and a unit test checks the generated data never mentions it. This affected 1.0.0-rc.1 and 1.1.0.

## 1.1.0

BuildCraft 7.1's robots, which 8.0 never ported.

### Phase 7: robots

- Robots, with 7.1's model and textures, drawn with 1.21's renderer: lights that show their charge, the items they carry, the tool they use, lasers, energy puffs, and helmets and mob heads they wear.
- All 17 of 7.1's redstone boards: picker, carrier, tank, delivery, lumberjack, harvester, miner, planter, farmer, leaf cutter, butcher, shovelman, pump, knight, bomber, stripes and builder, with 7.1's AIs, path finding and crop handling.
- Docking stations (pipe pluggables) that robots are placed on, sleep on and recharge at (from kinesis pipes), with 7.1's gate actions and triggers for stations and robots.
- The programming table (programs blank boards), charging table (charges robots and Forge Energy items) and robot programming in the integration table.
- The requester, which delivery robots fill, and the construction mark, a blueprint holder that builder robots supply (built on 8.0's builder).
- The redstone crystal, and 7.1's recipes for everything above and for the zone planner (which had none).
- Robots run on MJ: 10,000 MJ each, with 7.1's costs at 10 RF to the MJ.
- JEI shows programming table boards and robot programming; guide pages for the robots, boards, stations, tables, requester and construction mark.
- BuildCraft's crop API is back (robots farm through it), so the stripes pipe plants through it again, as in 1.12.
- Fixes to 8.0 found on the way: the pulsar item can be placed (it placed nothing), pipes can take results out of the integration table, and kinesis pipes power pluggables that ask for power.

## 1.0.0-rc.1 (first release candidate)

Everything BuildCraft 8.0 had for Minecraft 1.12.2 is ported to Minecraft 1.21.1 NeoForge, apart from what the README lists under "Not ported". This is the first build meant for playing rather than development; please report problems on this project, not to the BuildCraft Team.

### Phase 6: robotics, tools, guide, integrations

- The zone planner, with its 3D map GUI, zone painting with paintbrushes, map location copying and the small map on its screen.
- The list, map location and debugger.
- The RF engine (FE to MJ) and MJ dynamo (MJ to FE), off by default as in 1.12 (`enableRfEngine` and `enableMjDynamo` in the energy config).
- BuildCraft's guide book with all 107 pages, recipes and uses, sorting, search and bookmarks.
- JEI, Jade, The One Probe and CraftTweaker support (all optional).
- Translations into the 30 languages 1.12's BuildCraft had.
- Advancements for the guide, the list, swimming in oil, and the hidden one that unlocks the list's recipe.
- The zone planner's map refreshes sooner, its map slots take one map from a stack, and it copies zones in and out at the same time.

### Phase 5: builders

The quarry, filler (19 patterns), architect table, blueprints and templates, builder, electronic library, replacer, single schematics, volume boxes and the filler planner, with schematic rules for 1.21's blocks.

### Phase 4: transport and silicon

All 47 pipe types, wires, plugs, gates with their triggers and actions, the laser and the assembly, advanced crafting and integration tables, facades, lenses, the pulsar, light sensor and timer, the gate copier and the paintbrush.

### Phase 3: factory

The tank, mining well, pump, flood gate, chute, auto workbench, distiller, heat exchanger and water gel, with refinery recipes as data maps.

### Phase 2: core and energy

The redstone, stirling, combustion and creative engines, the wrench, markers, springs, decorations, the 30 oil and fuel fluids, oil wells, and fuels and coolants as data maps.

### Phase 1: foundation

The BuildCraft API, expression language, base block entity, network payloads, menus and screens, ledgers, MJ capabilities, tanks and item handlers.
