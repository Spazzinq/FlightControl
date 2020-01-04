package org.spazzinq.flightcontrol.hook.enchantment;

import me.badbones69.crazyenchantments.api.CrazyEnchantments;
import me.badbones69.crazyenchantments.api.enums.CEnchantments;
import org.bukkit.entity.Player;

public class CrazyEnchantmentsHook extends EnchantsHook {
    @Override public boolean canFly(Player p) {
        return CrazyEnchantments.getInstance().hasEnchantment(p.getEquipment().getBoots(), CEnchantments.WINGS);
    }

    @Override public boolean isHooked() {
        return true;
    }
}
