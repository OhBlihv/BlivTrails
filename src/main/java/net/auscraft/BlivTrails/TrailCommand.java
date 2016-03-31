package net.auscraft.BlivTrails;

import com.darkblade12.ParticleEffect.ParticleEffect;
import net.auscraft.BlivTrails.config.FlatFile;
import net.auscraft.BlivTrails.listeners.GUIListener;
import net.auscraft.BlivTrails.util.BUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

public class TrailCommand implements CommandExecutor
{

	private BlivTrails instance;
	private FlatFile cfg;

	public TrailCommand(BlivTrails instance)
	{
		this.instance = instance;
		cfg = FlatFile.getInstance();
	}

	public boolean onCommand(CommandSender sender, Command cmd, String label, String args[])
	{
		if (cmd.getName().equalsIgnoreCase("trail"))
		{
			GUIListener.mainMenu((Player) sender);
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
				if (args[0].equalsIgnoreCase("particles")) // /trailadmin particles|types|lengths|heights|colours
				{
					StringBuilder output = new StringBuilder(" ");
					for (ParticleEffect particleEff : TrailManager.usedTrails)
					{
						output.append(cfg.getString("trails." + BUtil.trailConfigName(particleEff.toString()) + ".name").replaceAll("[ ]", "_")).append(", ");
					}
					sender.sendMessage(ChatColor.GREEN + "Available Particles:\n" + ChatColor.WHITE + BUtil.stripColours(output.toString()));
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
					OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(args[1]);
					if (offlinePlayer == null || !offlinePlayer.isOnline())
					{
						BUtil.printError(sender, "Player is not currently online. Cannot remove.");
						return true;
					}

					sender.sendMessage(TrailManager.removePlayer(offlinePlayer.getUniqueId()));
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
								BUtil.printPlain(sender, TrailManager.addTrail(player.getUniqueId(), particle, "", "", "", ""));
							}
							else if (args.length >= 4)
							{
								try
								{
									BUtil.printPlain(sender, BUtil.translateColours(TrailManager.addTrail(player.getUniqueId(), particle, args[3], args[4], args[5], args[6])));
								}
								catch(ArrayIndexOutOfBoundsException e)
								{
									BUtil.printError(sender, "Usage: /trailadmin add <name> <trail> [<type> <length> <height> <colour>]");
									return true;
								}
							}
							return true;
						}
					}
					else
					{
						BUtil.printError(sender, "Usage: /trailadmin add <name> <trail> [<type> <length> <height> <colour>]");
						return true;
					}

				}
				else if (args[0].equalsIgnoreCase("reload"))
				{
					if (FlatFile.getInstance().reloadFile())
					{
						BUtil.printError(sender, "You have config errors -- See Console for full printout");
					}
					instance.getMessages().reloadFile();
					TrailManager.loadDefaultOptions();
					BUtil.logSuccess("Config and Messages Reloaded!");
					BUtil.printSuccess(sender, "Config and Messages Reloaded!");
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
