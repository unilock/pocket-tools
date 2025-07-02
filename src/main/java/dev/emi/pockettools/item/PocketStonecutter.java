package dev.emi.pockettools.item;

import dev.emi.pockettools.PocketToolsMain;
import dev.emi.pockettools.component.PocketStonecutterComponent;
import dev.emi.pockettools.tooltip.ConvertibleTooltipData;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ClickType;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PocketStonecutter extends Item {

	public PocketStonecutter(Settings settings) {
		super(settings);
	}

	@Override
	public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
		World world = player.getWorld();
		var data = stack.getOrDefault(PocketToolsMain.POCKET_STONECUTTER_DATA, PocketStonecutterComponent.DEFAULT);
		if (clickType == ClickType.RIGHT) {
			if (otherStack.isEmpty()) {
				if (!data.base().isEmpty()) {
					ItemStack base = data.base();
					List<RecipeEntry<StonecuttingRecipe>> list = world.getRecipeManager().getAllMatches(RecipeType.STONECUTTING, new SingleStackRecipeInput(base), world);
					int offset = data.offset();
					offset++;
					if (offset >= list.size()) {
						offset = 0;
					}
					PocketStonecutterComponent.applyOffset(stack, offset);
					if (world.isClient) {
						world.playSound(player, player.getBlockPos(), SoundEvents.UI_STONECUTTER_SELECT_RECIPE, SoundCategory.BLOCKS, 1.0F, 1.0F);
					}
					return true;
				}
			} else {
				List<RecipeEntry<StonecuttingRecipe>> list = world.getRecipeManager().getAllMatches(RecipeType.STONECUTTING, new SingleStackRecipeInput(otherStack), world);
				if (!list.isEmpty()) {
					PocketStonecutterComponent.applyBase(stack, otherStack);
					PocketStonecutterComponent.applyOffset(stack, 0);
					if (world.isClient) {
						world.playSound(player, player.getBlockPos(), SoundEvents.UI_STONECUTTER_SELECT_RECIPE, SoundCategory.BLOCKS, 1.0F, 1.0F);
					}
					return true;
				}
			}
		}
		return super.onClicked(stack, otherStack, slot, clickType, player, cursorStackReference);
	}

	@Override
	public boolean onStackClicked(ItemStack self, Slot slot, ClickType clickType, PlayerEntity player) {
		World world = player.getWorld();
		ItemStack stack = slot.getStack();
		if (clickType == ClickType.RIGHT) {
			var data = self.getOrDefault(PocketToolsMain.POCKET_STONECUTTER_DATA, PocketStonecutterComponent.DEFAULT);
			if (!data.base().isEmpty()) {
				ItemStack base = data.base();
				if (ItemStack.areItemsEqual(base, stack)) {
					List<RecipeEntry<StonecuttingRecipe>> list = world.getRecipeManager().getAllMatches(RecipeType.STONECUTTING, new SingleStackRecipeInput(stack), world);
					int offset = data.offset();
					if (offset < list.size()) {
						ItemStack output = list.get(offset).value().getResult(world.getRegistryManager()).copy();
						int count = output.getCount() * stack.getCount();
						output.setCount(Math.min(count, output.getMaxCount()));
						count -= output.getCount();
						if (slot.canInsert(output)) {
							slot.setStack(output);
							while (count > 0) {
								output = output.copy();
								output.setCount(Math.min(count, output.getMaxCount()));
								count -= output.getCount();
								player.getInventory().offerOrDrop(output);
							}
							if (world.isClient) {
								world.playSound(player, player.getBlockPos(), SoundEvents.UI_STONECUTTER_TAKE_RESULT, SoundCategory.BLOCKS, 1.0F, 1.0F);
							}
							return true;
						}
					}
				}
			}
		}
		return super.onStackClicked(self, slot, clickType, player);
	}

	@Override
	public Optional<TooltipData> getTooltipData(ItemStack stack) {
		return Optional.of(new PocketStonecutterTooltip(stack));
	}

	class PocketStonecutterTooltip implements ConvertibleTooltipData, TooltipComponent {
		public List<RecipeEntry<StonecuttingRecipe>> list = new ArrayList<>();
		public ItemStack stack;

		public PocketStonecutterTooltip(ItemStack stack) {
			this.stack = stack;
			var data = stack.getOrDefault(PocketToolsMain.POCKET_STONECUTTER_DATA, PocketStonecutterComponent.DEFAULT);
			if (!data.base().isEmpty()) {
				ItemStack base = data.base();
				MinecraftClient client = MinecraftClient.getInstance();
				ClientWorld world = client.world;
				list = world.getRecipeManager().getAllMatches(RecipeType.STONECUTTING, new SingleStackRecipeInput(base), world);
			}
		}

		@Override
		public TooltipComponent getComponent() {
			return this;
		}

		@Override
		public int getHeight() {
			if (!list.isEmpty()) {
				return ((list.size() - 1) / 4 + 1) * 18 + 4;
			}
			return 0;
		}

		@Override
		public int getWidth(TextRenderer textRenderer) {
			return 18 * 4 + 4;
		}

		private static final Identifier STONECUTTER_ICONS_TEXTURE = Identifier.ofVanilla("textures/gui/container/stonecutter.png");

		@Override
		public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
			var data = stack.getOrDefault(PocketToolsMain.POCKET_STONECUTTER_DATA, PocketStonecutterComponent.DEFAULT);
			if (!list.isEmpty()) {
				int offset = data.offset();

				final int maxX = x + 4 * 18;
				int sx = x;
				int sy = y;
				int i = 0;
				for (RecipeEntry<StonecuttingRecipe> entry : list) {
					ItemStack output = entry.value().getResult(MinecraftClient.getInstance().world.getRegistryManager());

					context.drawItem(output, sx + 2, sy + 2);
					context.drawItemInSlot(textRenderer, output, sx + 2, sy + 2);

					context.setShaderColor(1.f, 1.f, 1.f, 1.f);

					float v = i == offset ? 184.f : 166.f;
					context.drawTexture(STONECUTTER_ICONS_TEXTURE, sx + 2, sy + 1, 0.f, v, 18, 18, 256, 256);

					sx += 16;
					if (sx >= maxX) {
						sx = x;
						sy += 16;
					}

					i++;
				}
			}
			TooltipComponent.super.drawItems(textRenderer, x, y, context);
		}
	}
}
