package essentialcraft.client.gui;

import DummyCore.Client.GuiCommon;
import essentialcraft.client.gui.element.GuiBalanceState;
import essentialcraft.client.gui.element.GuiBoundGemState;
import essentialcraft.client.gui.element.GuiMRUState;
import essentialcraft.client.gui.element.GuiMRUStorage;
import net.minecraft.inventory.Container;
import net.minecraft.tileentity.TileEntity;

public class GuiMRUChunkLoader extends GuiCommon {

	public GuiMRUChunkLoader(Container c, TileEntity tile) {
		super(c,tile);
		this.elementList.add(new GuiMRUStorage(7, 4, tile));
		this.elementList.add(new GuiBoundGemState(88, 4, tile,0));
		this.elementList.add(new GuiBalanceState(46, 31, tile));
		this.elementList.add(new GuiMRUState(25, 58, tile,0));
	}
}
