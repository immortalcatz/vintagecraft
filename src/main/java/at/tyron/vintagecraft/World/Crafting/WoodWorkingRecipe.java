package at.tyron.vintagecraft.World.Crafting;

import at.tyron.vintagecraft.WorldProperties.EnumWorkableTechnique;
import net.minecraft.item.ItemStack;

public class WoodWorkingRecipe extends WorkableRecipeBase {
	public WoodWorkingRecipe(ItemStack output, ItemStack input, EnumWorkableTechnique[] techniques) {
		super(output, input, techniques);
	}

	public WoodWorkingRecipe(ItemStack output, ItemStack input, EnumWoodWorkingTechnique[] steps, boolean wildcardMatch) {
		super(output, input, steps, wildcardMatch);
	}

	public WoodWorkingRecipe(ItemStack output, ItemStack input, ItemStack input2, EnumWoodWorkingTechnique[] steps) {
		super(output, input, input2, steps);
	}


}
