package net.unethicalite.plugins.navminer;

import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("NavMiner")
public interface NavMinerConfig extends Config
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
}
