package me.ohblihv.BlivTrails.objects.player;

import lombok.Getter;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

/**
 * Created by Chris Brown (OhBlihv) on 7/08/2016.
 */
public class CheapPlayer_1_8_R3 extends CheapPlayer
{
	
	//Provide quick access to networking
	@Getter
	private PlayerConnection playerConnection;
	
	private EntityPlayer entityPlayer;
	
	public CheapPlayer_1_8_R3(Player player)
	{
		entityPlayer = ((CraftPlayer) player).getHandle();
		
		playerConnection = entityPlayer.playerConnection;
	}
	
	public void queuePacket(Object packet)
	{
		if(!(packet instanceof Packet))
		{
			//Ignore this for now.
			//Possibly throw an exception to narrow down the illegal calls?
			throw new IllegalArgumentException("queuePacket() expects a Packet, and was given a " + (packet != null ? packet.getClass().getName() : "null"));
		}
		
		((CraftPlayer) getPlayer()).getHandle().playerConnection.sendPacket((Packet) packet);
	}
}