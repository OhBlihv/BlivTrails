package net.auscraft.BlivTrails;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import net.auscraft.BlivTrails.config.ConfigAccessor;
import net.auscraft.BlivTrails.config.FlatFile;
import net.auscraft.BlivTrails.config.Messages;
import net.auscraft.BlivTrails.hooks.EssentialsListener;
import net.auscraft.BlivTrails.hooks.VanishListener;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.jolbox.bonecp.BoneCPDataSource;

public class BlivTrails extends JavaPlugin
{
	
	private TrailListener listener;
	private BoneCPDataSource ds = null;
	private ConfigAccessor cfg;
	private FlatFile flatfile = null;
	private Messages messages;
	private Utilities util;
	private Random rand;
	
	@Override
	public void onEnable()
	{
		util = new Utilities(this);
		setupCFG();
		messages = new Messages(this);
		
		if(cfg.getBoolean("database.mysql"))
		{
			util.logInfo("Using mySQL as the storage option");
			SQLSetup();
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
		
		if(!cfg.getBoolean("trails.misc.display-when-still"))
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
			ds.close();
		}
		catch(NullPointerException e)
		{
			//Not using SQL
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
	
	public Random getRand()
	{
		return rand;
	}
	
	public Object getSave()
	{
		if(ds != null)
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
			if(this.getServer().getPluginManager().getPlugin("VanishNoPacket") != null)
			{
				getServer().getPluginManager().registerEvents(new VanishListener(this), this);
			}
			else if(this.getServer().getPluginManager().getPlugin("Essentials") != null)
			{
				getServer().getPluginManager().registerEvents(new EssentialsListener(this), this);
			}
			else
			{
				util.logInfo("No Vanish Plugin Hooked.");
			}
		}
		catch(NullPointerException e)
		{
			e.printStackTrace();
		}
	}
	
	private void SQLSetup()
	{
		ds = new BoneCPDataSource();
		ds.setJdbcUrl(cfg.getString("database.url"));
		ds.setUsername(cfg.getString("database.username"));
		ds.setPassword(cfg.getString("database.password"));
		ds.close();
		ds.setPartitionCount(2);
		ds.setMinConnectionsPerPartition(3);
		ds.setMaxConnectionsPerPartition(7);
		Bukkit.getServer().getScheduler().runTaskAsynchronously(this, new Runnable()
		{

			@Override
			public void run() 
			{
				Connection conn = null;
				try
				{
					conn = ds.getConnection();
					Statement st = conn.createStatement();
					st.executeQuery("SELECT 1 FROM bliv_trails LIMIT 1;");
					//If no error, table is set up
					conn.close();
				}
				catch(SQLException e) //Else, create the table
				{
					try 
					{
						util.logInfo("Setting up BlivTrails database...");
						try
						{
							Statement st = conn.createStatement();
							st.execute("CREATE TABLE bliv_trails(uuid VARCHAR(36) PRIMARY KEY, particle VARCHAR(50), type INT(11), length INT(11), height INT(11), colour INT(11));");
							conn.close();
						}
						catch(NullPointerException e2)
						{
							util.logError("ERROR: mySQL Connection Issues. Do you have the correct db setup?");
							conn.close();
						}
					} 
					catch (SQLException e2) 
					{
						e2.printStackTrace();
					}
				}
			}
			
		});
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
				while(itr.hasMoreElements())
				//for(String uuid : trailTasks.keySet())
				{
					uuid = itr.nextElement();
					
					taskId = trailTasks.get(uuid);
					if(scheduler.isQueued(taskId) || scheduler.isCurrentlyRunning(taskId)) //If trail is active for given player
					{
						resultingTime = trailTime.get(uuid) - checkTime;
						if(resultingTime > 0)
						{
							trailTime.replace(uuid, resultingTime);
							//util.logDebug("Reduced " + Bukkit.getPlayer(UUID.fromString(uuid)).getName() + "'s trail time by " + checkTime + " to equal " + (trailTime.get(uuid) - checkTime) + " seconds left");
						}
						else
						{
							trailTime.remove(uuid);
							//util.logDebug("Removed " + Bukkit.getPlayer(UUID.fromString(uuid)).getName() + " from the trailTime Map");
							//util.logDebug("Trail should now be disabled");
						}
					}
					else
					{
						trailTasks.remove(uuid); //TaskID is stale and not in use anymore. Cleanup.
						//util.logDebug("Cleaned Up " + Bukkit.getPlayer(UUID.fromString(uuid)).getName() + "'s task, which wasn't attached to a trail.");
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
	
	public ConfigAccessor getCfg()
	{
		return cfg;
	}
	
	public Messages getMessages()
	{
		return messages;
	}
	
	public void setListener(TrailListener listener)
	{
		this.listener = listener;
	}
	  
	public TrailListener getListener()
	{
		return listener;
	}
}
