package net.swofty.types.generic.item.items.mining.vanilla;

import net.swofty.types.generic.item.ItemType;
import net.swofty.types.generic.item.MaterialQuantifiable;
import net.swofty.types.generic.item.SkyBlockItem;
import net.swofty.types.generic.item.impl.*;
import net.swofty.types.generic.item.impl.recipes.ShapedRecipe;
import net.swofty.types.generic.user.statistics.ItemStatistic;
import net.swofty.types.generic.user.statistics.ItemStatistics;
import net.swofty.types.generic.utility.groups.EnchantItemGroups;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StonePickaxe implements CustomSkyBlockItem, PickaxeImpl, Craftable {
    @Override
    public ItemStatistics getStatistics() {
        return ItemStatistics.builder()
                .with(ItemStatistic.MINING_SPEED, 110D)
                .with(ItemStatistic.DAMAGE, 20D)
                .build();
    }

    @Override
    public int getBreakingPower() {
        return 2;
    }

    @Override
    public SkyBlockRecipe<?> getRecipe() {
        Map<Character, MaterialQuantifiable> ingredientMap = new HashMap<>();
        ingredientMap.put('A', new MaterialQuantifiable(ItemType.COBBLESTONE, 1));
        ingredientMap.put('B', new MaterialQuantifiable(ItemType.STICK, 1));
        ingredientMap.put(' ', new MaterialQuantifiable(ItemType.AIR, 1));
        List<String> pattern = List.of(
                "AAA",
                " B ",
                " B ");

        return new ShapedRecipe(SkyBlockRecipe.RecipeType.NONE, new SkyBlockItem(ItemType.STONE_PICKAXE), ingredientMap, pattern);
    }
}

