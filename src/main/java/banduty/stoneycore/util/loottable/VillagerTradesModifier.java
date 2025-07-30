package banduty.stoneycore.util.loottable;

import banduty.stoneycore.items.SCItems;
import net.fabricmc.fabric.api.object.builder.v1.trade.TradeOfferHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOffers;
import net.minecraft.village.VillagerProfession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VillagerTradesModifier {
    private static final Map<VillagerProfession, Map<Integer, List<TradeOffers.Factory>>> PROFESSION_TO_LEVELED_TRADE = new HashMap<>();

    public static void registerCustomTrades() {
        PROFESSION_TO_LEVELED_TRADE.clear();
        registerClericTrades();

        for (Map.Entry<VillagerProfession, Map<Integer, List<TradeOffers.Factory>>> professionEntry : PROFESSION_TO_LEVELED_TRADE.entrySet()) {
            VillagerProfession profession = professionEntry.getKey();
            for (Map.Entry<Integer, List<TradeOffers.Factory>> levelEntry : professionEntry.getValue().entrySet()) {
                int level = levelEntry.getKey();
                List<TradeOffers.Factory> offers = levelEntry.getValue();
                TradeOfferHelper.registerVillagerOffers(profession, level, factories -> factories.addAll(offers));
            }
        }
    }

    private static void addTrade(VillagerProfession profession, int level, TradeOffers.Factory factory) {
        PROFESSION_TO_LEVELED_TRADE
                .computeIfAbsent(profession, p -> new HashMap<>())
                .computeIfAbsent(level, l -> new ArrayList<>())
                .add(factory);
    }

    private static void addTradeOffer(VillagerProfession profession, int level, int emeraldCount, int maxUses, int xp, Item item) {
        addTrade(profession, level, (entity, random) -> {
            return new TradeOffer(
                    new ItemStack(Items.EMERALD, emeraldCount), new ItemStack(item, 1), maxUses, xp, 0.05f);
        });
    }

    private static void addRandomTradeOffer(VillagerProfession profession, int level, int emeraldCount, int maxUses, int xp, Item... items) {
        addTrade(profession, level, (entity, random) -> {
            Item item = items[random.nextInt(items.length)];
            return new TradeOffer(new ItemStack(Items.EMERALD, emeraldCount), new ItemStack(item, 1), maxUses, xp, 0.05f);
        });
    }

    private static void registerClericTrades() {
        addTradeOffer(VillagerProfession.CLERIC, 5,10, 32, 15, SCItems.BLACK_POWDER.get());
    }
}