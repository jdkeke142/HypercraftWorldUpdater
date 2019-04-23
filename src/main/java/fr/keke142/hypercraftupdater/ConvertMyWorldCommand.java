package fr.keke142.hypercraftupdater;


import com.boydti.fawe.jnbt.anvil.MCAFile;
import com.boydti.fawe.jnbt.anvil.MCAFilter;
import com.boydti.fawe.jnbt.anvil.MCAQueue;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.world.storage.ChunkStore;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

public class ConvertMyWorldCommand implements CommandExecutor {
    private HypercraftUpdaterPlugin plugin;
    private Set<BlockVector2> blacklistedRegions = new HashSet<>();
    private Set<BlockVector2> blacklistedChunks = new HashSet<>();

    public ConvertMyWorldCommand(HypercraftUpdaterPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage("Please provide a world name !");
            return false;
        }

        String worldName = args[0];

        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            sender.sendMessage("This world not exists !");
            return false;
        }

        com.sk89q.worldedit.world.World weWorld = BukkitAdapter.adapt(world);

        sender.sendMessage("Deleting all chunks that are not touch WG region using FastAsyncWorldEdit FaweQueue...");

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regionManager = container.get(weWorld);

        if (regionManager == null) {
            sender.sendMessage("Unable to resolve WorldGuard RegionManager for this world !");
            return false;
        }

        Map<String, ProtectedRegion> regions = regionManager.getRegions();

        regions.forEach((id, region) -> {

            BlockVector3 min = region.getMinimumPoint().withY(0);
            BlockVector3 max = region.getMaximumPoint().withY(256);

            for (int x = min.getBlockX() >> ChunkStore.CHUNK_SHIFTS; x <= max.getBlockX() >> ChunkStore.CHUNK_SHIFTS; ++x) {
                for (int z = min.getBlockZ() >> ChunkStore.CHUNK_SHIFTS; z <= max.getBlockZ() >> ChunkStore.CHUNK_SHIFTS; ++z) {
                    blacklistedChunks.add(BlockVector2.at(x, z));
                }
            }

            blacklistedChunks.forEach(chunk -> {
                int chunkX = chunk.getX();
                int chunkZ = chunk.getZ();

                int regionX = chunkX >> 5;
                int regionZ = chunkZ >> 5;

                blacklistedRegions.add(BlockVector2.at(regionX, regionZ));
            });
        });

        File regionRoot = new File(worldName + File.separator + "region");
        MCAQueue queue = new MCAQueue(worldName, regionRoot, true);

        final MCAFile[] lastRegionFile = new MCAFile[1];

        queue.filterWorld(new MCAFilter() {
            @Override
            public MCAFile applyFile(MCAFile mca) {
                int mcaX = mca.getX();
                int mcaZ = mca.getZ();

                BlockVector2 mcaVector = BlockVector2.at(mcaX, mcaZ);

                for (BlockVector2 region : blacklistedRegions) {
                    if (mcaVector.equals(region)) {
                        lastRegionFile[0] = mca;
                        return mca;
                    }
                }

                mca.close(ForkJoinPool.commonPool());
                mca.getFile().delete();

                sender.sendMessage("Deleted region (" + mcaX + ";" + mcaZ + ")");
                return null;
            }

            @Override
            public boolean appliesChunk(int cx, int cz) {
                if (!blacklistedChunks.contains(BlockVector2.at(cx, cz))) {
                        try {
                            MCAFile mcaFile = lastRegionFile[0];

                            RandomAccessFile unChunk = new RandomAccessFile(mcaFile.getFile(), "rwd");

                            int offsetX = mcaFile.getX() << 5;
                            int offsetZ = mcaFile.getZ() << 5;

                            long wipePos = 4 * ((cx - offsetX) + ((cz - offsetZ) * 32));
                            unChunk.seek(wipePos);
                            unChunk.writeInt(0);

                            unChunk.close();
                            sender.sendMessage("Deleted chunk (" + cx + ";" + cz + ")");
                        } catch (IOException e) {
                            plugin.getLogger().severe("Error deleting chunk (" + cx + ";" + cz + ")");
                            e.printStackTrace();
                        }

                }
                return false;
            }

        });

        sender.sendMessage("Conversion task finished !");

        return true;
    }
}