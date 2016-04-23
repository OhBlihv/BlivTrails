package net.auscraft.BlivTrails.listeners;

import com.darkblade12.ParticleEffect.ParticleEffect;
import net.auscraft.BlivTrails.BlivTrails;
import net.auscraft.BlivTrails.PlayerConfig;
import net.auscraft.BlivTrails.TrailManager;
import net.auscraft.BlivTrails.config.TrailDefaults;
import net.auscraft.BlivTrails.runnables.TrailRunnable;
import net.auscraft.BlivTrails.util.BUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

import static net.auscraft.BlivTrails.TrailManager.loadTrail;
import static net.auscraft.BlivTrails.TrailManager.saveTrail;

/**
 * Created by OhBlihv (Chris) on 31/03/2016.
 * This file is part of the project BlivTrails
 */
public class TrailListener implements Listener
{

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		final Player player = event.getPlayer();
		Bukkit.getScheduler().runTaskLater(BlivTrails.getInstance(), new Runnable()
		{

			@Override
			public void run()
			{
				loadTrail(player);
				// Wait a few seconds for the async sql read to go through
			}

		}, 100L);
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		if (TrailManager.getTrailMap().containsKey(player.getUniqueId()))
		{
			//BUtil.logDebug(player.getName() + " has a trail.");
			saveTrail(player);
			TrailManager.getTrailMap().remove(player.getUniqueId());
		}
		/*else
		{
			BUtil.logDebug(player.getName() + " doesn't have an active trail");
		}*/
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event)
	{
		// Stop the trail from working while the player isn't technically moving
		if (event.getFrom().getX() == event.getTo().getX() && event.getFrom().getY() == event.getTo().getY() && event.getFrom().getZ() == event.getTo().getZ())
		{
			return;
		}

		UUID uuid = event.getPlayer().getUniqueId();
		if (TrailManager.getTrailMap().containsKey(uuid))
		{
			if (!TrailManager.getTaskMap().containsKey(uuid))
			{
				PlayerConfig playerConfig = TrailManager.getTrailMap().get(uuid);
				if (TrailManager.getVanishHook() != TrailManager.VanishHook.NONE && playerConfig.isVanished())
				{
					return; // If Vanished, don't do the trail.
				}

				if (playerConfig.getParticle().equals(ParticleEffect.FOOTSTEP))
				{
					TrailManager.removePlayer(uuid);
					return;
				}

				int speed = 0;
				TrailDefaults.ParticleDefaultStorage particleDefaults = TrailDefaults.getDefaults(playerConfig.getParticle());
				if (particleDefaults.getDisplaySpeed() != 0)
				{
					speed = particleDefaults.getDisplaySpeed();
				}

				if(speed == 0)
				{
					speed = 1;
				}

				TrailRunnable trailRunnable = new TrailRunnable(event.getPlayer(), playerConfig, TrailManager.getOption());
				//Fire a test particle to ensure we can actually display it with no errors
				try
				{
					trailRunnable.run();
				}
				catch(Exception e)
				{
					BUtil.logError("An error occurred while displaying " + event.getPlayer().getName() + "'s trail. It will be disabled.");
					e.printStackTrace();

					//Remove the PlayerConfig, since it is now invalid.
					TrailManager.getTrailMap().remove(event.getPlayer().getUniqueId());
					return;
				}

				// public TrailRunnable(BlivTrails instance, Player player, PlayerConfig playerConfig, TrailManager listener, Random rand, double[] option)
				TrailManager.getTaskMap().put(uuid, Bukkit.getScheduler().runTaskTimerAsynchronously(BlivTrails.getInstance(), trailRunnable, 1L, speed).getTaskId());
				TrailManager.getTrailTime().put(uuid, TrailManager.getTrailLength());
			}
			else
			{
				TrailManager.getTrailTime().replace(uuid, TrailManager.getTrailLength());
			}
		}
	}

}
