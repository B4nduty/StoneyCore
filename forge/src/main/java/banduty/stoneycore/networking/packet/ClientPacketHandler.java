package banduty.stoneycore.networking.packet;

import banduty.stoneycore.util.data.playerdata.StaminaData;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class ClientPacketHandler {
    public static void handleStaminaValue(double stamina) {
        if (Minecraft.getInstance().player != null) {
            StaminaData.setStamina(Minecraft.getInstance().player, stamina);
        }
    }
}