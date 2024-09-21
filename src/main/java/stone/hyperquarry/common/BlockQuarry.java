package stone.hyperquarry.common;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import stone.hyperquarry.HyperQuarry;
import stone.hyperquarry.Proxy;
import stone.hyperquarry.network.PacketOpenQuarryGui;

public class BlockQuarry extends Block implements ITileEntityProvider {

    public BlockQuarry() {
        super(Material.IRON);
        this.hasTileEntity = true;
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

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state,
        EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY,
        float hitZ) {
        if (!worldIn.isRemote)
        {
            TileEntity te = worldIn.getTileEntity(pos);
            if (te instanceof TileEntityQuarry quarry)
            { Proxy.NETWORK.sendTo(new PacketOpenQuarryGui(quarry), (EntityPlayerMP) playerIn); }
        }

        return true;
    }
}
