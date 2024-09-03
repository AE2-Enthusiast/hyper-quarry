package stone.hyperquarry;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;
import net.minecraftforge.fml.server.FMLServerHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.12.2]")
public class HyperQuarry {
    public static final Logger LOGGER = LogManager.getLogger(Tags.MODID);

    public MainSampler sampler;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START)
            sampler.tick();
    }

    @EventHandler
    public void onFMLServerStarting(FMLServerStartingEvent event) {
        try
        {
            this.sampler = new MainSampler();
        } catch (FileNotFoundException e)
        {
            FMLServerHandler.instance().getServer().stopServer();
        }
    }

    @EventHandler
    public void onFMLServerStopped(FMLServerStoppedEvent event) {
        // this.sampler.stop();
    }
}
