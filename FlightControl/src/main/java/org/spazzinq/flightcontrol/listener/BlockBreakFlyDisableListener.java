package org.spazzinq.flightcontrol.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.spazzinq.flightcontrol.FlightControl;

public class BlockBreakFlyDisableListener implements Listener {
    private final FlightControl pl;

    public BlockBreakFlyDisableListener(FlightControl pl) {
        this.pl = pl;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material blockType = event.getBlock().getType();

        if (pl.getConfManager().getBlockBreakDisableList().contains(blockType.name())) {
            if (player.isFlying()) {
                player.setAllowFlight(false);
                player.setFlying(false);

                String message = org.bukkit.ChatColor.translateAlternateColorCodes('&',
                        pl.getLangManager().getBlockBreakDisable().replace("%block%", blockType.name().toLowerCase()));
                player.sendMessage(message);
            }
        }
    }
}
