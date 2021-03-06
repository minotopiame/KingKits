package com.faris.kingkits.listener.command;

import com.faris.kingkits.*;
import com.faris.kingkits.gui.GuiKingKits;
import com.faris.kingkits.gui.GuiPreviewKit;
import com.faris.kingkits.helper.Lang;
import com.faris.kingkits.helper.Utilities;
import com.faris.kingkits.listener.KingCommand;
import mkremins.fanciful.FancyMessage;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.entity.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KitCommand extends KingCommand {

	public KitCommand(KingKits instance) {
		super(instance);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected boolean onCommand(CommandSender sender, String command, String[] args) {
		if (command.equalsIgnoreCase("pvpkit")) {
			if (sender.hasPermission(Permissions.KIT_USE)) {
				if (this.getPlugin().cmdValues.pvpKits) {
					if (this.isConsole(sender) || Utilities.inPvPWorld(((Player) sender))) {
						if (args.length == 0) {
							if (sender.hasPermission(Permissions.KIT_LIST)) {
								if (this.isConsole(sender) || (!this.getPlugin().configValues.kitListMode.equalsIgnoreCase("Gui") && !this.getPlugin().configValues.kitListMode.equalsIgnoreCase("Menu"))) {
									List<String> kitList = new ArrayList<>(this.getPlugin().kitList.keySet());
									Lang.sendMessage(sender, Lang.GEN_KIT_LIST_TITLE, String.valueOf(kitList.size()));
									if (!kitList.isEmpty()) {
										if (this.getPlugin().configValues.sortAlphabetically)
											Collections.sort(kitList, Utilities.ALPHANUMERICAL_ORDER);
										for (int kitPos = 0; kitPos < kitList.size(); kitPos++) {
											String kitName = kitList.get(kitPos).split(" ")[0];
											if (sender.hasPermission("kingkits.kits." + kitName.toLowerCase())) {
												if (!this.isConsole(sender) && sender.hasPermission(Permissions.KIT_LIST_TOOLTIP)) {
													FancyMessage listMessage = new FancyMessage((kitPos + 1) + ". ").color(ChatColor.GOLD).then(kitName).color(ChatColor.RED);
													Kit targetKit = KingKitsAPI.getKitByName(kitName, true);
													if (targetKit != null && targetKit.hasDescription()) {
														final List<String> kitDescription = new ArrayList<>();
														for (String descriptionLine : targetKit.getDescription()) {
															descriptionLine = Utilities.replaceChatColour(descriptionLine);
															descriptionLine = descriptionLine.replace("<player>", sender.getName());
															descriptionLine = descriptionLine.replace("<name>", targetKit.getName());
															descriptionLine = descriptionLine.replace("<cost>", String.valueOf(targetKit.getCost()));
															descriptionLine = descriptionLine.replace("<cooldown>", String.valueOf(targetKit.getCooldown()));
															descriptionLine = descriptionLine.replace("<maxhealth>", String.valueOf(targetKit.getMaxHealth()));
															kitDescription.add(descriptionLine);
														}
														if (!kitDescription.isEmpty())
															listMessage.tooltip(kitDescription);
													}
													listMessage.command("/" + command.toLowerCase() + " " + kitName).send(sender);
												} else {
													sender.sendMessage(this.rCC("&6" + (kitPos + 1) + ". &c" + kitName));
												}
											} else {
												if (this.getPlugin().configValues.kitListPermissions) {
													if (!this.isConsole(sender) && sender.hasPermission(Permissions.KIT_LIST_TOOLTIP)) {
														FancyMessage listMessage = new FancyMessage((kitPos + 1) + ". ").color(ChatColor.GOLD).then(kitName).color(ChatColor.DARK_RED);
														Kit targetKit = KingKitsAPI.getKitByName(kitName, true);
														if (targetKit != null && targetKit.hasDescription()) {
															final List<String> kitDescription = new ArrayList<>();
															for (String descriptionLine : targetKit.getDescription()) {
																descriptionLine = Utilities.replaceChatColour(descriptionLine);
																descriptionLine = descriptionLine.replace("<player>", sender.getName());
																descriptionLine = descriptionLine.replace("<name>", targetKit.getName());
																descriptionLine = descriptionLine.replace("<cost>", String.valueOf(targetKit.getCost()));
																descriptionLine = descriptionLine.replace("<cooldown>", String.valueOf(targetKit.getCooldown()));
																descriptionLine = descriptionLine.replace("<maxhealth>", String.valueOf(targetKit.getMaxHealth()));
																kitDescription.add(descriptionLine);
															}
															if (!kitDescription.isEmpty())
																listMessage.tooltip(kitDescription);
														}
														listMessage.command("/" + command.toLowerCase() + " " + kitName).send(sender);
													} else {
														sender.sendMessage(this.rCC("&4" + (kitPos + 1) + ". &4" + kitName));
													}
												}
											}
										}
									} else {
										Lang.sendMessage(sender, Lang.GEN_NO_KITS);
									}
								} else {
									KingKitsAPI.showKitMenu((Player) sender);
								}
							} else {
								Lang.sendMessage(sender, Lang.COMMAND_KIT_LIST_NO_PERMISSION);
							}
						} else if (args.length == 1) {
							if (!this.isConsole(sender)) {
								Player player = (Player) sender;
								String kitName = args[0];
								List<String> kitList = this.getPlugin().getKitList();
								List<String> kitListLC = Utilities.toLowerCaseList(kitList);
								if (kitListLC.contains(kitName.toLowerCase()))
									kitName = kitList.get(kitListLC.indexOf(kitName.toLowerCase()));
								try {
									final Kit kit = KingKitsAPI.getKitByName(kitName, false);
									if (kit != null && kit.hasCooldown() && !player.hasPermission(Permissions.KIT_COOLDOWN_BYPASS)) {
										if (this.getPlugin().getCooldownConfig().contains(player.getUniqueId().toString() + "." + kit.getRealName())) {
											long currentCooldown = this.getPlugin().getCooldown(player.getUniqueId(), kit.getRealName());
											if (System.currentTimeMillis() - currentCooldown >= kit.getCooldown() * 1000) {
												this.getPlugin().getCooldownConfig().set(player.getUniqueId().toString() + "." + kit.getRealName(), null);
												this.getPlugin().saveCooldownConfig();
											} else {
												Utilities.sendDelayMessage(player, kit, currentCooldown);
												return true;
											}
										}
									}
									if (this.getPlugin().configValues.showKitPreview && !player.hasPermission("kingkits.kits." + kitName.toLowerCase())) {
										if (!GuiKingKits.guiKitMenuMap.containsKey(player.getName()) && !GuiKingKits.guiPreviewKitMap.containsKey(player.getName())) {
											if (this.getPlugin().getKitsConfig().contains(kitName)) {
												GuiPreviewKit guiPreviewKit = new GuiPreviewKit(player, kitName);
												guiPreviewKit.openMenu();
											} else {
												SetKit.setKitWithDelay(player, kitName, true);
											}
										} else {
											SetKit.setKitWithDelay(player, kitName, true);
										}
									} else {
										SetKit.setKitWithDelay(player, kitName, true);
									}
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							} else {
								Lang.sendMessage(sender, Lang.COMMAND_GEN_IN_GAME);
							}
						} else if (args.length == 2) {
							if (sender.hasPermission(Permissions.KIT_USE_OTHER)) {
								String strTarget = args[1];
								Player target = sender.getServer().getPlayer(strTarget);
								if (target != null && target.isOnline()) {
									String kitName = args[0];
									List<String> kitList = this.getPlugin().getKitList();
									List<String> kitListLC = Utilities.toLowerCaseList(kitList);
									if (kitListLC.contains(kitName.toLowerCase()))
										kitName = kitList.get(kitListLC.indexOf(kitName.toLowerCase()));
									try {
										SetKit.setKit(target, kitName, false);
									} catch (Exception ex) {
										ex.printStackTrace();
										Lang.sendMessage(sender, Lang.COMMAND_GEN_ERROR);
										return true;
									}
									Lang.sendMessage(sender, Lang.COMMAND_KIT_OTHER_PLAYER, target.getName());
								} else {
									Lang.sendMessage(sender, Lang.COMMAND_GEN_NOT_ONLINE, strTarget);
								}
							} else {
								this.sendNoAccess(sender);
							}
						} else {
							Lang.sendMessage(sender, Lang.COMMAND_GEN_USAGE, command.toLowerCase() + " [<kit>|<kit> <player>]");
						}
					} else {
						Lang.sendMessage(sender, Lang.COMMAND_GEN_WORLD);
					}
				} else {
					Lang.sendMessage(sender, Lang.COMMAND_GEN_DISABLED);
				}
			} else {
				this.sendNoAccess(sender);
			}
			return true;
		}
		return false;
	}
}
