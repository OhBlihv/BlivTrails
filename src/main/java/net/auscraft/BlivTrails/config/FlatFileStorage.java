package net.auscraft.BlivTrails.config;

import net.auscraft.BlivTrails.BlivTrails;
import org.bukkit.Bukkit;

public class FlatFileStorage extends FlatFile
{

	private static FlatFileStorage instance = null;
	public static FlatFileStorage getInstance()
	{
		if(instance == null)
		{
			instance = new FlatFileStorage();
		}
		return instance;
	}

	private FlatFileStorage()
	{
		super("trails.yml");

		Bukkit.getScheduler().runTaskTimerAsynchronously(BlivTrails.getInstance(), new Runnable()
		{

			@Override
			public void run()
			{
				saveToFile();
			}

		}, 18000L, 18000L); //Save every 15 minutes
	}

	@Override
	public void saveEntry(String path, String entry)
	{
		save.set(path, entry);
		//saveToFile(); //Save this to the async saving task
	}

}

