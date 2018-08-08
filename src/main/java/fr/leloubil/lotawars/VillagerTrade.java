package fr.leloubil.lotawars;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

public class VillagerTrade {
    @Getter
    private ItemStack first;

    public VillagerTrade(ItemStack first, ItemStack sec, ItemStack third, int maxuses) {
        this.first = first;
        this.sec = sec;
        this.third = third;
        this.maxuses = maxuses;
    }

    @Getter
    private ItemStack sec;

    @Getter
    private ItemStack third;

    @Getter
    private int maxuses;
}
