package net.auscraft.BlivTrails.hooks;

import net.auscraft.BlivTrails.TrailManager;
import net.auscraft.BlivTrails.util.BUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.kitteh.vanish.event.VanishStatusChangeEvent;

public class VanishNoPacketListener extends VanishListener implements Listener
{

	public VanishNoPacketListener()
	{
		TrailManager.setVanishHook(TrailManager.VanishHook.VANISH_NO_PACKET);
		BUtil.logInfo("VanishNoPacket loaded | Hooking...");
	}

	@EventHandler
	public void onVanish(VanishStatusChangeEvent event)
	{
		onVanishEvent(event.getPlayer(), event.isVanishing());
	}

}
