package net.unethicalite.plugins.boneprayer;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.plugins.LoopedPlugin;
import net.unethicalite.api.utils.MessageUtils;
import net.unethicalite.api.widgets.Dialog;
import org.pf4j.Extension;
import javax.inject.Inject;
import java.time.Instant;

@Extension
@PluginDescriptor(
		name = "BonePrayer",
		description = "Testing",
		enabledByDefault = false
)
@Slf4j
public class BonePrayerPlugin extends LoopedPlugin
{
	@Inject
	private BonePrayerConfig config;
	@Inject
	Client client;

	@Inject
	private ClientThread clientThread;
	public static boolean startBot;
	Instant botTimer;
	int timeout;

	@Override
	protected void startUp()
	{
	}

	@Override
	protected void shutDown() {
	}

	@Override
	protected int loop() {
		if (!startBot){
			return 100;
		}

		if (Dialog.isEnterInputOpen() && !Bank.isOpen()){
			Dialog.close();
			return -1;
		}

		var bankBooth = TileObjects.getNearest(x -> x.getName().contains(" booth") || x.getName().contains("Banker"));

		if (!Bank.contains(config.bones().getID()) && !Inventory.contains(config.bones().getID())){
			startBot = false;
			MessageUtils.addMessage("Couldnt find selected bones in the bank. Stopping.");
			return -1;
		}
		if (Inventory.contains(config.bones().getID())){
			if (!Bank.isOpen()){
				if (client.getLocalPlayer().getAnimation() != 827){
					Inventory.getFirst(config.bones().getName()).interact("Bury");
				}
			}
		} else {
			if (bankBooth != null) {
				bankBooth.interact("Bank");
			}
			Time.sleepUntil(Bank::isOpen, 1000);
			if (Bank.isOpen()) {
				Bank.withdraw(config.bones().getID(), Inventory.getFreeSlots(), Bank.WithdrawMode.ITEM);
				Time.sleepUntil(() -> Inventory.contains(config.bones().getID()), 2500);
			}
			Bank.close();
		}

		return -1;

	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("BonePrayer")) {
			return;
		}
		log.debug("button {} pressed!", configButtonClicked.getKey());
		if (configButtonClicked.getKey().equals("startButton")) {
			if (!startBot) {
				Player player = client.getLocalPlayer();
				if (client != null && player != null && client.getGameState() == GameState.LOGGED_IN) {
					MessageUtils.addMessage("Starting plugin");
					startBot = true;
					timeout = 0;
					botTimer = Instant.now();
				} else {
					log.info("Start logged in");
				}
			} else {
				MessageUtils.addMessage("Stopping plugin");
				startBot = false;
				botTimer = null;
			}
		}
	}

	@Provides
	BonePrayerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(BonePrayerConfig.class);
	}
}