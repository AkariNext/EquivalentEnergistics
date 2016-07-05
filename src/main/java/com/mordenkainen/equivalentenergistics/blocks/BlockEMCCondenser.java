package com.mordenkainen.equivalentenergistics.blocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mordenkainen.equivalentenergistics.config.IConfigurable;
import com.mordenkainen.equivalentenergistics.registries.TextureEnum;
import com.mordenkainen.equivalentenergistics.tiles.TileEMCCondenser;
import com.mordenkainen.equivalentenergistics.util.CommonUtils;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockEMCCondenser extends BlockContainer implements IConfigurable {
		
	private static final String GROUP = "Condenser";
	private static final double OFFSET = 0.0625D;
	
	public static int itemsPerTick;
	public static int crystalsPerTick;
	public static double idlePower;
	public static double activePower;
	
	public BlockEMCCondenser() {
		super(Material.rock);
		setHardness(1.5f);
		setStepSound(Block.soundTypeStone);
	}

	@Override
	public TileEntity createNewTileEntity(final World world, final int meta) {
		return new TileEMCCondenser();
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public final void registerBlockIcons(final IIconRegister register) {}
	
	@SideOnly(Side.CLIENT)
	@Override
	public IIcon getIcon(final int side, final int meta) {
		if (side == 0 || side == 1) {
			return TextureEnum.EMCCONDENSER.getTexture();
		}
		return TextureEnum.EMCCONDENSER.getTexture(1);
	}

	@Override
	public void breakBlock(final World world, final int x, final int y, final int z, final Block block, final int metaData) {
		if (!world.isRemote) {
			final TileEMCCondenser tileCondenser = CommonUtils.getTE(TileEMCCondenser.class, world, x, y, z);

			if (tileCondenser != null) {
				final List<ItemStack> drops = new ArrayList<ItemStack>();
				tileCondenser.getDrops(world, x, y, z, drops);

				for (final ItemStack drop : drops) {
					CommonUtils.spawnEntItem(world, x, y, z, drop);
				}
			}
		}

		super.breakBlock(world, x, y, z, block, metaData);
	}
	
	@Override
	public void onBlockPlacedBy(final World world, final int x, final int y, final int z, final EntityLivingBase player, final ItemStack itemStack) {
		final TileEMCCondenser tileCondenser = CommonUtils.getTE(TileEMCCondenser.class, world, x, y, z);

		if (tileCondenser != null && player instanceof EntityPlayer) {
			tileCondenser.setOwner((EntityPlayer)player);
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
    public void randomDisplayTick(final World world, final int x, final int y, final int z, final Random random) {
		final TileEMCCondenser tileCondenser = CommonUtils.getTE(TileEMCCondenser.class, world, x, y, z);
		
		if (tileCondenser == null || !tileCondenser.isBlocked()) {
			return;
		}
		
		for (final ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            if (world.getBlock(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ).isOpaqueCube()) {
            	continue;
            }
        	
            spawnParticle(world, x, y, z, dir, random);
		}
	}
	
	private void spawnParticle(final World world, final int x, final int y, final int z, final ForgeDirection dir, final Random random) {
		double d1 = (double) ((float) x + random.nextFloat());
        double d2 = (double) ((float) y + random.nextFloat());
        double d3 = (double) ((float) z + random.nextFloat());
        
        switch (dir) {
            case EAST:
    			d1 = (double) (x + 1) + OFFSET;
    			break;
    		case WEST:
    			d1 = (double) x - OFFSET;
    			break;
            case UP:
    			d2 = (double) (y + 1) + OFFSET;
    			break;
    		case DOWN:
    			d2 = (double) y - OFFSET;
    			break;
    		case SOUTH:
    			d3 = (double) (z + 1) + OFFSET;
    			break;
    		case NORTH:
    			d3 = (double) z - OFFSET;
    			break;
			default:
				break;
    	}
    	
    	world.spawnParticle("reddust", d1, d2, d3, 0.0D, 0.0D, 0.0D);
	}

	@Override
	public void loadConfig(final Configuration config) {
		itemsPerTick = config.get(GROUP, "ItemsCondensedPerTick", 8).getInt(8);
        crystalsPerTick = config.get(GROUP, "CrystalsProducedPerTick", 16).getInt(16);
        idlePower = config.get(GROUP, "IdlePowerDrain", 0.0).getDouble(0.0);
        activePower = config.get(GROUP, "PowerDrainPerEMCCondensed", 0.01).getDouble(0.01);
	}
	
}
