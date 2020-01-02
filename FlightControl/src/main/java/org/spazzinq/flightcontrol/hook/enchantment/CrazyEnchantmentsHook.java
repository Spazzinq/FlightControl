package org.spazzinq.flightcontrol.hook.enchantment;

import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.hook.Hook;

public class CrazyEnchantmentsHook extends Hook {
    public boolean canFly(Player p) {
        return false;
    }
}
