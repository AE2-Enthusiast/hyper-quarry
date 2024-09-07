package stone.hyperquarry;

import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

public class ItemQuarry extends ItemBlock {

    public ItemQuarry(Block block) {
        super(block);
        setRegistryName(block.getRegistryName());
        setTranslationKey("hyper_quarry.quarry");

    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip,
        ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        tooltip
            .add("Mines blocks that have yet to exist, nullifying all the side effects");
        tooltip.add("");
        tooltip.add("Like Chronospheres, but less exponential");
    }
}
