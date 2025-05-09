package dev.emi.pockettools.item;

import dev.emi.pockettools.PocketToolsMain;
import dev.emi.pockettools.component.PocketArmorStandComponent;
import dev.emi.pockettools.tooltip.ConvertibleTooltipData;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ClickType;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Optional;

public class PocketArmorStand extends Item {

	public PocketArmorStand(Settings settings) {
		super(settings);
	}

	@Override
	public boolean onClicked(ItemStack self, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursor) {
		World world = player.getWorld();
		PocketArmorStandComponent data = self.getOrDefault(PocketToolsMain.POCKET_ARMOR_STAND_DATA, PocketArmorStandComponent.DEFAULT);
		if (clickType == ClickType.RIGHT) {
			if (otherStack.isEmpty()) {
				dumpArmor(self, data, player.getInventory());
				if (world.isClient) {
					world.playSound(player, player.getBlockPos(), SoundEvents.ITEM_ARMOR_EQUIP_GENERIC.value(), SoundCategory.PLAYERS, 1.0f, 1.0f);
				}
				return true;
			} else {
				EquipmentSlot es = player.getPreferredEquipmentSlot(otherStack);
				if (es != EquipmentSlot.MAINHAND && es != EquipmentSlot.OFFHAND) {
					ItemStack inner = swapArmor(self, otherStack, es, data, player);
					cursor.set(inner);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onStackClicked(ItemStack self, Slot slot, ClickType clickType, PlayerEntity player) {
		ItemStack stack = slot.getStack();
		PocketArmorStandComponent data = self.getOrDefault(PocketToolsMain.POCKET_ARMOR_STAND_DATA, PocketArmorStandComponent.DEFAULT);
		if (slot.canTakeItems(player) && clickType == ClickType.RIGHT) {
			EquipmentSlot es = player.getPreferredEquipmentSlot(stack);
			if (es != EquipmentSlot.MAINHAND && es != EquipmentSlot.OFFHAND && es != EquipmentSlot.BODY) {
				ItemStack inner = data.getStack(es.getName());
				if (slot.canInsert(inner) || inner.isEmpty()) {
					inner = swapArmor(self, stack, es, data, player);
					slot.setStack(inner);
					return true;
				}
			}
		}
		return super.onStackClicked(self, slot, clickType, player);
	}

	@Override
	public Optional<TooltipData> getTooltipData(ItemStack stack) {
		return Optional.of(new PocketArmorStandTooltip(stack));
	}

	private ItemStack swapArmor(ItemStack self, ItemStack stack, EquipmentSlot es, PocketArmorStandComponent data, PlayerEntity player) {
		var world = player.getWorld();
		var inner = data.getStack(es.getName());
		if (world.isClient()) {
			if (stack.getItem() instanceof ArmorItem armorItem) {
				world.playSound(player, player.getBlockPos(), armorItem.getEquipSound().value(),
						SoundCategory.PLAYERS, 1.0f, 1.0f);
			} else {
				world.playSound(player, player.getBlockPos(), SoundEvents.ITEM_ARMOR_EQUIP_GENERIC.value(),
						SoundCategory.PLAYERS, 1.0f, 1.0f);
			}
		}
		self.apply(PocketToolsMain.POCKET_ARMOR_STAND_DATA, PocketArmorStandComponent.DEFAULT, stack, (c, s) -> c.withStack(es.getName(), stack));
		PocketArmorStandComponent postData = self.getOrDefault(PocketToolsMain.POCKET_ARMOR_STAND_DATA, PocketArmorStandComponent.DEFAULT);
		int mask = 0;
		if (!postData.head().isEmpty()) {
			mask |= 1;
		}
		if (!postData.chest().isEmpty()) {
			mask |= 2;
		}
		if (!postData.legs().isEmpty()) {
			mask |= 4;
		}
		if (!postData.feet().isEmpty()) {
			mask |= 8;
		}
		stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, new CustomModelDataComponent(mask));
		return inner;
	}

	private void dumpArmor(ItemStack stack, PocketArmorStandComponent data, PlayerInventory playerInventory) {
		ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
		if (!data.head().isEmpty()) {
			stacks.add(data.head());
			stack.apply(PocketToolsMain.POCKET_ARMOR_STAND_DATA, PocketArmorStandComponent.DEFAULT, ItemStack.EMPTY, PocketArmorStandComponent::withHead);
		}
		if (!data.chest().isEmpty()) {
			stacks.add(data.chest());
			stack.apply(PocketToolsMain.POCKET_ARMOR_STAND_DATA, PocketArmorStandComponent.DEFAULT, ItemStack.EMPTY, PocketArmorStandComponent::withChest);
		}
		if (!data.legs().isEmpty()) {
			stacks.add(data.legs());
			stack.apply(PocketToolsMain.POCKET_ARMOR_STAND_DATA, PocketArmorStandComponent.DEFAULT, ItemStack.EMPTY, PocketArmorStandComponent::withLegs);
		}
		if (!data.feet().isEmpty()) {
			stacks.add(data.feet());
			stack.apply(PocketToolsMain.POCKET_ARMOR_STAND_DATA, PocketArmorStandComponent.DEFAULT, ItemStack.EMPTY, PocketArmorStandComponent::withFeet);
		}
		stack.set(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelDataComponent.DEFAULT);
		for (ItemStack s : stacks) {
			playerInventory.offerOrDrop(s);
		}
	}

	static class PocketArmorStandTooltip implements ConvertibleTooltipData, TooltipComponent {
		public ItemStack stack;

		public PocketArmorStandTooltip(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		public TooltipComponent getComponent() {
			return this;
		}

		@Override
		public int getHeight() {
			return 20;
		}

		@Override
		public int getWidth(TextRenderer textRenderer) {
			return 68;
		}

		@Override
		public void drawItems(TextRenderer textRenderer, int x, int y, DrawContext context) {
			PocketArmorStandComponent data = stack.getOrDefault(PocketToolsMain.POCKET_ARMOR_STAND_DATA, PocketArmorStandComponent.DEFAULT);
			if (!data.head().isEmpty()) {
				this.renderGuiItem(context, textRenderer, data.head(), x + 2, y + 2);
			}
			if (!data.chest().isEmpty()) {
				this.renderGuiItem(context, textRenderer, data.chest(), x + 18, y + 2);
			}
			if (!data.legs().isEmpty()) {
				this.renderGuiItem(context, textRenderer, data.legs(), x + 34, y + 2);
			}
			if (!data.feet().isEmpty()) {
				this.renderGuiItem(context, textRenderer, data.feet(), x + 50, y + 2);
			}
		}

		private void renderGuiItem(DrawContext context, TextRenderer textRenderer, ItemStack stack, int x, int y) {
			context.drawItem(stack, x, y);
			context.drawItemInSlot(textRenderer, stack, x, y);
		}
	}
}
