package stone.hyperquarry.common;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import stone.hyperquarry.HyperQuarry;

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

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn,
        BlockPos fromPos) {
        if (worldIn.getTileEntity(pos) instanceof TileEntityQuarry quarry) {
            
        }
    }

}
