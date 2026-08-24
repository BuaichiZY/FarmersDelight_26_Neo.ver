package vectorwing.farmersdelight.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import vectorwing.farmersdelight.FarmersDelight;
import vectorwing.farmersdelight.common.Configuration;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;
import vectorwing.farmersdelight.common.utility.TextUtils;

import javax.annotation.ParametersAreNonnullByDefault;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
public class CookingPotScreen extends AbstractRecipeBookScreen<CookingPotMenu>
{
	private static final Identifier BACKGROUND_TEXTURE = Identifier.fromNamespaceAndPath(FarmersDelight.MODID, "textures/gui/cooking_pot.png");
	private static final Rectangle HEAT_ICON = new Rectangle(47, 55, 17, 15);
	private static final Rectangle PROGRESS_ARROW = new Rectangle(89, 25, 0, 17);

	private final CookingPotRecipeBookComponent recipeBookComponent;

	public CookingPotScreen(CookingPotMenu menu, Inventory inventory, Component title) {
		this(menu, new CookingPotRecipeBookComponent(menu), inventory, title);
	}

	private CookingPotScreen(CookingPotMenu menu, CookingPotRecipeBookComponent recipeBookComponent, Inventory inventory, Component title) {
		super(menu, recipeBookComponent, inventory, title);
		this.recipeBookComponent = recipeBookComponent;
	}

	@Override
	public void init() {
		super.init();
		this.titleLabelX = 28;
		if (!Configuration.ENABLE_COOKING_POT_RECIPE_BOOK.get()) {
			this.recipeBookComponent.hide();
			this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
		}
	}

	@Override
	protected ScreenPosition getRecipeBookButtonPosition() {
		return new ScreenPosition(this.leftPos + 5, this.height / 2 - 49);
	}

	@Override
	protected void onRecipeBookButtonClick() {
		if (!Configuration.ENABLE_COOKING_POT_RECIPE_BOOK.get()) {
			this.recipeBookComponent.hide();
			this.leftPos = this.recipeBookComponent.updateScreenPosition(this.width, this.imageWidth);
		}
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
		super.extractBackground(graphics, mouseX, mouseY, partialTicks);
		graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE, this.leftPos, this.topPos, 0, 0,
				this.imageWidth, this.imageHeight, 256, 256);

		if (this.menu.isHeated()) {
			graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE,
					this.leftPos + HEAT_ICON.x, this.topPos + HEAT_ICON.y, 176, 0,
					HEAT_ICON.width, HEAT_ICON.height, 256, 256);
		}

		int progress = this.menu.getCookProgressionScaled();
		graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND_TEXTURE,
				this.leftPos + PROGRESS_ARROW.x, this.topPos + PROGRESS_ARROW.y, 176, 15,
				progress + 1, PROGRESS_ARROW.height, 256, 256);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		super.extractLabels(graphics, mouseX, mouseY);
		graphics.text(this.font, this.playerInventoryTitle, 8, this.imageHeight - 94, 4210752, false);
	}

	@Override
	protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
		if (this.isHovering(HEAT_ICON.x, HEAT_ICON.y, HEAT_ICON.width, HEAT_ICON.height, mouseX, mouseY)) {
			String key = "cooking_pot." + (this.menu.isHeated() ? "heated" : "not_heated");
			graphics.setTooltipForNextFrame(this.font, TextUtils.container(key), mouseX, mouseY);
		}

		if (this.minecraft != null && this.minecraft.player != null && this.menu.getCarried().isEmpty()
				&& this.hoveredSlot != null && this.hoveredSlot.hasItem() && this.hoveredSlot.index == CookingPotMenu.INDEX_MEAL) {
			List<Component> tooltip = new ArrayList<>();
			ItemStack mealStack = this.hoveredSlot.getItem();
			tooltip.add(Component.empty().append(mealStack.getHoverName())
					.withStyle(mealStack.getRarity().getStyleModifier()));

			ItemStack containerStack = this.menu.blockEntity.getContainer();
			if (!containerStack.isEmpty()) {
				tooltip.add(TextUtils.container("cooking_pot.served_on", containerStack.getHoverName().getString())
						.withStyle(ChatFormatting.GRAY));
			}
			graphics.setComponentTooltipForNextFrame(this.font, tooltip, mouseX, mouseY, mealStack);
		} else {
			super.extractTooltip(graphics, mouseX, mouseY);
		}
	}

	@Override
	protected boolean isBiggerResultSlot() {
		return false;
	}
}
