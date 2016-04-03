package net.auscraft.BlivTrails.runnables;

import com.darkblade12.ParticleEffect.ParticleEffect;
import de.myzelyam.api.vanish.VanishAPI;
import net.auscraft.BlivTrails.PlayerConfig;
import net.auscraft.BlivTrails.TrailManager;
import net.auscraft.BlivTrails.storage.ParticleData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by OhBlihv (Chris) on 4/3/2016.
 * This file is part of the project BlivTrails
 */
public class LoadRunnable extends MySQLRunnable
{

	private final Player player;

	public LoadRunnable(UUID uuid)
	{
		super(uuid);

		player = Bukkit.getPlayer(uuid);
	}
	
	@Override
	public void run()
	{
		ParticleData particleData;
		try
		{
			particleData = instance.getParticleStorage().queryForId(uuidBytes);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			return;
		}

		if (particleData == null)
		{
			return;
		}

		ParticleEffect particleEff = null;
		for (ParticleEffect pEff : ParticleEffect.values())
		{
			if (pEff.toString().equals(particleData.getParticle()))
			{
				if (pEff.equals(ParticleEffect.FOOTSTEP))
				{
					return;
				}
				particleEff = pEff;
				break;
			}
		}

		trailMap.put(uuid, new PlayerConfig(uuid, particleEff, particleData.getType(), particleData.getLength(), particleData.getHeight(), particleData.getColour()));

		if (vanishHook != TrailManager.VanishHook.NONE)
		{
			switch(vanishHook)
			{
				case VANISH_NO_PACKET:
				{
					// if(isVanished(player))
					if (player.hasPermission("vanish.silentjoin"))
					{
						if (trailMap.containsKey(player.getUniqueId()))
						{
							trailMap.get(player.getUniqueId()).setVanished(true);
							try
							{
								TrailManager.getTaskMap().remove(player.getUniqueId());
							}
							catch (NullPointerException e)
							{
								// Player doesn't have an active trail to hide
								// e.printStackTrace();
							}
						}
					}
					break;
				}
				case SUPER_PREMIUM_VANISH:
				{
					if(VanishAPI.isInvisible(player))
					{
						if (trailMap.containsKey(player.getUniqueId()))
						{
							trailMap.get(player.getUniqueId()).setVanished(true);
							try
							{
								TrailManager.getTaskMap().remove(player.getUniqueId());
							}
							catch (NullPointerException e)
							{
								// Player doesn't have an active trail to hide
								// e.printStackTrace();
							}
						}
					}
					break;
				}
			}
		}
	}

}
