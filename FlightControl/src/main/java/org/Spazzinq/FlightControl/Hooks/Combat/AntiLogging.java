package org.Spazzinq.FlightControl.Hooks.Combat;

import com.vk2gpz.anticombatlogging.AntiCombatLoggingAPI;
import org.Spazzinq.FlightControl.Multiversion.Combat;
import org.bukkit.entity.Player;

public class AntiLogging extends Combat { @Override public boolean tagged(Player p) { return AntiCombatLoggingAPI.isInCombat(p); } }
