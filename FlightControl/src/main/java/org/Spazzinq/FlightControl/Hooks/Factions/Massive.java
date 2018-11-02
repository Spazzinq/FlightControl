package org.Spazzinq.FlightControl.Hooks.Factions;

import com.massivecraft.factions.Rel;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.FactionColl;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.entity.Player;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;

public class Massive extends Factions {
    private HashMap<String, AbstractMap.SimpleEntry<List<String>, List<String>>> fCategories;
    public Massive(HashMap<String, AbstractMap.SimpleEntry<List<String>, List<String>>> fCategories) { this.fCategories = fCategories; }

    @Override
    public boolean rel(Player p, boolean type) {
        // 0 == auto_flight && 1 == disable_flight
        for (String category : fCategories.keySet()) {
            if (p.hasPermission("flightcontrol.factions." + category)) {
                List<String> types = type ? fCategories.get(category).getKey() : fCategories.get(category).getValue();
                boolean own = false, ally = false, truce = false, neutral = false, enemy = false, warzone = false, safezone = false, wilderness = false;
                MPlayer mp = MPlayer.get(p);
                Faction f = BoardColl.get().getFactionAt(PS.valueOf(p.getLocation()));
                FactionColl fColl = FactionColl.get();
                if (types.contains("WARZONE")) warzone = f == fColl.getWarzone();
                if (types.contains("SAFEZONE")) safezone = f == fColl.getSafezone();
                if (types.contains("WILDERNESS")) wilderness = f.isNone();
                if (mp.hasFaction()) {
                    Rel r = f.getRelationWish(mp.getFaction());
                    if (types.contains("OWN")) own = mp.isInOwnTerritory();
                    if (types.contains("ALLY")) ally = r == Rel.ALLY;
                    if (types.contains("TRUCE")) truce = r == Rel.TRUCE;
                    if (types.contains("NEUTRAL")) neutral = !f.isNone() && f != fColl.getWarzone() && f != fColl.getSafezone() && !mp.isInOwnTerritory() && r == Rel.NEUTRAL;
                    if (types.contains("ENEMY")) enemy = r == Rel.ENEMY;
                }
                return own || ally || truce || neutral || enemy || warzone || safezone || wilderness;
            }
        }
        return false;
    }
}
