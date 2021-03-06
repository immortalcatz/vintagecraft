package at.tyron.vintagecraft.Interfaces;

import net.minecraft.block.Block;


/* For classes/enums that supply blockstate infos to blocks */

public interface IStateEnum {
	public int getMetaData(Block block);
	public String getStateName();
	
	public void init(Block block, int meta);
	public int getId();
	
}
