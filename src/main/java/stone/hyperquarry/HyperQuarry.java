package stone.hyperquarry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartedEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.12.2]")
public class HyperQuarry {
    public static final Logger LOGGER = LogManager.getLogger(Tags.MODID);
    
    public SaveSampler sampler;

    private int time = 0;
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
        this.sampler = new SaveSampler(event.getServer());
    }

    @EventHandler
    public void onFMLServerStopped(FMLServerStoppedEvent event) {
        this.sampler.stop();
    }
}
