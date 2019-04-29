package fr.keke142.worldupdater;

import org.bukkit.World;

import java.util.Set;

public interface RegionProvider {
    Set<UpdaterRegion> getWorldRegions(World world) throws ResolvingRegionsException;
}
