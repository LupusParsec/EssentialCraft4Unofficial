package ec3.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/**
 *
 * @author Modbder
 * @Description use this interface to create new blocks for the heat generator to operate with
 *
 */
public interface IHotBlock {

	/**
	 * This is used to check how the mru gain will get affected(it is basically a multiplier)
	 * @return the multiplier of MRU gain
	 */
	public float getHeatModifier(IBlockAccess w, BlockPos pos);

}
