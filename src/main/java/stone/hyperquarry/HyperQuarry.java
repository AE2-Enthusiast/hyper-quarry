package stone.hyperquarry;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = Tags.MODID, version = Tags.VERSION, name = Tags.MODNAME, acceptedMinecraftVersions = "[1.12.2]")
public class HyperQuarry {
    public static final Logger LOGGER = LogManager.getLogger(Tags.MODID);
    
    public static final BlockQuarry QUARRY = new BlockQuarry();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        LOGGER.info("Registering Blocks!");
        event.getRegistry().register(QUARRY);

    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> evt) {
        IForgeRegistry<Item> r = evt.getRegistry();
        r
            .register(new ItemQuarry(QUARRY));
        GameRegistry.registerTileEntity(TileEntityQuarry.class, toLocation("quarry"));
    }

    public static ResourceLocation toLocation(String path) {
        return new ResourceLocation(Tags.MODID, path);
    }

}
