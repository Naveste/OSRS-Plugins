package net.unethicalite.plugins.abyss;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.ConfigButtonClicked;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.commons.Rand;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.plugins.LoopedPlugin;
import net.unethicalite.api.utils.MessageUtils;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.api.widgets.Widgets;
import net.unethicalite.client.Static;
import org.jetbrains.annotations.NotNull;
import org.pf4j.Extension;

import javax.inject.Inject;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Extension
@PluginDescriptor(
		name = "Abyss",
		description = "Testing",
		enabledByDefault = false
)
@Slf4j
public class AbyssPlugin extends LoopedPlugin
{
	@Inject
	private AbyssConfig config;
	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;
	private boolean startBot;
	Instant botTimer;
	int timeout;
	CurrentState state = new CurrentState();
	static final int GIANT_POUCH = ItemID.GIANT_POUCH;
	static final int LARGE_POUCH = ItemID.LARGE_POUCH;
	static final int MEDIUM_POUCH = ItemID.MEDIUM_POUCH;
	static final int SMALL_POUCH = ItemID.SMALL_POUCH;
	static final int GIANT_POUCH_5515 = ItemID.GIANT_POUCH_5515;
	static final int LARGE_POUCH_5513 = ItemID.LARGE_POUCH_5513;
	static final int MEDIUM_POUCH_5511 = ItemID.MEDIUM_POUCH_5511;
	private final int[] pouchIds = {GIANT_POUCH, GIANT_POUCH_5515, LARGE_POUCH, MEDIUM_POUCH, SMALL_POUCH};
	private final int[] degradedPouches = {GIANT_POUCH_5515, LARGE_POUCH_5513, MEDIUM_POUCH_5511};
	Random rand = new Random();

	@Override
	protected void startUp() {
	}

	@Override
	protected void shutDown() {
		state.resetState();
	}

	public void banking() {
		TileObject bankBooth = TileObjects.getNearest(x -> x.getName().contains("Bank booth"));
		if (Equipment.contains(config.tpmethod().getName() + "(1)")){
			Equipment.fromSlot(EquipmentInventorySlot.RING).interact("Remove");
			Time.sleep(1000);
			bankBooth.interact("Bank");
		}
		if (Inventory.contains(config.tpmethod().getName() + "(1)")){
			Bank.deposit(config.tpmethod().getName() + "(1)", 1);
			Time.sleep(1000);
		}
		if (config.tpmethod().equals(TPMethod.RING_OF_DUELING) && !Equipment.contains(ItemID.RING_OF_DUELING8)){
			Bank.withdraw(ItemID.RING_OF_DUELING8, 1, Bank.WithdrawMode.ITEM);
			Bank.close();
			Time.sleep(1000);
			Inventory.getFirst(ItemID.RING_OF_DUELING8).interact("Wear");
		} else {
			if (Bank.contains(ItemID.PURE_ESSENCE) || Bank.getCount(true, ItemID.PURE_ESSENCE) >= availableInventorySlots()){
				if (Inventory.contains(x -> x.getId() == ItemID.SMALL_POUCH
						|| x.getId() == ItemID.MEDIUM_POUCH
						|| x.getId() == ItemID.LARGE_POUCH
						|| x.getId() == ItemID.GIANT_POUCH)) {
					Bank.depositAll(config.runes().getName() + " rune");
					//start inventory with full of essences
					Bank.withdraw(ItemID.PURE_ESSENCE, availableInventorySlots(), Bank.WithdrawMode.ITEM);

					for (int pouchId : pouchIds) {
						try {
							if (Inventory.contains(pouchId)) {
								Bank.Inventory.getFirst(pouchId).interact("Fill");
							} else if (Inventory.getCount(ItemID.PURE_ESSENCE) != availableInventorySlots()) {
								Time.sleep(Rand.nextInt(100, 1000));
								Bank.withdraw(ItemID.PURE_ESSENCE, Inventory.getFreeSlots(), Bank.WithdrawMode.ITEM);
							}
						} catch (Exception e) {
							System.out.println("Error filling pouch: " + e.getMessage());
						}
					}
				} else if (!Bank.contains(ItemID.PURE_ESSENCE)){
					MessageUtils.addMessage("Did not find any essence pouch in the inventory. Stopping.");
					startBot = false;
				}
			} else {
				MessageUtils.addMessage("Did not find any pure essences in the bank. Stopping.");
				startBot = false;
			}
		}
	}

	@Override
	protected int loop() {

		if (!startBot || Game.getState() != GameState.LOGGED_IN && client.getLocalPlayer() != null){
			return 100;
		}

		if (Dialog.isEnterInputOpen() && !Bank.isOpen()){
			Dialog.close();
			return -1;
		}

		if (!Movement.isRunEnabled() && Static.getClient().getEnergy() > 10){
			Movement.toggleRun();
			return -1;
		}

		Player local = client.getLocalPlayer();
		TileObject bankBooth = TileObjects.getNearest(x -> x.getName().contains("Bank booth"));
		TileObject rift = TileObjects.getNearest(x -> x.getName().equals(config.runes().getName() + " rift"));
		TileObject altar = TileObjects.getNearest(x -> x.getName().equals("Altar"));
		if (client.getLocalPlayer().getWorldLocation().getRegionID() == 12342 || client.getLocalPlayer().getWorldLocation().getRegionID() == 12343) {
			if (Inventory.getCount(ItemID.PURE_ESSENCE) != availableInventorySlots()) {
				try {
					bankBooth.interact("Bank");
					Time.sleep(500);
					banking();
				} catch (Exception e){
					///
				}
			} else {
				walkToAbyss();
				TileObject wildernessDitch = TileObjects.getNearest(x -> x.getName().equals("Wilderness Ditch") && x.distanceTo(local) < 15);
				if (wildernessDitch != null && client.getLocalPlayer().getWorldLocation().getWorldY() <= 3522) {
					wildernessDitch.interact("Cross");
					Time.sleepUntil(() -> client.getLocalPlayer().getWorldY() == 3523, 5000);
				}
			}

			NPC MageofZamorak = NPCs.getNearest(2581);
			if (MageofZamorak != null && local.getWorldLocation().distanceTo(MageofZamorak) < 10) {
				MageofZamorak.interact("Teleport");
				Time.sleepUntil(() -> client.getLocalPlayer().getWorldLocation().getRegionID() == 12107, 5000);
			}
		}

		if (client.getLocalPlayer().getWorldLocation().getRegionID() == 12107) {
			NPC darkMage = NPCs.getNearest(2583);

			TileObject agilityGap = TileObjects.getNearest(x -> x.getName().contains("Gap") && x.distanceTo(local) < 20);
			TileObject miningRock = TileObjects.getNearest(x -> x.getName().contains("Rock") && x.distanceTo(local) < 20);
			TileObject thievingEyes = TileObjects.getNearest(x -> x.getName().contains("Eyes") && x.distanceTo(local) < 20);

			try {
				if (agilityGap != null && Reachable.isInteractable(agilityGap)) {
					interactWithObj(agilityGap, "Squeeze-through");

				} else if (miningRock != null && Reachable.isInteractable(miningRock) && Equipment.contains("Rune pickaxe")) {
					interactWithObj(miningRock, "Mine");

				} else if (thievingEyes != null && Reachable.isInteractable(thievingEyes)) {
					interactWithObj(thievingEyes, "Distract");
				}

				if (Inventory.contains(degradedPouches) && darkMage != null) {
					darkMage.interact("Repairs");
					Time.sleep(3000);
				} else if (Reachable.isInteractable(rift)) {
					rift.interact("Exit-through");
					Time.sleepUntil(() -> local.getWorldLocation().getRegionID() == config.runes().getRegionID(), 5000);
				}
			} catch (Exception e){
				/////////////
			}
		}

		if (state.getState() == 0 && altar != null && pouchIds != null
				&& local.getWorldLocation().getRegionID() == config.runes().getRegionID()){

			//craft the full inventory first before emptying the pouches.
			altar.interact("Craft-rune");
			Time.sleep(1200);

			for (int pouch : pouchesInInventory()) {
				Inventory.getFirst(pouch).interact("Empty");
				altar.interact("Craft-rune");
				Time.sleep(560);

			}
			state.setState(1);
		}

		if (state.getState() == 1) {
			if (config.tpmethod().equals(TPMethod.CONSTRUCTION_CAPET) && Equipment.contains(ItemID.CONSTRUCT_CAPET)){
				Equipment.fromSlot(EquipmentInventorySlot.CAPE).interact("Tele to POH");
			}
			if (config.tpmethod().equals(TPMethod.RING_OF_DUELING)){
				Equipment.fromSlot(EquipmentInventorySlot.RING).interact(config.banking().getName());
			}
			state.resetState();
			Time.sleep(4000);
		}

		TileObject pool = TileObjects.getNearest(x -> x.getName().toLowerCase().contains("pool") && x.hasAction("Drink"::equals));
		TileObject jewellery = TileObjects.getNearest(x -> x.getName().toLowerCase().contains("jewellery"));
		if (pool != null && jewellery != null) {
			if (client.getEnergy() / 100 < 50) {
				pool.interact("Drink");
			} else if (jewellery.hasAction(config.banking().getName())){
				if (config.banking().equals(Banking.EDGEVILLE) || config.banking().equals(Banking.FEROX_ENCLAVE) || config.banking().equals(Banking.CASTLE_WARS)) {
					jewellery.interact(config.banking().getName());
					Time.sleepUntil(() -> client.getLocalPlayer().getWorldLocation().getRegionID() == 12342, 3000);
				}
			} else {
				jewellery.interact("Teleport Menu");
				Time.sleep(1000);
				Widget widget = Widgets.get(590, 1, 0);
				if (widget != null && widget.isVisible()) {
					selectBanking();
				}
			}
		}

		return 500;
	}

	public void interactWithObj(@NotNull TileObject obj, String action){
		obj.interact(action);
		Time.sleep(3000);
	}

	public void walkToAbyss(){
		if (client.getLocalPlayer().getWorldLocation().getWorldY() <= 3520) {
			Time.sleep(1000);
			Movement.walkTo(new WorldPoint(3101, 3520, 0));
		} else {
			Time.sleep(750);
			Movement.walkTo(new WorldPoint(3105, 3557, 0));
		}
	}

	public List<Integer> pouchesInInventory() {
		List<Integer> pouches = new ArrayList<>();

		for (int pouch : pouchIds) {
			if (Inventory.contains(pouch)) {
				pouches.add(pouch);
			}
		}
		return pouches;
	}
	public void selectBanking(){
		if (config.banking().equals(Banking.EDGEVILLE)){
			Static.getClient().interact(1, 57, 5, 38666247);
		}
		if (config.banking().equals(Banking.FEROX_ENCLAVE)){
			Static.getClient().interact(1, 57, 7, 38666242);
		}
		if (config.banking().equals(Banking.CASTLE_WARS)){
			Static.getClient().interact(1, 57, 6, 38666242);
		}
	}

	public int availableInventorySlots() {
		ArrayList<Integer> pouches = new ArrayList<>();

		for (int pouch : pouchIds) {
			if (Inventory.contains(pouch)) {
				pouches.add(pouch);
			}
		}
		return 28 - pouches.size();
	}

	public int randomDelay(int min, int max){
		return rand.nextInt((max - min) + 1) + min;
	}

	@Subscribe
	private void onChatMessage(ChatMessage event){
		if (!startBot)
			return;
		if (event.getType() == ChatMessageType.CONSOLE) {
			return;
		}

		String pouchDegraded = "Your pouch has decayed through use.";

		if (event.getMessage().contains(pouchDegraded)){
			//add functionality here.
			MessageUtils.addMessage("Pouch degraded. Will repair at Dark Mage before entering rift.");
		}
	}


	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("Abyss")) {
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
	AbyssConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(AbyssConfig.class);
	}
}
