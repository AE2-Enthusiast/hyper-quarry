package stone.hyperquarry;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.fml.server.FMLServerHandler;

public class WorldSampler {

    private final WorldServer world;
    private final Random rand;
    public final int id;
    private long start;
    public int total = 0;
    private int target = 100000;
    public boolean isDone = false;
    public int[] stateCounts = new int[4096 * 16];
    
    public WorldSampler(WorldServer world, int id) {
        this.world = world;
        this.id = id;
        this.rand = new Random(world.getSeed());
        this.start = System.currentTimeMillis();
    }

    public void tick() {
        if (isDone)
            return;
        if (total > target) {
            int max = 0;
            int min = 0;
            for (int i = 0; i < stateCounts.length; i++) {
                int count = stateCounts[i];
                if (count == 0)
                    continue;
                if (stateCounts[max] < count)
                    max = i;
                else if (stateCounts[min] > count)
                    min = i;
            }

            double maxRatio = (double) stateCounts[max] / total;
            double minRatio = (double) stateCounts[min] / total;

            double maxRTarget = 10 / maxRatio;
            double maxITarget = 10 / (1 - maxRatio);
            double minRTarget = 10 / minRatio;
            double minITarget = 10 / (1 - minRatio);

            isDone = true;
            if (maxRTarget > total) {
                isDone = false;
                target = (int) maxRTarget;
            }
            if (maxITarget > total) {
                isDone = false;
                target = (int) maxITarget;
            }
            if (minRTarget > total) {
                isDone = false;
                target = (int) minRTarget;
            }
            if (minITarget > total) {
                isDone = false;
                target = (int) minITarget;
            }

            if (isDone) {
                HyperQuarry.LOGGER.info("DIM {} finished sampling", id);
            } else {
                int diff = target - total;
                long current = System.currentTimeMillis();
                double mpb = (double) (current - this.start) / total;
                int seconds = (int) (diff * mpb / 1000);
                HyperQuarry.LOGGER.info("DIM {} targetting new count {}", id, target);
                HyperQuarry.LOGGER.info("ETA {}:{}:{}", seconds / 3600, seconds % 3600 / 60, seconds % 60);
            }
            
        }
        int chunkX = getRandomChunkCoord();
        int chunkZ = getRandomChunkCoord();
        world.getChunk(chunkX + 1, chunkZ + 1);
        world.getChunk(chunkX + 1, chunkZ);
        world.getChunk(chunkX + 1, chunkZ - 1);
        world.getChunk(chunkX, chunkZ + 1);
        world.getChunk(chunkX, chunkZ - 1);
        world.getChunk(chunkX - 1, chunkZ + 1);
        world.getChunk(chunkX - 1, chunkZ);
        world.getChunk(chunkX - 1, chunkZ + 1);

        Chunk sample = world.getChunk(chunkX, chunkZ);

        world.getChunkProvider().loadedChunks.clear();
        
        ExtendedBlockStorage[] subchunks = sample.getBlockStorageArray();

        for (ExtendedBlockStorage subchunk : subchunks) {
            if (subchunk == null)
                continue;
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    for (int y = 0; y < 16; y++) {
                        total++;
                        stateCounts[Block.getStateId(subchunk.get(x, y, z))]++;
                    }
                }
            }
        }
    }

    private int getRandomChunkCoord() {
        int coord = this.rand.nextInt(30000000 / 16);
        return this.rand.nextBoolean() ? coord : -coord;
    }

    public void stop(OutputStream output) {
        try {
            for (int count : stateCounts) {
                for (int i = 3; i >= 0; i--) {   
                output.write(count >> 8 * i);
            }
            }
        } catch (IOException e) {
            HyperQuarry.LOGGER.error("Exception while writing DIM {} counts!", this.id);
            e.printStackTrace();
        }
        try {
            output.flush();
        } catch (IOException e) {
            HyperQuarry.LOGGER.warn("Exception while flushing DIM {}!", this.id);
        }
    }
}
