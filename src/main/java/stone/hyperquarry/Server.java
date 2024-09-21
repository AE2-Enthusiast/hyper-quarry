package stone.hyperquarry;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.registries.IForgeRegistry;

import stone.hyperquarry.common.ItemQuarry;
import stone.hyperquarry.common.TileEntityQuarry;
import stone.hyperquarry.network.PacketOpenFilterGui;
import stone.hyperquarry.network.PacketSetMask;
import stone.hyperquarry.network.PacketSetQuarryState;

public class Server {
    public void preInit(FMLPreInitializationEvent event) {
        Proxy.NETWORK
            .registerMessage(new PacketSetMask.ServerHandler(), PacketSetMask.class, Proxy.DISCRIMINATOR.getAndIncrement(),
                Side.SERVER);
        
        Proxy.NETWORK.registerMessage(new PacketOpenFilterGui.ServerHandler(), PacketOpenFilterGui.class, Proxy.DISCRIMINATOR.getAndIncrement(), Side.SERVER);

        Proxy.NETWORK
            .registerMessage(new PacketSetMask.ServerHandler(), PacketSetMask.class,
                Proxy.DISCRIMINATOR.getAndIncrement(), Side.SERVER);

        Proxy.NETWORK
            .registerMessage(new PacketSetQuarryState.ServerHandler(), PacketSetQuarryState.class,
                Proxy.DISCRIMINATOR.getAndIncrement(), Side.SERVER);
    }

    @SubscribeEvent
    public void registerBlocks(RegistryEvent.Register<Block> event) {
        event.getRegistry().register(HyperQuarry.QUARRY);

    }

    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> evt) {
        IForgeRegistry<Item> r = evt.getRegistry();
        r.register(new ItemQuarry(HyperQuarry.QUARRY));
        GameRegistry
            .registerTileEntity(TileEntityQuarry.class, HyperQuarry.toLocation("quarry"));
    }
}