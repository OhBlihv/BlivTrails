package net.auscraft.BlivTrails;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.darkblade12.ParticleEffect.ParticleEffect;

public class TrailCommand implements CommandExecutor
{

	private BlivTrails instance;
	private Utilities util;
	
	public TrailCommand(BlivTrails instance)
	{
		this.instance = instance;
		util = instance.getUtil();
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String args[]) 
	{
		if(cmd.getName().equalsIgnoreCase("trail") || cmd.getName().equalsIgnoreCase("trails"))
		{
			instance.getListener().mainMenu((Player) sender);
			return true;
		}
		else if(cmd.getName().equalsIgnoreCase("trailadmin") && sender.hasPermission("blivtrails.admin"))
		{
			if(args.length == 0)
			{
				sender.sendMessage(ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + 
						ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GOLD + ChatColor.ITALIC + "BlivTrails Admin " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + 
						ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + 
						ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " -\n"
						+ ChatColor.GREEN + "| " + ChatColor.AQUA + "/trailadmin remove <player>" + ChatColor.RESET + " - Forcefully remove a players trail\n"
						+ ChatColor.DARK_GREEN + "| " + ChatColor.AQUA + "/trailadmin reload" + ChatColor.RESET + " - Reload Config and Messages\n"
						+ ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + 
						ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + 
						ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + 
						ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + 
						ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - ");
			}
			else if(args.length == 1)
			{
				if(args.length >= 2)
				{
					if(args[0].equalsIgnoreCase("remove"))
					{
						try
						{
							Player player = Bukkit.getPlayer(args[1]);
							if(player != null)
							{
								sender.sendMessage(instance.getListener().removePlayer(player.getUniqueId().toString()));
							}
						}
						catch(NullPointerException e)
						{
							util.logError("Player is not currently online. Cannot remove.");
						}
						return true;
					}
					else if(args[0].equalsIgnoreCase("add"))
					{
						Player player = Bukkit.getPlayer(args[1]);
						if(player != null)
						{
							if(args.length == 3)
							{
								sender.sendMessage(util.translateColours(instance.getListener().addTrail(player.getUniqueId().toString(), args[2],
										"", "", "", "")));
							}
							else if(args.length >= 4)
							{
								sender.sendMessage(util.translateColours(instance.getListener().addTrail(player.getUniqueId().toString(), args[2],
										args[3], args[4], args[5], args[6])));
							}
								
						}
						return true;
					}
				}
				else
				{
					if(args[0].equalsIgnoreCase("reload"))
					{
						if(instance.getCfg().reloadMessages())
						{
							util.printError(sender, "You have config errors -- See Console for full printout");
						}
						instance.getMessages().reloadMessages();
						instance.getListener().loadDefaultOptions();
						util.logSuccess("Config and Messages Reloaded!");
						util.printSuccess(sender, "Config and Messages Reloaded!");
					}
				}
			}
			
		}
		return true;
	}
}
