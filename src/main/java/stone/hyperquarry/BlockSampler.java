package stone.hyperquarry;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class BlockSampler {

    private final long start = System.currentTimeMillis();

    static final WorldServer workDim = getWorld(0);
    private static final BlockPos pos = new BlockPos(0, 128, 0);
    final IBlockState state;
    final Block block;

    private static final NonNullList<ItemStack> buffer = NonNullList.create();
    Map<Item, Int2ObjectMap<MutableLong>> stackCounts = new HashMap<>();

    long total = 0;
    long target = 1_000;
    boolean isDone = false;

    public BlockSampler(IBlockState state) {
        this.state = state;
        this.block = state.getBlock();
    }

    public void tick() {
        workDim.setBlockState(pos, state, 0b0000);
        try
        {
            if (!block.canSilkHarvest(workDim, pos, state, null))
                return;
        } catch (NullPointerException e)
        {
            HyperQuarry.LOGGER
                .warn("Blockstate {} threw a NPE while determining silk harvestibility",
                    this.state);
            return;
        }
        Item item;
        int meta = 0;
        if (!(state.getBlockHardness(workDim, pos) >= 0))
        {
            item = Items.AIR;
        } else
        {
            item = Item.getItemFromBlock(this.block);
            if (item.getHasSubtypes())
            {
                meta = this.block.getMetaFromState(state);
            }
        }



        stackCounts
            .computeIfAbsent(item, ($) -> new Int2ObjectOpenHashMap<>())
            .computeIfAbsent(meta, ($) -> new MutableLong()).value = 1;

        buffer.forEach((stack) -> {
            ItemStack result = FurnaceRecipes.instance().getSmeltingResult(stack);
            if (result != ItemStack.EMPTY)
                stack = result;
            if (stack.hasTagCompound()) {
                HyperQuarry.LOGGER
                    .warn("Stack {} has nbt data, statistics are potentially wrong!",
                        stack.toString());
            } else
            {
            stackCounts
                .computeIfAbsent(stack.getItem(),
                    ($) -> new Int2ObjectOpenHashMap<>())
                    .computeIfAbsent(stack.getMetadata(), ($) -> new MutableLong()).value += stack
                        .getCount();
            }
        });
        total++;
        buffer.clear();

        if (total > target)
        {
            long max = Integer.MIN_VALUE;
            long min = Integer.MAX_VALUE;
            for (var entry : stackCounts.entrySet())
            {
                for (Entry<Integer, MutableLong> entry2 : entry.getValue().entrySet())
                {
                    long count = entry2.getValue().value;
                    if (count == 0)
                        continue;
                    if (max < count)
                        max = count;
                    else if (min > count)
                        min = count;
                }
            }

            double maxRatio = (double) max / total;
            double minRatio = (double) min / total;

            double confidence = 10;
            double maxRTarget = confidence / maxRatio;
            double maxITarget = confidence / (1 - maxRatio);
            double minRTarget = confidence / minRatio;
            double minITarget = confidence / (1 - minRatio);

            isDone = true;
            if (maxRTarget > total)
            {
                isDone = false;
                target = (long) maxRTarget;
            }
            if (maxITarget > total)
            {
                isDone = false;
                target = (long) maxITarget;
            }
            if (minRTarget > total)
            {
                isDone = false;
                target = (long) minRTarget;
            }
            if (minITarget > total)
            {
                isDone = false;
                target = (long) minITarget;
            }

            if (maxRatio >= .9999)
                isDone = true;

            if (total > 1_000_000l)
                isDone = true;
            if (target > 1_000_000l)
                target = 1_000_000l;

            if (isDone)
            {
                HyperQuarry.LOGGER.info("Block {} finished sampling in {} blocks", state, total);
            } else
            {
                long diff = target - total;
                long current = System.currentTimeMillis();
                double mpb = (double) (current - this.start) / total;
                long seconds = (long) (diff * mpb / 1000);
                HyperQuarry.LOGGER.info("Block {} targetting new count {}", state, target);
                HyperQuarry.LOGGER
                    .info("ETA {}:{}:{}", seconds / 3600, seconds % 3600 / 60, seconds % 60);
            }
        }
    }

    public static WorldServer getWorld(int dimension) {
        WorldServer ret = net.minecraftforge.common.DimensionManager.getWorld(dimension, true);
        if (ret == null)
        {
            net.minecraftforge.common.DimensionManager.initDimension(dimension);
            ret = net.minecraftforge.common.DimensionManager.getWorld(dimension);
        }
        return ret;
    }

    public static class MutableLong {
        public long value;

        public MutableLong() {
            this.value = 0;
        }

        public MutableLong(long value) {
            this.value = value;
        }
    }
}
