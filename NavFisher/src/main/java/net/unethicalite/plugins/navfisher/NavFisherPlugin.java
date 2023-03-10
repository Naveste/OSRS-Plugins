package net.unethicalite.plugins.navfisher;

import com.google.inject.Provides;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import net.runelite.client.config.ConfigManager;
import net.unethicalite.api.input.Keyboard;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.plugins.LoopedPlugin;
import net.unethicalite.api.utils.MessageUtils;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.widgets.Dialog;
import org.pf4j.Extension;
import javax.inject.Inject;

@Extension
@PluginDescriptor(
		name = "LunasFisher",
		description = "Testing",
		enabledByDefault = false
)
@Slf4j
public class NavFisherPlugin extends LoopedPlugin
{
	@Inject
	private NavFisherConfig config;
	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;
	private boolean startBot;
	Instant botTimer;
	int timeout;
	private static final String CATCH = "Catch";
	private static final String GET_BIRD = "Get bird";
	private static final String REMOVE = "Remove";
	private static final WorldPoint FISHING_SPOT_EAST = new WorldPoint(1376, 3629, 0);
	private static final WorldPoint FISHING_SPOT_WEST = new WorldPoint(1360, 3636, 0);
	private static final int LAKE_MOLCH = 5432;

	@Override
	protected void startUp() {
	}

	@Override
	protected void shutDown() {
	}


	@Override
	protected int loop() {
		if (!startBot){
			return 100;
		}

		if (client.getGameState() != GameState.LOGGED_IN){
			return 1000;
		}

		if (client.getLocalPlayer().isMoving()){
			return 100;
		}

		if (config.fishingMethod().equals(Method.AERIAL_FISHING) && isInRegion() && birdFood()) {
			unEquipStuff();
			if (!Equipment.contains("Cormorant's glove") && !Dialog.isOpen()) {
				NPC alry = NPCs.getNearest("Alry the Angler");
				if (alry != null) {
					alry.interact(GET_BIRD);
				}
				Time.sleep(1000);
			} else if (Dialog.isOpen() && Dialog.canContinue()) {
				Keyboard.sendSpace();
			} else {
				if (client.getLocalPlayer().getWorldLocation().distanceTo(fishingSpot()) < 1) {
					handleFishing();
				} else {
					Movement.walk(fishingSpot());
				}
			}
		}

		return -1;

	}

	private boolean birdFood(){
		if (Inventory.contains(ItemID.FISH_CHUNKS, ItemID.KING_WORM)){
			return true;
		}
		MessageUtils.addMessage("Bird food not found. Stopping");
		startBot = false;
		return false;
	}

	private boolean isInRegion(){
		if (client.getLocalPlayer().getWorldLocation().getRegionID() == LAKE_MOLCH){
			return true;
		}
		MessageUtils.addMessage("You are not at the correct location!");
		startBot = false;
		return false;
	}
	private void handleFishing(){
		NPC fishingSpot = NPCs.getNearest(x -> x.getName().equalsIgnoreCase("Fishing spot") && x.hasAction(CATCH)
				&& x.distanceTo(fishingSpot()) < config.aerialDistance());

		if (config.safeMode()) {
			if (fishingSpot != null && Equipment.contains(ItemID.CORMORANTS_GLOVE_22817)) {
				fishingSpot.interact(CATCH);
			}
		} else {
			if (fishingSpot != null && client.getLocalPlayer().getInteracting() == null) {
				fishingSpot.interact(CATCH);
				Time.sleep(Rand.nextInt(300, 600));
			}
		}
	}

	private void unEquipStuff(){
		Item equippedWeapon = Equipment.fromSlot(EquipmentInventorySlot.WEAPON);
		if (!Equipment.contains("Cormorant's glove") && equippedWeapon != null){
			MessageUtils.addMessage("Removing item from weapon slot.");
			removeEquip(equippedWeapon);
		}
		Item equippedShield = Equipment.fromSlot(EquipmentInventorySlot.SHIELD);
		if (equippedShield != null){
			MessageUtils.addMessage("Removing item from shield slot.");
			removeEquip(equippedShield);
		}
	}

	private void removeEquip(Item item){
		item.interact(REMOVE);
		Time.sleep(500);
	}

	@Subscribe
	private void onItemContainerChanged(ItemContainerChanged event){
		if (!startBot){
			return;
		}

		if (config.fishingMethod().equals(Method.AERIAL_FISHING)) {
			Item knife = Inventory.getFirst(x -> x.getId() == ItemID.KNIFE);
			Item fish = Inventory.getFirst(ItemID.BLUEGILL, ItemID.COMMON_TENCH, ItemID.MOTTLED_EEL, ItemID.GREATER_SIREN);
			if (knife != null && fish != null) {
				knife.useOn(fish);
				Time.sleep(10);
			}
		}
	}

	private WorldPoint fishingSpot() {
		return config.fishingSpot().equals(AerialFishingSpot.EAST) ? FISHING_SPOT_EAST : FISHING_SPOT_WEST;
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("LunasFisher")) {
			return;
		}
		log.debug("button {} pressed!", configButtonClicked.getKey());
		if (configButtonClicked.getKey().equals("startButton")) {
			if (!startBot) {
				Player player = client.getLocalPlayer();
				if (player != null && client.getGameState() == GameState.LOGGED_IN) {
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
	NavFisherConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(NavFisherConfig.class);
	}
}