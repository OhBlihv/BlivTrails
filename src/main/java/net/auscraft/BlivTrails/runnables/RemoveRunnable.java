package net.auscraft.BlivTrails.runnables;

import net.auscraft.BlivTrails.PlayerConfig;
import net.auscraft.BlivTrails.TrailManager;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by OhBlihv (Chris) on 4/3/2016.
 * This file is part of the project BlivTrails
 */
public class RemoveRunnable extends MySQLRunnable
{

	private PlayerConfig playerConfig;

	public RemoveRunnable(UUID uuid)
	{
		super(uuid);

		this.playerConfig = TrailManager.getPlayerConfig(uuid);
	}

	public RemoveRunnable(UUID uuid, PlayerConfig playerConfig)
	{
		super(uuid);

		this.playerConfig = playerConfig;
	}

	@Override
	public void run()
	{
		if(playerConfig == null)
		{
			return;
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
