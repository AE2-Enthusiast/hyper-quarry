package stone.hyperquarry.common;

import java.util.BitSet;

import com.github.bsideup.jabel.Desugar;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagLongArray;

@Desugar
public record Filter(String enchants, int dimension, BitSet mask) {
	public static final String ENCHANTS = "enchants";
	public static final String DIMENSION = "dimension";
	public static final String MASK = "mask";
	
	public static Filter readFromNBT(NBTTagCompound compound) {
		return new Filter(compound.getString(ENCHANTS), compound.getInteger("dimension"), BitSet.valueOf(compound.getByteArray(MASK)));
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setString(ENCHANTS, enchants);
		compound.setInteger(DIMENSION, dimension);
		compound.setByteArray(MASK, mask.toByteArray());
		return compound;
	}
}
