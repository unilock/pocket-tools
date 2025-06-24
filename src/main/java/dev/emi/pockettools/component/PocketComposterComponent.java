package dev.emi.pockettools.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.emi.pockettools.PocketToolsMain;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.dynamic.Codecs;

public record PocketComposterComponent(int fill, int compost) {
	public static final PocketComposterComponent DEFAULT = new PocketComposterComponent(0, 20);

	public static final Codec<PocketComposterComponent> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Codecs.NONNEGATIVE_INT.optionalFieldOf("fill", 0).forGetter(PocketComposterComponent::fill),
			Codecs.NONNEGATIVE_INT.optionalFieldOf("compost", 0).forGetter(PocketComposterComponent::compost)
	).apply(inst, PocketComposterComponent::new));

	public static final PacketCodec<RegistryByteBuf, PocketComposterComponent> PACKET_CODEC = PacketCodec.tuple(
			PacketCodecs.INTEGER,
			PocketComposterComponent::fill,
			PacketCodecs.INTEGER,
			PocketComposterComponent::compost,
			PocketComposterComponent::new
	);

	public static void applyFill(ItemStack stack, int value) {
		stack.apply(PocketToolsMain.POCKET_COMPOSTER_DATA, DEFAULT, value, PocketComposterComponent::withFill);
	}

	public static void applyCompost(ItemStack stack, int value) {
		stack.apply(PocketToolsMain.POCKET_COMPOSTER_DATA, DEFAULT, value, PocketComposterComponent::withCompost);
	}

	private PocketComposterComponent withFill(int value) {
		return new PocketComposterComponent(value, compost());
	}

	private PocketComposterComponent withCompost(int value) {
		return new PocketComposterComponent(fill(), value);
	}
}
