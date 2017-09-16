package ec3.common.tile;

import java.util.List;

import DummyCore.Utils.DataStorage;
import DummyCore.Utils.DummyData;
import DummyCore.Utils.MathUtils;
import DummyCore.Utils.MiscUtils;
import ec3.api.ApiCore;
import ec3.common.item.ItemCollectedMonsterSpawner;
import ec3.common.item.ItemsCore;
import ec3.utils.common.ECUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldEntitySpawner;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.eventhandler.Event.Result;

public class TileDarknessObelisk extends TileMRUGeneric {
	public static float cfgMaxMRU = ApiCore.DEVICE_MAX_MRU_GENERIC;
	public static boolean generatesCorruption = false;
	public static int genCorruption = 30;
	public static int mruUsage = 500;
	public static float worldSpawnChance = 0.1F;
	public static int mobSpanwerDelay = 100;
	public static boolean enableMobSpawners = true;
	public static int mobSpawnerRadius = 3, worldSpawnerRadius = 8;

	public TileDarknessObelisk() {
		super();
		maxMRU = (int)cfgMaxMRU;
		setSlotsNum(2);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void update() {
		if(getWorld().isBlockIndirectlyGettingPowered(pos) == 0) {
			if(getStackInSlot(1) == null || !(getStackInSlot(1).getItem() instanceof ItemCollectedMonsterSpawner)) {
				Biome biome = getWorld().getBiome(pos);
				List l = biome.getSpawnableList(EnumCreatureType.MONSTER);
				if(l != null && !l.isEmpty() && !getWorld().isRemote && getWorld().rand.nextFloat() < worldSpawnChance && getMRU() > mruUsage) {
					Biome.SpawnListEntry spawnlistentry = null;
					IEntityLivingData ientitylivingdata = null;
					int rndOffsetX = (int)(pos.getX() + MathUtils.randomDouble(getWorld().rand) * worldSpawnerRadius);
					int rndOffsetY = (int)(pos.getY() + MathUtils.randomDouble(getWorld().rand) * worldSpawnerRadius);
					int rndOffsetZ = (int)(pos.getZ() + MathUtils.randomDouble(getWorld().rand) * worldSpawnerRadius);
					BlockPos rndPos = new BlockPos(rndOffsetX, rndOffsetY, rndOffsetZ);
					if(WorldEntitySpawner.canCreatureTypeSpawnAtLocation(EntityLiving.SpawnPlacementType.ON_GROUND, getWorld(), rndPos)) {
						WorldServer wrld = (WorldServer) getWorld();
						spawnlistentry = wrld.getSpawnListEntryForTypeAt(EnumCreatureType.MONSTER, rndPos);
						if(spawnlistentry != null) {
							EntityLiving entityliving;

							try {
								entityliving = spawnlistentry.entityClass.getConstructor(new Class[] {World.class}).newInstance(new Object[] {wrld});
								entityliving.setLocationAndAngles((double)rndOffsetX+0.5F, rndOffsetY, rndOffsetZ+0.5D, wrld.rand.nextFloat()*360.0F, 0.0F);
								Result canSpawn = ForgeEventFactory.canEntitySpawn(entityliving, wrld, rndOffsetX+0.5F, rndOffsetY, rndOffsetZ+0.5F);
								if (canSpawn == Result.ALLOW || (canSpawn == Result.DEFAULT && entityliving.getCanSpawnHere())) {
									wrld.spawnEntity(entityliving);
									setMRU(getMRU() - mruUsage);
									if(generatesCorruption)
										ECUtils.increaseCorruptionAt(getWorld(), pos.getX(), pos.getY(), pos.getZ(), getWorld().rand.nextInt(genCorruption));
									if(!ForgeEventFactory.doSpecialSpawn(entityliving, wrld, rndOffsetX+0.5F, rndOffsetY, rndOffsetZ+0.5F)) {
										ientitylivingdata = entityliving.onInitialSpawn(getWorld().getDifficultyForLocation(rndPos), ientitylivingdata);
									}
								}
							}
							catch (Exception exception) {
								exception.printStackTrace();
								return;
							}
						}
					}
				}
			}
			else if(getStackInSlot(1).getItem() instanceof ItemCollectedMonsterSpawner && enableMobSpawners) {
				if(innerRotation >= mobSpanwerDelay && getMRU() > mruUsage) {
					NBTTagCompound tag = MiscUtils.getStackTag(getStackInSlot(1));
					if(tag.hasKey("monsterSpawner")) {
						NBTTagCompound spTag = tag.getCompoundTag("monsterSpawner");
						NBTTagCompound mobTag = spTag.getCompoundTag("SpawnData");
						String id = mobTag.getString("id");
						innerRotation = 0;
						int metadata = getStackInSlot(1).getItemDamage();
						Entity base = EntityList.createEntityByName(id, getWorld());
						if(base != null && base instanceof EntityLiving) {
							EntityLiving entityliving = (EntityLiving) base;
							int rndOffsetX = (int)(pos.getX() + MathUtils.randomDouble(getWorld().rand)*mobSpawnerRadius);
							int rndOffsetY = (int)(pos.getY() + MathUtils.randomDouble(getWorld().rand));
							int rndOffsetZ = (int)(pos.getZ() + MathUtils.randomDouble(getWorld().rand)*mobSpawnerRadius);
							entityliving.setLocationAndAngles(rndOffsetX+0.5D, rndOffsetY, rndOffsetZ+0.5D, getWorld().rand.nextFloat()*360.0F, 0.0F);
							if(entityliving.getCanSpawnHere()) {
								if(!getWorld().isRemote)
									getWorld().spawnEntity(entityliving);

								getWorld().playEvent(2004, pos, 0);

								if(entityliving != null)
									entityliving.spawnExplosionParticle();
								setMRU(getMRU() - mruUsage);
								if(generatesCorruption)
									ECUtils.increaseCorruptionAt(getWorld(), pos.getX(), pos.getY(), pos.getZ(), getWorld().rand.nextInt(genCorruption));
							}
						}
					}
				}
			}
		}
		ECUtils.manage(this, 0);
		super.update();

		//The following code is to make the secret record acquirable
		List<EntityPlayer> playerlist = getWorld().getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(pos).expandXyz(8));
		if(!playerlist.isEmpty()) {
			if(world.rand.nextFloat() < 0.0001F) {
				EntityItem ei = new EntityItem(world, pos.getX()+0.5, pos.getY()+1, pos.getZ()+0.5, new ItemStack(ItemsCore.secret,1,0));
				getWorld().spawnEntity(ei);
			}
		}
	}

	public static void setupConfig(Configuration cfg) {
		try {
			cfg.load();
			String[] cfgArrayString = cfg.getStringList("DarknessObeliskSettings", "tileentities", new String[] {
					"Max MRU:" + ApiCore.DEVICE_MAX_MRU_GENERIC,
					"MRU Usage:500",
					"Ticks required to try to create an entity:100",
					"Can this device actually generate corruption:false",
					"The amount of corruption generated each tick(do not set to 0!):30",
					"A chance per tick to try spawning a mob without a spawner:0.1",
					"Enable mob spawning using spawner:true",
					"Radius for a spawner:3",
					"Radius for no spawner:8"
			}, "");
			String dataString = "";

			for(int i = 0; i < cfgArrayString.length; ++i)
				dataString += "||" + cfgArrayString[i];

			DummyData[] data = DataStorage.parseData(dataString);

			mruUsage = Integer.parseInt(data[1].fieldValue);
			mobSpanwerDelay = Integer.parseInt(data[2].fieldValue);
			cfgMaxMRU = Float.parseFloat(data[0].fieldValue);
			generatesCorruption = Boolean.parseBoolean(data[3].fieldValue);
			genCorruption = Integer.parseInt(data[4].fieldValue);
			worldSpawnChance = Float.parseFloat(data[5].fieldValue);
			enableMobSpawners = Boolean.parseBoolean(data[6].fieldValue);
			mobSpawnerRadius = Integer.parseInt(data[7].fieldValue);
			worldSpawnerRadius = Integer.parseInt(data[8].fieldValue);

			cfg.save();
		}
		catch(Exception e) {
			return;
		}
	}

	@Override
	public int[] getOutputSlots() {
		return new int[0];
	}
}
