package dev.emi.pockettools.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

public record PocketArmorStandComponent(ItemStack head, ItemStack chest, ItemStack legs, ItemStack feet) {
	public static final PocketArmorStandComponent DEFAULT = new PocketArmorStandComponent(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY);

	public static final Codec<PocketArmorStandComponent> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			ItemStack.OPTIONAL_CODEC.fieldOf("heads").forGetter(PocketArmorStandComponent::head),
			ItemStack.OPTIONAL_CODEC.fieldOf("chest").forGetter(PocketArmorStandComponent::chest),
			ItemStack.OPTIONAL_CODEC.fieldOf("legs").forGetter(PocketArmorStandComponent::legs),
			ItemStack.OPTIONAL_CODEC.fieldOf("feet").forGetter(PocketArmorStandComponent::feet)
	).apply(inst, PocketArmorStandComponent::new));

	public static final PacketCodec<RegistryByteBuf, PocketArmorStandComponent> PACKET_CODEC = PacketCodec.tuple(
			ItemStack.OPTIONAL_PACKET_CODEC,
			PocketArmorStandComponent::head,
			ItemStack.OPTIONAL_PACKET_CODEC,
			PocketArmorStandComponent::chest,
			ItemStack.OPTIONAL_PACKET_CODEC,
			PocketArmorStandComponent::legs,
			ItemStack.OPTIONAL_PACKET_CODEC,
			PocketArmorStandComponent::feet,
			PocketArmorStandComponent::new
	);
	
	public PocketArmorStandComponent withHead(ItemStack value) {
		return new PocketArmorStandComponent(value, chest(), legs(), feet());
	}

	public PocketArmorStandComponent withChest(ItemStack value) {
		return new PocketArmorStandComponent(head(), value, legs(), feet());
	}

	public PocketArmorStandComponent withLegs(ItemStack value) {
		return new PocketArmorStandComponent(head(), chest(), value, feet());
	}

	public PocketArmorStandComponent withFeet(ItemStack value) {
		return new PocketArmorStandComponent(head(), chest(), legs(), value);
	}
	
	public ItemStack getStack(String name) {
		return switch (name) {
			case "head" -> head();
			case "chest" -> chest();
			case "legs" -> legs();
			case "feet" -> feet();
			default -> throw new IllegalStateException();
		};
	}

	public PocketArmorStandComponent withStack(String name, ItemStack stack) {
		return switch (name) {
			case "head" -> withHead(stack);
			case "chest" -> withChest(stack);
			case "legs" -> withLegs(stack);
			case "feet" -> withFeet(stack);
			default -> throw new IllegalStateException();
		};
	}
}
