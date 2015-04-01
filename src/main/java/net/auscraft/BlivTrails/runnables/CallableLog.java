package net.auscraft.BlivTrails.runnables;

import java.util.concurrent.Callable;

import net.auscraft.BlivTrails.utils.Utilities;

public class CallableLog implements Callable<Object>
{

	private Utilities util = new Utilities(true);
	private String message;
	
	//Required to already have the uuid.
	//A pre-translated string is recommended
	public CallableLog(String message)
	{
		this.message = message;
	}
	
	public Object call() throws Exception 
	{
		util.logDebug(message);
		return null;
	}

}
