package net.auscraft.BlivTrails.runnables;

import net.auscraft.BlivTrails.PlayerConfig;
import net.auscraft.BlivTrails.storage.ParticleData;

import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by OhBlihv (Chris) on 4/3/2016.
 * This file is part of the project BlivTrails
 */
public class SaveRunnable extends MySQLRunnable
{

	private final PlayerConfig playerConfig;

	public SaveRunnable(UUID uuid, PlayerConfig playerConfig)
	{
		super(uuid);

		this.playerConfig = playerConfig;
	}

	@Override
	public void run()
	{
		try
		{
			ParticleData particleData = instance.getParticleStorage().queryForId(uuidBytes);

			if (particleData == null)
			{
				instance.getParticleStorage().create(new ParticleData(uuidBytes, playerConfig.getParticle().toString(), playerConfig.getType(), playerConfig.getLength(), playerConfig.getHeight(), playerConfig.getColour()));
			}
			else
			{
				particleData.setParticle(playerConfig.getParticle().toString());
				particleData.setType(playerConfig.getType());
				particleData.setLength(playerConfig.getLength());
				particleData.setHeight(playerConfig.getHeight());
				particleData.setColour(playerConfig.getColour());
				instance.getParticleStorage().update(particleData);
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
	}
}
