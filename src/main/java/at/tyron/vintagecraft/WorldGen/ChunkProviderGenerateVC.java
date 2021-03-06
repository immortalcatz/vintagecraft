package at.tyron.vintagecraft.WorldGen;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.Random;

import at.tyron.vintagecraft.Entity.Animal.EntityCowVC;
import at.tyron.vintagecraft.Entity.Animal.EntitySheepVC;
import at.tyron.vintagecraft.World.BlocksVC;
import at.tyron.vintagecraft.World.VCraftWorld;
import at.tyron.vintagecraft.WorldGen.Helper.WorldChunkManagerFlatVC;
import at.tyron.vintagecraft.WorldGen.Helper.WorldChunkManagerVC;
import at.tyron.vintagecraft.WorldGen.Layer.GenLayerTerrain;
import at.tyron.vintagecraft.WorldProperties.Terrain.EnumCrustLayer;
import at.tyron.vintagecraft.WorldProperties.Terrain.EnumCrustLayerGroup;
import at.tyron.vintagecraft.WorldProperties.Terrain.EnumCrustType;
import at.tyron.vintagecraft.WorldProperties.Terrain.EnumRockType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.passive.EntityChicken;
import net.minecraft.entity.passive.EntityCow;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderGenerate;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraft.world.gen.NoiseGeneratorOctaves;

public class ChunkProviderGenerateVC extends ChunkProviderGenerate {
	World worldObj;
	long seed;
	private Random rand;
	ChunkPrimer primer;

	// 3D Simplex Noise Rock Generator
	GenRockLayers genrocklayers;
	
	// The idea of this layer is to sort of represent the age since the 
	// last large nature event (earthquake, volcano, flood) happened.
	// Current uses:
	// - Thickness of Kimberlite layer
	// - Age of forests (young age = small tress)
	GenLayerVC ageLayer;
	
	// Generates Vanilla caves
	MapGenCavesVC caveGenerator;
	
	// Generates Flowers, Trees, Tallgrass, Wild Crops 
	MapGenFlora floragenerator;
	
	// Generates above sealevel lakes
	MapGenLakes lakegenerator;

	// These create horizontal deformations of the 3D Rock Layer generator
	// This prevents the 100% straight lines between two rocktypes
	GenLayerVC rockOffsetNoiseX;
	GenLayerVC rockOffsetNoiseZ;
	
	// 3D Noise generator that ultimately determines the shape of the landscape 
	GenLayerTerrain normalTerrainGen;
	
	
	private BiomeGenBase[] biomeMap;
	
	
	int[] seaLevelOffsetMap = new int[256];
	int[] chunkGroundLevelMap = new int[256]; // Skips floating islands
	int[] chunkHeightMap = new int[256];


	
	
	public ChunkProviderGenerateVC(World worldIn, long seed, boolean mapfeaturesenabled, String customgenjson) {
		super(worldIn, seed, mapfeaturesenabled, customgenjson);
		ageLayer = GenLayerVC.genAgemap(seed);
		
		caveGenerator = new MapGenCavesVC();
		floragenerator = new MapGenFlora(seed, ageLayer);
		lakegenerator = new MapGenLakes();
		
		this.worldObj = worldIn;
		this.rand = new Random(seed);
		this.mobSpawnerNoise = new NoiseGeneratorOctaves(this.rand, 8);
		this.seed = seed;
		
		
		genrocklayers = new GenRockLayers(seed);
		
		rockOffsetNoiseX = GenLayerVC.genHorizontalRockOffsetMap(seed);
		rockOffsetNoiseZ = GenLayerVC.genHorizontalRockOffsetMap(seed+500);
		//heightmapGen = GenLayerVC.genHeightmap(seed);
		
		normalTerrainGen = new GenLayerTerrain(seed + 0);
	}
	
	
	@Override
	public Chunk provideChunk(int chunkX, int chunkZ) {
		if (worldObj.getWorldChunkManager() instanceof WorldChunkManagerFlatVC) {
			return provideFlatChunk(chunkX, chunkZ, (WorldChunkManagerFlatVC)worldObj.getWorldChunkManager());
		}
		WorldChunkManagerVC wcm = (WorldChunkManagerVC)worldObj.getWorldChunkManager();
		
		//System.out.println("provide chunk");
		
		primer = new ChunkPrimer();
		
		VCraftWorld.instance.setChunkNBT(chunkX, chunkZ, "climate", wcm.climateGen.getInts(chunkX * 16, chunkZ * 16, 16, 16));
		
		//this.rand.setSeed(chunkX * 341873128712L + chunkZ * 132897987541L);
		
		
		biomeMap = worldObj.getWorldChunkManager().loadBlockGeneratorData(biomeMap, chunkX * 16, chunkZ * 16, 16, 16);
		
		//if (chunkX % 4 != 0) { // && (chunkX+1) % 4 != 0) {
			normalTerrainGen.generateTerrain(chunkX, chunkZ, primer, worldObj);
		
			decorate(chunkX, chunkZ, rand, primer);
			caveGenerator.func_175792_a(this, this.worldObj, chunkX, chunkZ, primer);
			caveGenerator.func_175792_a(this, this.worldObj, chunkX, chunkZ, primer);
			
		//}
		
		Chunk chunk = new Chunk(this.worldObj, primer, chunkX, chunkZ);
		
		
		byte biomeMapbytes[] = new byte[256];
		for (int i = 0; i < biomeMap.length; i++) {
			biomeMapbytes[i] = (byte) biomeMap[i].biomeID;
		}
		
		chunk.setBiomeArray(biomeMapbytes);
		
		// Also generates the height map, so beyond this point we can do calculations based on that
		chunk.generateSkylightMap();
		
		
		
		
		
		return chunk;
	}
	
	
	private Chunk provideFlatChunk(int chunkX, int chunkZ, WorldChunkManagerFlatVC wcm) {
		primer = new ChunkPrimer();
		VCraftWorld.instance.setChunkNBT(chunkX, chunkZ, "climate", wcm.climateGen.getInts(chunkX * 16, chunkZ * 16, 16, 16));
		Random rand = new Random();
		
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				primer.setBlockState(x, 128, z, EnumCrustType.TOPSOIL.getBlock(EnumRockType.GRANITE, VCraftWorld.instance.getClimate(new BlockPos(chunkX*16 + x, 128, chunkZ*16+z))));
				primer.setBlockState(x, 127, z, BlocksVC.rock.getEntryFromKey(EnumRockType.GRANITE).getBlockState());
			}
		}
		
		Chunk chunk = new Chunk(this.worldObj, primer, chunkX, chunkZ);
		chunk.generateSkylightMap();
		
		return chunk;
	}


	@Override
	public void populate(IChunkProvider chunkprovider, int chunkX, int chunkZ) {
		checkUnpopulatedQueue(chunkprovider);
		
		if (chunkprovider instanceof ChunkProviderServer) {
			// Don't populate if not all direct neighbours have been populated
			// Instead put on queue
			if(!shouldPopulate((ChunkProviderServer)chunkprovider, chunkX, chunkZ)) {
				synchronized (VCraftWorld.instance.unpopulatedChunks) {
					VCraftWorld.instance.unpopulatedChunks.add(new BlockPos(chunkX, 0, chunkZ));	
				}
				
				VCraftWorld.instance.setChunkNBT(chunkX, chunkZ, "vcraftpopulated", false);	
				return;
			}
	
			populateNow(chunkprovider, chunkX, chunkZ);
		}
	}
	
	
	public void checkUnpopulatedQueue(IChunkProvider chunkprovider) {

		// Actually working check on whether or not to populate a chunk (prevents runaway chunk generation)
		// Vanilla pop-check is very strangely programmed and fails to prevent runaway chunk generation - mostly because they only small structures and trees
		// Seems to be buggy on rare occassions where a stripe of chunks are not populated, but still much better than the game crashing from endless chunk generation   
		
		// Principle: Populate a chunk only if all direct neighbours have been generated already 
		if (chunkprovider instanceof ChunkProviderServer) {
			Queue<Point> chunks2Pop = new LinkedList<Point>();
			
			synchronized(VCraftWorld.instance.unpopulatedChunks) {
				int x, z;
				if (VCraftWorld.instance.unpopulatedChunks.size() > 2000) {
					System.out.println("Warning: Over 2000 chunks in unpop queue - possible runaway chunk generation.");
				}
				
				// check queue of not yet populated chunks and see if we can pop these by now
				Iterator<BlockPos> it = VCraftWorld.instance.unpopulatedChunks.iterator();
				while (it.hasNext()) {
					BlockPos chunkpos = it.next();
					x = chunkpos.getX();
					z = chunkpos.getZ();
					
					if (shouldPopulate((ChunkProviderServer)chunkprovider, x, z)) {
						it.remove();
						chunks2Pop.add(new Point(x, z));		
					}
				}
			}
			
			while (!chunks2Pop.isEmpty()) {
				Point p = chunks2Pop.remove();
				populateNow(chunkprovider, p.x, p.y);
				VCraftWorld.instance.setChunkNBT(p.x, p.y, "vcraftpopulated", true);
			}
			
			
		}
	}
	
	public void populateNow(IChunkProvider chunkprovider, int chunkX, int chunkZ) {
		BlockPos pos;
		int xCoord = chunkX * 16;
		int zCoord = chunkZ * 16;
		
		
		HeightmapCache.putHeightMap(
			worldObj.provider.getDimensionId(), 
			worldObj.getChunkFromChunkCoords(chunkX, chunkZ).getHeightMap().clone(), 
			chunkX, chunkZ
		);
		
		

		lakegenerator.generate(rand, chunkX, chunkZ, worldObj, chunkprovider, chunkprovider);
		
		WorldGenAnimals.performWorldGenSpawning(this.worldObj, null, xCoord, zCoord, 16, 16, this.rand);
		
		floragenerator.generate(rand, chunkX, chunkZ, worldObj, chunkprovider, chunkprovider);
		
		
		BlockPos chunkpos = new BlockPos(xCoord, 0, zCoord);
		int temp;
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				pos = worldObj.getHorizon(chunkpos.add(x, 0, z));
				
				if (worldObj.getBlockState(pos.down()).getBlock().getMaterial() != Material.water) {
					temp = VCraftWorld.instance.getTemperature(pos);
					if (temp < -10 || (temp < 2 && rand.nextInt(temp + 11) == 0)) {
						worldObj.setBlockState(pos, Blocks.snow_layer.getDefaultState());
					}
				}
			}
		}
		
//		HeightmapCache.removeHeightMap(worldObj.provider.getDimensionId(), chunkX, chunkZ);
	}
	
	
	
	
	
	
	// Places the soil and rock layers
	void decorate(int chunkX, int chunkZ, Random rand, ChunkPrimer primer) {
		Arrays.fill(chunkGroundLevelMap, 0);
		Arrays.fill(chunkHeightMap, 0);
		
		EnumCrustLayer[] crustLayersByDepth = new EnumCrustLayer[255];
		
		int age[] = ageLayer.getInts(chunkX*16 - 1, chunkZ*16 - 1, 18, 18);
		
		// These create deformations in the transitions of rocks, so they are not in a straight line
		int rockoffsetx[] = rockOffsetNoiseX.getInts(chunkX*16, chunkZ*16, 16, 16);
		int rockoffsetz[] = rockOffsetNoiseZ.getInts(chunkX*16, chunkZ*16, 16, 16);
		
		IBlockState[] toplayers;
		
		for (int x = 0; x < 16; ++x) {
			for (int z = 0; z < 16; ++z) {
				
				int arrayIndexChunk = z*16 + x;
				int arrayIndexHeightmap = (z+1)*18 + (x+1);
				
				BiomeVC biome = (BiomeVC) biomeMap[arrayIndexChunk];

				int airblocks = 0;
				toplayers = null;
				
				for (int y = 255; y >= 0; --y) {
					if (y <= 0) {
						primer.setBlockState(x, y, z, BlocksVC.uppermantle.getDefaultState());
						break;
					}
					
					if (primer.getBlockState(x, y, z).getBlock() == Blocks.stone) {
						if (chunkGroundLevelMap[arrayIndexChunk] == 0) {
							chunkGroundLevelMap[arrayIndexChunk] = y;
							
							EnumRockType rocktype = genrocklayers.getRockType(
								chunkX * 16 + x + rockoffsetx[arrayIndexChunk], 
								2, 
								chunkZ * 16 + z + rockoffsetz[arrayIndexChunk], 
								Math.abs(age[arrayIndexHeightmap]), 
								rand
							);
							toplayers = EnumCrustLayerGroup.getTopLayers(rocktype, new BlockPos(chunkX*16 + x, y, chunkZ*16 + z), rand);
						}
						
						if (chunkHeightMap[arrayIndexChunk] == 0) {
							chunkHeightMap[arrayIndexChunk] = y;
						}
						
						int depth = chunkGroundLevelMap[arrayIndexChunk] - y;

						
						if (y < Math.abs(age[arrayIndexChunk])/2 - 40) {
							primer.setBlockState(x, y, z, BlocksVC.rock.getEntryFromKey(EnumRockType.KIMBERLITE).getBlockState());
						} else {
							if (toplayers.length > depth) {
								primer.setBlockState(x, y, z, toplayers[depth]);
							} else {
								EnumRockType rocktype = genrocklayers.getRockType(
									chunkX * 16 + x + rockoffsetx[arrayIndexChunk], 
									depth, 
									chunkZ * 16 + z + rockoffsetz[arrayIndexChunk], 
									Math.abs(age[arrayIndexHeightmap]), 
									rand
								);
								
								
								primer.setBlockState(x, y, z, EnumCrustType.ROCK.getBlock(rocktype, null));
							}
							
						}
					}
					
					if (chunkGroundLevelMap[arrayIndexChunk] != 0 && primer.getBlockState(x, y, z).getBlock() == Blocks.air) {
						airblocks++;
					}
					
					// Try to exclude floating islands in the ground level map
					if (airblocks > 8) {
						chunkGroundLevelMap[arrayIndexChunk] = 0;
						airblocks = 0;
					}
				}
			}
		}
	}
	
	
	
	
	

	public static List getCreatureSpawnsByChunk(World world, BiomeVC biome, int xcoord, int zcoord) {
		ArrayList<SpawnListEntry> list = new ArrayList<SpawnListEntry>();
		
		// Returns climate = int[temp, fertility, rain]
		int[] climate = VCraftWorld.instance.getClimate(world.getHorizon(new BlockPos(xcoord, 0, zcoord)));
		if (climate[2] < 70) return list;
		
		if (climate[0] < -3 && climate[0] > -15 && climate[2] > 70) {
			list.add(new SpawnListEntry(EntityWolf.class, 2, 1, 2));
		}

		if (climate[0] > 25) {
			list.add(new SpawnListEntry(EntityPig.class, 20, 2, 4));
			list.add(new SpawnListEntry(EntityChicken.class, 30, 2, 4));
			return list;
		}

		if (climate[0] > 10) {
			list.add(new SpawnListEntry(EntityCowVC.class, 25, 2, 5));
			list.add(new SpawnListEntry(EntityHorse.class, 1, 1, 2));
			list.add(new SpawnListEntry(EntitySheepVC.class, 1, 2, 5));
			list.add(new SpawnListEntry(EntityPig.class, 25, 2, 4));
			list.add(new SpawnListEntry(EntityChicken.class, 10, 2, 4));
			return list;
		}
		
		if (climate[0] > 0) {
			list.add(new SpawnListEntry(EntityCowVC.class, 15, 2, 4));
			list.add(new SpawnListEntry(EntityHorse.class, 1, 1, 2));
			list.add(new SpawnListEntry(EntitySheepVC.class, 10, 2, 4));
			list.add(new SpawnListEntry(EntityPig.class, 5, 2, 4));
			
			return list;
		}		

		if (climate[0] > -10) {
			list.add(new SpawnListEntry(EntityCow.class, 25, 2, 3));
			list.add(new SpawnListEntry(EntityHorse.class, 1, 1, 2));
			list.add(new SpawnListEntry(EntitySheep.class, 1, 2, 3));
			return list;
		}
		
		
		return list;
	}

	
	
	public boolean shouldPopulate(ChunkProviderServer chunkprovider, int chunkX, int chunkZ) {
		return
				// Direct neighbours
				 chunkprovider.chunkExists(chunkX - 1, chunkZ)
				 && chunkprovider.chunkExists(chunkX, chunkZ - 1)
				 && chunkprovider.chunkExists(chunkX + 1, chunkZ)
				 && chunkprovider.chunkExists(chunkX, chunkZ + 1)
				// Diagonals
				 && chunkprovider.chunkExists(chunkX - 1, chunkZ - 1)
				 && chunkprovider.chunkExists(chunkX - 1, chunkZ + 1)
				 && chunkprovider.chunkExists(chunkX + 1, chunkZ - 1)
				 && chunkprovider.chunkExists(chunkX + 1, chunkZ + 1)
		;
		
	}
	
	@Override
	public boolean func_177460_a(IChunkProvider p_177460_1_, Chunk p_177460_2_, int p_177460_3_, int p_177460_4_) {
		return false;
	}
	
	

	@Override
	public boolean unloadQueuedChunks()
	{
		return true;
	}




	// Get spawnable creatures list
	@Override
	public List func_177458_a(EnumCreatureType p_177458_1_, BlockPos p_177458_2_) {
		BiomeGenBase biomegenbase = this.worldObj.getBiomeGenForCoords(p_177458_2_);
		
		return biomegenbase.getSpawnableList(p_177458_1_);
	}
	
	@Override
	public void recreateStructures(Chunk p_180514_1_, int p_180514_2_, int p_180514_3_) {
				
	}
	
	
	
}
