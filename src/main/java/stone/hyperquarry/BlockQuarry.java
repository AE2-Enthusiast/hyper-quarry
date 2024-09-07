package stone.hyperquarry;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockQuarry extends Block implements ITileEntityProvider {

    public BlockQuarry() {
        super(Material.IRON);
        setRegistryName(HyperQuarry.toLocation("quarry"));
        setTranslationKey("hyper_quarry.quarry");
        setHardness(10);
        setHarvestLevel("pickaxe", 3);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityQuarry();
    }

}
