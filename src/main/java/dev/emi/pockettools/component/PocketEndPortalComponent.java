package dev.emi.pockettools.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.emi.pockettools.PocketToolsMain;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record PocketEndPortalComponent(boolean filled, boolean portal, boolean tp) {
	public static final PocketEndPortalComponent DEFAULT = new PocketEndPortalComponent(false, false, false);

	public static final Codec<PocketEndPortalComponent> CODEC = RecordCodecBuilder.create(inst -> inst.group(
			Codec.BOOL.fieldOf("filled").forGetter(PocketEndPortalComponent::filled),
			Codec.BOOL.fieldOf("portal").forGetter(PocketEndPortalComponent::portal),
			Codec.BOOL.fieldOf("tp").forGetter(PocketEndPortalComponent::tp)
	).apply(inst, PocketEndPortalComponent::new));

	public static final PacketCodec<RegistryByteBuf, PocketEndPortalComponent> PACKET_CODEC = PacketCodec.tuple(
			PacketCodecs.BOOL,
			PocketEndPortalComponent::filled,
			PacketCodecs.BOOL,
			PocketEndPortalComponent::portal,
			PacketCodecs.BOOL,
			PocketEndPortalComponent::tp,
			PocketEndPortalComponent::new
	);

	public static void applyFilled(ItemStack stack, boolean value) {
		stack.apply(PocketToolsMain.POCKET_END_PORTAL_DATA, DEFAULT, value, PocketEndPortalComponent::withFilled);
	}

	public static void applyTp(ItemStack stack, boolean value) {
		stack.apply(PocketToolsMain.POCKET_END_PORTAL_DATA, DEFAULT, value, PocketEndPortalComponent::withTp);
	}

	private PocketEndPortalComponent withFilled(boolean value) {
		return new PocketEndPortalComponent(value, portal, tp);
	}

	private PocketEndPortalComponent withTp(boolean value) {
		return new PocketEndPortalComponent(filled, portal, value);
	}

	public PocketEndPortalComponent withPortal(boolean value) {
		return new PocketEndPortalComponent(filled, value, tp);
	}
}
