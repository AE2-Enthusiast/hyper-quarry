package stone.hyperquarry.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import stone.hyperquarry.common.Filter;

import java.nio.charset.StandardCharsets;
import java.util.BitSet;

public class PacketFilter implements IMessage {

    protected Filter filter;
    
    public PacketFilter() {
    }
    public PacketFilter(Filter filter) {
    	this.filter = filter;
    }
    
    @Override
    public void fromBytes(ByteBuf buf) {
    	byte enchantsLength = buf.readByte();
    	byte[] enchantsBytes = new byte[enchantsLength];
    	buf.readBytes(enchantsBytes);
    	String enchants = new String(enchantsBytes, StandardCharsets.US_ASCII);
    	
    	int dimension = buf.readInt();

        short length = buf.readUnsignedByte();
        long[] words = new long[length];
        for (int i = 0; i < length; i++)
            words[i] = buf.readLong();
        BitSet mask = BitSet.valueOf(words);
        this.filter = new Filter(enchants, dimension, mask);
    }

    @Override
    public void toBytes(ByteBuf buf) {
    	byte[] enchantsBytes = filter.enchants().getBytes(StandardCharsets.US_ASCII);
    	buf.writeByte(enchantsBytes.length);
    	buf.writeBytes(enchantsBytes, 0, Math.min(Byte.MAX_VALUE, enchantsBytes.length));
    	
    	buf.writeInt(this.filter.dimension());

        long[] words = this.filter.mask().toLongArray();
        buf.writeByte(words.length);
        for (int i = 0; i < words.length; i++)
            buf.writeLong(words[i]);
    }

    

}
