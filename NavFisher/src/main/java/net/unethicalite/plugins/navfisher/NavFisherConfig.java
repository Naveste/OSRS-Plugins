package net.unethicalite.plugins.navfisher;

import net.runelite.client.config.*;

@ConfigGroup("LunasFisher")
public interface NavFisherConfig extends Config
{
	@ConfigItem(
			keyName = "startButton",
			name = "Start/Stop",
			description = "",
			position = 350
	)
	default Button startButton() {
		return new Button();
	}

	@ConfigItem(
			keyName = "method",
			name = "Fishing method",
			description = "",
			position = 0
	)
	default Method fishingMethod()
	{
		return Method.AERIAL_FISHING;
	}

	@ConfigItem(
			keyName = "fishingSpot",
			name = "Aerial fishing spot",
			description = "",
			position = 1
	)
	default AerialFishingSpot fishingSpot()
	{
		return AerialFishingSpot.EAST;
	}

	@Range(min = 1, max = 15)
	@ConfigItem(
			keyName = "aerialDistance",
			name = "Aerial fishing max distance",
			description = "The farthest distance the bot can select a fishing spot from the initial standing spot",
			position = 2
	)
	default int aerialDistance()
	{
		return 7;
	}

	@ConfigItem(
			keyName = "safeMode",
			name = "Safe mode",
			description = "LOWER XP/H. Sleeps until bird is on the glove.",
			position = 3
	)
	default boolean safeMode()
	{
		return false;
	}
}
