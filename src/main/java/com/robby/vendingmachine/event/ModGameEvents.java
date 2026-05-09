package com.robby.vendingmachine.event;

import com.robby.vendingmachine.VendingMachineMod;
import com.robby.vendingmachine.block.VendingMachineBlock;
import com.robby.vendingmachine.blockentity.VendingMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

@EventBusSubscriber(
        modid = VendingMachineMod.MOD_ID,
        bus = EventBusSubscriber.Bus.GAME
)
public class ModGameEvents {
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        Level level = player.level();
        BlockPos brokenPos = event.getPos();
        BlockState brokenState = event.getState();

        if (!(brokenState.getBlock() instanceof VendingMachineBlock)) {
            return;
        }

        BlockPos lowerPos = brokenState.getValue(VendingMachineBlock.HALF) == DoubleBlockHalf.UPPER
                ? brokenPos.below()
                : brokenPos;

        if (!(level.getBlockEntity(lowerPos) instanceof VendingMachineBlockEntity blockEntity)) {
            return;
        }

        if (blockEntity.canPlayerAdmin(player)) {
            return;
        }

        event.setCanceled(true);

        if (!level.isClientSide) {
            String ownerName = blockEntity.getOwnerName();

            if (ownerName == null || ownerName.isBlank()) {
                ownerName = "another player";
            }

            player.displayClientMessage(
                    Component.literal("You cannot break this vending machine. It belongs to " + ownerName + "."),
                    true
            );
        }
    }
}