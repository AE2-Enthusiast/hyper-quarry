package stone.hyperquarry;

import java.util.concurrent.atomic.AtomicInteger;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;

public abstract class Proxy {
    public static final SimpleNetworkWrapper NETWORK = new SimpleNetworkWrapper(Tags.MODID);
    public static final AtomicInteger DISCRIMINATOR = new AtomicInteger(0);
}
