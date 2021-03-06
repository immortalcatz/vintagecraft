package at.tyron.vintagecraft.Item;

import java.util.List;

import at.tyron.vintagecraft.VintageCraft;
import at.tyron.vintagecraft.Block.Utility.BlockOrePile;
import at.tyron.vintagecraft.Interfaces.Item.IItemFuel;
import at.tyron.vintagecraft.Interfaces.Item.IItemSmeltable;
import at.tyron.vintagecraft.Interfaces.Item.IItemSmithable;
import at.tyron.vintagecraft.Interfaces.Item.ISizedItem;
import at.tyron.vintagecraft.Interfaces.Item.ISubtypeFromStackPovider;
import at.tyron.vintagecraft.World.BlocksVC;
import at.tyron.vintagecraft.World.ItemsVC;
import at.tyron.vintagecraft.World.Crafting.WorkableRecipeBase;
import at.tyron.vintagecraft.WorldProperties.EnumItemSize;
import at.tyron.vintagecraft.WorldProperties.EnumMetal;
import at.tyron.vintagecraft.WorldProperties.EnumStrongHeatSource;
import at.tyron.vintagecraft.WorldProperties.Terrain.EnumOreType;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemOreVC extends ItemVC implements ISubtypeFromStackPovider, IItemFuel, IItemSmeltable, ISizedItem, IItemSmithable {

	public static int maxpilesize = 64;

	public ItemOreVC() {
        this.setHasSubtypes(true);
        setCreativeTab(VintageCraft.resourcesTab);
	}
	
	
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item itemIn, CreativeTabs tab, List subItems) {
    	ItemStack stack;
    	for (EnumOreType oretype : EnumOreType.values()) {
    		if (oretype == EnumOreType.OLIVINE) continue;
    		
    		stack = new ItemStack(ItemsVC.ore);
    		setOreType(stack, oretype);
    		subItems.add(stack);
    	}
    }

	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		if (getOreType(stack) == null) {
			return super.getUnlocalizedName() + ".unknown";
		}
		return super.getUnlocalizedName() + "." + getOreType(stack).getName();
	}
	
	
	public static EnumOreType getOreType(ItemStack itemstack) {
		if (itemstack.getTagCompound() != null && itemstack.getTagCompound().hasKey("oretype")) {
			return EnumOreType.byId(itemstack.getTagCompound().getInteger("oretype"));
		}
		return null;
	}
	
	public static ItemStack getItemStackFor(EnumOreType oretype, int quantity) {
		return setOreType(new ItemStack(ItemsVC.ore, quantity), oretype);
		
	}
	
	public static ItemStack setOreType(ItemStack itemstack, EnumOreType oretype) {
		NBTTagCompound nbt = getOrCreateNBT(itemstack); 
		nbt.setInteger("oretype", oretype.getId());
		itemstack.setTagCompound(nbt);
		return itemstack;
	}


	@Override
	public String getSubType(ItemStack stack) {
		EnumOreType oretype = getOreType(stack);
		if (oretype == null) {
			System.out.println("Error: Trying to get type of invalid ore item. NBT Tags: " + getOrCreateNBT(stack));
			return "unknown";
		}
		return oretype.getName();
	}


	@Override
	public int getBurningHeat(ItemStack stack) {
		if (getOreType(stack) == EnumOreType.LIGNITE) {
			return 1100;
		}
		if (getOreType(stack) == EnumOreType.BITUMINOUSCOAL) {
			return 1200;
		}
		if (getOreType(stack) == EnumOreType.COKE) {
			return 1300;
		}
		return 0;
	}


	@Override
	public float getBurnDurationMultiplier(ItemStack stack) {
		if (getOreType(stack) == EnumOreType.COKE) {
			return 1.8f;
		}
		if (getOreType(stack) == EnumOreType.LIGNITE) {
			return 2.2f;
		}
		if (getOreType(stack) == EnumOreType.BITUMINOUSCOAL) {
			return 2.4f;
		}
		return 0;
	}
	
	
	@Override
	public boolean isMetalWorkingFuel(ItemStack stack) {
		EnumOreType oretype = getOreType(stack); 
		return
			oretype == EnumOreType.LIGNITE ||
			oretype == EnumOreType.BITUMINOUSCOAL ||
			oretype == EnumOreType.COKE
		;
		
	}	


	@Override
	public ItemStack getSmelted(ItemStack ore) {
		switch (getOreType(ore)) {
			case LIMONITE:
			case MAGNETITE: return ItemIngot.setMetal(new ItemStack(ItemsVC.metalingot), EnumMetal.IRON);
			
			case NATIVEGOLD_QUARTZ: return ItemIngot.setMetal(new ItemStack(ItemsVC.metalingot), EnumMetal.GOLD);
			case NATIVECOPPER: return ItemIngot.setMetal(new ItemStack(ItemsVC.metalingot), EnumMetal.COPPER);
			case CASSITERITE: return ItemIngot.setMetal(new ItemStack(ItemsVC.metalingot), EnumMetal.TIN);
			case NATIVESILVER_QUARTZ: return ItemIngot.setMetal(new ItemStack(ItemsVC.metalingot), EnumMetal.SILVER);
			case PLATINUM: return ItemIngot.setMetal(new ItemStack(ItemsVC.metalingot), EnumMetal.PLATINUM);
			case ILMENITE: return ItemIngot.setMetal(new ItemStack(ItemsVC.metalingot), EnumMetal.TITANIUM);
			case CHROMITE: return ItemIngot.setMetal(new ItemStack(ItemsVC.metalingot), EnumMetal.CHROMIUM);
			case SPHALERITE: return ItemIngot.setMetal(new ItemStack(ItemsVC.metalingot), EnumMetal.ZINC);
			case BISMUTHINITE: return ItemIngot.setMetal(new ItemStack(ItemsVC.metalingot), EnumMetal.BISMUTH);
			case GALENA:  return ItemIngot.setMetal(new ItemStack(ItemsVC.metalingot), EnumMetal.LEAD);
			case QUARTZ:
			case QUARTZCRYSTAL:
				return new ItemStack(Blocks.glass);
			default:
				break; 
		}
		return null;
	}


	@Override
	public int getRaw2SmeltedRatio(ItemStack ore) {
		switch (getOreType(ore)) {
			case LIMONITE:
			case MAGNETITE:
			case NATIVECOPPER: return 4;
			case NATIVEGOLD_QUARTZ: return 8;
			case CASSITERITE: return 8;				// "even a rich cassiterite ore only has 5% tin" @ http://en.wikipedia.org/wiki/Smelting
			case NATIVESILVER_QUARTZ: return 4;
			case PLATINUM: return 4;
			case ILMENITE: return 4;
			case CHROMITE: return 4;
			case SPHALERITE: return 4;
			case BISMUTHINITE: return 4;
			case GALENA: return 4;
			case QUARTZ:
				return 2;
			case QUARTZCRYSTAL:
				return 1;

			default: break;
		
		}
		return 0;
	}
	
	
	@Override
	public int getMeltingPoint(ItemStack ore) {
		switch (getOreType(ore)) {
			case CASSITERITE: return EnumMetal.TIN.meltingpoint;
			case MAGNETITE:
			case LIMONITE: return EnumMetal.IRON.meltingpoint;
			case NATIVECOPPER: return EnumMetal.COPPER.meltingpoint;
			case NATIVEGOLD_QUARTZ: return EnumMetal.GOLD.meltingpoint;
			case NATIVESILVER_QUARTZ: return EnumMetal.SILVER.meltingpoint;
			case BISMUTHINITE: return EnumMetal.BISMUTH.meltingpoint;
			case CHROMITE: return EnumMetal.CHROMIUM.meltingpoint;
			case PLATINUM: return EnumMetal.PLATINUM.meltingpoint;
			case ILMENITE: return EnumMetal.TITANIUM.meltingpoint;
			case SPHALERITE: return EnumMetal.ZINC.meltingpoint;
			case GALENA: return EnumMetal.LEAD.meltingpoint;
			case QUARTZ:
			case QUARTZCRYSTAL:
				return 1350;
			default: break;
		}
		return 0;
	}
	
	
	@Override
	public float getSmeltingSpeedModifier(ItemStack raw) {
		return 0.25f * getRaw2SmeltedRatio(raw);
	}

	
	
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer playerIn, List tooltip, boolean advanced) {
		EnumStrongHeatSource.addItemStackInformation(itemstack, tooltip);
	}


	@Override
	public EnumItemSize getItemSize() {
		return EnumItemSize.SMALL;
	}


	@Override
	public int smokeLevel(ItemStack stack) {
		if (getOreType(stack) == EnumOreType.COKE) return 20;
		return 80;
	}
	
	@Override
	public ItemStack getCokedOutput(ItemStack stack) {
		EnumOreType oretype = getOreType(stack);
		
		if (oretype == EnumOreType.LIGNITE) {
			return getItemStackFor(EnumOreType.COKE, stack.stackSize / 2);
		}
		if (oretype == EnumOreType.BITUMINOUSCOAL) {
			return getItemStackFor(EnumOreType.COKE, (3 * stack.stackSize) / 4);
		}
		return null;
	}


	public boolean isPlaceable(ItemStack stack) {
		EnumOreType oretype = getOreType(stack);
		
		return oretype == EnumOreType.LIGNITE || oretype == EnumOreType.BITUMINOUSCOAL || oretype == EnumOreType.COKE;
	}


	

	
	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer entityplayer, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (super.onItemUse(itemstack, entityplayer, world, pos, side, hitX, hitY, hitZ)) return true;
		
		if (!entityplayer.isSneaking() || !isPlaceable(itemstack)) return false;
		
		boolean orePileAtPos = world.getBlockState(pos).getBlock() instanceof BlockOrePile;
		
		if (!orePileAtPos) {
			return BlockOrePile.tryCreatePile(itemstack, world, pos.offset(side));
		} else {
			BlocksVC.orepile.onBlockActivated(world, pos, world.getBlockState(pos), entityplayer, side, hitX, hitY, hitZ);
		}
		
		return false;
	}


	@Override
	public boolean isIngredient(ItemStack itemstack, ItemStack comparison, WorkableRecipeBase forrecipe) {
		return getOreType(itemstack) == EnumOreType.DIAMOND;
	}


	@Override
	public boolean workableOn(int anviltier, ItemStack itemstack, ItemStack itemstackoptional) {
		return getOreType(itemstack) == EnumOreType.DIAMOND;
	}


	@Override
	public int smeltBatchSize(ItemStack raw) {
		switch(getOreType(raw)) {
			case QUARTZ: return 4;
			case QUARTZCRYSTAL: return 4;
			default: return 1;
		}
	}

}
