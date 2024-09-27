package stone.hyperquarry.network;

import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import stone.hyperquarry.Client;
import stone.hyperquarry.common.TileEntityQuarry;

public class PacketOpenFilterGui extends PacketTargeted {
    public PacketOpenFilterGui() {
        super();
    }

    public PacketOpenFilterGui(BlockPos source) {
        super(source);
    }

    public static class ServerHandler implements IMessageHandler<PacketOpenFilterGui, PacketFilter> {
        @Override
        public PacketFilter onMessage(PacketOpenFilterGui message, MessageContext ctx) {
            WorldServer world = ctx.getServerHandler().player.getServerWorld();
            if (message.isInRange(ctx)) {
                TileEntity te = world.getTileEntity(message.target);
                if (te instanceof TileEntityQuarry quarry) {
                    world.addScheduledTask(() -> quarry.setRunning(false));
                    return new PacketFilter(quarry.getFilter());
                }
            }
            return null;
        }
    }

    public static class ClientHandler implements IMessageHandler<PacketFilter, IMessage> {
        @Override
        public IMessage onMessage(PacketFilter message, MessageContext ctx) {
            Minecraft minecraft = Minecraft.getMinecraft();
            minecraft
                    .addScheduledTask(
                                      () -> Client.showFilterScreen(message.filter));
            
            return null;
        }

    }
}
