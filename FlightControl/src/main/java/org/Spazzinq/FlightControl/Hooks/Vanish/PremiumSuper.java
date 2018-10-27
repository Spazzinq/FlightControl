package org.Spazzinq.FlightControl.Hooks.Vanish;

import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

public class PremiumSuper extends Vanish { @Override public boolean vanished(Player p) { return p.getMetadata("vanished").stream().findFirst().filter(MetadataValue::asBoolean).isPresent(); } }
