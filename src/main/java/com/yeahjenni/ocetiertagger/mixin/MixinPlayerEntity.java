package com.yeahjenni.ocetiertagger.mixin;

import com.yeahjenni.ocetiertagger.ocetiertagger;
import com.yeahjenni.ocetiertagger.TierCache;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class MixinPlayerEntity {

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    private void onGetDisplayName(CallbackInfoReturnable<Text> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        MinecraftClient client = MinecraftClient.getInstance();

        if (player.isInvisible()) return;

        boolean isLocalPlayer = player.equals(client.player);

        if (isLocalPlayer) {
            String gameMode = ocetiertagger.getManager().getConfig().getGameMode();
            String username = player.getName().getString();
            TierCache.getPlayerTier(username, gameMode);
        }

        cir.setReturnValue(ocetiertagger.appendTier(player, cir.getReturnValue()));
    }
}
