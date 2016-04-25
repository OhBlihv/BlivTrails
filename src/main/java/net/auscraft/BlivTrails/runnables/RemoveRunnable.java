package net.auscraft.BlivTrails.runnables;

import net.auscraft.BlivTrails.PlayerConfig;
import net.auscraft.BlivTrails.TrailManager;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by OhBlihv (Chris) on 4/3/2016.
 * This file is part of the project BlivTrails
 */
public class RemoveRunnable extends MySQLRunnable
{

	public RemoveRunnable(UUID uuid)
	{
		super(uuid);
	}

	@Override
	public void run()
	{
		PlayerConfig playerConfig = TrailManager.getTrailMap().get(uuid);
		if(playerConfig == null)
		{
			return;
		}

		if(playerConfig.isScheduled())
		{
			Bukkit.getScheduler().cancelTask(playerConfig.getTaskId());
		}

		try
		{
			instance.getParticleStorage().deleteById(uuidBytes);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}

}
