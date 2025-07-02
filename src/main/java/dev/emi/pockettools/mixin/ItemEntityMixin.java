package dev.emi.pockettools.mixin;

import dev.emi.pockettools.PocketToolsMain;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
	@Shadow
	public abstract void setDespawnImmediately();

	@Shadow
	public abstract void setStack(ItemStack stack);

	@Inject(at = @At("HEAD"), method = "setStack")
	public void setStack(ItemStack stack, CallbackInfo info) {
		if (stack.getItem() == PocketToolsMain.POCKET_END_PORTAL) {
			if (stack.contains(PocketToolsMain.POCKET_END_PORTAL_DATA)) {
				var data = stack.get(PocketToolsMain.POCKET_END_PORTAL_DATA);
				if (data.portal()) {
					setDespawnImmediately();
				} else if (data.filled()) {
					stack.remove(PocketToolsMain.POCKET_END_PORTAL_DATA);
				}
			}
		}
	}
}
