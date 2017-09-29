package ec3.common.tile;

import ec3.common.inventory.InventoryMagicFilter;
import ec3.common.item.ItemFilter;
import ec3.common.item.ItemsCore;
import ec3.utils.common.ECUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class TileNewMIMImportNode extends TileMRUGeneric {
	final Capability<IItemHandler> ITEM_HANDLER_CAPABILITY = CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;

	public TileNewMIMImportNode() {
		setMaxMRU(0);
		setSlotsNum(1);
		slot0IsBoundGem = false;
	}

	@Override
	public boolean isItemValidForSlot(int p_94041_1_, ItemStack p_94041_2_) {
		return p_94041_2_.getItem() == ItemsCore.filter;
	}

	public EnumFacing getRotation() {
		int metadata = this.getBlockMetadata();
		metadata %= 6;
		return EnumFacing.getFront(metadata);
	}

	@Override
	public int[] getOutputSlots() {
		return new int[0];
	}

	public IItemHandler getConnectedInventory() {
		EnumFacing side = getRotation();
		if(getWorld().getTileEntity(pos.offset(side)) != null) {
			TileEntity tile = getWorld().getTileEntity(pos.offset(side));
			if(tile.hasCapability(ITEM_HANDLER_CAPABILITY, side.getOpposite()))
				return tile.getCapability(ITEM_HANDLER_CAPABILITY, side.getOpposite());
		}

		return null;
	}

	public IItemHandler getConnectedInventoryNonSided() {
		EnumFacing side = getRotation();
		if(getWorld().getTileEntity(pos.offset(side)) != null) {
			TileEntity tile = getWorld().getTileEntity(pos.offset(side));
			if(tile.hasCapability(ITEM_HANDLER_CAPABILITY, null))
				return tile.getCapability(ITEM_HANDLER_CAPABILITY, null);
		}

		return null;
	}

	public void importAllPossibleItems(TileNewMIM parent) {
		if(getWorld().isBlockIndirectlyGettingPowered(pos) > 0)
			return;

		IItemHandler inv = getConnectedInventory();
		if(inv == null) {
			IItemHandler iinv = getConnectedInventoryNonSided();
		}
		int slots = inv.getSlots();

		if(slots <= 0)
			return;

		for(int j = 0; j < slots; ++j) {
			ItemStack stk = inv.getStackInSlot(j);
			if(stk != null) {
				if(getStackInSlot(0) == null || !(getStackInSlot(0).getItem() instanceof ItemFilter)) {
					if(parent.addItemStackToSystem(stk.copy()))
						inv.extractItem(j, stk.stackSize, false);
				}
				else if(ECUtils.canFilterAcceptItem(new InventoryMagicFilter(getStackInSlot(0)), stk, getStackInSlot(0))) {
					if(parent.addItemStackToSystem(stk.copy()))
						inv.extractItem(j, stk.stackSize, false);
				}
			}
		}
	}
}
