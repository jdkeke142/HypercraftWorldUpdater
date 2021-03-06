package fr.keke142.worldupdater;


import com.boydti.fawe.jnbt.anvil.MCAFile;
import com.boydti.fawe.jnbt.anvil.MCAFilter;
import com.boydti.fawe.jnbt.anvil.MCAQueue;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import fr.keke142.worldupdater.providers.GriefPreventionRegionProvider;
import fr.keke142.worldupdater.providers.WorldGuardRegionProvider;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;

public class ConvertMyWorldCommand implements CommandExecutor {
    private static final int REGION_SHIFTS = 5;
    private static final int CHUNK_SHIFTS = 4;
    private static final int WORLD_MIN_Y = 0;
    private static final int WORLD_MAX_Y = 256;
    private WorldUpdaterPlugin plugin;
    private Set<BlockVector2> blacklistedRegions = new HashSet<>();
    private Set<BlockVector2> blacklistedChunks = new HashSet<>();

    public ConvertMyWorldCommand(WorldUpdaterPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Please provide a world name !");
            return false;
        }

        String worldName = args[0];

        World world = Bukkit.getWorld(worldName);

        if (world == null) {
            sender.sendMessage(ChatColor.RED + "This world not exists !");
            return false;
        }

        Set<RegionProvider> regionProviders = new HashSet<>();

        PluginManager pluginManager = plugin.getPluginManager();

        ConsoleCommandSender consoleSender = plugin.getServer().getConsoleSender();

        if (pluginManager.isPluginEnabled("WorldGuard")) {
            regionProviders.add(new WorldGuardRegionProvider(WorldGuard.getInstance()));
            consoleSender.sendMessage(ChatColor.GREEN + "Added WorldGuard as region provider.");
        }

        if (pluginManager.isPluginEnabled("GriefPrevention")) {
            regionProviders.add(new GriefPreventionRegionProvider(GriefPrevention.instance));
            consoleSender.sendMessage(ChatColor.GREEN + "Added GriefPrevention as region provider.");
        }

        if (regionProviders.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Unable to find any region provider !");
            return false;
        }

        Set<UpdaterRegion> regions = new HashSet<>();

        sender.sendMessage(ChatColor.YELLOW + "Merging providers together...");
        for (RegionProvider provider : regionProviders) {
            try {
                regions.addAll(provider.getWorldRegions(world));
            } catch (ResolvingRegionsException e) {
                sender.sendMessage(ChatColor.RED + "Error during resolving world regions: " + e.getCause());
                e.printStackTrace();
            }
        }

        regions.forEach(region -> {
            BlockVector3 min = region.getMinimumPoint().withY(WORLD_MIN_Y);
            BlockVector3 max = region.getMaximumPoint().withY(WORLD_MAX_Y);

            for (int x = min.getBlockX() >> CHUNK_SHIFTS; x <= max.getBlockX() >> CHUNK_SHIFTS; ++x) {
                for (int z = min.getBlockZ() >> CHUNK_SHIFTS; z <= max.getBlockZ() >> CHUNK_SHIFTS; ++z) {
                    blacklistedChunks.add(BlockVector2.at(x, z));
                }
            }

            blacklistedChunks.forEach(chunk -> {
                int chunkX = chunk.getX();
                int chunkZ = chunk.getZ();

                int regionX = chunkX >> REGION_SHIFTS;
                int regionZ = chunkZ >> REGION_SHIFTS;

                blacklistedRegions.add(BlockVector2.at(regionX, regionZ));
            });
        });

        File regionRoot = new File(worldName + File.separator + "region");
        MCAQueue queue = new MCAQueue(worldName, regionRoot, true);

        final MCAFile[] lastRegionFile = new MCAFile[1];

        consoleSender.sendMessage(ChatColor.YELLOW + "Deleting all chunks that are not touch region using FastAsyncWorldEdit FaweQueue...");

        Bukkit.getOnlinePlayers().forEach(player -> player.kickPlayer(ChatColor.YELLOW + "Starting world conversion, please check your console !"));

        consoleSender.sendMessage(ChatColor.YELLOW + "Unloading all chunks of the world...");

        for (Chunk loadedChunk : world.getLoadedChunks()) {
            loadedChunk.unload();
        }

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

                consoleSender.sendMessage(ChatColor.YELLOW + "Deleted region (" + mcaX + ";" + mcaZ + ")");
                return null;
            }

            @Override
            public boolean appliesChunk(int cx, int cz) {
                if (!blacklistedChunks.contains(BlockVector2.at(cx, cz))) {
                    try {
                        MCAFile mcaFile = lastRegionFile[0];

                        RandomAccessFile unChunk = new RandomAccessFile(mcaFile.getFile(), "rwd");

                        int offsetX = mcaFile.getX() << REGION_SHIFTS;
                        int offsetZ = mcaFile.getZ() << REGION_SHIFTS;

                        long wipePos = 4 * ((cx - offsetX) + ((cz - offsetZ) * 32));
                        unChunk.seek(wipePos);
                        unChunk.writeInt(0);

                        unChunk.close();
                        consoleSender.sendMessage(ChatColor.YELLOW + "Deleted chunk (" + cx + ";" + cz + ")");
                    } catch (IOException e) {
                        plugin.getLogger().severe(ChatColor.RED + "Error deleting chunk (" + cx + ";" + cz + ")");
                        e.printStackTrace();
                    }
                }
                return false;
            }

        });

        consoleSender.sendMessage(ChatColor.GREEN + "Conversion task finished ! Stopping the server to apply changes...");
        Bukkit.shutdown();

        return true;
    }
}