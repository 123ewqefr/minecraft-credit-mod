package com.creditmod.block;

import com.creditmod.registry.ModTileEntities;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

public class CreditChestBlock extends Block {

    public CreditChestBlock() {
        super(Properties.of(Material.WOOD)
                .strength(2.5F)
                .sound(SoundType.WOOD)
                .noOcclusion());
    }

    @Override
    public ActionResultType use(BlockState state, World world, BlockPos pos,
                                PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if (!world.isClientSide) {
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof CreditChestTileEntity) {
                NetworkHooks.openGui((ServerPlayerEntity) player,
                        (INamedContainerProvider) te, pos);
            }
        }
        return ActionResultType.sidedSuccess(world.isClientSide);
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new CreditChestTileEntity();
    }

    @Override
    public BlockRenderType getRenderShape(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public void onRemove(BlockState state, World world, BlockPos pos,
                         BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof CreditChestTileEntity) {
                ((CreditChestTileEntity) te).dropContents(world, pos);
            }
            super.onRemove(state, world, pos, newState, isMoving);
        }
    }
}
