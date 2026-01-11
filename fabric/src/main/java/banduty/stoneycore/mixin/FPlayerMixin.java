package banduty.stoneycore.mixin;

import banduty.stoneycore.event.custom.PlayerNameTagEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(Player.class)
public abstract class FPlayerMixin {
    @Unique
    private final Player playerEntity = (Player)(Object)this;

    @ModifyArg(
            method = "getDisplayName",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;decorateDisplayNameComponent(Lnet/minecraft/network/chat/MutableComponent;)Lnet/minecraft/network/chat/MutableComponent;"
            )
    )
    private MutableComponent stoneycore$addTags(MutableComponent mutableComponent) {
        if (!(playerEntity instanceof ServerPlayer serverPlayer)) {
            return mutableComponent;
        }

        var tags = PlayerNameTagEvents.EVENT.invoker().collectTags(serverPlayer);
        MutableComponent result = Component.empty();
        for (var entry : tags) {
            if (!entry.component().getString().isEmpty()) {
                result = result.append(entry.component()).append(Component.literal(" "));
            }
        }
        return result.append(mutableComponent);
    }
}
