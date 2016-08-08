package me.ohblihv.BlivTrails.objects.player;

import org.bukkit.entity.Player;

/**
 * Created by Chris Brown (OhBlihv) on 9/08/2016.
 */
public class CheapPlayerFactory_1_8_R3 implements ICheapPlayerFactory
{
	
	@Override
	public CheapPlayer getCheapPlayer(Player player)
	{
		return new CheapPlayer_1_8_R3(player);
	}
	
}
