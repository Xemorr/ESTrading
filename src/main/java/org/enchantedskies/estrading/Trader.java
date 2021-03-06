package org.enchantedskies.estrading;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class Trader {
    private final Inventory inventory = Bukkit.createInventory(null, 54, "Trading Panel");
    private final HashMap<Integer, ItemStack> offeredItems = new HashMap<>();
    private final UUID playerUUID;
    private String status;

    private final ItemStack unacceptedItem;
    private final ItemStack verifyItem;
    private final ItemStack acceptedItem;
    private final ItemStack unacceptedGlassItem;
    private final ItemStack verifyGlassItem;
    private final ItemStack acceptedGlassItem;

    public Trader(Player trader, Player trader2) {
        this.playerUUID = trader.getUniqueId();
        this.status = "unaccepted";

        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta borderMeta = border.getItemMeta();
        borderMeta.setDisplayName("§7");
        border.setItemMeta(borderMeta);

        ItemStack cancelItem = new ItemStack(Material.RED_WOOL);
        ItemMeta cancelMeta = cancelItem.getItemMeta();
        cancelMeta.setDisplayName("§c§lCANCEL");
        cancelItem.setItemMeta(cancelMeta);

        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) playerHead.getItemMeta();
        skullMeta.setOwningPlayer(trader2);
        skullMeta.setDisplayName("§e" + trader2.getName());
        playerHead.setItemMeta(skullMeta);

        List<String> confirmLore = new ArrayList<>();
        confirmLore.add("§eClick to confirm");

        unacceptedItem = new ItemStack(Material.ORANGE_WOOL);
        ItemMeta unacceptedMeta = unacceptedItem.getItemMeta();
        unacceptedMeta.setDisplayName("§a§lACCEPT");
        unacceptedMeta.setLore(confirmLore);
        unacceptedItem.setItemMeta(unacceptedMeta);

        verifyItem = new ItemStack(Material.YELLOW_WOOL);
        ItemMeta verifyMeta = verifyItem.getItemMeta();
        verifyMeta.setDisplayName("§6§lAre you sure?");
        verifyMeta.setLore(confirmLore);
        verifyItem.setItemMeta(verifyMeta);

        acceptedItem = new ItemStack(Material.LIME_WOOL);
        ItemMeta acceptedMeta = acceptedItem.getItemMeta();
        acceptedMeta.setDisplayName("§a§lACCEPTED");
        acceptedItem.setItemMeta(acceptedMeta);

        unacceptedGlassItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta unacceptedGlassMeta = unacceptedGlassItem.getItemMeta();
        unacceptedGlassMeta.setDisplayName("§7");
        unacceptedGlassItem.setItemMeta(unacceptedGlassMeta);

        verifyGlassItem = new ItemStack(Material.ORANGE_STAINED_GLASS_PANE);
        ItemMeta verifyGlassMeta = verifyGlassItem.getItemMeta();
        verifyGlassMeta.setDisplayName("§7");
        verifyGlassItem.setItemMeta(verifyGlassMeta);

        acceptedGlassItem = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta acceptedGlassMeta = acceptedGlassItem.getItemMeta();
        acceptedGlassMeta.setDisplayName("§7");
        acceptedGlassItem.setItemMeta(acceptedGlassMeta);

        inventory.setItem(4, border);
        inventory.setItem(13, unacceptedGlassItem);
        inventory.setItem(22, unacceptedGlassItem);
        inventory.setItem(31, unacceptedGlassItem);
        inventory.setItem(40, border);
        for (int i = 45; i < 54; i++) {
            inventory.setItem(i, border);
        }
        inventory.setItem(48, cancelItem);
        inventory.setItem(49, playerHead);
        inventory.setItem(50, unacceptedItem);

        trader.openInventory(inventory);
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getStatus() {
        return status;
    }

    public Collection<ItemStack> getOfferedItems() {
        return offeredItems.values();
    }

    public void addItem(ItemStack item, int itemLoc) {
        if ((itemLoc % 9) <= 3 && itemLoc <= 39) {
            offeredItems.put(itemLoc, item);
            ESTrading.logger.logItemAdd(Bukkit.getPlayer(playerUUID), item);
        }
    }

    public void addDisplayItem(ItemStack item, int itemLoc) {
        itemLoc += 5;
        if ((itemLoc % 9) >= 5 && itemLoc <= 44) {
            inventory.setItem(itemLoc, item);
            resetStatus();
        }
    }

    public void removeItem(ItemStack item, int itemLoc) {
        if ((itemLoc % 9) <= 3 && itemLoc <= 39) {
            offeredItems.remove(itemLoc);
            ESTrading.logger.logItemRemove(Bukkit.getPlayer(playerUUID), item);
        }
    }

    public void removeDisplayItem(int itemLoc) {
        itemLoc += 5;
        if ((itemLoc % 9) >= 5 && itemLoc <= 44) {
            inventory.setItem(itemLoc, new ItemStack(Material.AIR));
            resetStatus();
        }
    }

    public void setTraderStatus(String thisStatus) {
        switch (thisStatus) {
            case "unaccepted" -> setStatusGlass(unacceptedGlassItem);
            case "checking" -> setStatusGlass(verifyGlassItem);
            case "accepted" -> setStatusGlass(acceptedGlassItem);
        }
    }

    public void setStatusGlass(ItemStack item) {
        for (int i = 13; i < 32; i += 9) {
            inventory.setItem(i, item);
        }
    }

    public void accept() {
        if (status.equals("unaccepted")) {
            status = "checking";
            inventory.setItem(50, verifyItem);
        } else if (status.equals("checking")) {
            status = "accepted";
            inventory.setItem(50, acceptedItem);
        }
        ESTrading.tradeManager.getTradeSession(playerUUID).updateStatus();
    }

    public void resetStatus() {
        status = "unaccepted";
        setStatusGlass(unacceptedGlassItem);
        inventory.setItem(50, unacceptedItem);
        ESTrading.tradeManager.getTradeSession(playerUUID).updateStatus();
    }

    public void cancelTrade() {
        Player player = Bukkit.getPlayer(playerUUID);
        player.closeInventory();
        player.sendMessage("§cThe trade has been cancelled.");
    }

    public void completeTrade() {
        Player player = Bukkit.getPlayer(playerUUID);
        player.closeInventory();
        player.sendMessage("§aThe trade has been completed.");
    }
}
