package me.ohblihv.BlivTrails.cosmetics;

import lombok.Getter;
import me.ohblihv.BlivTrails.objects.player.CheapPlayer;
import me.ohblihv.BlivTrails.util.StaticNMS;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Chris Brown (OhBlihv) on 6/08/2016.
 */
public class ActiveCosmetic
{
	
	private final CopyOnWriteArraySet<CheapPlayer> nearbyPlayers = new CopyOnWriteArraySet<>();
	
	@Getter
	final BaseCosmetic cosmetic;
	
	@Getter
	private final Player activatingPlayer;
	
	//Variables
	@Getter
	final int viewDistance;
	
	public ActiveCosmetic(BaseCosmetic cosmetic, Player activatingPlayer, int viewDistance)
	{
		this.cosmetic = cosmetic;
		this.activatingPlayer = activatingPlayer;
		this.viewDistance = viewDistance;
		
		this.nearbyPlayers.add(StaticNMS.getCheapPlayerFactoryInstance().getCheapPlayer(activatingPlayer));
	}
	
	public void updateNearbyPlayers()
	{
		Location activatingPlayerLocation = getLocation();
		
		for(Player player : Bukkit.getOnlinePlayers())
		{
			if(player == activatingPlayer)
			{
				continue;
			}
			
			boolean inRange = true;
			
			Location playerLocation = player.getLocation();
			
			if(activatingPlayerLocation.getWorld() != playerLocation.getWorld() || getDistance(activatingPlayerLocation, playerLocation) > viewDistance)
			{
				inRange = false;
			}
			
			//Create this for insertion and contains checks
			CheapPlayer cheapPlayer = StaticNMS.getCheapPlayerFactoryInstance().getCheapPlayer(player);
			if(inRange)
			{
				nearbyPlayers.add(cheapPlayer);
				
				//TODO: Initialization
			}
			else
			{
				//Remove the boss if previously in the nearby players collection
				if(nearbyPlayers.contains(cheapPlayer))
				{
					//TODO: Teardown
					
					nearbyPlayers.remove(cheapPlayer);
				}
				
				//Otherwise, do nothing
			}
		}
	}
	
	private static int getDistance(Location location, Location comparedLocation)
	{
		int blockX = location.getBlockX(), comparedBlockX = comparedLocation.getBlockX(),
			blockZ = location.getBlockZ(), comparedBlockZ = comparedLocation.getBlockZ();
		
		double squaredDistance = NumberConversions.square(blockX - comparedBlockX) + NumberConversions.square(blockZ - comparedBlockZ);
		
		return (int) Math.sqrt(squaredDistance);
	}
	
	public Location getLocation()
	{
		return activatingPlayer.getLocation();
	}
	
	public void onTick(long tick)
	{
		cosmetic.onTick(tick, activatingPlayer.getLocation(), nearbyPlayers);
	}
	
}
