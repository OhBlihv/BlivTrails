package net.auscraft.BlivTrails.runnables;

import com.darkblade12.ParticleEffect.ParticleEffect;
import net.auscraft.BlivTrails.BlivTrails;
import net.auscraft.BlivTrails.PlayerConfig;
import net.auscraft.BlivTrails.storage.ParticleData;
import net.auscraft.BlivTrails.utils.UUIDUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class MySQLRunnable implements Runnable
{

	private String uuid;
	private PlayerConfig pcfg;
	private BlivTrails instance;
	private ConcurrentHashMap<String, PlayerConfig> trailMap = null;
	private boolean vanishEnabled;
	private int vanishHook;
	private Player player;

	// Control flag to determine if the runnable is saving or loading
	short process;

	public MySQLRunnable(String uuid, PlayerConfig pcfg, short process, ConcurrentHashMap<String, PlayerConfig> trailMap, BlivTrails instance)
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

			vanishEnabled = instance.getListener().vanishEnabled();
			vanishHook = instance.getListener().vanishHook();
			player = Bukkit.getPlayer(UUID.fromString(uuid));
		}
		else
		{
			this.pcfg = pcfg;
			this.trailMap = trailMap;
		}
	}

	public void run()
	{
		byte[] uuidBytes = UUIDUtils.toBytes(UUID.fromString(uuid));
		
		//Save
		if (process == 0)
		{
			try
			{
				ParticleData particleData = instance.getParticleStorage().queryForId(uuidBytes);

				if (particleData == null)
				{
					instance.getParticleStorage().create(new ParticleData(uuidBytes, pcfg.getParticle().toString(), pcfg.getLength(), pcfg.getHeight(), pcfg.getColour()));
				}
				else
				{
					particleData.setParticle(pcfg.getParticle().toString());
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
			if (vanishEnabled)
			{
				if (vanishHook == 1) // VanishNoPacket
				{
					// if(isVanished(player))
					if (player.hasPermission("vanish.silentjoin"))
					{
						if (trailMap.containsKey(player.getUniqueId().toString()))
						{
							trailMap.get(player.getUniqueId().toString()).setVanish(true);
							try
							{
								Bukkit.getServer().getScheduler().cancelTask(instance.getListener().getActiveTrails().get(player.getUniqueId().toString()));
								instance.getListener().getActiveTrails().remove(player.getUniqueId().toString());
								// util.logDebug("Player has had their trail hidden");
							}
							catch (NullPointerException e)
							{
								// Player doesnt have an active trail to hide
								// e.printStackTrace();
							}
						}
					}
				}
			}
		}
		//Remove
		else
		{
			trailMap.put(uuid.toString(), new PlayerConfig(uuid, ParticleEffect.FOOTSTEP, 0, 0, 0, 0));
			if(instance.getListener().getActiveTrails().containsKey(uuid))
			{
				Bukkit.getServer().getScheduler().cancelTask(instance.getListener().getActiveTrails().remove(uuid));
			}
			
			try
			{
				instance.getParticleStorage().deleteById(uuidBytes);
			}
			catch (SQLException e)
			{
				e.printStackTrace();
			}
			//new CallablePrintout(UUID.fromString(uuid), "messages.generic.force-remove-receive");
		}
	}
}
