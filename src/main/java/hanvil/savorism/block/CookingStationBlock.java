package hanvil.savorism.block;

import com.mojang.serialization.MapCodec;

import hanvil.savorism.block.entity.CookingStationBlockEntity;
import hanvil.savorism.block.entity.SavorismBlockEntities;

import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import eu.pb4.polymer.core.api.utils.PolymerUtils;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import xyz.nucleoid.packettweaker.PacketContext;
import net.minecraft.util.StringIdentifiable;

public final class CookingStationBlock extends HorizontalFacingBlock implements PolymerBlock, BlockEntityProvider, BlockWithElementHolder {
    private static final MapCodec<CookingStationBlock> CODEC = createCodec(CookingStationBlock::new);

    public CookingStationBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state.with(FACING, state.get(FACING)), placer, itemStack);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return super.getPlacementState(ctx).with(Properties.HORIZONTAL_FACING, ctx.getHorizontalPlayerFacing());

    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player,  BlockHitResult hit) {
        var blockEntity = world.getBlockEntity(pos);

        if (blockEntity instanceof CookingStationBlockEntity cookingStationBlock && cookingStationBlock.canPlayerUse(player)) {
            cookingStationBlock.openGui((ServerPlayerEntity) player);
            world.playSound(null,
                    cookingStationBlock.getPos().getX() + 0.5,
                    cookingStationBlock.getPos().getY() + 0.5,
                    cookingStationBlock.getPos().getZ() + 0.5, SoundEvents.BLOCK_BARREL_OPEN, SoundCategory.BLOCKS, 0.5F, world.random.nextFloat() * 0.1F + 0.9F);
            return ActionResult.SUCCESS_SERVER;
        }

        return ActionResult.PASS;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new CookingStationBlockEntity(pos, state);
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        // TODO
        var holder = new ElementHolder();

        return holder;
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return Blocks.SMOKER.getDefaultState().with(SmokerBlock.FACING, state.get(FACING).getOpposite());
    }
}