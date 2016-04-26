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

	DEFAULT_X_VARIATION("trails.defaults.type.random.x-variation", 0),
	DEFAULT_Y_VARIATION("trails.defaults.type.random.y-variation", 1),
	DEFAULT_Z_VARIATION("trails.defaults.type.random.z-variation", 2),

	DEFAULT_SPRAY_VARIATION("trails.defaults.type.dynamic.spray-variation", 3),

	DEFAULT_FEET_LOCATION("trails.defaults.height.feet-location", 4),
	DEFAULT_WAIST_LOCATION("trails.defaults.height.waist-location", 5),
	DEFAULT_HALO_LOCATION("trails.defaults.height.halo-location", 6),

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
		return activeCfgId.getCfgId() == this.getCfgId();
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

	public static OptionType getOptionType(OptionType optionType, int valueInt)
	{
		int baseValue = 0;
		if(optionType == HEIGHT)
		{
			baseValue = 1; //HEIGHT_FEET = 0, so it is functionally identical to HEIGHT
		}

		return values()[baseValue + optionType.ordinal() + valueInt];
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
