package net.auscraft.BlivTrails.runnables;

import java.util.UUID;
import java.util.concurrent.Callable;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import net.auscraft.BlivTrails.config.Messages;
import net.auscraft.BlivTrails.utils.Utilities;

public class CallablePrintout implements Callable<Object>
{

	private CommandSender sender;
	private String message;

	// Required to already have the uuid.
	// A pre-translated string is recommended
	public CallablePrintout(UUID uuid, String message)
	{
		this.sender = Bukkit.getPlayer(uuid);
		this.message = message;
	}

	public Object call() throws Exception
	{
		String cfgmsg = Messages.getInstance().getString(message);
		if (cfgmsg != null)
		{
			cfgmsg = message;
		}
		Utilities.printPlain(sender, cfgmsg);
		return null;
	}

}
