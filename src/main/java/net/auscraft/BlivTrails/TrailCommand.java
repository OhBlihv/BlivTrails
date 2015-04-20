package net.auscraft.BlivTrails;

import java.util.regex.Pattern;

import net.auscraft.BlivTrails.config.ConfigAccessor;
import net.auscraft.BlivTrails.utils.Utilities;

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
	private ConfigAccessor cfg;

	public TrailCommand(BlivTrails instance)
	{
		this.instance = instance;
		this.util = instance.getUtil();
		this.cfg = instance.getCfg();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String args[])
	{
		if (cmd.getName().equalsIgnoreCase("trail") || cmd.getName().equalsIgnoreCase("trails"))
		{
			instance.getListener().mainMenu((Player) sender);
			return true;
		}
		else if (cmd.getName().equalsIgnoreCase("trailadmin") && sender.hasPermission("blivtrails.admin"))
		{
			if (args.length == 0)
			{
				doMainMenu(sender);
				return true;
			}
			else if (args.length >= 1)
			{
				if (args[0].equalsIgnoreCase("particles")) // /trailadmin
															// particles|types|lengths|heights|colours
				{
					String output = " ";
					for (ParticleEffect particleEff : instance.getListener().usedTrails)
					{
						output += util.stripColours(cfg.getString("trails." + util.trailConfigName(particleEff.toString()) + ".name")).replaceAll("[ ]", "_") + ", ";
					}
					sender.sendMessage(ChatColor.GREEN + "Available Particles:\n" + ChatColor.WHITE + output);
				}
				else if (args[0].equalsIgnoreCase("types"))
				{
					sender.sendMessage(ChatColor.GREEN + "Available Types:\n" + ChatColor.DARK_GREEN + "| " + ChatColor.WHITE + "trace, random, dynamic");
				}
				else if (args[0].equalsIgnoreCase("lengths"))
				{
					sender.sendMessage(ChatColor.GREEN + "Available Lengths:\n" + ChatColor.DARK_GREEN + "| " + ChatColor.WHITE + "short, medium, long");
				}
				else if (args[0].equalsIgnoreCase("heights"))
				{
					sender.sendMessage(ChatColor.GREEN + "Available Heights:\n" + ChatColor.DARK_GREEN + "| " + ChatColor.WHITE + "feet, waist, halo");
				}
				else if (args[0].equalsIgnoreCase("colours"))
				{
					sender.sendMessage(ChatColor.GREEN + "Available Colours:\n" + ChatColor.DARK_GREEN + "| " + ChatColor.WHITE + "black, red, green, brown, blue, purple, cyan, light-grey/light gray," + " grey/gray, pink, lime, yellow, light-blue, magenta, orange, white, random");
				}
				else if (args[0].equalsIgnoreCase("remove"))
				{
					try
					{
						Player player = Bukkit.getPlayer(args[1]);
						if (player != null)
						{
							sender.sendMessage(instance.getListener().removePlayer(player.getUniqueId().toString()));
						}
					}
					catch (NullPointerException e)
					{
						util.printError(sender, "Player is not currently online. Cannot remove.");
					}
					return true;
				}
				else if (args[0].equalsIgnoreCase("add"))
				{
					if (args.length >= 3)
					{
						Player player = Bukkit.getPlayer(args[1]);
						if (player != null)
						{
							Pattern underscorePattern = Pattern.compile("[_]");
							String particle = underscorePattern.matcher(args[2]).replaceAll(" ");

							if (args.length == 3)
							{
								util.printPlain(sender, instance.getListener().addTrail(player.getUniqueId().toString(), particle, "", "", "", ""));
							}
							else if (args.length >= 4)
							{
								try
								{
									util.printPlain(sender, util.translateColours(instance.getListener().addTrail(player.getUniqueId().toString(), particle, args[3], args[4], args[5], args[6])));
								}
								catch(ArrayIndexOutOfBoundsException e)
								{
									util.printError(sender, "Usage: /trailadmin add <name> <trail> [<type> <length> <height> <colour>]");
									return true;
								}
							}
							return true;
						}
					}
					else
					{
						util.printError(sender, "Usage: /trailadmin add <name> <trail> [<type> <length> <height> <colour>]");
						return true;
					}

				}
				else if (args[0].equalsIgnoreCase("reload"))
				{
					if (instance.getCfg().reloadMessages())
					{
						util.printError(sender, "You have config errors -- See Console for full printout");
					}
					instance.getMessages().reloadMessages();
					instance.getListener().loadDefaultOptions();
					util.setConfig(instance.getCfg());
					util.logSuccess("Config and Messages Reloaded!");
					util.printSuccess(sender, "Config and Messages Reloaded!");
					return true;
				}
				else
				{
					doMainMenu(sender);
				}
			}
			return true;
		}
		return true;
	}

	private void doMainMenu(CommandSender sender)
	{
		sender.sendMessage(ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GOLD + ChatColor.ITALIC
				+ "BlivTrails Admin " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - "
				+ ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " -\n" + ChatColor.GREEN + "| " + ChatColor.AQUA + "/trailadmin reload" + ChatColor.RESET + " - Reload Config and Messages\n" + ChatColor.DARK_GREEN + "| " + ChatColor.AQUA
				+ "/trailadmin <particles|types|lengths|heights|colours>\n" + ChatColor.GREEN + "| " + ChatColor.AQUA + "/trailadmin remove <player>" + ChatColor.RESET + " - Forcefully remove a players trail\n" + ChatColor.DARK_GREEN + "| " + ChatColor.AQUA
				+ "/trailadmin add <player> <particle> [type] [length] [height]\n" + ChatColor.GREEN + "| " + ChatColor.AQUA + "[colour]" + ChatColor.RESET + " - Forcefully remove a players trail\n" + ChatColor.DARK_GREEN + "| " + ChatColor.WHITE
				+ "Use an underscore between words when defining a particle\n" + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN
				+ " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - "
				+ ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN + " - " + ChatColor.DARK_GREEN + " - " + ChatColor.GREEN
				+ " - ");
	}
}
