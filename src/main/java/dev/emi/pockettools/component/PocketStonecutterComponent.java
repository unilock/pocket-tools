package dev.emi.pockettools.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.emi.pockettools.PocketToolsMain;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record PocketStonecutterComponent(ItemStack base, int offset) {
	public static final PocketStonecutterComponent DEFAULT = new PocketStonecutterComponent(ItemStack.EMPTY, 0);

	public static final Codec<PocketStonecutterComponent> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			ItemStack.CODEC.optionalFieldOf("base", ItemStack.EMPTY).forGetter(PocketStonecutterComponent::base),
			Codec.INT.optionalFieldOf("offset", 0).forGetter(PocketStonecutterComponent::offset)
	).apply(inst, PocketStonecutterComponent::new));

	public static final PacketCodec<RegistryByteBuf, PocketStonecutterComponent> PACKET_CODEC = PacketCodec.tuple(
			ItemStack.PACKET_CODEC,
			PocketStonecutterComponent::base,
			PacketCodecs.INTEGER,
			PocketStonecutterComponent::offset,
			PocketStonecutterComponent::new
	);

	public static void applyBase(ItemStack stack, ItemStack value) {
		stack.apply(PocketToolsMain.POCKET_STONECUTTER_DATA, DEFAULT, value, PocketStonecutterComponent::withBase);
	}

	public static void applyOffset(ItemStack stack, int value) {
		stack.apply(PocketToolsMain.POCKET_STONECUTTER_DATA, DEFAULT, value, PocketStonecutterComponent::withOffset);
	}

	private PocketStonecutterComponent withBase(ItemStack value) {
		return new PocketStonecutterComponent(value, offset);
	}

	private PocketStonecutterComponent withOffset(int value) {
		return new PocketStonecutterComponent(base, value);
	}
}
