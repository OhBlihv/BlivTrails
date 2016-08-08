package me.ohblihv.BlivTrails;

import com.darkblade12.ParticleEffect.IParticlePacketFactory;
import com.darkblade12.ParticleEffect.ParticleEffect;
import lombok.Getter;
import me.ohblihv.BlivTrails.objects.player.ICheapPlayerFactory;
import me.ohblihv.BlivTrails.util.BUtil;
import me.ohblihv.BlivTrails.util.StaticNMS;
import me.ohblihv.BlivTrails.util.nms.INMSHelper;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class BlivTrails extends JavaPlugin implements IBlivTrails
{

	@Getter
	private static BlivTrails instance = null;

	@Override
	public void onEnable()
	{
		instance = this;
		
		//Initialise any static NMS code
		ParticleEffect.setPluginInstance(this); //Horrible initialisation, but required.
		
		try
		{
			//Run the first NMS library through a try-catch to fail-fast if this server isn't supported at all.
			getNMSHelper();
		}
		catch(IllegalArgumentException e)
		{
			//Delay so we can use our printing library with a 'fully-loaded' plugin.
			Bukkit.getScheduler().runTask(this, () ->
			{
				BUtil.logInfo("This server is not supported, BlivTrails cannot be used: Shutting Down.");
				Bukkit.getPluginManager().disablePlugin(this);
			});
			
			return;
		}
		getParticleFactoryInstance();
		getCheapPlayerFactoryInstance();
	}

	@Override
	public void onDisable()
	{
		
	}
	
	@Override
	public INMSHelper getNMSHelper()
	{
		return StaticNMS.getNMSHelper();
	}
	
	@Override
	public IParticlePacketFactory getParticleFactoryInstance() throws IllegalArgumentException
	{
		return StaticNMS.getParticleFactoryInstance();
	}
	
	@Override
	public ICheapPlayerFactory getCheapPlayerFactoryInstance() throws IllegalArgumentException
	{
		return StaticNMS.getCheapPlayerFactoryInstance();
	}
	
}
