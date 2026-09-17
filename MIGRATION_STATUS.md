# Farmer's Delight 26.3 NeoForge migration status

## Baseline

- Source: official `vectorwing/FarmersDelight` repository
- Source branch: `1.21`
- Baseline commit: `71abf1c0` (`Bump version`)
- Target Minecraft: `26.3`
- Target/minimum NeoForge: `26.3.0.0-beta`
- ModDevGradle: `2.0.147`
- JEI development runtime: `31.0.0.5` (requires NeoForge `26.3.0.1-beta` or newer)
- Compiler/runtime: project-local Java 25 toolchain
- Gradle: 9.6.1, launched through `gradle-local.bat`

This port was produced in a separate project from the 26.1.2 and 26.2 ports.
Those source projects and their outputs are not modified by the 26.3 build.

## Build isolation

`gradle-local.bat` sets `GRADLE_USER_HOME` to `.gradle-user-home` inside this
project. It prefers the included `work/jdk-25` runtime, while retaining a
portable system-Java fallback. Toolchain downloading is disabled. Build
outputs, game runs, downloaded assets, and Gradle caches remain under this
project folder, and distributable project files contain no machine-specific
absolute paths.

## Working 26.3 port

- `clean build` succeeds on the exact NeoForge lower bound and produces both
  `build/libs/FarmersDelight-26.3-1.3.3.jar` and
  `build/libs/FarmersDelight-26.3-1.3.3_source.jar`.
- The distributed NeoForge dependency range is `[26.3.0.0-beta,)`.
- The dedicated GameTest server starts on both `26.3.0.0-beta` and
  `26.3.0.3-beta`, loads 2,084 advancements, and passes all six required tests.
- Runtime tests cover cooking and cutting recipes, food and effect components,
  item translations and knife tags, cabinet transactions, organic-compost
  conversion, mushroom planting, rich-soil colonies and sapling growth,
  hydrated rich-soil farmland growth, dry/trampled farmland preservation,
  skillet attack speed/animation/durability, and the 26.3 cooking-fuel and
  compostable components.
- The September 17 startup crash on `26.3.0.3-beta` was caused by removal of
  NeoForge's deprecated `items` package. All references to that package have
  been replaced with mod-owned inventory storage, vanilla menu slots and
  recipe inputs. External automation still uses transactional `ResourceHandler`.
- Additional regression tests verify cooking-pot sided insertion/extraction,
  rollback, menu/storage consistency, cutting-board and basket automation, and
  persistence of ingredients, bowls and a 64-serving meal display. The existing
  `Inventory` / `Size` / `Items` save format is preserved.
- A client smoke test on NeoForge `26.3.0.3-beta` with JEI `31.0.0.5`, using
  classes compiled against the `26.3.0.0-beta` lower bound, completes
  mod discovery, applies the mixins, creates the game window, and reloads the
  Farmer's Delight and JEI resources without a mod, model, or texture error.
- JEI remains optional in the distributed JAR. Cooking-pot, cutting-board, and
  decomposition categories are packaged; running with JEI requires the JEI
  release's own NeoForge lower bound (`26.3.0.1-beta`).
- Custom food-effect components store the registry's real effect holder rather
  than NeoForge's deferred wrapper, retaining compatibility with Bukkit-based
  hybrid servers when Nourishment or Comfort is applied.
- World generation uses 26.3 feature and placed-feature dynamic registries.
- Advancements and block loot tables use the 26.3 condition/modifier schemas;
  the legacy configured-feature and NeoForge fuel/compost data-map formats are
  no longer used.
- Fuel and compost behavior is supplied through 26.3 item components. Custom
  400- and 1,000-tick fuel providers preserve the vanilla fast-furnace halving
  behavior.
- Organic compost retains the original 3x3x3 catalyst rules: activators add 2%,
  water adds 10%, sky light adds 5% or 10%, and eight successful rolls convert
  it into rich soil. Its comparator output starts at 8 and counts down.
- Moist rich-soil farmland applies its configured growth boost, while dry soil
  does not. Rich-soil farmland does not revert when dry or trampled.
- Continuous skillet attacks remain available at an effective 2.0 attacks per
  second with the original six-tick whack animation and one durability consumed
  per successful melee attack.
- The cooking-pot bowl placeholder uses the GUI sprite atlas, the cutting-board
  JEI category is packaged, and all official language resources are retained.

## Remaining release checks

- Perform a manual in-world gameplay pass for interaction-heavy paths such as
  cooking-pot automation, skillet cooking and flipping, canvas-sign editing,
  village trades, and naturally generated wild crops.
- Optional EMI, AppleSkin, CraftTweaker, and legacy Java data-generator
  integrations remain disabled until 26.3-compatible dependencies and API
  migrations are available. Checked-in game data and assets are packaged and
  validated independently of those integrations.

The produced JAR is a tested port build; the manual gameplay matrix above is
still recommended before a public release.
