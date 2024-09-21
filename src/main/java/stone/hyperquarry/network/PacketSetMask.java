package stone.hyperquarry.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import stone.hyperquarry.common.Filter;
import stone.hyperquarry.common.TileEntityQuarry;

public class PacketSetMask extends PacketFilter {
	protected BlockPos target;
	
    public PacketSetMask() { super(); }
    public PacketSetMask(Filter filter, BlockPos target) {
		super(filter);
		this.target = target;
	}

    @Override
    public void fromBytes(ByteBuf buf) {
        super.fromBytes(buf);
        this.target = BlockPos.fromLong(buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        super.toBytes(buf);
        buf.writeLong(this.target.toLong());

    }

    public static class ServerHandler implements IMessageHandler<PacketSetMask, IMessage> {
        @Override
        public IMessage onMessage(PacketSetMask message, MessageContext ctx) {
            WorldServer server = ctx.getServerHandler().player
                .getServerWorld();
            TileEntity te = server.getTileEntity(message.target);
            if (te != null && te instanceof TileEntityQuarry quarry) {
                server.addScheduledTask(() -> quarry.setFilter(message.filter));
            }
            return null;
        }
    }
}
