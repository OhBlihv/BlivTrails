package net.auscraft.BlivTrails.config;

import lombok.Getter;
import net.auscraft.BlivTrails.BlivTrails;
import org.bukkit.Bukkit;

import java.util.concurrent.atomic.AtomicInteger;

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

	@Getter
	private static AtomicInteger totalBans = new AtomicInteger();

	private FlatFileStorage()
	{
		super("trails.yml");

		load();

		Bukkit.getScheduler().runTaskTimerAsynchronously(BlivTrails.getInstance(), new Runnable()
		{

			@Override
			public void run()
			{
				save();
			}

		}, 36000L, 36000L);
	}

	public void load()
	{
		totalBans = new AtomicInteger(getInt("total"));
	}

	public void save()
	{
		saveValue("total", totalBans.intValue());
	}

}

