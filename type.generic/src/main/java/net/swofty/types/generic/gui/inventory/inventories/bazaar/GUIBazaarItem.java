package net.swofty.types.generic.gui.inventory.inventories.bazaar;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.ServiceType;
import net.swofty.commons.bazaar.BazaarItem;
import net.swofty.commons.protocol.objects.bazaar.BazaarGetItemProtocolObject;
import net.swofty.commons.protocol.objects.bazaar.BazaarSellProtocolObject;
import net.swofty.proxyapi.ProxyService;
import net.swofty.types.generic.bazaar.BazaarCategories;
import net.swofty.types.generic.gui.inventory.ItemStackCreator;
import net.swofty.types.generic.gui.inventory.RefreshingGUI;
import net.swofty.types.generic.gui.inventory.SkyBlockInventoryGUI;
import net.swofty.types.generic.gui.inventory.inventories.bazaar.selections.GUIBazaarPriceSelection;
import net.swofty.types.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.types.generic.gui.inventory.item.GUIItem;
import net.swofty.types.generic.item.SkyBlockItem;
import net.swofty.types.generic.item.updater.NonPlayerItemUpdater;
import net.swofty.types.generic.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GUIBazaarItem extends SkyBlockInventoryGUI implements RefreshingGUI {
    private final ItemTypeLinker itemTypeLinker;
    public GUIBazaarItem(ItemTypeLinker itemTypeLinker) {
        super(BazaarCategories.getFromItem(itemTypeLinker).getKey() + " -> " + itemTypeLinker.getDisplayName(null),
                InventoryType.CHEST_4_ROW);

        this.itemTypeLinker = itemTypeLinker;

        fill(ItemStackCreator.createNamedItemStack(Material.BLACK_STAINED_GLASS_PANE));
        set(GUIClickableItem.getGoBackItem(30, new GUIBazaarItemSet(
                BazaarCategories.getFromItem(itemTypeLinker).getKey(),
                BazaarCategories.getFromItem(itemTypeLinker).getValue())));
        set(new GUIClickableItem(31) {
            @Override
            public void run(InventoryPreClickEvent e, SkyBlockPlayer player) {
                new GUIBazaar(BazaarCategories.getFromItem(itemTypeLinker).getKey()).open(player);
            }

            @Override
            public ItemStack.Builder getItem(SkyBlockPlayer player) {
                return ItemStackCreator.getStackHead("§6Go Back",
                        "c232e3820897429157619b0ee099fec0628f602fff12b695de54aef11d923ad7",
                        1, "§7To Bazaar");
            }
        });
        set(new GUIClickableItem(32) {
            @Override
            public void run(InventoryPreClickEvent e, SkyBlockPlayer player) {
                new GUIBazaarOrders().open(player);
            }

            @Override
            public ItemStack.Builder getItem(SkyBlockPlayer player) {
                return ItemStackCreator.getStack("§aManage Orders", Material.BOOK, 1,
                        "§7You don't have any ongoing orders.",
                        " ",
                        "§eClick to manage!");
            }
        });
        set(new GUIItem(13) {
            @Override
            public ItemStack.Builder getItem(SkyBlockPlayer player) {
                return new NonPlayerItemUpdater(new SkyBlockItem(itemTypeLinker)).getUpdatedItem();
            }
        });
    }


    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        Thread.startVirtualThread(() -> {
            BazaarGetItemProtocolObject.BazaarGetItemResponse response = (BazaarGetItemProtocolObject.BazaarGetItemResponse) new ProxyService(ServiceType.BAZAAR).handleRequest(
                    new BazaarGetItemProtocolObject.BazaarGetItemMessage(itemTypeLinker.name())
            ).join();

            BazaarItem item = response.item();
            updateItems(item);
        });
    }

    public void updateItems(BazaarItem item) {
        set(new GUIClickableItem(10) {
            @Override
            public void run(InventoryPreClickEvent e, SkyBlockPlayer player) {

            }

            @Override
            public ItemStack.Builder getItem(SkyBlockPlayer player) {
                List<String> lore = new ArrayList<>();
                lore.add("§8" + itemTypeLinker.getDisplayName(null));
                lore.add(" ");

                if (!item.getSellOrders().isEmpty()) {
                    double pricePerUnit = item.getSellStatistics().getLowestOrder();
                    lore.add("§7Price per unit: §6" + pricePerUnit + " coins");
                    lore.add("§7Stack price: §6" + (pricePerUnit * 64) + " coins");
                    lore.add(" ");
                    lore.add("§eClick to pick amount!");
                } else {
                    lore.add("§cNo sell orders available.");
                    lore.add(" ");
                    lore.add("§8Cannot buy instantly");
                }

                return ItemStackCreator.getStack("§aBuy Instantly", Material.GOLDEN_HORSE_ARMOR,
                        1, lore);
            }
        });
        set(new GUIClickableItem(11) {
            @Override
            public void run(InventoryPreClickEvent e, SkyBlockPlayer player) {

            }

            @Override
            public ItemStack.Builder getItem(SkyBlockPlayer player) {
                List<String> lore = new ArrayList<>();
                lore.add("§8" + itemTypeLinker.getDisplayName(null));
                lore.add(" ");

                int amountInInventory = player.getAllOfTypeInInventory(itemTypeLinker).size();

                if (amountInInventory == 0) {
                    lore.add("§7Inventory: §cNone!");
                    lore.add(" ");
                    lore.add("§7Price Per Unit: §6" + item.getBuyStatistics().getHighestOrder() + " coins");
                    lore.add(" ");
                    lore.add("§8None to sell in your inventory!");
                } else {
                    lore.add("§7Inventory: §a" + amountInInventory + " items");
                    lore.add(" ");

                    if (!item.getBuyOrders().isEmpty()) {
                        double pricePerUnit = item.getBuyStatistics().getHighestOrder();
                        lore.add("§7Amount: §a" + amountInInventory + "§7x");
                        lore.add("§7Total price: §6" + (pricePerUnit * amountInInventory) + " coins");
                        lore.add(" ");
                        lore.add("§eClick to sell!");
                    } else {
                        lore.add("§cNo buy orders available.");
                        lore.add(" ");
                        lore.add("§8Cannot sell instantly");
                    }
                }

                return ItemStackCreator.getStack("§6Sell Instantly", Material.HOPPER,
                        1, lore);
            }
        });

        set(new GUIClickableItem(16) {
            @Override
            public void run(InventoryPreClickEvent e, SkyBlockPlayer player) {
                Map<Integer, Integer> itemInInventory = player.getAllOfTypeInInventory(itemTypeLinker);
                if (itemInInventory.isEmpty()) {
                    player.sendMessage("§cYou don't have any §e" + itemTypeLinker.getDisplayName(null) + "§c in your inventory!");
                    return;
                }

                new GUIBazaarPriceSelection(GUIBazaarItem.this,
                        itemInInventory.size(),
                        item.getSellStatistics().getLowestOrder(),
                        item.getSellStatistics().getHighestOrder(),
                        itemTypeLinker, true).openPriceSelection(player).thenAccept(price -> {
                            if (price <= 0) {
                                if (player.isOnline())
                                    player.sendMessage("§cYou can only sell for a positive amount of coins!");
                                return;
                            }

                            player.sendMessage("§6[Bazaar] §7Putting goods in escrow...");

                            int amountInInventory = player.getAmountInInventory(itemTypeLinker);
                            Map<Integer, Integer> inInventory = player.getAllOfTypeInInventory(itemTypeLinker);
                            inInventory.forEach((slot, amount) -> {
                                player.getInventory().setItemStack(slot, ItemStack.AIR);
                            });

                            ProxyService bazaar = new ProxyService(ServiceType.BAZAAR);
                            Map<String, Object> requestParam = new HashMap<>();
                            requestParam.put("item-name", itemTypeLinker.name());
                            requestParam.put("amount", amountInInventory);
                            requestParam.put("price", price);
                            requestParam.put("player-uuid", player.getUuid());

                            player.sendMessage("§6[Bazaar] §7Submitting sell order...");

                            BazaarSellProtocolObject.BazaarSellMessage message =
                                    new BazaarSellProtocolObject.BazaarSellMessage(itemTypeLinker.name(), player.getUuid(), price, amountInInventory);
                            CompletableFuture<BazaarSellProtocolObject.BazaarSellResponse> future = bazaar.handleRequest(message);
                            future.thenAccept(response -> {
                                if (response.successful) {
                                    player.sendMessage("§6[Bazaar] §eSell Order Setup! §a" + amountInInventory + "x §e" + itemTypeLinker.getDisplayName(null) + "§a for §e" + price + " coins each!");
                                } else {
                                    player.sendMessage("§c[Bazaar] §cFailed to submit buy order!");
                                    player.sendMessage("§c[Bazaar] §cYou cannot place orders on items you already have orders on!");
                                }
                            });
                });
            }

            @Override
            public ItemStack.Builder getItem(SkyBlockPlayer player) {
                List<String> lore = new ArrayList<>();
                lore.add("§8" + itemTypeLinker.getDisplayName(null));
                lore.add(" ");
                lore.add("§6Top Offers:");

                if (item.getSellOrders().isEmpty()) {
                    lore.add("§cNo sell orders available.");
                    lore.add(" ");
                    Map<Integer, Integer> itemInInventory = player.getAllOfTypeInInventory(itemTypeLinker);
                    if (!itemInInventory.isEmpty()) {
                        lore.add("§7You have §e" + player.getAmountInInventory(itemTypeLinker) + "§7x in your inventory.");
                        lore.add("§eClick to setup Sell Order");
                    } else {
                        lore.add("§8None in inventory!");
                    }
                } else {
                    item.getSellStatistics().getTop(7, true).forEach((order) -> {
                        lore.add("§7- §6" + order.totalCoins() + " coins §7each | §a" + order.totalItems() + "§7x in §f" + order.numberOfOrders() + "§7 orders");
                    });
                    lore.add(" ");

                    Map<Integer, Integer> itemInInventory = player.getAllOfTypeInInventory(itemTypeLinker);
                    if (!itemInInventory.isEmpty()) {
                        lore.add("§7You have §e" + player.getAmountInInventory(itemTypeLinker) + "§7x in your inventory.");
                        lore.add("§eClick to setup Sell Order");
                    } else {
                        lore.add("§8None in inventory!");
                    }
                }

                return ItemStackCreator.getStack("§6Create Sell Offer",
                        Material.PAPER, 1, lore);
            }
        });

        updateItemStacks(getInventory(), getPlayer());
    }

    @Override
    public void refreshItems(SkyBlockPlayer player) {
        if (!new ProxyService(ServiceType.BAZAAR).isOnline().join()) {
            player.sendMessage("§cThe Bazaar is currently offline!");
            player.closeInventory();
        }
    }

    @Override
    public int refreshRate() {
        return 10;
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }
}
