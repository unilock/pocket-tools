package dev.emi.pockettools.item;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ClickType;
import net.minecraft.world.World;

import java.util.Set;

public class PocketGrindstone extends Item {

	public PocketGrindstone(Settings settings) {
		super(settings);
	}

	@Override
	public boolean onClicked(ItemStack stack, ItemStack applied, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursor) {
		if (clickType == ClickType.RIGHT && (applied.hasEnchantments() || applied.isOf(Items.ENCHANTED_BOOK))) {
			World world = player.getWorld();
			if (!world.isClient()) {
				ExperienceOrbEntity.spawn((ServerWorld) world, player.getPos(), getExperience(applied, world));
			}
			cursor.set(grind(applied));
			world.syncWorldEvent(1042, player.getBlockPos(), 0);
			return true;
		}
		return false;
	}

	private ItemStack grind(ItemStack stack) {
		ItemStack copy = stack.copy();
		stack.remove(DataComponentTypes.ENCHANTMENTS);
		stack.remove(DataComponentTypes.STORED_ENCHANTMENTS);

		ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(copy.getEnchantments());
		builder.remove(entry -> !entry.isIn(EnchantmentTags.CURSE));
		Set<RegistryEntry<Enchantment>> set = builder.getEnchantments();
		EnchantmentHelper.set(stack, builder.build());
		stack.remove(DataComponentTypes.REPAIR_COST);
		if (stack.isOf(Items.ENCHANTED_BOOK) && set.isEmpty()) {
			stack = new ItemStack(Items.BOOK);
			if (copy.contains(DataComponentTypes.CUSTOM_NAME)) {
				stack.set(DataComponentTypes.CUSTOM_NAME, copy.getName());
			}
		}

		for(int i = 0; i < set.size(); ++i) {
			stack.set(DataComponentTypes.REPAIR_COST, AnvilScreenHandler.getNextCost(stack.getOrDefault(DataComponentTypes.REPAIR_COST, 0)));
		}
		return stack;
	}

	private int getExperience(ItemStack stack, World world) {
		int i = 0;
		Set<Object2IntMap.Entry<RegistryEntry<Enchantment>>> set = stack.getEnchantments().getEnchantmentEntries();

		for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : set) {
			RegistryEntry<Enchantment> enchantment = entry.getKey();
			int integer = entry.getIntValue();
			if (!enchantment.isIn(EnchantmentTags.CURSE)) {
				i += enchantment.value().getMinPower(integer);
			}
		}
		int j = (int)Math.ceil((double)i / 2.0D);
		return j + world.random.nextInt(j);
	}
}
