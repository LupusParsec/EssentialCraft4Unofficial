package essentialcraft.common.tile;

import DummyCore.Utils.DataStorage;
import DummyCore.Utils.DummyData;
import DummyCore.Utils.MiscUtils;
import DummyCore.Utils.Notifier;
import DummyCore.Utils.TileStatTracker;
import essentialcraft.common.capabilities.espe.CapabilityESPEHandler;
import essentialcraft.common.capabilities.espe.ESPEStorage;
import essentialcraft.common.mod.EssentialCraftCore;
import essentialcraft.utils.common.ECUtils;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.config.Configuration;

public class TileMithrilineCrystal extends TileEntity implements ITickable {

	public static double energyEachTick = 0.025D;
	public static double energyEachTick_End = 0.1D;
	public static double maxEnergy = 10000D;
	public static boolean requiresUnobstructedSky = true;
	private TileStatTracker tracker;
	public int syncTick = 10;
	public boolean requestSync = true;
	protected ESPEStorage espeStorage = new ESPEStorage(maxEnergy);

	public TileMithrilineCrystal() {
		super();
		tracker = new TileStatTracker(this);
	}

	public TileMithrilineCrystal(int tier) {
		this();
		espeStorage.setTier(tier);
	}

	@Override
	public void readFromNBT(NBTTagCompound i) {
		super.readFromNBT(i);
		espeStorage.readFromNBT(i);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound i) {
		super.writeToNBT(i);
		espeStorage.writeToNBT(i);
		return i;
	}

	@Override
	public void update() {
		if(syncTick == 0) {
			if(tracker == null)
				Notifier.notifyCustomMod("EssentialCraft", "[WARNING][SEVERE]TileEntity " + this + " at pos " + pos.getX() + "," + pos.getY() + "," + pos.getZ() + " tries to sync itself, but has no TileTracker attached to it! SEND THIS MESSAGE TO THE DEVELOPER OF THE MOD!");
			else if(!getWorld().isRemote && tracker.tileNeedsSyncing()) {
				MiscUtils.sendPacketToAllAround(getWorld(), getUpdatePacket(), pos.getX(), pos.getY(), pos.getZ(), getWorld().provider.getDimension(), 32);
			}
			syncTick = 20;
		}
		else
			--syncTick;

		boolean hasSky = getWorld().canBlockSeeSky(pos.up(3)) || !requiresUnobstructedSky;
		if(hasSky) {
			double energyGenerated = getWorld().provider != null && getWorld().provider.getDimension() == 1 ? energyEachTick_End : energyEachTick;
			espeStorage.addESPE(energyGenerated, true);
		}

		if(requestSync && getWorld().isRemote) {
			requestSync = false;
			ECUtils.requestScheduledTileSync(this, EssentialCraftCore.proxy.getClientPlayer());
		}
	}

	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		writeToNBT(nbttagcompound);
		return new SPacketUpdateTileEntity(pos, -10, nbttagcompound);
	}

	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		if(pkt.getTileEntityType() == -10)
			readFromNBT(pkt.getNbtCompound());
	}

	public static void setupConfig(Configuration cfg) {
		try {
			cfg.load();
			String[] cfgArrayString = cfg.getStringList("MithrilineCrystalSettings", "tileentities", new String[] {
					"Enderstar pulse generated each tick:0.025",
					"Enderstar pulse generated each tick in the end:0.1",
					"Requires a direct view to the sky:true",
					"Maximum enderstar pulse stored:10000"
			}, "");
			String dataString="";

			for(int i = 0; i < cfgArrayString.length; ++i)
				dataString += "||" + cfgArrayString[i];

			DummyData[] data = DataStorage.parseData(dataString);

			energyEachTick = Double.parseDouble(data[0].fieldValue);
			energyEachTick_End = Double.parseDouble(data[1].fieldValue);
			requiresUnobstructedSky = Boolean.parseBoolean(data[2].fieldValue);
			maxEnergy = Double.parseDouble(data[3].fieldValue);

			cfg.save();
		}
		catch(Exception e) {
			return;
		}
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		AxisAlignedBB bb = new AxisAlignedBB(pos, pos.add(1, 3, 1));
		return bb;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityESPEHandler.ESPE_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == CapabilityESPEHandler.ESPE_HANDLER_CAPABILITY ? (T)espeStorage : super.getCapability(capability, facing);
	}
}
