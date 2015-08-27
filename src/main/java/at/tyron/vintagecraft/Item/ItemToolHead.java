package at.tyron.vintagecraft.Item;

import java.util.List;

import at.tyron.vintagecraft.VintageCraft;
import at.tyron.vintagecraft.Interfaces.ISubtypeFromStackPovider;
import at.tyron.vintagecraft.WorldProperties.EnumTool;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class ItemToolHead extends ItemVC implements ISubtypeFromStackPovider {
	String material;
	EnumTool tooltype;
	
	public ItemToolHead(EnumTool tooltype, String material) {
		this.tooltype = tooltype;
		this.material = material;
		setCreativeTab(VintageCraft.toolsarmorTab);
		maxStackSize = 1;
	}

	@Override
	public String getSubType(ItemStack stack) {
		return material + "_" + tooltype.getName();
	}

	
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer playerIn, List tooltip, boolean advanced) {
		tooltip.add("Combine with a stick to complete the tool");
	}
	
}