package stone.hyperquarry;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import stone.hyperquarry.common.BlockQuarry;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.12.2]")
public class HyperQuarry {
    public static final Logger LOGGER = LogManager.getLogger(Tags.MODID);
    
    public static final BlockQuarry QUARRY = new BlockQuarry();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        if (event.getSide() == Side.CLIENT) {
        	Client client = new Client();
            MinecraftForge.EVENT_BUS.register(client);
            client.preInit(event);
        } else {
        	Server server = new Server();
            MinecraftForge.EVENT_BUS.register(server);
            server.preInit(event);
        }
    }
    


    public static ResourceLocation toLocation(String path) {
        return new ResourceLocation(Tags.MODID, path);
    }

}
