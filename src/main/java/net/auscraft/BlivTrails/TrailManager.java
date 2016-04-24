package net.auscraft.BlivTrails;

import com.darkblade12.ParticleEffect.ParticleEffect;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import lombok.Getter;
import lombok.Setter;
import net.auscraft.BlivTrails.config.FlatFile;
import net.auscraft.BlivTrails.config.FlatFileStorage;
import net.auscraft.BlivTrails.config.Messages;
import net.auscraft.BlivTrails.config.TrailDefaults;
import net.auscraft.BlivTrails.config.TrailDefaults.ParticleDefaultStorage;
import net.auscraft.BlivTrails.runnables.LoadRunnable;
import net.auscraft.BlivTrails.runnables.RemoveRunnable;
import net.auscraft.BlivTrails.runnables.SaveRunnable;
import net.auscraft.BlivTrails.util.BUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.IllegalPluginAccessException;
import org.bukkit.scheduler.BukkitScheduler;
import org.kitteh.vanish.VanishPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TrailManager
{

	public enum VanishHook
	{

		NONE(0),
		VANISH_NO_PACKET(1),
		ESSENTIALS(2),
		SUPER_PREMIUM_VANISH(3);

		@Getter
		int hookType;

		VanishHook(int hookType)
		{
			this.hookType = hookType;
		}

	}

	public static final ParticleEffect[] usedTrails = { ParticleEffect.VILLAGER_ANGRY, ParticleEffect.BARRIER, ParticleEffect.CLOUD, ParticleEffect.CRIT,
			ParticleEffect.CRIT_MAGIC, ParticleEffect.DRIP_LAVA, ParticleEffect.DRIP_WATER, ParticleEffect.ENCHANTMENT_TABLE, ParticleEffect.EXPLOSION_NORMAL,
			ParticleEffect.FIREWORKS_SPARK, ParticleEffect.FLAME, ParticleEffect.HEART, ParticleEffect.LAVA, ParticleEffect.NOTE, ParticleEffect.PORTAL,
			ParticleEffect.REDSTONE, ParticleEffect.SLIME, ParticleEffect.SMOKE_LARGE, ParticleEffect.SNOW_SHOVEL, ParticleEffect.SNOWBALL,
			ParticleEffect.SPELL, ParticleEffect.SPELL_INSTANT, ParticleEffect.SPELL_MOB, ParticleEffect.SPELL_WITCH, ParticleEffect.TOWN_AURA,
			ParticleEffect.VILLAGER_HAPPY, ParticleEffect.WATER_DROP, ParticleEffect.WATER_SPLASH,
	        //1.9 Trails
			ParticleEffect.END_ROD, ParticleEffect.DRAGON_BREATH, ParticleEffect.DAMAGE_INDICATOR, ParticleEffect.SWEEP_ATTACK};

	@Getter
	private static ConcurrentHashMap<UUID, PlayerConfig> trailMap = new ConcurrentHashMap<>();
	@Getter
	private static ConcurrentHashMap<UUID, Integer> taskMap = new ConcurrentHashMap<>();
	@Getter
	private static ConcurrentHashMap<UUID, Float> trailTime = new ConcurrentHashMap<>();

	private static FlatFileStorage flatFileStorage = null;
	private static Messages msg;

	@Getter
	private static double option[];

	@Getter
	private static float trailLength;

	private static BukkitScheduler scheduler;

	/*
	 * vanishHook 0 = disabled 1 = VanishNoPacket 2 = Essentials Vanish
	 */
	@Getter
	@Setter
	private static VanishHook vanishHook = VanishHook.NONE;

	//Prevent accidental construction
	private TrailManager()
	{
		//NOOP
	}

	public static void init()
	{
		loadDefaultOptions();
		scheduler = Bukkit.getScheduler();

		FlatFile cfg = FlatFile.getInstance();
		msg = Messages.getInstance();

		trailLength = cfg.getFloat("trails.scheduler.trail-length");

		Object saveLoc = BlivTrails.getSave();
		if (!(saveLoc instanceof JdbcPooledConnectionSource))
		{
			flatFileStorage = (FlatFileStorage) saveLoc;
		}

		for (Player player : Bukkit.getOnlinePlayers())
		{
			loadTrail(player);
		}
	}

	public static void doDisable()
	{
		for (Player player : Bukkit.getOnlinePlayers())
		{
			if (trailMap.containsKey(player.getUniqueId()))
			{
				saveTrail(player);
			}
		}
	}

	public static void loadDefaultOptions()
	{
		FlatFile cfg = FlatFile.getInstance();
		/*
		 * option[0] = random.x-variation 
		 * option[1] = random.y-variation
		 * option[2] = random.z-variation
		 * 
		 * option[3] = dynamic.spray-variation
		 * 
		 * option[4] = height.feet-location
		 * option[5] = height.waist-location
		 * option[6] = height.halo-location
		 */
		option = new double[7];
		option[0] = cfg.getDouble("trails.defaults.type.random.x-variation");
		option[1] = cfg.getDouble("trails.defaults.type.random.y-variation");
		option[2] = cfg.getDouble("trails.defaults.type.random.z-variation");

		option[3] = cfg.getDouble("trails.defaults.type.dynamic.spray-variation");

		option[4] = cfg.getDouble("trails.defaults.height.feet-location");
		option[5] = cfg.getDouble("trails.defaults.height.waist-location");
		option[6] = cfg.getDouble("trails.defaults.height.halo-location");
		
		BUtil.logInfo("Finished Loading Defaults!");
	}

	public static void doDefaultTrail(UUID uuid, ParticleEffect particle)
	{
		ParticleDefaultStorage particleDefaults = TrailDefaults.getDefaults(particle);

		// Trail for the first time
		String trailName = particleDefaults.getDisplayName();

		//TODO: Include global variable
		if (!FlatFile.getInstance().getBoolean("misc.trail-name-colour"))
		{
			trailName = BUtil.stripColours(trailName);
		}

		BUtil.printPlain(Bukkit.getPlayer(uuid), msg.getString("messages.generic.trail-applied").replace("%trail%", trailName));

		trailMap.put(uuid, new PlayerConfig(uuid, particle, particleDefaults.getType(), particleDefaults.getLength(), particleDefaults.getHeight(), particleDefaults.getColour()));
		try
		{
			scheduler.cancelTask(taskMap.get(uuid));
		}
		catch (NullPointerException e)
		{
			//
		}

		taskMap.remove(uuid);
		trailTime.remove(uuid);
		BUtil.logDebug(trailMap.get(uuid).getParticle().getName());
	}

	public static void loadTrail(Player player)
	{
		if (flatFileStorage == null)
		{
			try
			{
				scheduler.runTaskAsynchronously(BlivTrails.getInstance(), new LoadRunnable(player.getUniqueId()));
			}
			catch (NullPointerException e)
			{
				e.printStackTrace();
				// Player has no trail config -- Do nothing
			}
		}
		else
		{
			/*
			 * dataSplit[0] == Particle String dataSplit[1] == Type dataSplit[2] == Length dataSplit[3] == Height dataSplit[4] == Colour 
			 */
			String data = flatFileStorage.loadEntry(player.getUniqueId().toString());

			if(data == null || data.isEmpty())
			{
				return;
			}

			try
			{
				String[] dataSplit = data.split("[,]");
				ParticleEffect particleEff = null;
				for (ParticleEffect pEff : ParticleEffect.values())
				{
					if (pEff.toString().equals(dataSplit[0]))
					{
						if (pEff.equals(ParticleEffect.FOOTSTEP))
						{
							return;
						}
						particleEff = pEff;
						break;
					}
				}
				trailMap.put(player.getUniqueId(), new PlayerConfig(player.getUniqueId(), particleEff, Integer.parseInt(dataSplit[1]), Integer.parseInt(dataSplit[2]), Integer.parseInt(dataSplit[3]), Integer.parseInt(dataSplit[4])));
			}
			catch (NullPointerException e)
			{
				if (BUtil.DEBUG)
				{
					e.printStackTrace();
				}
				// Player is not in the file
			}
		}
	}

	//I hate myself for doing this, but there is nothing wrong with creating a new MySQLRunnable there
	@SuppressWarnings("unused")
	public static void saveTrail(OfflinePlayer player)
	{
		PlayerConfig pcfg = trailMap.get(player.getUniqueId());
		if (pcfg != null)
		{
			if (pcfg.getParticle().equals(ParticleEffect.FOOTSTEP))
			{
				removePlayer(player.getUniqueId());
				return;
			}
			if (flatFileStorage == null)
			{
				//Construct this here to avoid constructing it twice if the server is shutting down
				SaveRunnable saveRunnable = new SaveRunnable(player.getUniqueId(), pcfg);
				try
				{
					// Run MySQL off the main thread to avoid lockups
					scheduler.runTaskAsynchronously(BlivTrails.getInstance(), saveRunnable);
				}
				catch (IllegalPluginAccessException e) // If the plugin is shutting down, tasks cannot be scheduled.
				{
					saveRunnable.run();
				}
				catch (NullPointerException e)
				{
					// Player has no trail config
					e.printStackTrace();
				}
			}
			else
			{
				/*
				 * dataSplit[0] == UUID 
				 * dataSplit[1] == Particle String
				 * dataSplit[2] == Type 
				 * dataSplit[3] == Length 
				 * dataSplit[4] == Height
				 * dataSplit[5] == Colour
				 */
				try
				{
					flatFileStorage.saveEntry(player.getUniqueId().toString(),
					                          pcfg.getParticle().toString() + "," + pcfg.getType() + "," + pcfg.getLength() + "," + pcfg.getHeight() + "," + pcfg.getColour());
					//BUtil.logDebug("Successfully saved " + player.getName() + "'s trail config to file");
				}
				catch (NullPointerException e)
				{
					// No data
				}
			}
		}
	}

	/**
	 * @param uuid
	 *            UUID String of target player
	 * @param particleString
	 *            ENUM name of the particle
	 * @param typeString
	 *            String representation of type (from config)
	 * @param lengthString
	 *            String representation of length (from config)
	 * @param heightString
	 *            String representation of height (from config)
	 * @param colourString
	 *            String representation of colour (from config)
	 *
	 * @return Success/Error Message Output
	 */
	public static String addTrail(UUID uuid, String particleString, String typeString, String lengthString, String heightString, String colourString)
	{
		ParticleEffect particleEff = ParticleEffect.FOOTSTEP;
		ParticleDefaultStorage particleDefaults = null;

		for (Map.Entry<ParticleEffect, ParticleDefaultStorage> entry : TrailDefaults.getParticleDefaults().entrySet())
		{
			if (BUtil.stripColours(entry.getValue().getDisplayName()).equalsIgnoreCase(particleString))
			{
				particleEff = entry.getKey();
				particleDefaults = entry.getValue();
				break;
			}
		}

		if (particleEff.equals(ParticleEffect.FOOTSTEP))
		{
			return ChatColor.RED + "Trail effect does not exist. (/trailadmin particles)";
		}

		int type, length = 1, height = 0, colour = 0;
		if (particleEff == ParticleEffect.BARRIER)
		{
			// Barriers don't support anything. Give up. Leave everything default
			return "&aTrail Successfully Applied";
		}

		if (typeString.isEmpty() && particleDefaults != null) // Use Trail Defaults
		{
			type = particleDefaults.getType();
			length = particleDefaults.getLength();
			height = particleDefaults.getHeight();
			colour = particleDefaults.getColour();
		}
		else
		// Use defined inputs
		{
			switch (typeString.toLowerCase())
			{
				case "trace":
					type = 1;
					break;
				case "random":
					type = 2;
					break;
				case "dynamic":
					type = 3;
					break;
				default:
					return "&cInvalid Type | (trace, random, dynamic)";
			}
			if (lengthString != null)
			{
				switch (lengthString.toLowerCase())
				{
					case "short":
						length = 1;
						break;
					case "medium":
						length = 2;
						break;
					case "long":
						length = 3;
						break;
					default:
						return "&cInvalid Length | (short, medium, long)";
				}
			}
			if (heightString != null)
			{
				switch (heightString.toLowerCase())
				{
					case "feet":
						height = 0;
						break;
					case "waist":
						height = 1;
						break;
					case "halo":
						height = 2;
						break;
					default:
						return "&cInvalid Height | (feet, waist, halo)";
				}
			}
			if (colourString != null)
			{
				switch (colourString.toLowerCase())
				{
					case "white":
						colour = 0;
						break;
					case "red":
						colour = 1;
						break;
					case "dark green":
						colour = 2;
						break;
					case "brown":
						colour = 3;
						break;
					case "dark blue":
						colour = 4;
						break;
					case "purple":
						colour = 5;
						break;
					case "cyan":
						colour = 6;
						break;
					case "light grey":
					case "light gray":
						colour = 7;
						break;
					case "grey":
					case "gray":
						colour = 8;
						break;
					case "pink":
						colour = 9;
						break;
					case "lime":
						colour = 10;
						break;
					case "yellow":
						colour = 11;
						break;
					case "light blue":
						colour = 12;
						break;
					case "magenta":
						colour = 13;
						break;
					case "orange":
						colour = 14;
						break;
					case "black":
						colour = 15;
						break;
					case "random":
						colour = 16;
						break;
					default:
						return "&cInvalid Colour. See /trailadmin colours | for colours";
				}
			}
		}

		trailMap.put(uuid, new PlayerConfig(uuid, particleEff, type, length, height, colour));
		return "&aTrail Successfully Applied";
	}

	public static String removePlayer(UUID uuid)
	{
		PlayerConfig pcfg = trailMap.remove(uuid);
		if (pcfg != null)
		{
			if (flatFileStorage == null)
			{
				scheduler.runTaskAsynchronously(BlivTrails.getInstance(), new RemoveRunnable(uuid));
			}
			else
			{
				try
				{
					flatFileStorage.removeEntry(uuid.toString());
					flatFileStorage.saveToFile();
				}
				catch (NullPointerException e)
				{
					// No data
				}
			}
		}
		else
		{
			return msg.getString("messages.error.player-no-trail");
		}
		return msg.getString("messages.error.unexpected");
	}

	public boolean isVanished(Player player)
	{
		boolean isVanished = false;
		switch(vanishHook)
		{
			case VANISH_NO_PACKET:
			{
				try
				{
					isVanished = ((VanishPlugin) Bukkit.getPluginManager().getPlugin("VanishNoPacket")).getManager().isVanished(player);
				}
				catch (NullPointerException | NoClassDefFoundError e)
				{
					BUtil.logDebug("VanishNoPacket was called, but isn't loaded.");
					// VanishNoPacket isn't loaded on the server
				}
				break;
			}
			case ESSENTIALS:
			{
				//?
				break;
			}
			case SUPER_PREMIUM_VANISH:
			{
				break;
			}
		}

		return isVanished;
	}

	public static void vanishHook(VanishHook inVanishHook)
	{
		vanishHook = inVanishHook;
	}

}
