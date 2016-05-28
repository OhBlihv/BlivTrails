package net.auscraft.BlivTrails.hooks;

import net.auscraft.BlivTrails.PlayerConfig;
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
		PlayerConfig playerConfig = TrailManager.getPlayerConfig(player.getUniqueId());
		if (playerConfig != null)
		{
			playerConfig.setVanished(isVanishing);
			if (isVanishing && playerConfig.isScheduled())
			{
				Bukkit.getScheduler().cancelTask(playerConfig.getTaskId());
			}
		}
	}

}
