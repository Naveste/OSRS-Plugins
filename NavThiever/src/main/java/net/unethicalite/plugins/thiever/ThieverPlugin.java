package net.unethicalite.plugins.thiever;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.PluginDescriptor;
import net.unethicalite.api.commons.Time;
import net.unethicalite.api.entities.NPCs;
import net.unethicalite.api.entities.TileItems;
import net.unethicalite.api.entities.TileObjects;
import net.unethicalite.api.game.Combat;
import net.unethicalite.api.game.Game;
import net.unethicalite.api.game.GameThread;
import net.unethicalite.api.items.Bank;
import net.unethicalite.api.items.Equipment;
import net.unethicalite.api.items.Inventory;
import net.unethicalite.api.movement.Movement;
import net.unethicalite.api.movement.Reachable;
import net.unethicalite.api.plugins.LoopedPlugin;
import net.unethicalite.api.utils.MessageUtils;
import net.unethicalite.api.widgets.Dialog;
import net.unethicalite.client.Static;
import org.pf4j.Extension;
import javax.inject.Inject;
import javax.swing.*;
import java.time.Instant;
import java.util.*;

@Extension
@PluginDescriptor(
		name = "Thiever",
		description = "Tickles the elves for money",
		enabledByDefault = false
)
@Slf4j
public class ThieverPlugin extends LoopedPlugin
{
	@Inject
	private ThieverConfig config;
	@Inject
	private Client client;
	@Inject
	private ClientThread clientThread;
	private boolean startBot;
	Instant botTimer;
	int timeout;
	private int randomPouchCount;
	private Random rand = new Random();

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

		if (Dialog.isEnterInputOpen() && !Bank.isOpen()){
			Dialog.close();
		}

		handleElf(config.elf());

		return -1;

	}

	private void handleElf(Elf elf) {
		if (elf.equals(Elf.CELEBRIAN)) {
			WorldPoint spot = new WorldPoint(3269, 6125, 0);
			handleCore(spot, 6123, 6124, "Celebrian");
		}
		if (elf.equals(Elf.LINDIR)){
			WorldPoint spot = new WorldPoint(3243, 6071, 0);
			handleCore(spot, 6072, 6070, "Lindir");
		}
	}
	
	private void handleCore(WorldPoint spot, int getWorldY_1, int getWorldY_2, String npcName){

		TileObject door = TileObjects.getNearest(x -> x.getName().contains("Door") && x.distanceTo(spot) < 5);
		TileObject bankBooth = TileObjects.getNearest(x -> x.getName().contains("Bank booth") && x.distanceTo(client.getLocalPlayer().getWorldLocation()) < 10);
		WorldPoint bank = new WorldPoint(3256, 6106, 0);

		if (Inventory.contains(config.food().getId()) && dodgyNeckExists()) {
			Item food = Inventory.getFirst(x -> x.getId() == config.food().getId());
			if (food != null && Combat.getCurrentHealth() < config.healthValue()){
				food.interact("Eat");
			}

			if (door != null && door.hasAction("Open") && !client.getLocalPlayer().isMoving()
					&& ((config.elf().equals(Elf.LINDIR) && client.getLocalPlayer().getWorldLocation().getWorldY() >= getWorldY_1) ||
					(config.elf().equals(Elf.CELEBRIAN) && client.getLocalPlayer().getWorldLocation().getWorldY() <= getWorldY_1))) {
				door.interact("Open");
			}

			Item dodgyNecklace = Inventory.getFirst(ItemID.DODGY_NECKLACE);
			if (!Bank.isOpen() && !Equipment.contains(ItemID.DODGY_NECKLACE) && dodgyNecklace != null){
				dodgyNecklace.interact("Wear");
			}

			Item runes = Inventory.getFirst(ItemID.DEATH_RUNE, ItemID.NATURE_RUNE);
			Item runesPouch = Inventory.getFirst("Rune pouch");
			if (config.runesToPouch() && runes != null && Inventory.getFreeSlots() < 6) {
				runes.useOn(runesPouch);
			}

			List<String> excludedItems = List.of(config.excludedItems().split(","));
			Item itemToDrop = Inventory.getFirst(x -> (x.getName() != null && excludedItems.stream().anyMatch(a -> x.getName().equalsIgnoreCase(a)))
					&& x.hasAction("Drop"));
			if (itemToDrop != null && Inventory.getFreeSlots() < 3) {
				itemToDrop.interact("Drop");
				MessageUtils.addMessage("Dropping item to make space");
			} else if (Inventory.getFreeSlots() > 5 && !isStunned()) {
				TileItem loot = TileItems.getNearest(x ->
						x.getTile().getWorldLocation().distanceTo(client.getLocalPlayer().getWorldLocation()) < 5
								&& x.getName() != null && config.excludedItems().contains(x.getName()));
				if (loot != null) {
					if (!Reachable.isInteractable(loot.getTile())) {
						Movement.walkTo(loot.getTile().getWorldLocation());
						return;
					}

					loot.pickup();
					MessageUtils.addMessage("Picking up item");
					return;
				}
			}

			NPC elf = NPCs.getNearest(x -> x.getName().contains(npcName) && x.distanceTo(spot) < 10 && x.distanceTo(client.getLocalPlayer().getWorldLocation()) < 4);
			if (elf != null && client.getLocalPlayer().getWorldLocation().getWorldY() >= getWorldY_2) {
				if (config.useShadowVeil()) {
					GameThread.invoke(() -> {
						if (hasRunes() && Equipment.contains(ItemID.MYSTIC_LAVA_STAFF)) {
							if (shadowVeilReq()) {
								Static.getClient().interact(1, 57, -1, 14287025);
								MessageUtils.addMessage("Debug casting Shadow Veil");
							} else {
								pickpocketNPC(elf);
							}
						} else {
							MessageUtils.addMessage("Stopping: mystic lava staff and/or cosmic runes not found");
							logout();
						}
					});
				} else {
					pickpocketNPC(elf);
				}
			} else {
				Movement.walkTo(spot);
				Time.sleep(1000);
			}

			Item jugOfWine = Inventory.getFirst(ItemID.JUG_OF_WINE);
			List<Item> jug = Inventory.getAll(ItemID.JUG);
			if (jugOfWine != null && Combat.getHealthPercent() < 85) {
				jugOfWine.interact("Drink");
				Time.sleep(1500);
			} else if (jug != null){
				jug.forEach(x -> x.interact("Drop"));
			}
		} else {
			if (!isStunned()) {
				walkToBank(door, bankBooth, bank);
			}
		}
		if (!Inventory.contains(config.food().getId()) || !Inventory.contains(ItemID.DODGY_NECKLACE)){
			if (bankBooth == null) {
				return;
			}
			bankBooth.interact("Bank");
			Time.sleepUntil(Bank::isOpen, 3000);
			banking();
		}
	}
	
	private void walkToBank(TileObject door, TileObject bankBooth, WorldPoint bank){
		if (door != null && door.hasAction("Open")) {
			door.interact("Open");
		} else if (bankBooth == null){
			Movement.walkTo(bank);
			Time.sleep(1100);
		}
	}

	public void banking(){
		if (Bank.isOpen()) {
			if (!Bank.isMainTabOpen()) {
				Bank.openMainTab();
			} else {
				depositAll();
				Time.sleep(1000);
				if (Bank.contains(ItemID.DODGY_NECKLACE)) {
					if (!Inventory.contains(ItemID.DODGY_NECKLACE) || Inventory.getCount(ItemID.DODGY_NECKLACE) < config.dodgyNeck()) {
						Bank.withdraw(ItemID.DODGY_NECKLACE, config.dodgyNeck() - Inventory.getCount(ItemID.DODGY_NECKLACE), Bank.WithdrawMode.ITEM);
					}
				} else {
					MessageUtils.addMessage("Couldn't find Dodgy necklace in the bank.");
					if (config.logoutIfNull()) {
						logout();
					} else {
						startBot = false;
					}
				}
				Time.sleep(1000);
				if (Bank.contains(config.food().getId())) {
					if (!Inventory.contains(config.food().getId()) || Inventory.getCount(config.food().getId()) < config.foodAmount()) {
						//withdraw less food if current HP is above 80% to avoid initial inventory congestion
						if (Combat.getHealthPercent() > 80) {
							Bank.withdraw(config.food().getId(), (config.foodAmount() - 3) - Inventory.getCount(config.food().getId()), Bank.WithdrawMode.ITEM);
						} else {
							Bank.withdraw(config.food().getId(), config.foodAmount() - Inventory.getCount(config.food().getId()), Bank.WithdrawMode.ITEM);
						}
					}
				} else {
					MessageUtils.addMessage("Couldn't find " + config.food().getName() + " in the bank.");
					if (config.logoutIfNull()) {
						logout();
					} else {
						startBot = false;
					}
				}
				if (config.runesToPouch() && Bank.contains(ItemID.RUNE_POUCH) && !Inventory.contains(ItemID.RUNE_POUCH)){
					Bank.withdraw(ItemID.RUNE_POUCH, 1, Bank.WithdrawMode.ITEM);
				}
			}
		}
	}

	private boolean isStunned(){
		return client.getLocalPlayer().getGraphic() == 245;
	}

	private boolean dodgyNeckExists(){
		return Equipment.contains(ItemID.DODGY_NECKLACE) || Inventory.contains(ItemID.DODGY_NECKLACE);
	}

	private boolean isPickpocketing(){
		return client.getLocalPlayer().getAnimation() == 881 || client.getLocalPlayer().getAnimation() == -1;
	}

	private boolean shadowVeilReq(){
		return !(client.getLocalPlayer().getInteracting() instanceof NPC)
				&& client.getVarbitValue(12414) == 0
				&& client.getVarbitValue(4070) == 3
				&& client.getVarbitValue(12291) == 0
				&& Inventory.contains(config.food().getId())
				&& dodgyNeckExists()
				&& !isStunned();
	}

	private void pickpocketNPC(NPC npc){
		if (Reachable.isInteractable(npc) && !isStunned()) {
			npc.interact("Pickpocket");
			Time.sleep(100);
		}
	}

	private void depositAll(){
		List<Integer> itemsToDeposit = List.of(ItemID.ENHANCED_CRYSTAL_TELEPORT_SEED, ItemID.CRYSTAL_SHARD, ItemID.COINS, ItemID.COINS_995,
				ItemID.COINS_6964, ItemID.COINS_8890, ItemID.DEATH_RUNE, ItemID.NATURE_RUNE, ItemID.GOLD_ORE, ItemID.FIRE_ORB, ItemID.DIAMOND);

		itemsToDeposit.stream().filter(Inventory::contains).forEach(Bank::depositAll);
	}

	private void logout(){
		if (!isStunned()) {
			startBot = false;
			GameThread.invokeLater(() -> {
				Game.logout();
				return -1;
			});
		}
	}

	private int randomInt() {
		int randomPouchQuantity;
		switch (config.diaryLevel()) {
			case NONE:
				randomPouchQuantity = rand.nextInt(27 - 12 + 1) + 12;
				break;
			case MEDIUM:
				randomPouchQuantity = rand.nextInt(55 - 28 + 1) + 28;
				break;
			case HARD:
				randomPouchQuantity = rand.nextInt(83 - 56 + 1) + 56;
				break;
			default:
				randomPouchQuantity = rand.nextInt(139 - 100 + 1) + 100;
				break;
		}

		return randomPouchQuantity;
	}
	private boolean hasRunes(){
		Item pouch = Inventory.getFirst("Rune pouch");
		return (pouch != null && client.getVarbitValue(Varbits.RUNE_POUCH_AMOUNT3) > 5) || Inventory.contains(ItemID.COSMIC_RUNE);
	}

	private void openCoinPouch(){
		Item pouch = Inventory.getFirst(x -> x.getName().equalsIgnoreCase("Coin pouch") && x.getQuantity() >= randomPouchCount);
		if (pouch != null && !Bank.isOpen()){
			pouch.interact("Open-all");
			MessageUtils.addMessage("Opened pouch at quantity " + pouch.getQuantity());
			randomPouchCount = randomInt();
			//MessageUtils.addMessage("Opening pouch at next quantity " + randomPouchCount);
		}
	}

	@Subscribe
	private void onGameTick(GameTick event){
		if (!startBot || Game.getState() != GameState.LOGGED_IN){
			return;
		}

		openCoinPouch();
	}

	@Subscribe
	private void onConfigButtonPressed(ConfigButtonClicked configButtonClicked) {
		if (!configButtonClicked.getGroup().equalsIgnoreCase("Thiever")) {
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
					randomPouchCount = randomInt();
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
	ThieverConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ThieverConfig.class);
	}
}