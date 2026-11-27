package vectorwing.farmersdelight.gametest;

import net.minecraft.core.ClientAsset;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import vectorwing.farmersdelight.FarmersDelight;

/** Checks the actual loaded advancement and the texture path resolved by the client. */
final class AdvancementResourceGameTests {
    private AdvancementResourceGameTests() {
    }

    static void testBackground(GameTestHelper helper) {
        var root = helper.getLevel().getServer().getAdvancements()
                .get(Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "main/root"));
        helper.assertTrue(root != null, "Farmer's Delight root advancement is missing");
        var display = root.value().display().orElseThrow();
        ClientAsset.ResourceTexture background = display.getBackground().orElseThrow();
        helper.assertValueEqual(background.id(), Identifier.withDefaultNamespace("block/bricks"),
                "advancement background asset ID");
        helper.assertValueEqual(background.texturePath(), Identifier.withDefaultNamespace("textures/block/bricks.png"),
                "resolved advancement background texture");
        // Dedicated-server class loaders filter out client assets. Their PNG files are
        // verified separately against the client resource JAR, not through this class loader.
        FarmersDelight.LOGGER.info("Advancement background regression: {} resolves to {}",
                background.id(), background.texturePath());
        helper.succeed();
    }
}
