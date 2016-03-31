package net.auscraft.BlivTrails.hooks;

import net.auscraft.BlivTrails.PlayerConfig;
import net.auscraft.BlivTrails.TrailManager;
import net.auscraft.BlivTrails.util.BUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class EssentialsListener implements Listener
{

	public EssentialsListener()
	{
		TrailManager.setVanishHook(TrailManager.VanishHook.ESSENTIALS);
		BUtil.logInfo("Essentials loaded | (Limited Support) Hooking...");
	}

	@EventHandler
	public void vanishCommand(PlayerCommandPreprocessEvent event)
	{
		String cmd = event.getMessage();
		// if(cmd.contains("^(evanish|vanish|ev|essentials:vanish)$"))
		if (cmd.equals("/v") || cmd.equals("/evanish") || cmd.equals("/vanish") || cmd.equals("/ev") || cmd.equals("/essentials:vanish"))
		{
			if (TrailManager.getTrailMap().containsKey(event.getPlayer().getUniqueId()))
			{
				PlayerConfig pcfg = TrailManager.getTrailMap().get(event.getPlayer().getUniqueId());
				pcfg.setVanished(!pcfg.isVanished());
			}
		}
	}

}
