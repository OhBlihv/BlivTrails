package net.auscraft.BlivTrails;

import lombok.Getter;

/**
 * Created by Chris on 4/24/2016.
 */
public enum OptionType
{

	//Type
	TYPE("type", 0),
	TYPE_TRACE("type.trace", 1),
	TYPE_RANDOM("type.random", 2),
	TYPE_DYNAMIC("type.dynamic", 3),

	//Length
	LENGTH("length", 0),
	LENGTH_SHORT("length.short", 1),
	LENGTH_MEDIUM("length.medium", 2),
	LENGTH_LONG("length.long", 3),

	//Height
	HEIGHT("height", 0),
	HEIGHT_FEET("height.feet", 0),
	HEIGHT_WAIST("height.waist", 1),
	HEIGHT_HALO("height.halo", 2),

	NONE("", 0);

	//Colour?

	@Getter
	private String configName;

	@Getter
	private int cfgId;

	OptionType(String configName, int cfgId)
	{
		this.configName = configName;
		this.cfgId = cfgId;
	}

	public boolean isOptionActive(OptionType activeCfgId)
	{
		return activeCfgId == this;
	}

	public static OptionType parseTypeString(String typeString)
	{
		if(typeString == null || typeString.isEmpty()) return TYPE_TRACE;

		switch (typeString)
		{
			case "trace":
				return TYPE_TRACE;
			case "random":
				return TYPE_RANDOM;
			case "dynamic":
				return TYPE_DYNAMIC;
			default:
				return NONE; //Should be invalid
		}
	}

	public static OptionType parseTypeInt(int typeInt)
	{
		switch(typeInt)
		{
			case 1:
				return TYPE_TRACE;
			case 2:
				return TYPE_RANDOM;
			case 3:
				return TYPE_DYNAMIC;
			default:
				return NONE; //Should be invalid
		}
	}

	public static OptionType parseLengthString(String lengthString)
	{
		if(lengthString == null || lengthString.isEmpty()) return LENGTH_SHORT;

		switch (lengthString)
		{
			case "short":
				return LENGTH_SHORT;
			case "medium":
				return LENGTH_MEDIUM;
			case "long":
				return LENGTH_LONG;
			default:
				return NONE; //Should be invalid
		}
	}

	public static OptionType parseLengthInt(int lengthInt)
	{
		switch(lengthInt)
		{
			case 1:
				return LENGTH_SHORT;
			case 2:
				return LENGTH_MEDIUM;
			case 3:
				return LENGTH_LONG;
			default:
				return NONE; //Should be invalid
		}
	}

	public static OptionType parseHeightString(String heightString)
	{
		if(heightString == null || heightString.isEmpty()) return HEIGHT_FEET;

		switch (heightString)
		{
			case "feet":
				return HEIGHT_FEET;
			case "waist":
				return HEIGHT_WAIST;
			case "halo":
				return HEIGHT_HALO;
			default:
				return NONE; //Should be invalid
		}
	}

	public static OptionType parseHeightInt(int heightInt)
	{
		switch(heightInt)
		{
			case 0:
				return HEIGHT_FEET;
			case 1:
				return HEIGHT_WAIST;
			case 2:
				return HEIGHT_HALO;
			default:
				return NONE; //Should be invalid
		}
	}

	public static int parseColourString(String colourString)
	{
		if(colourString == null || colourString.isEmpty()) return 0;

		switch (colourString)
		{
			case "red":
				return 1;
			case "dark green":
				return 2;
			case "brown":
				return 3;
			case "dark blue":
				return 4;
			case "purple":
				return 5;
			case "cyan":
				return 6;
			case "light grey":
			case "light gray":
				return 7;
			case "grey":
			case "gray":
				return 8;
			case "pink":
				return 9;
			case "lime":
				return 10;
			case "yellow":
				return 11;
			case "light blue":
				return 12;
			case "magenta":
				return 13;
			case "orange":
				return 14;
			case "black":
				return 15;
			case "random":
				return 16;
			default:
				return 0;
		}
	}

}
