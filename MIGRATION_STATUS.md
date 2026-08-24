# Farmer's Delight 26.1.2 NeoForge migration status

## Baseline

- Source: official `vectorwing/FarmersDelight` repository
- Source branch: `1.21`
- Baseline commit: `71abf1c0` (`Bump version`)
- Target Minecraft: `26.1.2`
- Target/minimum NeoForge: `26.1.2.95`
- ModDevGradle: `2.0.144`
- Compiler/runtime: Java 25 toolchain
- Gradle: 9.6.1, launched through the project wrapper via `gradle-local.bat`

The abandoned upstream `26.1`-branch attempt is not used as the source for this
port. Its build output and project caches were removed as requested.

## Build isolation

`gradle-local.bat` sets `GRADLE_USER_HOME` to `.gradle-user-home` inside this
project. An optional `work/jdk-25` installation is preferred, while system Java
25 installations are also detected. Toolchain downloading remains disabled.
Build output, game runs, downloaded assets, and Gradle caches therefore remain
under this project folder.

## Working core port

- The default source set compiles with no errors.
- `clean build` succeeds and produces
  `build/libs/FarmersDelight-26.1.2-1.3.3.jar`.
- A dedicated NeoForge server reaches `Done` with Farmer's Delight loaded.
- The client completes mod discovery, resource reload, model baking, texture
  atlas creation, and reaches the interactive main menu without Farmer's
  Delight errors.
- All 1,819 recipes load successfully on the dedicated server.
- A repeatable NeoForge GameTest suite validates core recipe matching and
  outputs, food components, knife tags, transactional cabinet automation, and
  organic-compost/rich-soil behavior. All five required tests (four Farmer's
  Delight tests plus NeoForge's baseline test) pass with
  `gradle-local.bat runGameTestServer` on the minimum supported NeoForge
  `26.1.2.95`.
- The old recipe ingredient/result JSON formats were migrated to 26.1.
- Block and item registration now supplies the mandatory 26.1 resource keys.
- Food, consumable, effect, particle, GUI, recipe-book, block-entity, render,
  payload, villager-trade, loot-modifier, and world-generation APIs have been
  migrated for the core mod.
- Rich soil is preserved beneath tree growth using the current tree-growth hook
  and tag behavior instead of obsolete tree mixins.
- Organic compost uses Minecraft 26.1's current mushroom-light-override tag.
  Its original 3x3x3 conversion rules are retained: each activator adds 2%,
  water adds 10%, sky light adds 5% or 10%, and eight successful rolls convert
  the block to rich soil. Comparator output starts at 8 and counts down.
- Moist rich-soil farmland applies the configured 20% bonemeal-style crop
  boost. Runtime tests verify that the boost triggers while hydrated and does
  not trigger when dry; the farmland also remains intact when dry or trampled.
- Handheld skillet ingredients and their flip animation use the 26.1 special
  item-model renderer pipeline.
- The skillet arm-pose enum extension uses NeoForge 26.1.2.95's current
  two-boolean plus transformer constructor and loads successfully on the
  client.
- The skillet now supplies Minecraft 26.1's required `WEAPON` component, so
  ordinary melee hits enter the vanilla post-hit pipeline and consume exactly
  one durability. This path is covered by the required GameTest suite.
- Continuous skillet attacks remain available, with an effective attack speed
  of 2.0 attacks per second and the original six-tick whack animation.
- The cooking-pot bowl placeholder was moved to the 26.1 GUI sprite atlas. The
  obsolete item-atlas copy was removed, eliminating the missing-texture icon
  and duplicate-sprite warning.
- JEI 29.21 integration is migrated and enabled as an optional integration.
  A real client connected to the development server registered 28 cooking-pot
  recipes and 105 cutting-board recipes, including the dedicated cutting-board
  category.
- Cooking and cutting recipe contents are synchronized to multiplayer clients
  through NeoForge's requested recipe-content mechanism, so JEI no longer
  depends on server-side recipe internals being present on the client.
- All official language files are packaged unchanged. Block items use the
  official `block.*` translation keys while standalone items use `item.*`.
- The 184 legacy generated item models are wrapped in Minecraft 26.1 item
  definitions during the isolated build; together with the authored skillet
  definition, all 185 registered items have a packaged definition.
- Standing and hanging canvas-sign edit screens use canvas-specific previews.
- The dedicated server no longer verifies or loads client-only recipe helpers.
- Cabinets now use the native NeoForge 26.1 `ItemStacksResourceHandler` for
  shared menu and automation storage, including transaction commit/rollback.

## Remaining work

- Perform manual in-world gameplay checks for interaction paths not yet suited
  to headless tests, especially cooking pot automation, skillet cooking,
  canvas-sign editing, village trades, and world generation.
- Replace the legacy `IItemHandler` compatibility layer with the new NeoForge
  resource-handler API in the remaining basket, cooking pot, cutting board, and
  skillet inventories. The cabinet migration is complete; the current core
  clean compilation has 71 removal warnings in total and should be modernized before
  a long-term release.
- Rewrite the optional Java data generators. NeoForge 26.1 removed the old
  model-generator and `ExistingFileHelper` stack; enabling
  `-Penable_data_generators=true` currently exposes 479 migration errors.
  Checked-in generated resources are packaged and load successfully, so this
  does not block the runtime JAR.
- Re-enable and migrate EMI, AppleSkin, and CraftTweaker after compatible
  Minecraft 26.1.2 artifacts are available. Their upstream 1.21 integrations
  remain excluded from the default build; JEI is already migrated and tested.

The current JAR is a test build, not a final release, until the manual gameplay
matrix above is completed.
