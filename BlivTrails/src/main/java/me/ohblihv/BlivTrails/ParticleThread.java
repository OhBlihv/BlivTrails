package me.ohblihv.BlivTrails;

import lombok.Getter;
import lombok.Setter;
import me.ohblihv.BlivTrails.cosmetics.ActiveCosmetic;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Chris Brown (OhBlihv) on 26/05/2016.
 */
public class ParticleThread extends Thread
{

	private static final String THREAD_PREFIX = "[BlivTrails-Thread] ";

	@Setter
	private volatile boolean isRunning = true;

	//Thread Variables
	private static final long WAIT_MILLIS = 50L; //Wait 1 tick (MC) between loops

	private long currentTick = 0L;

	//Cosmetics
	@Getter
	private final CopyOnWriteArraySet<ActiveCosmetic> cosmeticSet = new CopyOnWriteArraySet<>();

	public ParticleThread()
	{
		super("BlivTrails-Thread");
	}

	@Override
	public void run()
	{
		while(isRunning)
		{
			if(cosmeticSet.isEmpty())
			{
				doSleep();
				continue;
			}

			for(ActiveCosmetic cosmetic : cosmeticSet)
			{
				if(currentTick % 40 == 0) //Update nearby players every 2 seconds
				{
					cosmetic.updateNearbyPlayers();
				}

				try
				{
					cosmetic.onTick(currentTick);
				}
				catch(Throwable e)
				{
					System.out.println(THREAD_PREFIX + "An unexpected error occurred while ticking cosmetic.");
					e.printStackTrace();
				}
			}

			++currentTick;
			doSleep(); //Wait until the next tick
		}
		this.interrupt();
	}

	public void doSleep()
	{
		try
		{
			sleep(WAIT_MILLIS);
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
	}
	
}
