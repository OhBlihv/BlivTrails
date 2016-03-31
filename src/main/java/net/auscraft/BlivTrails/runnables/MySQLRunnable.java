package net.auscraft.BlivTrails.runnables;

import com.darkblade12.ParticleEffect.ParticleEffect;
import de.myzelyam.api.vanish.VanishAPI;
import net.auscraft.BlivTrails.BlivTrails;
import net.auscraft.BlivTrails.PlayerConfig;
import net.auscraft.BlivTrails.TrailManager;
import net.auscraft.BlivTrails.storage.ParticleData;
import net.auscraft.BlivTrails.util.UUIDUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MySQLRunnable implements Runnable
{

	private UUID uuid;
	private PlayerConfig pcfg;
	private BlivTrails instance;
	private ConcurrentHashMap<UUID, PlayerConfig> trailMap = null;
	private TrailManager.VanishHook vanishHook;
	private Player player;

	// Control flag to determine if the runnable is saving or loading
	//Save == 0
	//Load == 1
	//Remove == 2
	short process;

	public MySQLRunnable(UUID uuid, PlayerConfig pcfg, short process, ConcurrentHashMap<UUID, PlayerConfig> trailMap, BlivTrails instance)
	{
		this.uuid = uuid;
		this.process = process;
		this.instance = instance;
		if (process == 0) // Seperate save/load sources
		{
			this.pcfg = pcfg;
		}
		else if (process == 1)
		{
			this.trailMap = trailMap;

			vanishHook = TrailManager.getVanishHook();
			player = Bukkit.getPlayer(uuid);
		}
		else
		{
			this.pcfg = pcfg;
			this.trailMap = trailMap;
		}
	}

	public void run()
	{
		byte[] uuidBytes = UUIDUtils.toBytes(uuid);
		
		//Save
		if (process == 0)
		{
			try
			{
				ParticleData particleData = instance.getParticleStorage().queryForId(uuidBytes);

				if (particleData == null)
				{
					instance.getParticleStorage().create(new ParticleData(uuidBytes, pcfg.getParticle().toString(), pcfg.getType(), pcfg.getLength(), pcfg.getHeight(), pcfg.getColour()));
				}
				else
				{
					particleData.setParticle(pcfg.getParticle().toString());
					particleData.setType(pcfg.getType());
					particleData.setLength(pcfg.getLength());
					particleData.setHeight(pcfg.getHeight());
					particleData.setColour(pcfg.getColour());
					instance.getParticleStorage().update(particleData);
				}
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}

		}
		//Load
		else if (process == 1)
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
				return;

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
		//Remove
		else
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
}
