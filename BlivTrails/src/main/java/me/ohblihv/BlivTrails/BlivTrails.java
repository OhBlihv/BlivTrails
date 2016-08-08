package me.ohblihv.BlivTrails;

import com.darkblade12.ParticleEffect.IParticlePacketFactory;
import com.darkblade12.ParticleEffect.ParticleEffect;
import lombok.Getter;
import me.ohblihv.BlivTrails.util.StaticNMS;
import me.ohblihv.BlivTrails.util.nms.INMSHelper;
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
		
		getNMSHelper();
		getParticleFactoryInstance();
	}

	@Override
	public void onDisable()
	{
		
	}
	
	public INMSHelper getNMSHelper()
	{
		return StaticNMS.getNMSHelper();
	}
	
	@Override
	public IParticlePacketFactory getParticleFactoryInstance() throws IllegalArgumentException
	{
		return StaticNMS.getParticleFactoryInstance();
	}

}
