package fr.keke142.worldupdater;

import com.sk89q.worldedit.math.BlockVector3;

public class UpdaterRegion {
    private BlockVector3 minimumPoint;
    private BlockVector3 maximumPoint;

    public UpdaterRegion(BlockVector3 minimumPoint, BlockVector3 maximumPoint) {
        this.minimumPoint = minimumPoint;
        this.maximumPoint = maximumPoint;
    }

    public BlockVector3 getMinimumPoint() {
        return minimumPoint;
    }

    public BlockVector3 getMaximumPoint() {
        return maximumPoint;
    }
}
