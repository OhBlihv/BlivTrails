package me.ohblihv.BlivTrails.cosmetics;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.ohblihv.BlivTrails.objects.player.CheapPlayer;
import me.ohblihv.BlivTrails.util.StaticNMS;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Chris Brown (OhBlihv) on 6/08/2016.
 */
@RequiredArgsConstructor
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
	
	public void updateNearbyPlayers()
	{
		Location activatingPlayerLocation = getLocation();
		World.Environment activatingPlayerEnvironment = activatingPlayerLocation.getWorld().getEnvironment();
		
		for(Player player : Bukkit.getOnlinePlayers())
		{
			boolean inRange = true;
			
			Location playerLocation = player.getLocation();
			
			if(playerLocation.getWorld().getEnvironment() != activatingPlayerEnvironment)
			{
				inRange = false;
			}
			//If boss is in view distance of the player, make sure to add this player to the nearbyPlayers collection
			else if( Math.abs(playerLocation.getBlockX()) - activatingPlayerLocation.getX() > viewDistance ||
				         Math.abs(playerLocation.getBlockY()) - activatingPlayerLocation.getY() > viewDistance ||
				         Math.abs(playerLocation.getBlockZ()) - activatingPlayerLocation.getZ() > viewDistance)
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
	
	public Location getLocation()
	{
		return activatingPlayer.getLocation();
	}
	
	public void onTick(long tick)
	{
		cosmetic.onTick(tick, activatingPlayer.getLocation());
	}
	
}
