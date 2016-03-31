package net.auscraft.BlivTrails.hooks;

import net.auscraft.BlivTrails.TrailManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Created by OhBlihv (Chris) on 31/03/2016.
 * This file is part of the project BlivTrails
 */
public abstract class VanishListener
{

	public void onVanishEvent(Player player, boolean isVanishing)
	{
		if (TrailManager.getTrailMap().containsKey(player.getUniqueId()))
		{
			TrailManager.getTrailMap().get(player.getUniqueId()).setVanished(isVanishing);
			if (isVanishing)
			{
				try
				{
					Bukkit.getScheduler().cancelTask(TrailManager.getTaskMap().get(player.getUniqueId()));
				}
				catch (NullPointerException e)
				{
					// Player has no trail, or just joined.
				}
			}
		}
	}

}
