# Changelog

## 1.2.3

Complete translations.

- All 31 languages now translate every line of the mod. Most languages had only BuildCraft's old community translations, which covered a third of the text or less, so the rest showed in English. The community translations are kept; the missing lines (about 15,000 across all languages) were translated by an AI model (Claude) using each language's existing BuildCraft terms and BuildCraft 7.1's translations, and haven't been checked by native speakers yet. Corrections are welcome on the issue tracker.
- Serbian now comes in both of Minecraft's scripts: Cyrillic (`sr_sp`, as Minecraft's own Serbian is) and Latin (`sr_cs`, new). BuildCraft's Serbian translations were in Latin script.
- Fixes in the old community translations: the iron kinesis pipe's power levels were one step off in ten languages ("Very High" read "Full Capacity"), its limit message said RF/t instead of MJ/t, the blank robot board was called "Cleaner" in some languages, and a few names meant something else (the Slovak blueprint was "List", the Czech oil spring was "spring-season oil", the Turkish dense oil was a question).
- Lens names use a format key, so languages that put the colour after the noun can ("Lente roja").
- A unit test checks that every language has every line, with the same format arguments as the English.

## 1.2.2

Other mods' refined fuels in the combustion engine.

- The combustion engine burns other mods' diesel, gasoline, kerosene, biodiesel, ethanol and LPG through their common tags (`c:diesel`, `c:gasoline`, `c:kerosene`, `c:biodiesel`, `c:ethanol` and `c:bioethanol`, `c:lpg`), and the premium diesels (`c:high_power_biodiesel`, `c:boosted_diesel`). That covers PneumaticCraft, Modern Industrialization, Immersive Engineering, Oritech, Create: Diesel Generators, TFMG and Mekanism Generators. Each has its own power and burn time, near BuildCraft's light fuel and well below dense fuel (see the README for the table).
- Some mods put every fuel they make in `c:fuel`, which BuildCraft burns as light fuel. A fuel with its own entry now burns with that, whichever order the entries load in, and anything only in `c:fuel` still burns like light fuel.
- JEI and EMI list each fuel once: mods that tag their flowing fluids too no longer get a second entry for those.
- New GameTests (142, or 147 with PneumaticCraft) and a unit test that pins the agreed fuel values.

## 1.2.1

EMI support.

- **EMI** (optional) shows everything JEI does: assembly table recipes (including every facade), programming table boards, robot programming in the integration table, distiller, heat exchanger and combustion engine fuel and coolant recipes, with their machines as workstations; the stirling engine for furnace fuels; the auto workbench, advanced crafting table and stamping table as crafting tables. EMI's fill button puts a crafting recipe into the auto workbench's and advanced crafting table's blueprints and moves an assembly recipe's ingredients into the assembly table, items can be dragged from EMI into filter, blueprint and pattern slots, ledgers are kept clear of EMI's item list, and gates, lenses, facades, paintbrushes, guide books, robots and boards are listed one by one. With JEI installed as well, EMI uses BuildCraft's own EMI support rather than its JEI plugin.
- JEI and EMI can put a crafting recipe into the recipe packager's pattern. Filling a blueprint or pattern from a recipe uses the items the player has where a slot takes several (oak planks rather than whichever planks JEI was showing).
- BuildCraft's common tags (`c:gears` and its five gears, `c:oil`, `c:crude_oil`, `c:fuel`, `c:light_fuel`) have names, which JEI and EMI show for tag ingredients.
- Fixes items dragged from JEI into the recipe packager's pattern: the slot showed the item but wasn't switched on, so the packager left it out.
- For development: `recipe_viewer` in `gradle.properties` (or `-Precipe_viewer=emi` or `both`) picks the recipe viewer the dev runs load, and a new GameTest checks what recipe viewers send to the packager's pattern.

## 1.2.0

BuildCraft 7.1's crafting by package, which 8.0 never ported.

### Phase 8: packages

- The **recipe packager** packs a crafting recipe's ingredients into a package. Its 3x3 pattern sets each slot to an item or to "anything", and it takes the ingredients from its nine storage slots and from the inventories on its four sides, using paper (or a package with empty slots) for each package.
- The **stamping table**, a laser table, crafts packages as the crafting grid they hold (40 MJ an item), and other items on their own as one-item recipes (a log into planks). What can't be crafted passes through; what recipes leave behind goes in three leftover slots.
- **Packages** show their grid in their tooltip, and can be thrown by hand, by dispensers and by stripes pipes: they burst open where they land.
- 7.1's recipes, GUIs, textures and stamping table model; guide pages for all three; the stamping table is a crafting catalyst in JEI.
- The mod has a logo, shown in the mods list and on the GitHub page.
- The integration table no longer says "No recipes use this!" (robots are programmed in it).

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
