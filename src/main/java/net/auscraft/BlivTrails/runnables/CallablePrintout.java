package net.auscraft.BlivTrails.runnables;

import java.util.UUID;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import net.auscraft.BlivTrails.utils.Utilities;

public class CallablePrintout implements Callable<Object>
{

	private Utilities util = new Utilities(true);
	private CommandSender sender;
	private String message;
	
	//Required to already have the uuid.
	//A pre-translated string is recommended
	public CallablePrintout(UUID uuid, String message)
	{
		this.sender = (CommandSender) Bukkit.getPlayer(uuid);
		this.message = message;
	}
	
	public Object call() throws Exception 
	{
		String cfgmsg = util.getInstance().getMessages().getString(message);
		if(cfgmsg != null)
		{
			cfgmsg = message;
		}
		util.printPlain(sender, cfgmsg);
		return null;
	}

}
