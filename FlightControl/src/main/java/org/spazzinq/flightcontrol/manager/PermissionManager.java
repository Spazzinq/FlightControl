package org.spazzinq.flightcontrol.manager;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.object.Category;
import org.spazzinq.flightcontrol.object.FlyPermission;

public class PermissionManager {
    PermissionManager() {

    }

    public static boolean hasPermission(CommandSender p, FlyPermission flyPermission) {
        return p.hasPermission(flyPermission.toString());
    }

    public static boolean hasPermissionFly(Player p, String data) {
        return p.hasPermission(FlyPermission.FLY_STUB + data);
    }

    public static boolean hasPermissionNoFly(Player p, String data) {
        return p.hasPermission(FlyPermission.NO_FLY_STUB + data);
    }

    public static boolean hasPermissionCategory(Player p, Category category) {
        return p.hasPermission(FlyPermission.CATEGORY_STUB + category.getName());
    }
}
