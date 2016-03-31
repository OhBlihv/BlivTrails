package net.auscraft.BlivTrails.hooks;

import net.auscraft.BlivTrails.TrailManager;
import net.auscraft.BlivTrails.util.BUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.kitteh.vanish.event.VanishStatusChangeEvent;

public class VanishListener implements Listener
{

	public VanishListener()
	{
		TrailManager.setVanishEnabled(true);
		TrailManager.setVanishHook(1);
		BUtil.logInfo("VanishNoPacket loaded | Hooking...");
	}

	@EventHandler
	public void onVanish(VanishStatusChangeEvent event)
	{
		if (TrailManager.getTrailMap().containsKey(event.getPlayer().getUniqueId()))
		{
			TrailManager.getTrailMap().get(event.getPlayer().getUniqueId()).setVanished(event.isVanishing());
			if (event.isVanishing())
			{
				try
				{
					Bukkit.getScheduler().cancelTask(TrailManager.getTaskMap().get(event.getPlayer().getUniqueId()));
				}
				catch (NullPointerException e)
				{
					// Player has no trail, or just joined.
				}
			}
		}
	}

}
