package stone.hyperquarry;

import it.unimi.dsi.fastutil.ints.IntSortedSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.server.FMLServerHandler;

import java.io.File;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public class SaveSampler {

    int[] samplers;
    int time = 0;
    Set<Integer> sampledDimensions = new TreeSet<>();

    public SaveSampler(MinecraftServer server) {
        int total = 0;

        File root = FMLServerHandler.instance().getSavesDirectory();
        for (File file : root.listFiles())
        {
            String name = file.getName();
            if (name.startsWith("stats_") && name.endsWith(".txt"))
            {
                sampledDimensions.add(Integer.valueOf(name.substring(6, name.length() - 4)));
            }
        }

        /*
        Map<DimensionType, IntSortedSet> dimensions = DimensionManager.getRegisteredDimensions();
        for (var dimTypes : dimensions.entrySet()) {
            total += dimTypes.getValue().size();
        }
        samplers = new int[total];
        int i = 0;
        
        
        for (var dimTypes : dimensions.entrySet()) {
            for (int dimID : dimTypes.getValue()) {
                samplers[i] = dimID;
                i++;
            }
        }
        */
        samplers = new int[] {-1, 0, 1};
        selectNewSampler();
        
    }

    private long total = 0;

    private int index = -1;
    private double samplerSpeed = 1;

    private WorldSampler sampler;

    public void tick() {
        long tickTime = mean(FMLServerHandler.instance().getServer().tickTimeArray);

        total -= sampler.total;
        long start = System.nanoTime();
        for (int i = 0; i < samplerSpeed; i++)
            sampler.tick();
        double genTime = (System.nanoTime() - start) / (samplerSpeed == 0 ? 1 : samplerSpeed);
        total += sampler.total;

        samplerSpeed += ((49_000_000d - tickTime) / (genTime == 0 ? 1 : genTime)) / 100;
        if (time < 10 && samplerSpeed > 10)
            samplerSpeed = 10;
        if (samplerSpeed < 1)
            samplerSpeed = 1;

        boolean isDone = false;
        if (sampler.isDone) {
            isDone = selectNewSampler();
        }
        if (isDone) {
            HyperQuarry.LOGGER.info("All samplers finished in {} blocks", total);
            this.stop();
            FMLServerHandler.instance().getServer().stopServer();
        } else if (time % (20 * 15) == 0) {
            HyperQuarry.LOGGER.info("Blocks Sampled: {}", total);

            HyperQuarry.LOGGER
                .info("Sampling at {} chunks a tick in DIM {}, sampled {} / {} blocks",
                    samplerSpeed, sampler.id, sampler.total, sampler.target);
        }
        this.time++;
    }

    private boolean selectNewSampler() {
        do
        {
            index++;
        } while (index < samplers.length && sampledDimensions.contains(samplers[index]));
        samplerSpeed = 1;
        if (index >= samplers.length)
        {
            return true;
        } else
        {
            this.sampler = new WorldSampler(samplers[index]);
            return false;
        }
    }

    public void stop() {
        sampler.stop();
    }

    private static long mean(long[] values)
    {
        long sum = 0L;
        for (long v : values)
        {
            sum += v;
        }
        return sum / values.length;
    }

}
