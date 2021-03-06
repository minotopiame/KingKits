package com.faris.kingkits.gui;

import com.faris.kingkits.KingKits;
import com.faris.kingkits.KingKitsAPI;
import com.faris.kingkits.Kit;
import com.faris.kingkits.Permissions;
import com.faris.kingkits.helper.UUIDFetcher;
import com.faris.kingkits.helper.Utilities;
import com.faris.kingkits.helper.container.KitStack;
import com.faris.kingkits.listener.command.SetKit;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.inventory.InventoryType.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.*;

import java.util.ArrayList;
import java.util.List;

public class GuiKitMenu extends GuiKingKits {

	private KitStack[] guiKitStacks = null;
	private int page = 1, maxPage = -1;

	/**
	 * Create a new gui menu instance.
	 *
	 * @param player - The player that is using the menu
	 * @param title - The title of the menu
	 * @param kitStacks - The kits in the menu
	 */
	public GuiKitMenu(Player player, String title, KitStack[] kitStacks) {
		this(player, title, kitStacks, 1);
	}

	/**
	 * Create a new gui menu instance.
	 *
	 * @param player - The player that is using the menu
	 * @param title - The title of the menu
	 * @param kitStacks - The kits in the menu
	 */
	public GuiKitMenu(Player player, String title, KitStack[] kitStacks, int page) {
		super(player, player.getServer().createInventory(null, KingKits.getInstance() != null ? KingKits.getInstance().configValues.guiSize : 54, Utilities.trimString(title, 32)));
		this.guiKitStacks = kitStacks;
		this.maxPage = this.guiKitStacks.length > 0 ? (int) ((double) (this.guiKitStacks.length - 1) / (this.guiInventory.getSize() - 9) + 1) : 1;
		this.page = page > this.maxPage ? this.maxPage : page;
	}

	@Override
	public boolean openMenu() {
		try {
			if (guiKitMenuMap.containsKey(this.getPlayerName())) {
				GuiKitMenu guiKitMenu = guiKitMenuMap.get(this.getPlayerName());
				if (guiKitMenu != null) guiKitMenu.closeMenu(true, true);
				guiKitMenuMap.remove(this.getPlayerName());
			}
			if (super.openMenu()) {
				guiKitMenuMap.put(this.getPlayerName(), this);
				return true;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	@Override
	public void closeMenu(boolean unregisterEvents, boolean closeInventory) {
		super.closeMenu(unregisterEvents, closeInventory);
		guiKitMenuMap.remove(this.getPlayerName());
	}

	@Override
	protected void fillInventory() {
		this.guiInventory.clear();

		int startPosition = 0;
		int endPosition = this.guiKitStacks.length - 1;
		if (this.getPlugin().configValues.sortAlphabetically) {
			if (this.guiInventory.getSize() > 9) {
				startPosition = (this.page - 1) * (this.guiInventory.getSize() - 9);
				endPosition = startPosition + this.guiInventory.getSize() - 9;
			}
			for (int i = startPosition; i < this.guiKitStacks.length; i++) {
				try {
					if (i > endPosition) break;
					ItemStack currentStack = this.guiKitStacks[i].getItemStack();
					if (currentStack != null && currentStack.getType() != Material.AIR) {
						currentStack = currentStack.clone();
						if (currentStack.getItemMeta() != null) {
							ItemMeta itemMeta = currentStack.getItemMeta();
							Kit targetKit = KingKitsAPI.getKitByName(this.guiKitStacks[i].getKitName(), this.getPlayer() != null ? this.getPlayer().getUniqueId() : null);
							if (targetKit == null) continue;

							ChatColor kitColour = this.getPlayer().hasPermission("kingkits.kits." + targetKit.getRealName().toLowerCase()) ? ChatColor.GREEN : ChatColor.DARK_RED;
							itemMeta.setDisplayName(ChatColor.RESET.toString() + kitColour + this.guiKitStacks[i].getKitName());

							if (targetKit.hasDescription()) {
								List<String> kitDescription = new ArrayList<>();
								for (String descriptionLine : targetKit.getDescription()) {
									descriptionLine = Utilities.replaceChatColour(descriptionLine);
									descriptionLine = descriptionLine.replace("<player>", this.getPlayerName());
									descriptionLine = descriptionLine.replace("<name>", targetKit.getName());
									descriptionLine = descriptionLine.replace("<cost>", String.valueOf(targetKit.getCost()));
									descriptionLine = descriptionLine.replace("<cooldown>", String.valueOf(targetKit.getCooldown()));
									descriptionLine = descriptionLine.replace("<maxhealth>", String.valueOf(targetKit.getMaxHealth()));
									kitDescription.add(descriptionLine);
								}
								itemMeta.setLore(kitDescription);
							}
							currentStack.setItemMeta(itemMeta);
							if (!this.guiInventory.addItem(currentStack).isEmpty()) break;
						}
					}
				} catch (Exception ex) {
					break;
				}
			}
			if (this.guiInventory.getSize() > 9) {
				try {
					Material buttonType = Material.getMaterial(KingKits.getInstance().configValues.guiItemID);
					if (buttonType == Material.AIR) buttonType = Material.STONE_BUTTON;
					this.guiInventory.setItem(this.guiInventory.getSize() - 9, Utilities.ItemUtils.setName(new ItemStack(buttonType, 1, KingKits.getInstance().configValues.guiItemData), this.page == 1 ? "&8Previous Page" : "&fPrevious Page"));
					this.guiInventory.setItem(this.guiInventory.getSize() - 1, Utilities.ItemUtils.setName(new ItemStack(buttonType, 1, KingKits.getInstance().configValues.guiItemData), this.page >= this.maxPage ? "&8Next Page" : "&fNext Page"));
				} catch (Exception ignored) {
				}
			}
		} else {
			List<ItemStack> addItems = new ArrayList<>();
			for (int i = startPosition; i < this.guiKitStacks.length; i++) {
				try {
					ItemStack currentStack = this.guiKitStacks[i].getItemStack();
					if (currentStack != null && currentStack.getType() != Material.AIR) {
						currentStack = currentStack.clone();
						Kit targetKit = KingKitsAPI.getKitByName(this.guiKitStacks[i].getKitName(), (this.getPlayer() != null ? this.getPlayer().getUniqueId() : UUIDFetcher.lookupName(this.getPlayerName()).getId()));
						if (currentStack.getItemMeta() != null) {
							ItemMeta itemMeta = currentStack.getItemMeta();
							ChatColor kitColour = this.getPlayer().hasPermission("kingkits.kits." + (targetKit != null ? targetKit.getRealName().toLowerCase() : Utilities.stripColour(this.guiKitStacks[i].getKitName().toLowerCase()))) ? ChatColor.GREEN : ChatColor.DARK_RED;
							itemMeta.setDisplayName(ChatColor.RESET + "" + kitColour + this.guiKitStacks[i].getKitName());

							if (targetKit != null && targetKit.hasDescription()) {
								List<String> kitDescription = new ArrayList<>();
								for (String descriptionLine : targetKit.getDescription()) {
									descriptionLine = Utilities.replaceChatColour(descriptionLine);
									descriptionLine = descriptionLine.replace("<player>", this.getPlayerName());
									descriptionLine = descriptionLine.replace("<name>", targetKit.getName());
									descriptionLine = descriptionLine.replace("<cost>", String.valueOf(targetKit.getCost()));
									descriptionLine = descriptionLine.replace("<cooldown>", String.valueOf(targetKit.getCooldown()));
									descriptionLine = descriptionLine.replace("<maxhealth>", String.valueOf(targetKit.getMaxHealth()));
									kitDescription.add(descriptionLine);
								}
								itemMeta.setLore(kitDescription);
							}
							currentStack.setItemMeta(itemMeta);
						}
						if (targetKit != null && targetKit.getGuiPosition() > 0 && targetKit.getGuiPosition() < this.guiInventory.getSize()) {
							try {
								this.guiInventory.setItem(targetKit.getGuiPosition() - 1, currentStack);
							} catch (Exception ex) {
								ex.printStackTrace();
								addItems.add(currentStack);
							}
						} else {
							addItems.add(currentStack);
						}
					}
				} catch (Exception ignored) {
				}
			}
			for (ItemStack itemStack : addItems) this.guiInventory.addItem(itemStack);
		}
	}

	/**
	 * Returns the kit item stacks *
	 */
	public KitStack[] getKitStacks() {
		return this.guiKitStacks;
	}

	/**
	 * Sets the kit item stacks *
	 */
	public GuiKitMenu setKitStacks(KitStack[] kitStacks) {
		this.guiKitStacks = kitStacks;
		return this;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	protected void onPlayerClickInventory(InventoryClickEvent event) {
		try {
			if (this.guiInventory != null && event.getInventory() != null && event.getWhoClicked() != null) {
				if (event.getWhoClicked() instanceof Player) {
					if (event.getSlot() >= 0) {
						if (event.getSlotType() == SlotType.CONTAINER) {
							if (event.getWhoClicked().getName().equals(this.getPlayerName())) {
								ItemStack clickedItem = event.getCurrentItem();
								event.setCancelled(true);
								if (clickedItem != null && clickedItem.getType() != Material.AIR) {
									String itemName = ChatColor.stripColor(Utilities.ItemUtils.getName(clickedItem));
									if (event.getRawSlot() == this.guiInventory.getSize() - 9 && itemName.equals("Previous Page") && clickedItem.getType().getId() == KingKits.getInstance().configValues.guiItemID && clickedItem.getDurability() == KingKits.getInstance().configValues.guiItemData) {
										if (this.page > 1) {
											this.page--;
											this.fillInventory();
										}
									} else if (event.getRawSlot() == this.guiInventory.getSize() - 1 && itemName.equals("Next Page") && clickedItem.getType().getId() == KingKits.getInstance().configValues.guiItemID && clickedItem.getDurability() == KingKits.getInstance().configValues.guiItemData) {
										if (this.page < this.maxPage) {
											this.page++;
											this.fillInventory();
										}
									} else {
										this.closeMenu(true, true);
										if (clickedItem.getItemMeta() != null) {
											final String kitName = Utilities.stripColour(clickedItem.getItemMeta().getDisplayName());
											if (kitName != null) {
												final Kit kit = KingKitsAPI.getKitByName(kitName, event.getWhoClicked().getUniqueId());
												if (kit != null) {
													if (KingKitsAPI.isUserKit(kit.getRealName(), event.getWhoClicked().getUniqueId()) || event.getWhoClicked().hasPermission("kingkits.kits." + kit.getRealName().toLowerCase())) {
														final Player player = (Player) event.getWhoClicked();
														boolean validCooldown = true;
														if (kit != null && kit.hasCooldown() && !player.hasPermission(Permissions.KIT_COOLDOWN_BYPASS)) {
															if (this.getPlugin().getCooldownConfig().contains(player.getUniqueId().toString() + "." + kit.getRealName())) {
																long currentCooldown = this.getPlugin().getCooldown(player.getUniqueId(), kit.getRealName());
																if (System.currentTimeMillis() - currentCooldown >= kit.getCooldown() * 1000) {
																	this.getPlugin().getCooldownConfig().set(player.getName() + "." + kit.getRealName(), null);
																	this.getPlugin().saveCooldownConfig();
																} else {
																	Utilities.sendDelayMessage(player, kit, currentCooldown);
																	validCooldown = false;
																}
															}
														}
														if (validCooldown) {
															SetKit.setKitWithDelay(player, kit != null ? kit.getRealName() : kitName, true);
														}
													} else if (this.getPlugin().configValues.showKitPreview) {
														if (!guiPreviewKitMap.containsKey(event.getWhoClicked().getName())) {
															final Player player = (Player) event.getWhoClicked();
															player.getServer().getScheduler().runTaskLater(this.getPlugin(), new Runnable() {
																public void run() {
																	if (player.isOnline()) {
																		if (!guiPreviewKitMap.containsKey(player.getName())) {
																			new GuiPreviewKit(player, kitName, page).openMenu();
																		}
																	}
																}
															}, 3L);
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		} catch (Exception ex) {
			if (event.getInventory() != null && this.guiInventory != null) {
				if (this.getPlayerName().equals(event.getWhoClicked().getName())) {
					event.setCurrentItem(null);
					event.setCancelled(true);
					this.closeMenu(true, true);
				}
			}
			ex.printStackTrace();
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	protected void onPlayerCloseInventory(InventoryCloseEvent event) {
		try {
			if (this.guiInventory != null && event.getInventory() != null) {
				if (event.getPlayer() instanceof Player) {
					if (this.getPlayerName().equals(event.getPlayer().getName())) {
						this.closeMenu(true, false);
					}
				}
			}
		} catch (Exception ex) {
		}
	}

}
