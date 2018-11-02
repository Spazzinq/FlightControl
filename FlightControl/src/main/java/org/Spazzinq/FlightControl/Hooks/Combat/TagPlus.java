package org.Spazzinq.FlightControl.Hooks.Combat;

import net.minelink.ctplus.TagManager;
import org.Spazzinq.FlightControl.Multiversion.Combat;
import org.bukkit.entity.Player;

public class TagPlus extends Combat {
    private TagManager m;
    public TagPlus(TagManager m) { this.m = m; }
    @Override public boolean tagged(Player p) { return m.isTagged(p.getUniqueId());}
}
