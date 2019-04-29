package fr.keke142.worldupdater.providers;

import com.sk89q.worldedit.math.BlockVector3;
import fr.keke142.worldupdater.RegionProvider;
import fr.keke142.worldupdater.ResolvingRegionsException;
import fr.keke142.worldupdater.UpdaterRegion;
import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.DataStore;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Location;
import org.bukkit.World;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class GriefPreventionRegionProvider implements RegionProvider {
    private GriefPrevention griefPrevention;

    public GriefPreventionRegionProvider(GriefPrevention griefPrevention) {
        this.griefPrevention = griefPrevention;
    }

    @Override
    public Set<UpdaterRegion> getWorldRegions(World world) throws ResolvingRegionsException {
        DataStore dataStore = griefPrevention.dataStore;
        try {
            Field fld = DataStore.class.getDeclaredField("claims");
            fld.setAccessible(true);
            Object o = fld.get(dataStore);
            ArrayList<Claim> claims = (ArrayList<Claim>) o;

            Set<UpdaterRegion> updaterRegions = new HashSet<>();

            claims.forEach(claim -> {
                Location lesserCorner = claim.getLesserBoundaryCorner();
                BlockVector3 lesserCornerVector = BlockVector3.at(lesserCorner.getX(), lesserCorner.getY(), lesserCorner.getZ());

                Location greaterCorner = claim.getGreaterBoundaryCorner();
                BlockVector3 greaterCornerVector = BlockVector3.at(greaterCorner.getX(), greaterCorner.getY(), greaterCorner.getZ());

                updaterRegions.add(new UpdaterRegion(lesserCornerVector, greaterCornerVector));
            });

            return updaterRegions;
        } catch (NoSuchFieldException | IllegalAccessException | IllegalArgumentException e) {
            throw new ResolvingRegionsException(e);
        }
    }
}
