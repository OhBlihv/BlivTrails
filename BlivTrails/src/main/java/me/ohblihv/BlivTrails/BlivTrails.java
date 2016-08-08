package me.ohblihv.BlivTrails;

import lombok.Getter;
import me.ohblihv.BlivTrails.util.BUtil;
import me.ohblihv.BlivTrails.util.nms.INMSHelper;
import me.ohblihv.BlivTrails.util.nms.NMSHelper_1_10_R1;
import me.ohblihv.BlivTrails.util.nms.NMSHelper_1_7_R1;
import me.ohblihv.BlivTrails.util.nms.NMSHelper_1_7_R2;
import me.ohblihv.BlivTrails.util.nms.NMSHelper_1_7_R3;
import me.ohblihv.BlivTrails.util.nms.NMSHelper_1_7_R4;
import me.ohblihv.BlivTrails.util.nms.NMSHelper_1_8_R1;
import me.ohblihv.BlivTrails.util.nms.NMSHelper_1_8_R2;
import me.ohblihv.BlivTrails.util.nms.NMSHelper_1_8_R3;
import me.ohblihv.BlivTrails.util.nms.NMSHelper_1_9_R2;
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
		getNMSHelper();
	}

	@Override
	public void onDisable()
	{
		
	}
	
	private INMSHelper nmsHelper = null;
	public INMSHelper getNMSHelper()
	{
		if(nmsHelper == null)
		{
			switch(BUtil.getNMSVersion())
			{
				case "v_1_7_R1": return new NMSHelper_1_7_R1();
				case "v_1_7_R2": return new NMSHelper_1_7_R2();
				case "v_1_7_R3": return new NMSHelper_1_7_R3();
				case "v_1_7_R4": return new NMSHelper_1_7_R4();
				case "v_1_8_R1": return new NMSHelper_1_8_R1();
				case "v_1_8_R2": return new NMSHelper_1_8_R2();
				case "v_1_8_R3": return new NMSHelper_1_8_R3();
				case "v_1_9_R1": return new NMSHelper_1_9_R2();
				case "v_1_9_R2": return new NMSHelper_1_9_R2();
				case "v_1_10_R1": return new NMSHelper_1_10_R1();
			}
		}
		
		return nmsHelper;
	}

}
