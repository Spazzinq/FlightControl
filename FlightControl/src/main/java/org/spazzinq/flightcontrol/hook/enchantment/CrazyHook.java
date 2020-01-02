package org.spazzinq.flightcontrol.hook.enchantment;

import me.badbones69.crazyenchantments.api.managers.WingsManager;
import org.bukkit.entity.Player;

public class CrazyHook extends CrazyEnchantmentsHook {
    @Override public boolean canFly(Player p) {
        return WingsManager.getInstance().isFlyingPlayer(p);
    }

    @Override public boolean isHooked() {
        return true;
    }
}
