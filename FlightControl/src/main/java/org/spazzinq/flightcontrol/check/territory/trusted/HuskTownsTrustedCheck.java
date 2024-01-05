/*
 * This file is part of FlightControl, which is licensed under the MIT License.
 * Copyright (c) 2023 Spazzinq
 */

package org.spazzinq.flightcontrol.check.territory.trusted;

import net.william278.husktowns.api.HuskTownsAPI;
import net.william278.husktowns.claim.TownClaim;
import net.william278.husktowns.town.Member;
import net.william278.husktowns.town.Town;
import org.bukkit.entity.Player;
import org.spazzinq.flightcontrol.check.territory.TerritoryCheck;

import java.util.Optional;

public class HuskTownsTrustedCheck extends TerritoryCheck {
    @Override public boolean check(Player p) {
        Optional<TownClaim> townClaim = getTownClaim(p);
        Optional<Member> member = getMember(p);

        return townClaim.isPresent() && member.isPresent()
                && townClaim.get().town().getRelationWith(member.get().town()) == Town.Relation.ALLY;
    }

    private Optional<TownClaim> getTownClaim(Player p) {
        return HuskTownsAPI.getInstance().getClaimAt(p.getLocation());
    }

    private Optional<Member> getMember(Player p) {
        return HuskTownsAPI.getInstance().getUserTown(p);
    }
}
