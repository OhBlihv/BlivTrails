package net.auscraft.BlivTrails.hooks;

import net.auscraft.BlivTrails.BlivTrails;
import net.auscraft.BlivTrails.TrailListener;
import net.auscraft.BlivTrails.utils.Utilities;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.kitteh.vanish.event.VanishStatusChangeEvent;

public class VanishListener implements Listener
{

	private TrailListener listener;

	public VanishListener(BlivTrails instance)
	{
		listener = instance.getListener();
		listener.vanishEnabled(true);
		listener.vanishHook(1);
		Utilities.logInfo("VanishNoPacket loaded | Hooking...");
	}

	@EventHandler
	public void onVanish(VanishStatusChangeEvent event)
	{
		if (listener.getPlayerConfig().containsKey(event.getPlayer().getUniqueId().toString()))
		{
			listener.getPlayerConfig().get(event.getPlayer().getUniqueId().toString()).setVanish(event.isVanishing());
			if (event.isVanishing())
			{
				try
				{
					Bukkit.getScheduler().cancelTask(listener.getActiveTrails().get(event.getPlayer().getUniqueId().toString()));
				}
				catch (NullPointerException e)
				{
					// Player has no trail, or just joined.
				}
			}
		}
	}

}
