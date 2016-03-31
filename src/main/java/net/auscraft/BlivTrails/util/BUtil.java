package net.auscraft.BlivTrails.util;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.util.Vector;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class BUtil
{

	public enum FireworkType
	{

		CRATE_SPAWN(Type.BALL, Color.ORANGE, Color.WHITE, Color.YELLOW),
		OPEN_CRATE(Type.BALL, Color.GRAY, Color.WHITE, Color.BLACK),
		PVP_DEATH(Type.BALL, Color.RED, Color.ORANGE, Color.RED);

		@Getter
		private Type type;
		@Getter
		private Color colour1, colour2, colour3;

		FireworkType(Type type, Color colour1, Color colour2, Color colour3)
		{
			this.type = type;
			this.colour1 = colour1;
			this.colour2 = colour2;
			this.colour3 = colour3;
		}

	}

	private static boolean useConsoleColours = false;
	public static boolean DEBUG = false;

	private static final String PLUGIN_NAME = "DWatchdog";
	private static final String prefix = "&f[&b" + PLUGIN_NAME + "&f]&r ";
	private static final String playerPrefix = "\u00A78[\u00A7e" + PLUGIN_NAME + "\u00A78]\u00A7r ";
	private static final java.util.logging.Logger log = Bukkit.getLogger();

	// ------------------------------------------------------------------------------------------------------
	// Miscellaneous
	// ------------------------------------------------------------------------------------------------------

	public static void createFirework(Location location, boolean instantExplosion, FireworkType fireworkType)
	{
		if(fireworkType == null)
		{
			fireworkType = FireworkType.CRATE_SPAWN;
		}

		Random rand = new Random(System.currentTimeMillis());

		FireworkEffect effect = FireworkEffect.builder().flicker(rand.nextBoolean())
				.withColor(fireworkType.getColour1(), fireworkType.getColour2()).withFade(fireworkType.getColour3())
				.with(fireworkType.getType()).trail(rand.nextBoolean()).build();

		if(instantExplosion)
		{
			location.setY(location.getY() + 2.0D);
			//CustomEntityFirework.spawn(location, effect);
		}
		else
		{
			Firework firework = location.getWorld().spawn(location, Firework.class);
			FireworkMeta meta = firework.getFireworkMeta();
			meta.addEffect(effect);
			firework.setFireworkMeta(meta);
			firework.setVelocity(new Vector(0.00, 0.05, 0.00));
		}
	}
	
	// ------------------------------------------------------------------------------------------------------
	// String Translation
	// ------------------------------------------------------------------------------------------------------

	public static String capitaliseFirst(String string)
	{
		return Character.toTitleCase(string.charAt(0)) + string.substring(1, string.length());
	}
	
	public static List<String> translateVariable(List<String> lines, String variable, String content)
	{
		if(lines == null) { return null; }

		if(!lines.isEmpty())
		{
			for(int lineNum = 0;lineNum < lines.size();lineNum++)
			{
				lines.set(lineNum, lines.get(lineNum).replace(variable, content));
			}
		}
		return lines;
	}
	
	public static String stripColours(String toFix)
	{
		return Pattern.compile("[&](.)").matcher(toFix).replaceAll("");
	}

	public static String translateConsoleColours(String toFix)
	{
		if(!useConsoleColours)
		{
			return Pattern.compile("(?i)(&|Â§)([a-f0-9k-r])").matcher(toFix).replaceAll("");
		}
		toFix = Pattern.compile("(?i)(&|Â§)([a])").matcher(toFix).replaceAll("\u001B[32m\u001B[1m"); // Light Green
		toFix = Pattern.compile("(?i)(&|Â§)([b])").matcher(toFix).replaceAll("\u001B[36m"); // Aqua
		toFix = Pattern.compile("(?i)(&|Â§)([c])").matcher(toFix).replaceAll("\u001B[31m"); // Red
		toFix = Pattern.compile("(?i)(&|Â§)([d])").matcher(toFix).replaceAll("\u001B[35m\u001B[1m"); // Pink
		toFix = Pattern.compile("(?i)(&|Â§)([e])").matcher(toFix).replaceAll("\u001B[33m\u001B[1m"); // Yellow
		toFix = Pattern.compile("(?i)(&|Â§)([f])").matcher(toFix).replaceAll("\u001B[0m"); // White
		toFix = Pattern.compile("(?i)(&|Â§)([0])").matcher(toFix).replaceAll("\u001B[30m"); // Black
		toFix = Pattern.compile("(?i)(&|Â§)([1])").matcher(toFix).replaceAll("\u001B[34m"); // Dark Blue
		toFix = Pattern.compile("(?i)(&|Â§)([2])").matcher(toFix).replaceAll("\u001B[32m"); // Dark Green
		toFix = Pattern.compile("(?i)(&|Â§)([3])").matcher(toFix).replaceAll("\u001B[34m\u001B[1m"); // Light Blue
		toFix = Pattern.compile("(?i)(&|Â§)([4])").matcher(toFix).replaceAll("\u001B[31m"); // Dark Red
		toFix = Pattern.compile("(?i)(&|Â§)([5])").matcher(toFix).replaceAll("\u001B[35m"); // Purple
		toFix = Pattern.compile("(?i)(&|Â§)([6])").matcher(toFix).replaceAll("\u001B[33m"); // Gold
		toFix = Pattern.compile("(?i)(&|Â§)([7])").matcher(toFix).replaceAll("\u001B[37m"); // Light Grey
		toFix = Pattern.compile("(?i)(&|Â§)([8])").matcher(toFix).replaceAll("\u001B[30m\u001B[1m"); // Dark Grey
		toFix = Pattern.compile("(?i)(&|Â§)([9])").matcher(toFix).replaceAll("\u001B[34m"); // Dark Aqua
		toFix = Pattern.compile("(?i)(&|Â§)([r])").matcher(toFix).replaceAll("\u001B[0m");
		toFix += "\u001B[0m"; // Stop colour from overflowing to the next line with a reset code

		return toFix;
	}

	private static final Pattern colourPattern = Pattern.compile("(?i)&([0-9A-Fa-f-l-oL-OrR])");

	public static String translateColours(String toFix)
	{
		// Convert every single colour code and formatting code, excluding
		// 'magic' (&k), capitals and lowercase are converted.
		return colourPattern.matcher(toFix).replaceAll("\u00A7$1");
	}

	public static List<String> translateColours(List<String> lines)
	{
		if (lines == null || lines.isEmpty())
		{
			return null;
		}

		for(int lineNum = 0;lineNum < lines.size();lineNum++)
		{
			lines.set(lineNum, colourPattern.matcher(lines.get(lineNum)).replaceAll("\u00A7$1"));
		}
		return lines;
	}

	public static List<String> convertPlaceholders(List<String> lines, String[] placeholders, String[] content)
	{
		if (placeholders.length != content.length || lines == null)
		{
			if(lines != null)
			{
				BUtil.logError("Placeholder length does not match content length! Returning plain lines:\n" + lines.toString());
			}
			return lines;
		}
		lines = new ArrayList<>(lines);

		for(int lineNum = 0;lineNum < lines.size();lineNum++)
		{
			String line = lines.get(lineNum);
			for(int index = 0;index < placeholders.length;index++)
			{
				line = line.replace(placeholders[index], content[index]);
			}
			lines.set(lineNum, line);
		}
		return lines;
	}

	public static String convertPlaceholders(String line, String[] placeholders, String[] content)
	{
		if (placeholders.length != content.length)
		{
			return null;
		}

		for (int index = 0; index < placeholders.length; index++)
		{
			line = line.replace(placeholders[index], content[index]);
		}
		return line;
	}

	// ------------------------------------------------------------------------------------------------------
	// Printing
	// ------------------------------------------------------------------------------------------------------

	public static void printSuccess(CommandSender sender, String message)
	{
		sender.sendMessage(playerPrefix + ChatColor.GREEN + translateColours(message));
	}

	public static void printPlain(CommandSender sender, String message)
	{
		sender.sendMessage(playerPrefix + translateColours(message));
	}

	public static void printInfo(CommandSender sender, String message)
	{
		sender.sendMessage(playerPrefix + ChatColor.YELLOW + translateColours(message));
	}

	public static void printError(CommandSender sender, String message)
	{
		sender.sendMessage(playerPrefix + ChatColor.RED + translateColours(message));
	}

	public static void printSelf(String message)
	{
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer("OhBlihv");
		if(offlinePlayer != null && offlinePlayer.isOnline())
		{
			offlinePlayer.getPlayer().sendMessage(translateColours(message));
		}
	}

	public static void printToOfflinePlayer(String playerName, String message)
	{
		if(playerName == null)
		{
			return;
		}

		printToOfflinePlayer(Bukkit.getOfflinePlayer(playerName), message);
	}

	public static void printToOfflinePlayer(UUID uuid, String message)
	{
		if(uuid == null)
		{
			return;
		}

		printToOfflinePlayer(Bukkit.getOfflinePlayer(uuid), message);
	}

	private static void printToOfflinePlayer(OfflinePlayer offlinePlayer, String message)
	{
		if(offlinePlayer != null && offlinePlayer.isOnline())
		{
			offlinePlayer.getPlayer().sendMessage(message);
		}
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
		//System.out.println(translateConsoleColours(prefix + "&9INFO: &b" + message));
	}

	public static void logError(String message)
	{
		log.log(Level.WARNING, translateConsoleColours(prefix + "&4ERROR: &c" + message));
	}

	public static void logSevere(String message)
	{
		log.log(Level.SEVERE, translateConsoleColours(prefix + "&4SEVERE: &c" + message));
	}

	public static void logDebug(String message)
	{
		if(DEBUG)
		{
			log.log(Level.INFO, translateConsoleColours(prefix + "&2DEBUG: &a" + message));
		}
	}

	// ------------------------------------------------------------------------------------------------------
	// Miscellaneous
	// ------------------------------------------------------------------------------------------------------

	public static Collection<? extends Player> getOnlinePlayers()
	{
		try
		{
			Method getOnlinePlayers = Bukkit.class.getMethod("getOnlinePlayers");
			if(getOnlinePlayers.getReturnType() == Collection.class)
			{
				return (Collection<? extends Player>) getOnlinePlayers.invoke(null);
			}
			else
			{
				return Arrays.asList(((Player[]) getOnlinePlayers.invoke(null)));
			}
		}
		catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e)
		{
			e.printStackTrace();
		}

		return new ArrayList<>();
	}

	private static final BASE64Encoder base64Encoder = new BASE64Encoder();
	private static final BASE64Decoder base64Decoder = new BASE64Decoder();

	public static String compressUUID(UUID uuid)
	{
		return base64Encoder.encode(toBytes(uuid)).split("=")[0];
	}

	public static UUID deCompressUUID(String uuid)
	{
		try
		{
			return fromBytes(base64Decoder.decodeBuffer(uuid.split(":")[0].concat("==")));
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * Copied from UUIDUtils.class to avoid NoClassDefErrors which occur on Compilex' Spigot.
	 */

	public static byte[] toBytes(UUID uuid)
	{
		ByteBuffer byteBuffer = ByteBuffer.wrap(new byte[16]);
		byteBuffer.putLong(uuid.getMostSignificantBits());
		byteBuffer.putLong(uuid.getLeastSignificantBits());
		return byteBuffer.array();
	}

	public static UUID fromBytes(byte[] array)
	{
		if (array.length != 16)
		{
			throw new IllegalArgumentException("Illegal byte array length: " + array.length);
		}

		ByteBuffer byteBuffer = ByteBuffer.wrap(array);
		long mostSignificant = byteBuffer.getLong();
		long leastSignificant = byteBuffer.getLong();

		return new UUID(mostSignificant, leastSignificant);
	}

	// ------------------------------------------------------------------------------------------------------
	// Trails
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
