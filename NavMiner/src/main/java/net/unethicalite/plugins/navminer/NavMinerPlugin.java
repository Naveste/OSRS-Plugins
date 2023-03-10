package net.unethicalite.plugins.navminer;

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.plugins.LoopedPlugin;
import net.unethicalite.api.utils.MessageUtils;
import net.runelite.client.plugins.PluginDescriptor;
import org.pf4j.Extension;
import javax.inject.Inject;

@Extension
@PluginDescriptor(
		name = "NavMiner",
		description = "Testing",
		enabledByDefault = false
)
@Slf4j
public class NavMinerPlugin extends LoopedPlugin
{
	@Inject
	private NavMinerConfig config;
	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;
	private boolean startBot;
	Instant botTimer;
	int timeout;
	private final WorldPoint miningSpot = new WorldPoint(3021, 9721, 0);

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

		TileObject ironRock = TileObjects.getNearest(x -> x.getId() == 11365 || x.getId() == 11364
				&& x.distanceTo(client.getLocalPlayer().getWorldLocation()) <= 1);
		if (client.getLocalPlayer().getWorldLocation().distanceTo(miningSpot) == 0 && !Inventory.isFull()
				&& !Bank.isOpen() && ironRock != null){
			if (client.getLocalPlayer().getAnimation() == -1) {
				ironRock.interact("Mine");
				Time.sleepUntil(() -> client.getLocalPlayer().getAnimation() != -1, 5000);
			}
			TileItem unidentifiedMinerals = TileItems.getNearest(x ->
					x.getTile().getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation()) < 5
							&& x.getName().contains("Unidentified minerals"));
			if (unidentifiedMinerals != null && !Inventory.isFull()){
				unidentifiedMinerals.interact("Take");
				return -1;
			}
		} else if (Inventory.isFull()){
			TileObject bankChest = TileObjects.getNearest(x -> x.getName().equalsIgnoreCase("Bank chest")
					&& x.hasAction("Collect") && x.getName() != null);
			bankChest.interact("Use");
			Time.sleepUntil(Bank::isOpen, 5000);

			if (Bank.isOpen()){
				Bank.depositInventory();
				Time.sleep(500);
				Bank.close();
			}
		} else {
			Movement.walkTo(miningSpot);
			Time.sleepUntil(() -> client.getLocalPlayer().getWorldLocation().distanceTo(miningSpot) == 0, 1000);
		}

		return 500;
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("NavMiner")) {
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
	NavMinerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NavMinerConfig.class);
	}
}