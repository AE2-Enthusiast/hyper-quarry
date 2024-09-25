package stone.hyperquarry.common;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import org.apache.commons.rng.sampling.distribution.SharedStateDiscreteSampler;

import java.util.BitSet;

import javax.annotation.Nullable;

public class TileEntityQuarry extends TileEntity implements ITickable, IEnergyStorage {
    private SharedStateDiscreteSampler sampler;

    public static final String ENERGY = "energy";
    private int energy = 0;
    public static final String MINED = "mined";
    private long mined = 0;
    public static final String IS_RUNNING = "is_running";
    private boolean isRunning = false;
	private DropList drops;
	public static final String FILTER = "filter";
	private Filter filter;

	public TileEntityQuarry() {
	}
	@Override
	public void update() {
        if (this.world.isRemote || !isRunning)
            return;
        if (energy < this.drops.cost())
            return;
		TileEntity maybeEntity = this.world.getTileEntity(pos.up());
		if (maybeEntity != null) {
			IItemHandler handler = maybeEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN);
			if (handler != null) {
                int toMine = energy / this.drops.cost();
                this.mined += toMine;
                energy = energy % this.drops.cost();
                // bypass the stack limit cause why not
                int stacks = toMine / Byte.MAX_VALUE;
                byte leftover = (byte) (toMine % Byte.MAX_VALUE);
                
                int slot = 0;
                int max = handler.getSlots();
                ItemStack mined;
                while (stacks > 0)
                {
                    mined = this.drops.getStack(this.sampler.sample(), Byte.MAX_VALUE);
                    while ((mined = handler.insertItem(slot, mined, false)).getCount() > 0)
                    {
                        slot++;
                        if (slot >= max)
                        {
                            refundEnergy(stacks, leftover);
                            return;
                        }
                    }
                    stacks--;
                }
                mined = this.drops.getStack(this.sampler.sample(), leftover);
                while ((mined = handler.insertItem(slot, mined, false)).getCount() > 0)
                {
                    slot++;
                    if (slot >= max)
                    {
                        refundEnergy(stacks, leftover);
                        return;
                    }
                }
			}
		}
	}

    private void refundEnergy(int stacks, int leftover) {
        this.mined -= stacks * Byte.MAX_VALUE + leftover;
        try {
            this.energy = Math.addExact(this.energy,  Math.multiplyExact(stacks * Byte.MAX_VALUE + leftover, this.drops.cost()));
    } catch (ArithmeticException e) {
        this.energy = Integer.MAX_VALUE;
    }
    }

    @Override
	public int receiveEnergy(int maxReceive, boolean simulate) {
        if (!simulate)
        {
            try
            {
                this.energy = Math.addExact(this.energy, maxReceive);
        } catch (ArithmeticException e)
            {
                int toAdd = Integer.MAX_VALUE - this.energy;
                this.energy = Integer.MAX_VALUE;
                return toAdd;
            }
        }
        return maxReceive;
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {
		return 0;
	}
	
	

	@Override
	public void onLoad() {
		super.onLoad();
		if (filter == null) {
			this.filter = new Filter("drops", 0, new BitSet());
		}
		this.drops = DropList.of(filter);
		if (isRunning) {
			this.sampler = this.drops.init();
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.mined = compound.getLong(MINED);
		this.energy = compound.getInteger(ENERGY);
		this.filter = Filter.readFromNBT(compound.getCompoundTag(FILTER));
		this.isRunning = compound.getBoolean(IS_RUNNING);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		compound.setLong(MINED, mined);
		compound.setInteger(ENERGY, energy);
		compound.setTag(FILTER, filter.writeToNBT(new NBTTagCompound()));
		compound.setBoolean(IS_RUNNING, isRunning);
		return compound;
	}

	@Override
	public int getEnergyStored() {
        return energy;
	}

	@Override
	public int getMaxEnergyStored() {
        return Integer.MAX_VALUE;
	}

	@Override
    public boolean hasCapability(net.minecraftforge.common.capabilities.Capability<?> capability,
        @Nullable net.minecraft.util.EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY)
            return true;
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY)
            return (T) this;
        return super.getCapability(capability, facing);
    }

    @Override
	public boolean canExtract() {
        return false;
	}

	@Override
	public boolean canReceive() {
        return true;
	}

    public void setFilter(Filter filter) {
        if (isRunning)
            isRunning = false;
        DropList temp = DropList.of(filter);
        if (temp != null) {
        	this.drops = temp;
        	this.filter = filter;
        }
    }

	public Filter getFilter() {
		return this.filter;
	}

    public long getMined() {
        return this.mined;
    }
    
    public boolean isRunning() { return this.isRunning; }

    public void setRunning(boolean isRunning) {
        if (this.isRunning != isRunning)
        {
            this.isRunning = isRunning;
            if (isRunning)
            {
                this.sampler = this.drops.init();
            }
        }
    }
	public int getCost() {
		// TODO Auto-generated method stub
		return this.drops.cost();
	}
}
