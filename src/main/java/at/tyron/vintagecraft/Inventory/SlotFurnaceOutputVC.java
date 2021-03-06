package at.tyron.vintagecraft.Inventory;

import at.tyron.vintagecraft.AchievementsVC;
import at.tyron.vintagecraft.Item.ItemCeramicVessel;
import at.tyron.vintagecraft.Item.ItemIngot;
import at.tyron.vintagecraft.Item.ItemVC;
import at.tyron.vintagecraft.WorldProperties.EnumMetal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.SlotFurnaceOutput;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.AchievementList;

public class SlotFurnaceOutputVC extends SlotFurnaceOutput {
	EntityPlayer player;
	
	public SlotFurnaceOutputVC(EntityPlayer player, IInventory inventory, int slotIndex, int xPosition, int yPosition) {
		super(player, inventory, slotIndex, xPosition, yPosition);
		this.player = player;
	}
	
	@Override
	protected void onCrafting(ItemStack stack) {
		super.onCrafting(stack);
		
        if (stack.getItem() instanceof ItemIngot && ItemIngot.getMetal(stack) == EnumMetal.COPPER) {
            player.triggerAchievement(AchievementsVC.copperAge);
        }
        
        if (stack.getItem() instanceof ItemCeramicVessel) {
        	ItemStack[] contents = ItemCeramicVessel.getContainedItemStacks(stack);
        	for (ItemStack content : contents) {
        		if (content != null && content.getItem() instanceof ItemIngot) {
        			if (ItemIngot.getMetal(content) == EnumMetal.TINBRONZE || ItemIngot.getMetal(content) == EnumMetal.BISMUTHBRONZE) {
        				player.triggerAchievement(AchievementsVC.bronzeAge);
        			}
        		}
        	}
        }

	}

}
