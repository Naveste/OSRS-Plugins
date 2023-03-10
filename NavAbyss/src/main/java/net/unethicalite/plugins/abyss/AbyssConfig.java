package net.unethicalite.plugins.abyss;

import net.runelite.client.config.Button;import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("Abyss")
public interface AbyssConfig extends Config
{
	@ConfigItem(
			keyName = "startButton",
			name = "Start/Stop",
			description = "Test button that changes variable value",
			position = 350
	)
	default Button startButton() {
		return new Button();
	}


	@ConfigItem(
			keyName = "banking",
			name = "Banking",
			description = "Choose where to restock",
			position = 0
	)
	default Banking banking()
	{
		return Banking.EDGEVILLE;
	}

	@ConfigItem(
			keyName = "runes",
			name = "Runes to craft",
			description = "",
			position = 1
	)
	default Runes runes()
	{
		return Runes.NATURE_RUNE;
	}

	@ConfigItem(
			keyName = "tpmethod",
			name = "Teleport method",
			description = "",
			position = 2
	)
	default TPMethod tpmethod()
	{
		return TPMethod.CONSTRUCTION_CAPET;
	}

}
