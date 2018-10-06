package io.anuke.mindustry.ai.control;

import io.anuke.mindustry.ai.control.tasks.BuildBlockTask;
import io.anuke.mindustry.content.Items;
import io.anuke.mindustry.content.blocks.ProductionBlocks;
import io.anuke.mindustry.entities.traits.BuilderTrait.BuildRequest;
import io.anuke.mindustry.entities.units.types.WorkerDrone;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.type.Recipe;
import io.anuke.mindustry.world.Tile;
import io.anuke.ucore.entities.EntityGroup;
import io.anuke.ucore.entities.impl.BaseEntity;

import static io.anuke.mindustry.Vars.*;

public class AI{
    private final Team team;

    public AI(Team team) {
        this.team = team;
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

    void assignTask(WorkerDrone drone){
        drone.beginTask(new BuildBlockTask(getClosestDrillReq()));
    }

    BuildRequest getClosestDrillReq(){
        Tile core = state.teams.get(team).cores.first();
        Tile tile = world.indexer().findClosestOre(core.drawx(), core.drawy(), Items.copper);
        return new BuildRequest(tile.x, tile.y, 0, Recipe.getByResult(ProductionBlocks.mechanicalDrill));
    }

}
