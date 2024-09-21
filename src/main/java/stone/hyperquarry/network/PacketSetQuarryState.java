package stone.hyperquarry.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import stone.hyperquarry.common.TileEntityQuarry;

public class PacketSetQuarryState extends PacketTargeted {

    private boolean isRunning;

    public PacketSetQuarryState() {}
    public PacketSetQuarryState(BlockPos target, boolean isRunning) {
        super(target);
        this.isRunning = isRunning;
    }
    @Override
    public void fromBytes(ByteBuf buf) { super.fromBytes(buf); isRunning = buf.readBoolean(); }

    @Override
    public void toBytes(ByteBuf buf) { super.toBytes(buf); buf.writeBoolean(isRunning); }

    public static class ServerHandler implements IMessageHandler<PacketSetQuarryState, IMessage> {
        @Override
        public IMessage onMessage(PacketSetQuarryState message, MessageContext ctx) {
            if (message.isInRange(ctx))
            {
                WorldServer server = ctx.getServerHandler().player.getServerWorld();
                TileEntity te = server.getTileEntity(message.target);
                if (te instanceof TileEntityQuarry quarry)
                {
                    server.addScheduledTask(() -> quarry.setRunning(message.isRunning));
                }
            }
            return null;
        }
    }
}
