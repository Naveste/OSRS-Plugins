package net.unethicalite.plugins.boneprayer;

import net.runelite.client.config.Button;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("BonePrayer")
public interface BonePrayerConfig extends Config
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
			keyName = "bones",
			name = "Bones",
			description = "",
			position = 1
	)
	default Bones bones()
	{
		return Bones.NORMAL_BONES;
	}
}
