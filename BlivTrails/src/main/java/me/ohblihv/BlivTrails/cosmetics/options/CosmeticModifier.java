package me.ohblihv.BlivTrails.cosmetics.options;

/**
 * Created by Chris Brown (OhBlihv) on 18/08/2016.
 */
public enum CosmeticModifier
{

	//Cosmetic Display Height
	HEIGHT_MIN(0.1D, "height.min"),
	HEIGHT(0.1D, true, "height.default"),
	HEIGHT_MAX(1.9D, "height.max"),
	
	//Cosmetic Length
	LENGTH_MIN(1D, "length.min"),
	LENGTH(1D, true, "length.default"),
	LENGTH_MAX(1D, "length.max"),
	
	//Cosmetic Display Speed
	SPEED_MIN(1D, "speed.min"),
	SPEED(1D, true, "speed.default"),
	SPEED_MAX(1D, "speed.max"),
	
	//Display Radius
	RADIUS_MIN(1D, "radius.min"),
	RADIUS(1D, true, "radius.default"),
	RADIUS_MAX(1D, "radius.max");
	
	public final boolean modifiable;
	public final double defaultValue; //If defaults are not given in the config, the value will default to this.
	public final String configurationKey;
	
	CosmeticModifier(double defaultValue, String configurationKey)
	{
		this(false, defaultValue, configurationKey);
	}
	
	CosmeticModifier(boolean modifiable, double defaultValue, String configurationKey)
	{
		this.modifiable = modifiable;
		this.defaultValue = defaultValue;
		this.configurationKey = configurationKey;
	}
	
}
