package at.tyron.vintagecraft.Interfaces;

import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public interface IIgniteable {

	public boolean ignite(World world, BlockPos pos, ItemStack firestarter);
}
