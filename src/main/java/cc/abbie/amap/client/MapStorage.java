package cc.abbie.amap.client;

import com.google.common.collect.Multimap;
import folk.sisby.surveyor.WorldSummary;
import folk.sisby.surveyor.client.SurveyorClientEvents;
import folk.sisby.surveyor.landmark.LandmarkType;
import folk.sisby.surveyor.terrain.ChunkSummary;
import folk.sisby.surveyor.terrain.LayerSummary;
import folk.sisby.surveyor.terrain.RegionSummary;
import folk.sisby.surveyor.terrain.WorldTerrainSummary;
import folk.sisby.surveyor.util.RegistryPalette;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;

import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

// mostly copied from https://github.com/HestiMae/hoofprint/blob/777a182f5e0136d8e3b9a5b9c9362fe6dc41ea72/src/main/java/garden/hestia/hoofprint/HoofprintMapStorage.java
public class MapStorage implements SurveyorClientEvents.WorldLoad, SurveyorClientEvents.TerrainUpdated {
    public static final MapStorage INSTANCE = new MapStorage();

    public Map<ChunkPos, LayerSummary.Raw[][]> terrain = new HashMap<>();
    public Map<ChunkPos, RegistryPalette<Block>.ValueView> blockPalettes = new HashMap<>();

    @Override
    public void onTerrainUpdated(Level level, WorldTerrainSummary terrainSummary, Collection<ChunkPos> chunks) {
        for (ChunkPos pos : chunks) {
            ChunkSummary chunk = terrainSummary.get(pos);
            if (chunk == null) continue;
            LayerSummary.Raw layerSummary = chunk.toSingleLayer(null, null, level.getHeight());
            if (layerSummary == null) continue;
            terrain.computeIfAbsent(
                    new ChunkPos(RegionSummary.chunkToRegion(pos.x), RegionSummary.chunkToRegion(pos.z)),
                    c -> new LayerSummary.Raw[32][32]
            )[RegionSummary.regionRelative(pos.x)][RegionSummary.regionRelative(pos.z)]
                    = chunk.toSingleLayer(null, null, level.getHeight());
            blockPalettes.put(pos, terrainSummary.getBlockPalette(pos));
        }
    }

    @Override
    public void onWorldLoad(ClientLevel clientLevel, WorldSummary summary, LocalPlayer player, Map<ChunkPos, BitSet> terrain, Multimap<ResourceKey<Structure>, ChunkPos> structures, Multimap<LandmarkType<?>, BlockPos> landmarks) {
        onTerrainUpdated(clientLevel, summary.terrain(), WorldTerrainSummary.toKeys(terrain));
    }
}