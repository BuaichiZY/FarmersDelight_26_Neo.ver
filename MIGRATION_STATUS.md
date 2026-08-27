# Farmer's Delight 26.2 NeoForge migration status

## Baseline

- Source: official `vectorwing/FarmersDelight` repository
- Source branch: `1.21`
- Baseline commit: `71abf1c0` (`Bump version`)
- Target Minecraft: `26.2`
- Target/minimum NeoForge: `26.2.0.69`
- ModDevGradle: `2.0.144`
- JEI development runtime: `30.26.0.186`
- Compiler/runtime: project-local Java 25 toolchain
- Gradle: 9.6.1, launched through `gradle-local.bat`

This port was produced in a separate project from the completed 26.1.2 port.
The 26.1.2 source project and its outputs are not modified by the 26.2 build.

## Build isolation

`gradle-local.bat` sets `GRADLE_USER_HOME` to `.gradle-user-home` inside this
project. It prefers the included `work/jdk-25` runtime, while retaining a
portable system-Java fallback. Toolchain downloading is disabled. Build
outputs, game runs, downloaded assets, and Gradle caches remain under this
project folder, and project sources contain no machine-specific absolute paths.

## Working 26.2 port

- `clean build` succeeds and produces both
  `build/libs/FarmersDelight-26.2-1.3.3.jar` and
  `build/libs/FarmersDelight-26.2-1.3.3_source.jar`.
- The 26.2 client completes mod discovery, applies all mixins, reloads assets,
  bakes models and atlases, loads JEI, and reaches the main menu without a
  Farmer's Delight error, missing model, or missing texture.
- The dedicated GameTest server loads 1,889 recipes and 1,906 advancements.
  All five required tests pass.
- Runtime tests cover cooking and cutting recipes, food/item components, knife
  tags, cabinet transactions, organic-compost conversion, mushroom planting,
  rich-soil mushroom colonies and sapling growth, hydrated rich-soil farmland,
  dry/trampled farmland preservation, and skillet attack behavior.
- 26.2's advancement and entity-predicate package/schema changes are applied to
  Java triggers, loot modifiers, and advancements.
- HUD overlays, GUI screen opening, lighting lookup, colored block collections,
  entity types, and block-center calls use their 26.2 APIs.
- The canvas-rug break-overlay mixin targets 26.2's `LevelExtractor` pipeline.
- Canvas signs use the 26.2 sign model system. All standing, wall, hanging, and
  wall-hanging variants have generated blockstates/models and converted block
  textures; canvas-specific sign edit previews remain available.
- The safety net uses 26.2's restitution property while retaining its fall
  damage behavior.
- Organic compost retains the original 3x3x3 catalyst rules: activators add 2%,
  water adds 10%, sky light adds 5% or 10%, and eight successful rolls convert
  it into rich soil. Its comparator output starts at 8 and counts down.
- Moist rich-soil farmland applies its configured growth boost, while dry soil
  does not. Rich-soil farmland does not revert when dry or trampled.
- Continuous skillet attacks remain available at an effective 2.0 attacks per
  second with the original six-tick whack animation and one durability consumed
  per successful melee attack.
- The cooking-pot bowl placeholder uses the GUI sprite atlas.
- JEI `30.26.0.186` is optional in the distributed JAR and is present only in
  the development runtime. Cooking-pot, cutting-board, and decomposition
  categories register during client startup.
- All 45 official language files are packaged, including `en_us` and `zh_cn`.
  All 185 registered items have packaged 26.2 item definitions.
- Cabinets retain transactional menu and automation storage behavior.

## Remaining release checks

- Perform a manual in-world gameplay pass for interaction-heavy paths such as
  cooking-pot automation, skillet cooking and flipping, canvas-sign editing,
  village trades, and world generation.
- The legacy item-handler compatibility surface still produces NeoForge
  removal warnings. It remains functional in 26.2 but should be migrated to the
  resource-handler API for a later long-term maintenance release.
- Optional EMI, AppleSkin, CraftTweaker, and Java data-generator integrations
  remain disabled until their 26.2 migrations are completed. Checked-in data
  and assets are packaged and validated independently of those integrations.

The produced JAR is a tested port build; the manual gameplay matrix above is
still recommended before publishing it as a final release.
