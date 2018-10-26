package io.anuke.mindustry.ai.control;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import io.anuke.mindustry.ai.control.tasks.BuildBlockTask;
import io.anuke.mindustry.content.Items;
import io.anuke.mindustry.content.blocks.ProductionBlocks;
import io.anuke.mindustry.entities.traits.BuilderTrait.BuildRequest;
import io.anuke.mindustry.entities.units.types.WorkerDrone;
import io.anuke.mindustry.game.EventType.TileChangeEvent;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.type.Recipe;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;
import io.anuke.ucore.core.Events;
import io.anuke.ucore.entities.EntityGroup;
import io.anuke.ucore.entities.impl.BaseEntity;

import static io.anuke.mindustry.Vars.*;

public class AI{
    private final Team team;
    private ObjectMap<Block, ObjectSet<Tile>> blocks = new ObjectMap<>();

    private Block drillBlock = ProductionBlocks.mechanicalDrill;

    public AI(Team team) {
        this.team = team;

        Events.on(TileChangeEvent.class, event -> {
            for(ObjectSet<Tile> set : blocks.values()){
                set.remove(event.tile);
            }

            if(event.tile.getTeam() == team){
                if(!blocks.containsKey(event.tile.block())) blocks.put(event.tile.block(), new ObjectSet<>());
                blocks.get(event.tile.block()).add(event.tile);
            }
        });
    }

    public void update(){
        EntityGroup<? extends BaseEntity> group = unitGroups[team.ordinal()];
        for(BaseEntity entity : group.all()){
            if(!(entity instanceof WorkerDrone)) continue;

            WorkerDrone drone = (WorkerDrone)entity;
            if(drone.getTask() == null){
                assignTask(drone);
            }
        }
    }

    ObjectSet<Tile> getBlock(Block block){
        if(!blocks.containsKey(block)) blocks.put(block, new ObjectSet<>());
        return blocks.get(block);
    }

    void assignTask(WorkerDrone drone){
        if(getBlock(drillBlock).size > 10){
            //pathfind to core.
        }

        drone.beginTask(new BuildBlockTask(getClosestDrillReq()));
    }

    BuildRequest getClosestDrillReq(){
        Tile core = state.teams.get(team).cores.first();
        Tile tile = world.indexer().findClosestOre(core.drawx(), core.drawy(), Items.copper);
        return new BuildRequest(tile.x, tile.y, 0, Recipe.getByResult(drillBlock));
    }

}
