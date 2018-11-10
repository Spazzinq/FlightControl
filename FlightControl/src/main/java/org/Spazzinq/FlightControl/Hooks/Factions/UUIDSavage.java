package org.Spazzinq.FlightControl.Hooks.Factions;

import com.massivecraft.factions.Board;
import com.massivecraft.factions.FLocation;
import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.FPlayers;
import com.massivecraft.factions.struct.Relation;
import org.Spazzinq.FlightControl.Config;
import org.bukkit.entity.Player;

import java.util.List;

public class UUIDSavage extends Factions {

    @Override
    public boolean rel(Player p, boolean type) {
        for (String category : Config.getCategories().keySet()) {
            if (p.hasPermission("flightcontrol.factions." + category)) {
                List<String> types = type ? Config.getCategories().get(category).getKey() : Config.getCategories().get(category).getValue();
                boolean own = false, ally = false, truce = false, neutral = false, enemy = false, warzone = false, safezone = false, wilderness = false;
                FPlayer fP = FPlayers.getInstance().getByPlayer(p);
                Board b = Board.getInstance();
                FLocation l = new FLocation(p.getLocation());
                if (types.contains("WARZONE")) warzone = b.getFactionAt(l).isWarZone();
                if (types.contains("SAFEZONE")) safezone = b.getFactionAt(l).isSafeZone();
                if (types.contains("WILDERNESS")) wilderness = b.getFactionAt(l).isWilderness();
                if (fP.hasFaction()) {
                    if (types.contains("OWN")) own = fP.isInOwnTerritory();
                    if (types.contains("ALLY")) ally = fP.isInAllyTerritory();
                    if (types.contains("TRUCE")) truce = fP.getRelationToLocation() == Relation.TRUCE;
                    if (types.contains("NEUTRAL")) neutral = fP.isInNeutralTerritory();
                    if (types.contains("ENEMY")) enemy = fP.isInEnemyTerritory();
                }
                return own || ally || truce || neutral || enemy || warzone || safezone || wilderness;
            }
        }
        return false;
    }
}
