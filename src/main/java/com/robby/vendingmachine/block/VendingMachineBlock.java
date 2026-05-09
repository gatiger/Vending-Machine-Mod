package com.robby.vendingmachine.block;

import com.robby.vendingmachine.blockentity.VendingMachineBlockEntity;
import com.robby.vendingmachine.menu.VendingMachineAdminMenu;
import com.robby.vendingmachine.menu.VendingMachineMenu;
import com.robby.vendingmachine.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class VendingMachineBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    public VendingMachineBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(HALF, DoubleBlockHalf.LOWER)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos clickedPos = context.getClickedPos();
        Level level = context.getLevel();

        if (clickedPos.getY() < level.getMaxBuildHeight() - 1
                && level.getBlockState(clickedPos.above()).canBeReplaced(context)) {

            Direction facing = context.getHorizontalDirection().getOpposite();

            return this.defaultBlockState()
                    .setValue(FACING, facing)
                    .setValue(HALF, DoubleBlockHalf.LOWER);
        }

        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide) {
            BlockState upperState = state.setValue(HALF, DoubleBlockHalf.UPPER);
            level.setBlock(pos.above(), upperState, 3);

            if (level.getBlockEntity(pos) instanceof VendingMachineBlockEntity blockEntity) {
                blockEntity.loadFromItemStack(stack, level.registryAccess());

                // If this is a brand-new machine with no saved owner, assign the placer as owner.
                if (!blockEntity.hasOwner() && placer instanceof Player player) {
                    blockEntity.setOwner(player);
                }

                blockEntity.setChanged();
            }
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        DoubleBlockHalf half = state.getValue(HALF);

        if (half == DoubleBlockHalf.UPPER) {
            BlockState lowerState = level.getBlockState(pos.below());
            return lowerState.is(this) && lowerState.getValue(HALF) == DoubleBlockHalf.LOWER;
        }

        return true;
    }

    @Override
    public BlockState updateShape(
            BlockState state,
            Direction direction,
            BlockState neighborState,
            LevelAccessor level,
            BlockPos pos,
            BlockPos neighborPos
    ) {
        DoubleBlockHalf half = state.getValue(HALF);

        if (half == DoubleBlockHalf.UPPER && direction == Direction.DOWN) {
            if (!neighborState.is(this) || neighborState.getValue(HALF) != DoubleBlockHalf.LOWER) {
                return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
            }
        }

        if (half == DoubleBlockHalf.LOWER && direction == Direction.UP) {
            if (!neighborState.is(this) || neighborState.getValue(HALF) != DoubleBlockHalf.UPPER) {
                return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
            }
        }

        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            DoubleBlockHalf half = state.getValue(HALF);
            BlockPos lowerPos = half == DoubleBlockHalf.UPPER ? pos.below() : pos;
            BlockPos otherHalfPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();

            if (!player.isCreative()
                    && level.getBlockEntity(lowerPos) instanceof VendingMachineBlockEntity blockEntity) {

                ItemStack droppedMachine = new ItemStack(this.asItem());
                blockEntity.saveToItemStack(droppedMachine, level.registryAccess());

                popResource(level, lowerPos, droppedMachine);
            }

            BlockState otherHalfState = level.getBlockState(otherHalfPos);

            if (otherHalfState.is(this)) {
                level.setBlock(otherHalfPos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 35);
            }
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        openCustomerMenu(state, level, pos, player);
        return InteractionResult.CONSUME;
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        if (stack.is(ModItems.ADMIN_TOOL.get())) {
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }

            openAdminMenu(state, level, pos, player);
            return ItemInteractionResult.CONSUME;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private void openCustomerMenu(BlockState state, Level level, BlockPos pos, Player player) {
        BlockPos lowerPos = getLowerPos(state, pos);

        if (level.getBlockEntity(lowerPos) instanceof VendingMachineBlockEntity blockEntity
                && player instanceof ServerPlayer serverPlayer) {

            serverPlayer.openMenu(
                    new SimpleMenuProvider(
                            (containerId, playerInventory, openedPlayer) ->
                                    new VendingMachineMenu(containerId, playerInventory, blockEntity),
                            Component.translatable("menu.vendingmachine.vending_machine")
                    ),
                    buffer -> buffer.writeBlockPos(lowerPos)
            );
        }
    }

    private void openAdminMenu(BlockState state, Level level, BlockPos pos, Player player) {
        BlockPos lowerPos = getLowerPos(state, pos);

        if (level.getBlockEntity(lowerPos) instanceof VendingMachineBlockEntity blockEntity
                && player instanceof ServerPlayer serverPlayer) {

            // Old machines from before ownership existed can be claimed by the first admin-tool user.
            if (!blockEntity.hasOwner()) {
                blockEntity.setOwner(player);
            }

            if (!blockEntity.canPlayerAdmin(player)) {
                String ownerName = blockEntity.getOwnerName();

                if (ownerName == null || ownerName.isBlank()) {
                    ownerName = "another player";
                }

                player.displayClientMessage(
                        Component.literal("This vending machine belongs to " + ownerName + "."),
                        true
                );
                return;
            }

            serverPlayer.openMenu(
                    new SimpleMenuProvider(
                            (containerId, playerInventory, openedPlayer) ->
                                    new VendingMachineAdminMenu(containerId, playerInventory, blockEntity),
                            Component.translatable("menu.vendingmachine.vending_machine_admin")
                    ),
                    buffer -> buffer.writeBlockPos(lowerPos)
            );
        }
    }

    private BlockPos getLowerPos(BlockState state, BlockPos pos) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return new VendingMachineBlockEntity(pos, state);
        }

        return null;
    }
}