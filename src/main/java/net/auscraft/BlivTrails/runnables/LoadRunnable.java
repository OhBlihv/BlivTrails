package net.auscraft.BlivTrails.runnables;

import com.darkblade12.ParticleEffect.ParticleEffect;
import de.myzelyam.api.vanish.VanishAPI;
import net.auscraft.BlivTrails.OptionType;
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

		PlayerConfig playerConfig =  new PlayerConfig(uuid, particleEff,
		                                              OptionType.parseTypeInt(particleData.getType()),
		                                              OptionType.parseLengthInt(particleData.getLength()),
		                                              OptionType.parseHeightInt(particleData.getHeight()),
		                                              particleData.getColour());

		TrailManager.getTrailMap().put(uuid, playerConfig);

		if (TrailManager.hasVanishHook())
		{
			TrailManager.VanishHook vanishHook = TrailManager.getVanishHook();

			boolean shouldBeVanished = false;
			switch(vanishHook)
			{
				case VANISH_NO_PACKET:
				{
					shouldBeVanished = player.hasPermission("vanish.silentjoin");
					break;
				}
				case SUPER_PREMIUM_VANISH:
				{
					shouldBeVanished = VanishAPI.isInvisible(player);
					break;
				}
			}

			if(shouldBeVanished)
			{
				playerConfig.setVanished(true);
				if(playerConfig.isScheduled())
				{
					Bukkit.getScheduler().cancelTask(playerConfig.getTaskId());
				}
			}
		}
	}

}
