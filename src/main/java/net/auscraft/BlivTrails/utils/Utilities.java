package net.auscraft.BlivTrails.utils;

import net.auscraft.BlivTrails.BlivTrails;
import net.auscraft.BlivTrails.config.ConfigAccessor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class Utilities
{

	private static final String prefix = "&f[&bBlivTrails&f] ";
	private static String playerPrefix = "";
	private static final java.util.logging.Logger log = Bukkit.getLogger();
	private static boolean debug = true;

	// ------------------------------------------------------------------------------------------------------
	// Extra Setup
	// ------------------------------------------------------------------------------------------------------

	public static void setConfig(ConfigAccessor cfg)
	{
		playerPrefix = translateColours(BlivTrails.getInstance().getMessages().getString("messages.prefix"));
		if (!(playerPrefix.length() == 0))
		{
			playerPrefix += " ";
		}
		debug = cfg.getBoolean("misc.debug");
	}

	// ------------------------------------------------------------------------------------------------------
	// String Translation
	// ------------------------------------------------------------------------------------------------------

	public static String stripColours(String toFix)
	{
		Pattern chatColorPattern = Pattern.compile("[&](.)");
		String fixedString = chatColorPattern.matcher(toFix).replaceAll("");
		return fixedString;
	}

	public static String translateConsoleColours(String toFix)
	{
		toFix = Pattern.compile("(?i)(&|§)([a])").matcher(toFix).replaceAll("\u001B[32m\u001B[1m"); // Light Green
		toFix = Pattern.compile("(?i)(&|§)([b])").matcher(toFix).replaceAll("\u001B[36m"); // Aqua
		toFix = Pattern.compile("(?i)(&|§)([c])").matcher(toFix).replaceAll("\u001B[31m"); // Red
		toFix = Pattern.compile("(?i)(&|§)([d])").matcher(toFix).replaceAll("\u001B[35m\u001B[1m"); // Pink
		toFix = Pattern.compile("(?i)(&|§)([e])").matcher(toFix).replaceAll("\u001B[33m\u001B[1m"); // Yellow
		toFix = Pattern.compile("(?i)(&|§)([f])").matcher(toFix).replaceAll("\u001B[0m"); // White
		toFix = Pattern.compile("(?i)(&|§)([0])").matcher(toFix).replaceAll("\u001B[30m"); // Black
		toFix = Pattern.compile("(?i)(&|§)([1])").matcher(toFix).replaceAll("\u001B[34m"); // Dark Blue
		toFix = Pattern.compile("(?i)(&|§)([2])").matcher(toFix).replaceAll("\u001B[32m"); // Dark Green
		toFix = Pattern.compile("(?i)(&|§)([3])").matcher(toFix).replaceAll("\u001B[34m\u001B[1m"); // Light Blue
		toFix = Pattern.compile("(?i)(&|§)([4])").matcher(toFix).replaceAll("\u001B[31m"); // Dark Red
		toFix = Pattern.compile("(?i)(&|§)([5])").matcher(toFix).replaceAll("\u001B[35m"); // Purple
		toFix = Pattern.compile("(?i)(&|§)([6])").matcher(toFix).replaceAll("\u001B[33m"); // Gold
		toFix = Pattern.compile("(?i)(&|§)([7])").matcher(toFix).replaceAll("\u001B[37m"); // Light Grey
		toFix = Pattern.compile("(?i)(&|§)([8])").matcher(toFix).replaceAll("\u001B[30m\u001B[1m"); // Dark Grey
		toFix = Pattern.compile("(?i)(&|§)([9])").matcher(toFix).replaceAll("\u001B[34m"); // Dark Aqua
		toFix = Pattern.compile("(?i)(&|§)([r])").matcher(toFix).replaceAll("\u001B[0m");
		toFix += "\u001B[0m"; // Stop colour from overflowing to the next line with a reset code

		return toFix;
	}

	public static String translateColours(String toFix)
	{
		// Convert every single colour code and formatting code, excluding
		// 'magic' (&k), capitals and lowercase are converted.
		Pattern chatColorPattern = Pattern.compile("(?i)&([0-9A-Fa-f-l-oL-OrR])");
		String fixedString = chatColorPattern.matcher(toFix).replaceAll("\u00A7$1");
		return fixedString;
	}

	public static List<String> translateColours(List<?> lines)
	{
		try
		{
			String[] lineString = null;
			if (lines.size() > 0)
			{
				lineString = lines.toArray(new String[lines.size()]);
			}
			else
			{
				return null;
			}
			
			for (int i = 0; i < lines.size(); i++)
			{
				lineString[i] = Pattern.compile("(?i)&([0-9A-Fa-fk-oK-OrR])").matcher(lineString[i]).replaceAll("\u00A7$1");
			}
			return Arrays.asList(lineString);
		}
		catch (NullPointerException e)
		{
			return null;
		}

	}

	// ------------------------------------------------------------------------------------------------------
	// Printing
	// ------------------------------------------------------------------------------------------------------

	public static void printSuccess(CommandSender sender, String message)
	{
		sender.sendMessage(playerPrefix + ChatColor.DARK_GREEN + "" + ChatColor.BOLD + "" + ChatColor.ITALIC + "SUCCESS: " + ChatColor.GREEN + translateColours(message));
	}

	public static void printPlain(CommandSender sender, String message)
	{
		sender.sendMessage(playerPrefix + translateColours(message));
	}

	public static void printInfo(CommandSender sender, String message)
	{
		sender.sendMessage(playerPrefix + ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "" + ChatColor.ITALIC + "INFO: " + ChatColor.BLUE + translateColours(message));
	}

	public static void printError(CommandSender sender, String message)
	{
		sender.sendMessage(playerPrefix + ChatColor.DARK_RED + "" + ChatColor.BOLD + "" + ChatColor.ITALIC + "ERROR: " + ChatColor.RED + translateColours(message));
	}

	// ------------------------------------------------------------------------------------------------------
	// Broadcasting
	// ------------------------------------------------------------------------------------------------------

	public void broadcastPlain(String message)
	{
		Bukkit.broadcastMessage(message);
	}

	// ------------------------------------------------------------------------------------------------------
	// Logging
	// ------------------------------------------------------------------------------------------------------

	public static void logSuccess(String message)
	{
		log.log(Level.INFO, translateConsoleColours(prefix + "&2SUCCESS: &a" + message));
	}

	public static void logPlain(String message)
	{
		log.log(Level.INFO, translateConsoleColours(prefix + message));
	}

	public static void logInfo(String message)
	{
		log.log(Level.INFO, translateConsoleColours(prefix + "&9INFO: &b" + message));
	}

	public static void logError(String message)
	{
		log.log(Level.WARNING, translateConsoleColours(prefix + "&4ERROR: &c" + message));
	}

	public static void logDebug(String message)
	{
		if (debug)
		{
			log.log(Level.INFO, translateConsoleColours(prefix + "&2DEBUG: &a" + message));
		}
	}

	public static void logSevere(String message)
	{
		log.log(Level.SEVERE, translateConsoleColours(prefix + "&4SEVERE: &c" + message));
	}

	// ------------------------------------------------------------------------------------------------------
	// Miscellaneous
	// ------------------------------------------------------------------------------------------------------

	public static String trailConfigName(String particleString)
	{
		switch (particleString)
		{
			case "BARRIER": return "barrier";
			case "CLOUD": return "cloud";
			case "CRIT": return "criticals";
			case "CRIT_MAGIC": return "criticals-magic";
			case "DRIP_LAVA": return "drip-lava";
			case "DRIP_WATER": return "drip-water";
			case "ENCHANTMENT_TABLE": return "enchant";
			case "EXPLOSION_NORMAL": return "explosion-smoke";
			case "FIREWORKS_SPARK": return "firework";
			case "FLAME": return "flame";
			case "HEART": return "hearts";
			case "LAVA": return "lava";
			case "NOTE": return "note";
			case "PORTAL": return "portal";
			case "REDSTONE": return "redstone";
			case "SLIME": return "slime";
			case "SMOKE_LARGE": return "smoke";
			case "SNOW_SHOVEL": return "snow-shovel";
			case "SNOWBALL": return "snow-ball";
			case "SPELL": return "spell";
			case "SPELL_INSTANT": return "spell-instant";
			case "SPELL_MOB": return "spell-mob";
			case "SPELL_WITCH": return "spell-witch";
			case "VILLAGER_ANGRY": return "angry-villager";
			case "VILLAGER_HAPPY": return "happy-villager";
			case "TOWN_AURA": return "town-aura";
			case "WATER_DROP": return "water-drop";
			case "WATER_SPLASH": return "water-splash";
			default: return "";
		}
	}
	
	public static String intToColour(int colour)
	{
		switch (colour)
		{
			case 1: return "red";
			case 2: return "darkgreen";
			case 3: return "brown";
			case 4: return "darkblue";
			case 5: return "purple";
			case 6: return "cyan";
			case 7: return "lightgrey";
			case 8: return "grey";
			case 9: return "pink";
			case 10: return "lime";
			case 11: return "yellow";
			case 12: return "lightblue";
			case 13: return "magenta";
			case 14: return "orange";
			case 16: return "random";
			default: return "black";
		}
	}
}
