package stone.hyperquarry;

import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;

import stone.hyperquarry.network.PacketFilter;
import stone.hyperquarry.network.PacketOpenFilterGui;
import stone.hyperquarry.network.PacketOpenQuarryGui;

public class Client extends Server {
	@Override
	public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        Proxy.NETWORK
            .registerMessage(new PacketOpenFilterGui.ClientHandler(), PacketFilter.class,
                Proxy.DISCRIMINATOR.getAndIncrement(), Side.CLIENT);
        Proxy.NETWORK
            .registerMessage(new PacketOpenQuarryGui.ClientHandler(), PacketOpenQuarryGui.class,
                Proxy.DISCRIMINATOR.getAndIncrement(), Side.CLIENT);
    }
	
}