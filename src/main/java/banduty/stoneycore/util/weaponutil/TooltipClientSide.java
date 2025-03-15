package banduty.stoneycore.util.weaponutil;

import banduty.stoneycore.event.KeyInputHandler;
import net.minecraft.text.Text;

import java.util.List;

public class TooltipClientSide {
    public static void setTooltip(List<Text> tooltip) {
        tooltip.add(Text.translatable("text.tooltip.stoneycore.need_to_hold",
                KeyInputHandler.reload.getBoundKeyLocalizedText()));
    }
}
