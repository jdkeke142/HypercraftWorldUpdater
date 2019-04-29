package fr.keke142.worldupdater.providers;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import fr.keke142.worldupdater.RegionProvider;
import fr.keke142.worldupdater.ResolvingRegionsException;
import fr.keke142.worldupdater.UpdaterRegion;
import org.bukkit.World;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WorldGuardRegionProvider implements RegionProvider {
    private WorldGuard worldGuard;

    public WorldGuardRegionProvider(WorldGuard worldGuard) {
        this.worldGuard = worldGuard;
    }

    @Override
    public Set<UpdaterRegion> getWorldRegions(World world) throws ResolvingRegionsException {
        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(world);

        RegionContainer container = worldGuard.getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(weWorld);

        if (regionManager == null) {
            throw new ResolvingRegionsException();
        }

        Map<String, ProtectedRegion> regions = regionManager.getRegions();

        Set<UpdaterRegion> updaterRegions = new HashSet<>();
        regions.forEach((id, region) -> updaterRegions.add(new UpdaterRegion(region.getMinimumPoint(), region.getMaximumPoint())));

        return updaterRegions;
    }
}
