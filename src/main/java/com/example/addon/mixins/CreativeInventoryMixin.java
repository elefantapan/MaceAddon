package com.example.addon.mixins;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.ScreenHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.Minecraft;

@Mixin(CreativeModeInventoryScreen.class)
public class CreativeInventoryMixin {

    @Inject(
        method = "slotClicked",
        at = @At("HEAD"),
        cancellable = true
    )
    private void fakeCreativeClick(
        Slot slot,
        int slotId,
        int button,
        AbstractContainerMenu.ClickType clickType,
        CallbackInfoReturnable<Boolean> cir
    ) {

        Minecraft mc = Minecraft.getInstance();

        if (mc.player == null) return;

        /*
         * Don't allow real creative packets.
         * Creative inventory packets create ghost items.
         *
         * Instead, only allow items already owned.
         */

        if (slot != null && !slot.hasItem()) {
            cir.setReturnValue(false);
            return;
        }

        ItemStack clicked = slot.getItem();

        boolean found = false;

        for (ItemStack stack : mc.player.getInventory().items) {
            if (ItemStack.isSameItemSameComponents(stack, clicked)) {
                found = true;
                break;
            }
        }

        if (!found) {
            cir.setReturnValue(false);
        }
    }
}
