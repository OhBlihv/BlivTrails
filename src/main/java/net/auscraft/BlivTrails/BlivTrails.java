package net.auscraft.BlivTrails;

import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.logger.LocalLog;
import com.j256.ormlite.table.TableUtils;
import lombok.Getter;
import lombok.Setter;
import net.auscraft.BlivTrails.config.ConfigAccessor;
import net.auscraft.BlivTrails.config.FlatFile;
import net.auscraft.BlivTrails.config.Messages;
import net.auscraft.BlivTrails.hooks.EssentialsListener;
import net.auscraft.BlivTrails.hooks.VanishListener;
import net.auscraft.BlivTrails.storage.ParticleData;
import net.auscraft.BlivTrails.storage.ParticleStorage;
import net.auscraft.BlivTrails.utils.Utilities;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class BlivTrails extends JavaPlugin
{

	@Getter
	@Setter
	private TrailListener listener;
	private JdbcPooledConnectionSource ds = null;
	@Getter
	private ConfigAccessor cfg;
	private FlatFile flatfile = null;
	@Getter
	private Messages messages;
	private Utilities util;
	@Getter
	private Random rand;
	@Getter
	private ParticleStorage particleStorage;

	@Override
	public void onEnable()
	{
		util = new Utilities(this);
		setupCFG();
		messages = new Messages(this);

		if (cfg.getBoolean("database.mysql"))
		{
			util.logInfo("Using mySQL as the storage option");
			disableDatabaseLogging();
			try
			{
				SQLSetup();
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
			util.logInfo("Using FlatFile as the storage option");
			flatfile = new FlatFile(this);
		}
		getServer().getPluginManager().registerEvents(new TrailListener(this), this);
		getCommand("trail").setExecutor(new TrailCommand(this));
		getCommand("trailadmin").setExecutor(new TrailCommand(this));
		doHooks();
		rand = new Random(System.currentTimeMillis());

		if (!cfg.getBoolean("trails.misc.display-when-still"))
		{
			doTrailTimeouts();
		}
	}

	@Override
	public void onDisable()
	{
		getListener().doDisable();
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
		getServer().getPluginManager().registerEvents(new ItemListener(this), this);
	}

	public Utilities getUtil()
	{
		return util;
	}

	public Object getSave()
	{
		if (ds != null)
		{
			return ds;
		}
		else
		{
			return flatfile;
		}
	}

	private void doHooks()
	{
		try
		{
			if (this.getServer().getPluginManager().getPlugin("VanishNoPacket") != null)
			{
				getServer().getPluginManager().registerEvents(new VanishListener(this), this);
			}
			else if (this.getServer().getPluginManager().getPlugin("Essentials") != null)
			{
				getServer().getPluginManager().registerEvents(new EssentialsListener(this), this);
			}
			else
			{
				util.logInfo("No Vanish Plugin Hooked.");
			}
		}
		catch (NullPointerException e)
		{
			e.printStackTrace();
		}
	}

	private void SQLSetup() throws SQLException
	{
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

	private void disableDatabaseLogging()
	{
		System.setProperty(LocalLog.LOCAL_LOG_LEVEL_PROPERTY, "INFO");
	}

	private void doTrailTimeouts()
	{
		final int checkTime = cfg.getInt("trails.scheduler.check-time");

		Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable()
		{

			public void run()
			{
				BukkitScheduler scheduler = Bukkit.getServer().getScheduler();
				ConcurrentHashMap<String, Integer> trailTasks = listener.getActiveTrails();
				ConcurrentHashMap<String, Float> trailTime = listener.getTrailTimeLeft();

				int taskId = 0;
				float resultingTime = 0;

				String uuid = "";
				final Enumeration<String> itr = trailTasks.keys();
				while (itr.hasMoreElements())
				{
					uuid = itr.nextElement();

					taskId = trailTasks.get(uuid);
					//If trail is active for the current player
					if (scheduler.isQueued(taskId) || scheduler.isCurrentlyRunning(taskId)) 
					{
						resultingTime = trailTime.get(uuid) - checkTime;
						if (resultingTime > 0)
						{
							trailTime.replace(uuid, resultingTime);
						}
						else
						{
							trailTime.remove(uuid);
						}
					}
					else
					{
						trailTasks.remove(uuid); // TaskID is stale and not in use anymore. Cleanup.
					}
				}
			}
		}, 0L, checkTime * 20);

	}

	public void setupCFG()
	{
		this.saveDefaultConfig();
		this.getConfig().options().copyDefaults(true);
		cfg = new ConfigAccessor(this);
	}

}
