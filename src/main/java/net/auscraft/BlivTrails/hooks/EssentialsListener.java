package net.auscraft.BlivTrails.hooks;

import net.auscraft.BlivTrails.BlivTrails;
import net.auscraft.BlivTrails.PlayerConfig;
import net.auscraft.BlivTrails.TrailListener;
import net.auscraft.BlivTrails.utils.Utilities;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class EssentialsListener implements Listener
{

	private TrailListener listener;

	public EssentialsListener(BlivTrails instance)
	{
		listener = instance.getListener();
		listener.vanishEnabled(true);
		listener.vanishHook(2);
		Utilities.logInfo("Essentials loaded | (Limited Support) Hooking...");
	}

	@EventHandler
	public void vanishCommand(PlayerCommandPreprocessEvent event)
	{
		String cmd = event.getMessage();
		// if(cmd.contains("^(evanish|vanish|ev|essentials:vanish)$"))
		if (cmd.equals("/v") || cmd.equals("/evanish") || cmd.equals("/vanish") || cmd.equals("/ev") || cmd.equals("/essentials:vanish"))
		{
			if (listener.getPlayerConfig().containsKey(event.getPlayer().getUniqueId().toString()))
			{
				PlayerConfig pcfg = listener.getPlayerConfig().get(event.getPlayer().getUniqueId().toString());
				if (pcfg.isVanished())
				{
					pcfg.setVanished(false);
				}
				else
				{
					pcfg.setVanished(true);
				}
			}
		}
	}

}
