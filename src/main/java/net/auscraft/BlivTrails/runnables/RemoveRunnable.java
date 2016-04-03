package net.auscraft.BlivTrails.runnables;

import com.darkblade12.ParticleEffect.ParticleEffect;
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
		trailMap.put(uuid, new PlayerConfig(uuid, ParticleEffect.FOOTSTEP, 0, 0, 0, 0));
		if(TrailManager.getTaskMap().containsKey(uuid))
		{
			Bukkit.getServer().getScheduler().cancelTask(TrailManager.getTaskMap().remove(uuid));
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
