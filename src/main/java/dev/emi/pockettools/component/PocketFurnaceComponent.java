package dev.emi.pockettools.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.dynamic.Codecs;

public record PocketFurnaceComponent(ItemStack input, ItemStack fuel, ItemStack output, int fuelTime, int cookTime, int maxFuelTime, int maxCookTime) {
	public static final PocketFurnaceComponent DEFAULT = new PocketFurnaceComponent(ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, 0, 0, 0, 0);

	public static final Codec<PocketFurnaceComponent> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			ItemStack.OPTIONAL_CODEC.fieldOf("input").forGetter(PocketFurnaceComponent::input),
			ItemStack.OPTIONAL_CODEC.fieldOf("fuel").forGetter(PocketFurnaceComponent::fuel),
			ItemStack.OPTIONAL_CODEC.fieldOf("output").forGetter(PocketFurnaceComponent::output),
			Codecs.NONNEGATIVE_INT.optionalFieldOf("fuelTime", 0).forGetter(PocketFurnaceComponent::fuelTime),
			Codecs.NONNEGATIVE_INT.optionalFieldOf("cookTime", 0).forGetter(PocketFurnaceComponent::cookTime),
			Codecs.NONNEGATIVE_INT.optionalFieldOf("maxFuelTime", 0).forGetter(PocketFurnaceComponent::maxFuelTime),
			Codecs.NONNEGATIVE_INT.optionalFieldOf("maxCookTime", 0).forGetter(PocketFurnaceComponent::maxCookTime)
	).apply(inst, PocketFurnaceComponent::new));

	public static final PacketCodec<RegistryByteBuf, PocketFurnaceComponent> PACKET_CODEC = new PacketCodec<RegistryByteBuf, PocketFurnaceComponent>() {
		@Override
		public PocketFurnaceComponent decode(RegistryByteBuf buf) {
			ItemStack input = ItemStack.OPTIONAL_PACKET_CODEC.decode(buf);
			ItemStack fuel = ItemStack.OPTIONAL_PACKET_CODEC.decode(buf);
			ItemStack output = ItemStack.OPTIONAL_PACKET_CODEC.decode(buf);
			int fuelTime = PacketCodecs.INTEGER.decode(buf);
			int cookTime = PacketCodecs.INTEGER.decode(buf);
			int maxFuelTime = PacketCodecs.INTEGER.decode(buf);
			int maxCookTime = PacketCodecs.INTEGER.decode(buf);
			return new PocketFurnaceComponent(input, fuel, output, fuelTime, cookTime, maxFuelTime, maxCookTime);
		}

		@Override
		public void encode(RegistryByteBuf buf, PocketFurnaceComponent value) {
			ItemStack.OPTIONAL_PACKET_CODEC.encode(buf, value.input());
			ItemStack.OPTIONAL_PACKET_CODEC.encode(buf, value.fuel());
			ItemStack.OPTIONAL_PACKET_CODEC.encode(buf, value.output());
			PacketCodecs.INTEGER.encode(buf, value.fuelTime());
			PacketCodecs.INTEGER.encode(buf, value.cookTime());
			PacketCodecs.INTEGER.encode(buf, value.maxFuelTime());
			PacketCodecs.INTEGER.encode(buf, value.maxCookTime());
		}
	};
	
	public PocketFurnaceComponent withInput(ItemStack value) {
		return new PocketFurnaceComponent(value, fuel(), output(), fuelTime(), cookTime(), maxFuelTime(), maxCookTime());
	}

	public PocketFurnaceComponent withFuel(ItemStack value) {
		return new PocketFurnaceComponent(input(), value, output(), fuelTime(), cookTime(), maxFuelTime(), maxCookTime());
	}

	public PocketFurnaceComponent withOutput(ItemStack value) {
		return new PocketFurnaceComponent(input(), fuel(), value, fuelTime(), cookTime(), maxFuelTime(), maxCookTime());
	}

	public PocketFurnaceComponent withFuelTime(int value) {
		return new PocketFurnaceComponent(input(), fuel(), output(), value, cookTime(), maxFuelTime(), maxCookTime());
	}

	public PocketFurnaceComponent withCookTime(int value) {
		return new PocketFurnaceComponent(input(), fuel(), output(), fuelTime(), value, maxFuelTime(), maxCookTime());
	}

	public PocketFurnaceComponent withMaxFuelTime(int value) {
		return new PocketFurnaceComponent(input(), fuel(), output(), fuelTime(), cookTime(), value, maxCookTime());
	}

	public PocketFurnaceComponent withMaxCookTime(int value) {
		return new PocketFurnaceComponent(input(), fuel(), output(), fuelTime(), cookTime(), maxFuelTime(), value);
	}
}
