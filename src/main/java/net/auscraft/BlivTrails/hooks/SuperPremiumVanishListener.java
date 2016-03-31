package net.auscraft.BlivTrails.hooks;

import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import net.auscraft.BlivTrails.TrailManager;
import net.auscraft.BlivTrails.util.BUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Created by OhBlihv (Chris) on 31/03/2016.
 * This file is part of the project BlivTrails
 */
public class SuperPremiumVanishListener extends VanishListener implements Listener
{

	public SuperPremiumVanishListener()
	{
		TrailManager.setVanishHook(TrailManager.VanishHook.SUPER_PREMIUM_VANISH);
		BUtil.logInfo("Super/PremiumVanish loaded | Hooking...");
	}

	@EventHandler
	public void onVanish(PlayerHideEvent event)
	{
		onVanishEvent(event.getPlayer(), true);
	}

	@EventHandler
	public void onReAppear(PlayerShowEvent event)
	{
		onVanishEvent(event.getPlayer(), false);
	}

}
