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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

import static net.auscraft.BlivTrails.BlivTrails.rand;
import static net.auscraft.BlivTrails.TrailManager.saveTrail;

/**
 * Created by OhBlihv (Chris) on 31/03/2016.
 * This file is part of the project BlivTrails
 */
public class TrailListener implements Listener
{

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		if (TrailManager.getTrailMap().containsKey(player.getUniqueId()))
		{
			BUtil.logDebug(player.getName() + " has a trail.");
			saveTrail(player);
			TrailManager.getTrailMap().remove(player.getUniqueId());
		}
		else
		{
			BUtil.logDebug(player.getName() + " doesn't have an active trail");
		}
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
				PlayerConfig pcfg = TrailManager.getTrailMap().get(uuid);
				if (TrailManager.isVanishEnabled() && pcfg.isVanished())
				{
					return; // If Vanished, don't do the trail.
				}

				if (pcfg.getParticle().equals(ParticleEffect.FOOTSTEP))
				{
					TrailManager.removePlayer(uuid);
					return;
				}

				int speed = TrailDefaults.getDefaultSpeed();
				if (speed == 0) // If config option is not set, will default to 1
				{
					speed = 1;
				}

				TrailDefaults.ParticleDefaultStorage particleDefaults = TrailDefaults.getDefaults(pcfg.getParticle());
				if (particleDefaults.getDisplaySpeed() != 0)
				{
					speed = particleDefaults.getDisplaySpeed();
				}

				// public TrailRunnable(BlivTrails instance, Player player, PlayerConfig pcfg, TrailManager listener, Random rand, double[] option)
				TrailManager.getTaskMap().put(uuid, Bukkit.getScheduler().runTaskTimerAsynchronously(BlivTrails.getInstance(), new TrailRunnable(event.getPlayer(), pcfg, rand, TrailManager.getOption()), 0L, speed).getTaskId());
				TrailManager.getTrailTime().put(uuid, TrailManager.getTrailLength());
			}
			else
			{
				TrailManager.getTrailTime().replace(uuid, TrailManager.getTrailLength());
			}
		}
	}

}
