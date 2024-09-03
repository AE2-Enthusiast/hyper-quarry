package stone.hyperquarry;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.server.FMLServerHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainSampler {

    IBlockState[] samplers;
    Map<IBlockState, BlockSampler> samplerMap = new HashMap<>();
    Int2ObjectMap<Object2LongMap<IBlockState>> dim2StateCounts = new Int2ObjectOpenHashMap<>();
    int time = 0;


    public MainSampler() throws FileNotFoundException {
        File root = FMLServerHandler.instance().getSavesDirectory();
        Pattern pattern = Pattern.compile("(.+?):(.+?):(\\d+?)-(\\d+)");
        Set<IBlockState> blockStates = new HashSet<>();
        for (File file : root.listFiles())
        {
            String name = file.getName();

            if (name.startsWith("stats_") && name.endsWith(".txt"))
            {
                Object2LongMap<IBlockState> stateCounts = new Object2LongOpenHashMap<>();
                Scanner scanner = new Scanner(file);
                while (scanner.hasNextLine())
                {
                    Matcher matcher = pattern.matcher(scanner.nextLine());
                    if (matcher.find())
                    {
                        IBlockState state = Block.REGISTRY
                            .getObject(new ResourceLocation(matcher.group(1), matcher.group(2)))
                            .getStateFromMeta(Integer.valueOf(matcher.group(3)));
                        blockStates
                            .add(state);
                        stateCounts.put(state, Long.valueOf(matcher.group(4)));
                    }
                }
                dim2StateCounts
                    .put(Integer.valueOf(name.substring(6, name.length() - 4)), stateCounts);

            }
        }
        
        this.samplers = new IBlockState[blockStates.size()];
        int i = 0;
        for (IBlockState state : blockStates)
        {
            samplers[i] = state;
                i++;
        }
        selectNewSampler();
    }

    private long total = 0;

    private int index = -1;
    private double samplerSpeed = 1;

    private BlockSampler sampler;

    public void tick() {
        long tickTime = mean(FMLServerHandler.instance().getServer().tickTimeArray);

        total -= sampler.total;
        long start = System.nanoTime();
        for (int i = 0; i < samplerSpeed; i++)
        {
            sampler.tick();
            if (sampler.isDone)
                break;
        }
        BlockSampler.workDim.loadedEntityList.clear();
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
                .info("Sampling at {} blocks a tick for state {}, sampled {} / {} blocks",
                    samplerSpeed, sampler.state, sampler.total, sampler.target);
        }
        this.time++;
    }

    private boolean selectNewSampler() {
            index++;
        if (index >= samplers.length)
        {
            return true;
        } else
        {
            this.sampler = new BlockSampler(samplers[index]);
            samplerMap.put(samplers[index], this.sampler);
            return false;
        }
    }

    public void stop() {
        for (var dimension : dim2StateCounts.entrySet())
        {
            long totalBlocks = 0;
            for (var stateCounts : dimension.getValue().entrySet())
            {
                totalBlocks += stateCounts.getValue();
            }

            File root = FMLServerHandler.instance().getSavesDirectory();
            // relative dropWeight of every item dropped in this dimension
            Map<Item, Int2DoubleMap> dropWeights = new HashMap<>();
            for (var stateCounts : dimension.getValue().entrySet())
            {
                // relative weight of this blockstate in the dimensinn
                double stateWeight = (double) stateCounts.getValue() / totalBlocks;
                BlockSampler sampler = samplerMap.get(stateCounts.getKey());
                for (var dropCount : sampler.stackCounts.entrySet()) {
                    Int2DoubleMap metaWeights = dropWeights
                        .computeIfAbsent(dropCount.getKey(), ($) -> new Int2DoubleOpenHashMap());
                    for (var metaCount : dropCount.getValue().entrySet()) {
                        double metaWeight = metaWeights
                            .computeIfAbsent(metaCount.getKey(), ($) -> 0d);
                        HyperQuarry.LOGGER
                            .info(((double) metaCount.getValue().value / sampler.total)
                                * stateWeight);
                        metaWeight += (metaCount.getValue().value / sampler.total) * stateWeight;
                        metaWeights.put((int) metaCount.getKey(), metaWeight);
                    }
                }
            }
            try (PrintWriter output = new PrintWriter(
                new File(root, "drops_" + dimension.getKey() + ".txt")))
            {
                for (var entry : dropWeights.entrySet())
                {
                    Int2DoubleMap dropWeight = entry.getValue();
                    String name = Item.REGISTRY.getNameForObject(entry.getKey()).toString();
                    for (var metaWeight : dropWeight.entrySet())
                    {
                        output.print(name);
                        output.print(':');
                        output.print(metaWeight.getKey());
                        output.print('-');
                        output.println(metaWeight.getValue());
                    }
                }
        } catch (FileNotFoundException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
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
