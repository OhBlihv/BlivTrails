package net.auscraft.BlivTrails;

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
	}
	
	@EventHandler
	public void onVanish(VanishStatusChangeEvent event)
	{
		if(listener.getPlayerConfig().containsKey(event.getPlayer().getUniqueId().toString()))
		{
			listener.getPlayerConfig().get(event.getPlayer().getUniqueId().toString()).setVanish(event.isVanishing());
		}
	}
	
}
