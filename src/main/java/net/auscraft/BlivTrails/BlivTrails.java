package net.auscraft.BlivTrails;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.logger.LocalLog;
import com.j256.ormlite.table.TableUtils;
import lombok.Getter;
import net.auscraft.BlivTrails.config.FlatFile;
import net.auscraft.BlivTrails.config.FlatFileStorage;
import net.auscraft.BlivTrails.config.Messages;
import net.auscraft.BlivTrails.hooks.EssentialsListener;
import net.auscraft.BlivTrails.hooks.VanishListener;
import net.auscraft.BlivTrails.listeners.GUIListener;
import net.auscraft.BlivTrails.listeners.TrailListener;
import net.auscraft.BlivTrails.storage.ParticleData;
import net.auscraft.BlivTrails.storage.ParticleStorage;
import net.auscraft.BlivTrails.util.BUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BlivTrails extends JavaPlugin
{

	@Getter
	private static BlivTrails instance = null;

	private JdbcPooledConnectionSource ds = null;

	private FlatFileStorage flatFileStorage = null;

	@Getter
	private Messages messages;

	public static final Random rand = new Random();

	@Getter
	private ParticleStorage particleStorage;

	//Variables

	private int trailTimeoutCheckTime = 20;

	@Override
	public void onEnable()
	{
		instance = this;

		messages = Messages.getInstance();
		FlatFile cfg = FlatFile.getInstance();

		BUtil.DEBUG = cfg.getBoolean("misc.debug"); //Init here, since we're not using constructors in BUtil

		if (cfg.getBoolean("database.mysql"))
		{
			BUtil.logInfo("Using MySQL as the storage option");
			disableDatabaseLogging();
			try
			{
				sqlSetup();
			}
			catch (SQLException e)
			{
				e.printStackTrace();
				getPluginLoader().disablePlugin(this);
				return;
			}
		}
		else
		{
			BUtil.logInfo("Using FlatFile as the storage option");
			flatFileStorage = FlatFileStorage.getInstance();
		}

		TrailManager.init(this);
		getServer().getPluginManager().registerEvents(new TrailListener(), this);

		GUIListener.reload();
		getServer().getPluginManager().registerEvents(new GUIListener(), this);

		getCommand("trail").setExecutor(new TrailCommand(this));
		getCommand("trailadmin").setExecutor(new TrailCommand(this));
		doHooks();

		if (!cfg.getBoolean("trails.misc.display-when-still"))
		{
			trailTimeoutCheckTime = cfg.getInt("trails.scheduler.check-time");
			doTrailTimeouts();
		}
	}

	@Override
	public void onDisable()
	{
		TrailManager.doDisable();
		try
		{
			ds.closeQuietly();
		}
		catch (NullPointerException e)
		{
			// Not using SQL
		}
	}

	public void doItemListener()
	{
		getServer().getPluginManager().registerEvents(new ItemListener(), this);
	}

	private void doHooks()
	{
		try
		{
			if (this.getServer().getPluginManager().getPlugin("VanishNoPacket") != null)
			{
				getServer().getPluginManager().registerEvents(new VanishListener(), this);
			}
			else if (this.getServer().getPluginManager().getPlugin("Essentials") != null)
			{
				getServer().getPluginManager().registerEvents(new EssentialsListener(), this);
			}
			else
			{
				BUtil.logInfo("No Vanish Plugin Hooked.");
			}
		}
		catch (NullPointerException e)
		{
			e.printStackTrace();
		}
	}

	private void sqlSetup() throws SQLException
	{
		FlatFile cfg = FlatFile.getInstance();

		ds = new JdbcPooledConnectionSource(cfg.getString("database.url"));

		if (!cfg.getString("database.username").isEmpty())
		{
			ds.setUsername(cfg.getString("database.username"));
		}
		if (!cfg.getString("database.password").isEmpty())
		{
			ds.setPassword(cfg.getString("database.password"));
		}

		ds.setMaxConnectionsFree(3);
		/*
		 * There is a memory leak in ormlite-jbcd that means we should not use
		 * this. AutoReconnect handles this for us.
		 */
		ds.setTestBeforeGet(false);
		/* Keep the connection open for 15 minutes */
		ds.setMaxConnectionAgeMillis(900000);
		/*
		 * We should not use this. Auto reconnect does this for us. Waste of
		 * packets and CPU.
		 */
		ds.setCheckConnectionsEveryMillis(0);
		ds.initialize();

		particleStorage = new ParticleStorage(ds);

		if (!particleStorage.isTableExists())
		{
			TableUtils.createTable(ds, ParticleData.class);
		}
	}

	public Object getSave()
	{
		if (ds != null)
		{
			return ds;
		}
		return flatFileStorage;
	}

	private void disableDatabaseLogging()
	{
		System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "INFO");
	}

	private void doTrailTimeouts()
	{
		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
		{

			public void run()
			{
				BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
				ConcurrentHashMap<UUID, Integer> trailTasks = TrailManager.getTaskMap();
				ConcurrentHashMap<UUID, Float> trailTime = TrailManager.getTrailTime();

				int taskId;
				float resultingTime;

				UUID uuid;
				final Enumeration<UUID> itr = trailTasks.keys();
				while (itr.hasMoreElements())
				{
					uuid = itr.nextElement();

					taskId = trailTasks.get(uuid);
					//If trail is active for the current player
					if (scheduler.isQueued(taskId) || scheduler.isCurrentlyRunning(taskId)) 
					{
						resultingTime = trailTime.get(uuid) - trailTimeoutCheckTime;
						if (resultingTime > 0)
						{
							trailTime.replace(uuid, resultingTime);
							continue;
						}
					}

					trailTasks.remove(uuid); // TaskID is stale and not in use anymore. Cleanup.
				}
			}
		}, 0L, trailTimeoutCheckTime * 20L);

	}

}
