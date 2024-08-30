package stone.hyperquarry;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.server.FMLServerHandler;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class WorldSampler {

    private final WorldServer world;
    private final Random rand;
    public final int id;
    private long start;
    public long total = 0;
    long target = 10_000_000l;
    public boolean isDone = false;
    public Map<Block, long[]> stateCounts = new HashMap<>();

    private int chunkX = 0;
    private int chunkZ = 0;
    private int dX = 1;
    private int dZ = 0;
    private int length = 0;
    
    public WorldSampler(int id) {
        this.world = getWorld(id);
        this.id = id;
        this.rand = new Random(world.getSeed());
        this.start = System.currentTimeMillis();
    }

    public WorldServer getWorld(int dimension) {
        WorldServer ret = net.minecraftforge.common.DimensionManager.getWorld(dimension, true);
        if (ret == null)
        {
            net.minecraftforge.common.DimensionManager.initDimension(dimension);
            ret = net.minecraftforge.common.DimensionManager.getWorld(dimension);
        }
        return ret;
    }

    public void tick() {
        if (isDone)
            return;
        if (total > target) {
            long max = Integer.MIN_VALUE;
            long min = Integer.MAX_VALUE;
            for (var entry : stateCounts.entrySet()) {
                for (long count : entry.getValue()) {
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
            if (maxRTarget > total) {
                isDone = false;
                target = (long) maxRTarget;
            }
            if (maxITarget > total) {
                isDone = false;
                target = (long) maxITarget;
            }
            if (minRTarget > total) {
                isDone = false;
                target = (long) minRTarget;
            }
            if (minITarget > total) {
                isDone = false;
                target = (long) minITarget;
            }

            if (total > 10_000_000_000l)
                isDone = true;
            if (target > 10_000_000_000l)
                target = 10_000_000_000l;

            if (isDone) {
                HyperQuarry.LOGGER.info("DIM {} finished sampling in {} blocks", id, total);
                this.stop();
            } else {
                long diff = target - total;
                long current = System.currentTimeMillis();
                double mpb = (double) (current - this.start) / total;
                long seconds = (long) (diff * mpb / 1000);
                HyperQuarry.LOGGER.info("DIM {} targetting new count {}", id, target);
                HyperQuarry.LOGGER.info("ETA {}:{}:{}", seconds / 3600, seconds % 3600 / 60, seconds % 60);
            }
            
        }
        
        world.getChunk(chunkX + 1, chunkZ + 1);
        world.getChunk(chunkX + 1, chunkZ);
        world.getChunk(chunkX + 1, chunkZ - 1);
        world.getChunk(chunkX, chunkZ + 1);
        world.getChunk(chunkX, chunkZ - 1);
        world.getChunk(chunkX - 1, chunkZ + 1);
        world.getChunk(chunkX - 1, chunkZ);
        world.getChunk(chunkX - 1, chunkZ + 1);

        Chunk sample = world.getChunk(chunkX, chunkZ);
        /*
        for (var list : sample.getEntityLists()) {
            if (!list.isEmpty())
            list.clear();
        }
        */

        //world.getChunkProvider().loadedChunks.clear();
        
        ExtendedBlockStorage[] subchunks = sample.getBlockStorageArray();

        for (ExtendedBlockStorage subchunk : subchunks) {
            if (subchunk == null)
                continue;
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 0; y < 16; y++) {
                        IBlockState key = subchunk.get(x, y, z);
                        if (key.getBlock() == Blocks.AIR) {
                            continue;
                        }
                        long[] counts = stateCounts.computeIfAbsent(key.getBlock(), ($) -> new long[16]);
                        int meta = key.getBlock().getMetaFromState(key);
                        if (meta > 16 || meta < 0) {
                            Block block = key.getBlock();
                            HyperQuarry.LOGGER.warn("Block ID {} ({}:{}) had a metadata outside of accepted values!", Block.getIdFromBlock(block), Block.REGISTRY.getNameForObject(block).toString(), meta); 
                        } else {
                            total++;
                            counts[meta]++;
                        }
                    }
                }
            }
        }

        chunkX += dX;
        chunkZ += dZ;
        
        if (dX == 1 && chunkX > length) {
            dX = 0;
            dZ = 1;
        } else if (dZ == 1 && chunkZ > length) {
            dX = -1;
            dZ = 0;
            length = -(length + 1);
        } else if (dX == -1 && chunkX < length) {
            dX = 0;
            dZ = -1;
        } else if (dZ == -1 && chunkZ < length) {
            dX = 1;
            dZ = 0;
            length = (-length + 1);
        }
            
    }

    private int getRandomChunkCoord() {
        int coord = this.rand.nextInt(30000000 / 16);
        return this.rand.nextBoolean() ? coord : -coord;
    }


    boolean isWritten = false;
    public void stop() {
        if (!isWritten)
        {
            File root = FMLServerHandler.instance().getSavesDirectory();
            try (PrintWriter output = new PrintWriter(new File(root, "stats_" + this.id + ".txt")))
        {
                for (var entry : this.stateCounts.entrySet())
            {
                long[] counts = entry.getValue();
                Block block = entry.getKey();
                for (int i = 0; i < 16; i++)
                {
                    if (counts[i] > 0)
                    {
                        output.print(block.getRegistryName().toString());
                        output.print(':');
                        output.print(i);
                        output.print("-");
                        output.println(counts[i]);
                    }
                }
            }
            output.flush();
        } catch (IOException e) {
            HyperQuarry.LOGGER.error("Exception while outputting DIM {} counts!", this.id);
        }
        isWritten = true;
    }
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
