package stone.hyperquarry;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

public class TileEntityQuarry extends TileEntity implements ITickable, IEnergyStorage {

    public int cost = 20000;

    private int energy = 0;
	private DropList drops;
	
	public TileEntityQuarry() {
        this.drops = DropList.of(-1);
        Set<Item> filter = new HashSet<>();
        filter.add(Item.getItemFromBlock(Blocks.NETHERRACK));
        this.drops = this.drops.filter(false, filter);
        this.cost = this.drops.init();

	}

	@Override
	public void update() {
        if (this.world.isRemote)
            return;
        if (energy < cost)
            return;
		TileEntity maybeEntity = this.world.getTileEntity(pos.up());
		if (maybeEntity != null) {
			IItemHandler handler = maybeEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.DOWN);
			if (handler != null) {
                int toMine = energy / cost;
                energy = energy % cost;
                // bypass the stack limit cause why not
                int stacks = toMine / Byte.MAX_VALUE;
                byte leftover = (byte) (toMine % Byte.MAX_VALUE);
                
                int slot = 0;
                int max = handler.getSlots();
                ItemStack mined;
                while (stacks > 0)
                {
                    mined = this.drops.getStack(Byte.MAX_VALUE);
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
                mined = this.drops.getStack(leftover);
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
        this.energy += (stacks * Byte.MAX_VALUE + leftover) * cost;
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
	public int getEnergyStored() {
        return energy;
	}

	@Override
	public int getMaxEnergyStored() {
		// TODO Auto-generated method stub
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

}
