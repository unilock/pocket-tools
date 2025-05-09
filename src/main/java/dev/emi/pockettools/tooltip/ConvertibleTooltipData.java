package dev.emi.pockettools.tooltip;

import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.item.tooltip.TooltipData;

public interface ConvertibleTooltipData extends TooltipData {
	TooltipComponent getComponent();
}
