package stone.hyperquarry.network;

import java.io.IOException;
import java.util.BitSet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;

public class PacketSetFilter implements Packet {
	
	private BitSet filter;
	
	@Override
	public void readPacketData(PacketBuffer buf) throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void writePacketData(PacketBuffer buf) throws IOException {
		buf.writeLongArray(filter.toLongArray());
	}

	@Override
	public void processPacket(INetHandler handler) {
		
	}

}
