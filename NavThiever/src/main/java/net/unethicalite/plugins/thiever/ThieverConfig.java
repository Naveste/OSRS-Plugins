package net.unethicalite.plugins.thiever;

import net.runelite.client.config.*;

@ConfigGroup("Thiever")
public interface ThieverConfig extends Config
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
			keyName = "elfspot",
			name = "Elf",
			description = "Choose elf to rob",
			position = 0
	)
	default Elf elf()
	{
		return Elf.CELEBRIAN;
	}

	@ConfigItem(
			keyName = "shadowVeil",
			name = "Use Shadow Veil",
			description = "",
			position = 0
	)
	default boolean useShadowVeil()
	{
		return false;
	}

	@ConfigItem(
			keyName = "storeRunesToPouch",
			name = "Store runes in Rune pouch",
			description = "Stores Death runes and Nature runes in the Rune pouch. \nNote: make sure the Rune pouch isn't full.",
			position = 0
	)
	default boolean runesToPouch()
	{
		return false;
	}

	@Range(min = 1, max = 28)
	@ConfigItem(
			keyName = "dodgyNecklace",
			name = "Dodgy Necklace",
			description = "Amount to withdraw",
			position = 1
	)
	default int dodgyNeck()
	{
		return 4;
	}

	@ConfigItem(
			keyName = "food",
			name = "Food",
			description = "Food to eat",
			position = 2
	)
	default Food food()
	{
		return Food.LOBSTER;
	}

	@Range(max = 28)
	@ConfigItem(
			keyName = "foodAmount",
			name = "Food amount",
			description = "Amount of food to withdraw",
			position = 3
	)
	default int foodAmount()
	{
		return 10;
	}

	@Range(max = 99)
	@ConfigItem(
			keyName = "eatHealth",
			name = "Eat below health",
			description = "Minimum health to eat at",
			position = 4
	)
	default int healthValue()
	{
		return 30;
	}

	@ConfigItem(keyName = "excludedItems",
			name = "Items to drop",
			description = "Full names of items to drop. Format: item,item. Case insensitive.",
			position = 5
	)
	default String excludedItems() {
		return "Gold ore,Fire orb,Diamond";
	}

	@ConfigItem(
			keyName = "achievementDiary",
			name = "Achievement Diary",
			description = "Pouch is opened at random quantity within range that is based on your achievement diary",
			position = 6
	)
	default AchievementDiary diaryLevel()
	{
		return AchievementDiary.NONE;
	}

	@ConfigItem(
			keyName = "logout",
			name = "Logout if no resources found",
			description = "Logs out if required resources are not found in bank. Note: only required if you have Never Log enabled.",
			position = 6
	)
	default boolean logoutIfNull()
	{
		return true;
	}
}


