package net.auscraft.BlivTrails;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import net.auscraft.BlivTrails.config.ConfigAccessor;
import net.auscraft.BlivTrails.config.FlatFile;
import net.auscraft.BlivTrails.config.Messages;

import org.bukkit.plugin.java.JavaPlugin;

import com.jolbox.bonecp.BoneCPDataSource;

public class BlivTrails extends JavaPlugin
{
	
	private TrailListener listener;
	private BoneCPDataSource ds = null;
	private ConfigAccessor cfg;
	private FlatFile flatfile = null;
	private Messages messages;
	private Utilities util;
	
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
		try
		{
			if(this.getServer().getPluginManager().getPlugin("VanishNoPacket") != null)
			{
				getServer().getPluginManager().registerEvents(new VanishListener(this), this);
			}
		}
		catch(NullPointerException e)
		{
			util.logInfo("VanishNoPacket not loaded | Not Hooking");
			e.printStackTrace();
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
	
	public Utilities getUtil()
	{
		return util;
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
					this.getPluginLoader().disablePlugin(this);
				}
				
				//ALTER TABLE  `bliv_trails` ADD  `colour` INT NOT NULL DEFAULT  '0';
			} 
			catch (SQLException e2) 
			{
				e2.printStackTrace();
			}
		}
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
