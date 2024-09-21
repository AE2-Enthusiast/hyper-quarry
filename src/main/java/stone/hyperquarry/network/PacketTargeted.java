package stone.hyperquarry.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketTargeted implements IMessage {

    protected BlockPos target;

    public PacketTargeted() {}
    public PacketTargeted(BlockPos pos) { target = pos; }

    @Override
    public void fromBytes(ByteBuf buf) { target = BlockPos.fromLong(buf.readLong()); }

    @Override
    public void toBytes(ByteBuf buf) { buf.writeLong(target.toLong()); }

    public boolean isInRange(MessageContext ctx) {
        return ctx.getServerHandler().player.getPosition().distanceSq(target) < 16 * 16;
    }
}
