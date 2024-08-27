package stone.hyperquarry;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;

import it.unimi.dsi.fastutil.ints.IntSortedSet;
import net.minecraft.block.Block;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.DimensionType;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.server.FMLServerHandler;

public class SaveSampler {

    WorldSampler[] samplers;
    int time = 0;

    public SaveSampler(MinecraftServer server) {
        int total = 0;
        Map<DimensionType, IntSortedSet> dimensions = DimensionManager.getRegisteredDimensions();
        for (var dimTypes : dimensions.entrySet()) {
            total += dimTypes.getValue().size();
        }
        samplers = new WorldSampler[total];
        int i = 0;
        for (var dimTypes : dimensions.entrySet()) {
            for (int dimID : dimTypes.getValue()) {
                samplers[i] = new WorldSampler(server.getWorld(dimID), dimID);
                i++;
            }
        }
    }

    public void tick() {
        long total = 0;
        time++;
        boolean isDone = true;
        for (WorldSampler sampler : samplers) {
            sampler.tick();
            total += sampler.total;
            isDone &= sampler.isDone;
        }
        if (isDone) {
            HyperQuarry.LOGGER.info("All samplers finished in {} blocks", total);
            FMLServerHandler.instance().getServer().stopServer();
        } else if (time % (20 * 15) == 0)
            HyperQuarry.LOGGER.info("Blocks Sampled: {}", total);

    }

    public void stop() {
        File root = FMLServerHandler.instance().getSavesDirectory();
        for (WorldSampler sampler : samplers) {
            try (OutputStream output = new FileOutputStream(new File(root, "stats_" + sampler.id + ".dat"))) {
                sampler.stop(output);
            } catch (IOException e) {
                HyperQuarry.LOGGER.error("Exception while creating outputstream for DIM {}", sampler.id);
            }
        }

        try (PrintWriter output = new PrintWriter(new File(root, "stats_map.txt"))) {
            for (int i = 0; i < 4096; i++) {
                Block.getBlockById(i);
                output.print(Block.getBlockById(i).getRegistryName().toString());
                output.print(" - ");
                output.println(i);
            }
            output.flush();
        } catch (IOException e) {
            HyperQuarry.LOGGER.error("Exception while outputting ID mappings!");
        }
    }

}
