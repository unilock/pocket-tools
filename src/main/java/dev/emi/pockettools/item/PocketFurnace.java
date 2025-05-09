package dev.emi.pockettools.item;

import dev.emi.pockettools.PocketToolsMain;
import dev.emi.pockettools.component.PocketFurnaceComponent;
import dev.emi.pockettools.tooltip.ConvertibleTooltipData;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.Optional;

public class PocketFurnace<T extends AbstractCookingRecipe> extends Item {
	private RecipeType<T> type;

	public PocketFurnace(RecipeType<T> type, Settings settings) {
		super(settings);
		this.type = type;
	}

	@Override
	public boolean isItemBarVisible(ItemStack stack) {
		PocketFurnaceComponent data = stack.get(PocketToolsMain.POCKET_FURNACE_DATA);
		if (data != null) {
			int cookTime = data.cookTime();
			int fuelTime = data.fuelTime();
			return cookTime > 0 && fuelTime > 0;
		}
		return false;
	}

	@Override
	public int getItemBarStep(ItemStack stack) {
		PocketFurnaceComponent data = stack.get(PocketToolsMain.POCKET_FURNACE_DATA);
		if (data != null) {
			int cookTime = data.cookTime();
			int maxCookTime = data.maxCookTime();
			return Math.round((maxCookTime - cookTime) / ((float) (maxCookTime)) * 13f);
		}
		return 0;
	}

	@Override
	public int getItemBarColor(ItemStack stack) {
		PocketFurnaceComponent data = stack.get(PocketToolsMain.POCKET_FURNACE_DATA);
		if (data != null) {
			return MathHelper.packRgb(0, 150, 150);
		}
		return MathHelper.packRgb(0, 150, 150);
	}

	/*
	 * Emi did you really reimplement the furnace logic without copying code why
	 * didn't you just copy code you're gonna have so many edge cases that you
	 * haven't accounted for
	 */
	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
		if (!world.isClient()) {
			PocketFurnaceComponent data = stack.getOrDefault(PocketToolsMain.POCKET_FURNACE_DATA, PocketFurnaceComponent.DEFAULT);
			ItemStack input = data.input();
			ItemStack fuel = data.fuel();
			ItemStack output = data.output();
			int fuelTime = data.fuelTime();
			int cookTime = data.cookTime();
			int customModelData = stack.getOrDefault(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelDataComponent.DEFAULT).value();
			if (cookTime > 0) {
				if (fuelTime > 0) {
					fuelTime--;
					stack.apply(PocketToolsMain.POCKET_FURNACE_DATA, PocketFurnaceComponent.DEFAULT, fuelTime, PocketFurnaceComponent::withFuelTime);
				}
				if (fuelTime == 0) {
					if (fuel.getCount() > 0) {
						fuelTime = FuelRegistry.INSTANCE.get(fuel.getItem());
						stack.apply(PocketToolsMain.POCKET_FURNACE_DATA, PocketFurnaceComponent.DEFAULT, fuelTime, PocketFurnaceComponent::withFuelTime);
						stack.apply(PocketToolsMain.POCKET_FURNACE_DATA, PocketFurnaceComponent.DEFAULT, fuelTime, PocketFurnaceComponent::withMaxFuelTime);
						fuel.decrement(1);
						stack.apply(PocketToolsMain.POCKET_FURNACE_DATA, PocketFurnaceComponent.DEFAULT, fuel, PocketFurnaceComponent::withFuel);
					} else {
						return;
					}
				}
				cookTime--;
				if (cookTime == 0) {
					Optional<RecipeEntry<T>> recipe = world.getRecipeManager().getFirstMatch(type,
							new SingleStackRecipeInput(input), world);
					if (recipe.isPresent()) {
						ItemStack recipeOutput = recipe.get().value().getResult(world.getRegistryManager());

						if (output.isEmpty()) {
							output = recipeOutput.copy();
						} else if (ItemStack.areItemsEqual(output, recipeOutput)) {
							output.increment(1);
						}
						stack.apply(PocketToolsMain.POCKET_FURNACE_DATA, PocketFurnaceComponent.DEFAULT, output, PocketFurnaceComponent::withOutput);
						if (output.getCount() < output.getMaxCount() && input.getCount() > 1) {
							cookTime = recipe.get().value().getCookingTime();
							stack.apply(PocketToolsMain.POCKET_FURNACE_DATA, PocketFurnaceComponent.DEFAULT, cookTime, PocketFurnaceComponent::withMaxCookTime);
						}
					}
					input.decrement(1);
					stack.apply(PocketToolsMain.POCKET_FURNACE_DATA, PocketFurnaceComponent.DEFAULT, input, PocketFurnaceComponent::withInput);
				}
				stack.apply(PocketToolsMain.POCKET_FURNACE_DATA, PocketFurnaceComponent.DEFAULT, cookTime, PocketFurnaceComponent::withCookTime);
			} else {
				if (fuelTime > 0) {
					fuelTime--;
					stack.apply(PocketToolsMain.POCKET_FURNACE_DATA, PocketFurnaceComponent.DEFAULT, fuelTime, PocketFurnaceComponent::withFuelTime);
				}
				if (output.getCount() < output.getMaxCount() && input.getCount() > 0) {
					Optional<RecipeEntry<T>> recipe = world.getRecipeManager().getFirstMatch(type,
							new SingleStackRecipeInput(input), world);
					if (recipe.isPresent() && (output.isEmpty() || (ItemStack.areItemsEqual(output, recipe.get().value().getResult(world.getRegistryManager()))
							&& output.getCount() < output.getMaxCount()))) {
						cookTime = recipe.get().value().getCookingTime();
						stack.apply(PocketToolsMain.POCKET_FURNACE_DATA, PocketFurnaceComponent.DEFAULT, cookTime, PocketFurnaceComponent::withCookTime);
						stack.apply(PocketToolsMain.POCKET_FURNACE_DATA, PocketFurnaceComponent.DEFAULT, cookTime, PocketFurnaceComponent::withCookTime);
					}
				}
			}
			if ((customModelData == 0) != (fuelTime == 0)) {
				stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(1 - customModelData));
			}
		}
	}

	@Override
	public boolean onClicked(ItemStack stack, ItemStack applied, Slot slot, ClickType clickType,
			PlayerEntity player, StackReference cursor) {
		PocketFurnaceComponent data = stack.getOrDefault(PocketToolsMain.POCKET_FURNACE_DATA, PocketFurnaceComponent.DEFAULT);
		if (clickType == ClickType.RIGHT) {
			if (applied.isEmpty()) {
				if (!data.output().isEmpty()) {
					ItemStack output = data.output();
					cursor.set(output.copy());
					stack.apply(PocketToolsMain.POCKET_FURNACE_DATA, PocketFurnaceComponent.DEFAULT, ItemStack.EMPTY, PocketFurnaceComponent::withOutput);
					return true;
				}
			}
			ItemStack input = data.input();
			if ((!input.isEmpty() && ItemStack.areItemsEqual(input, applied))
					|| (input.isEmpty() && isSmeltable(player.getWorld(), applied))) {
				if (input.isEmpty()) {
					input = applied.copy();
					applied.setCount(0);
				} else {
					if (input.getCount() + applied.getCount() > input.getMaxCount()) {
						applied.setCount(input.getCount() + applied.getCount() - input.getMaxCount());
						input.setCount(input.getMaxCount());
					} else {
						input.setCount(input.getCount() + applied.getCount());
						applied.setCount(0);
					}
				}
				stack.apply(PocketToolsMain.POCKET_FURNACE_DATA, PocketFurnaceComponent.DEFAULT, input, PocketFurnaceComponent::withInput);
				return true;
			}
			ItemStack fuel = data.fuel();
			if ((!fuel.isEmpty() && ItemStack.areItemsEqual(fuel, applied))
					|| (fuel.isEmpty() && AbstractFurnaceBlockEntity.canUseAsFuel(applied))) {
				if (fuel.isEmpty()) {
					fuel = applied.copy();
					applied.setCount(0);
				} else {
					if (fuel.getCount() + applied.getCount() > fuel.getMaxCount()) {
						applied.setCount(fuel.getCount() + applied.getCount() - fuel.getMaxCount());
						fuel.setCount(fuel.getMaxCount());
					} else {
						fuel.setCount(fuel.getCount() + applied.getCount());
						applied.setCount(0);
					}
				}
				stack.apply(PocketToolsMain.POCKET_FURNACE_DATA, PocketFurnaceComponent.DEFAULT, fuel, PocketFurnaceComponent::withFuel);
				return true;
			}
		}
		return false;
	}

	@Override
	public Optional<TooltipData> getTooltipData(ItemStack stack) {
		return Optional.of(new PocketFurnaceTooltip(stack));
	}

	protected boolean isSmeltable(World world, ItemStack itemStack) {
		return world.getRecipeManager().getFirstMatch(type, new SingleStackRecipeInput(itemStack), world)
				.isPresent();
	}

	static class PocketFurnaceTooltip implements ConvertibleTooltipData, TooltipComponent {
		private final Identifier FURNACE = Identifier.of("pockettools", "textures/gui/component/furnace.png");
		public ItemStack stack;

		public PocketFurnaceTooltip(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		public TooltipComponent getComponent() {
			return this;
		}

		@Override
		public int getHeight() {
			return 40;
		}

		@Override
		public int getWidth(TextRenderer textRenderer) {
			return 66;
		}

		@Override
		public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
			PocketFurnaceComponent data = stack.getOrDefault(PocketToolsMain.POCKET_FURNACE_DATA, PocketFurnaceComponent.DEFAULT);
			ItemStack input = data.input();
			ItemStack fuel = data.fuel();
			ItemStack output = data.output();
			int fuelTime = data.fuelTime();
			int cookTime = data.cookTime();
			int maxFuelTime = data.maxFuelTime();
			int maxCookTime = data.maxCookTime();
			this.renderGuiItem(context, textRenderer, input, x + 2, y + 1);
			this.renderGuiItem(context, textRenderer, output, x + 48, y + 2);
			this.renderGuiItem(context, textRenderer, fuel, x + 2, y + 20);
			context.setShaderColor(1.f, 1.f, 1.f, 1.f);
			if (maxFuelTime > 0) {
				int fuelProgress = fuelTime * 13 / maxFuelTime;
				context.drawTexture(FURNACE, x + 26, y + 36 - fuelProgress, 0, 13 - fuelProgress, 13, fuelProgress + 1, 256, 256);
			}
			if (cookTime > 0 && maxCookTime > 0) {
				int cookProgress = 22 - (cookTime * 22 / maxCookTime);
				context.drawTexture(FURNACE, x + 22, y + 3, 0, 13, cookProgress, 15, 256, 256);
			}
		}

		private void renderGuiItem(DrawContext context, TextRenderer textRenderer, ItemStack stack, int x, int y) {
			context.drawItem(stack, x, y);
			context.drawItemInSlot(textRenderer, stack, x, y);
		}
	}
}
