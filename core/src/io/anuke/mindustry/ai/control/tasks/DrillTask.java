package io.anuke.mindustry.ai.control.tasks;

import com.badlogic.gdx.utils.async.AsyncResult;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.ai.control.AI;
import io.anuke.mindustry.ai.control.BfsFinder;
import io.anuke.mindustry.ai.control.WorkTask;
import io.anuke.mindustry.content.blocks.ProductionBlocks;
import io.anuke.mindustry.entities.traits.BuilderTrait.BuildRequest;
import io.anuke.mindustry.entities.units.types.WorkerDrone;
import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.type.Recipe;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Build;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.distribution.Conveyor;

public class DrillTask implements WorkTask{
    private final AsyncResult<Tile> result;
    private final Item item;

    public DrillTask(Tile core, Item item){
        this.item = item;
        Block block = ProductionBlocks.mechanicalDrill;

        result = AI.executor.submit(() -> BfsFinder.findGoal(core,
        tile -> tile.floor().dropsItem(item) && Build.validPlace(core.getTeam(), tile.x, tile.y, block, 0),
        other -> (other.synthetic() || other.floor().isLiquid) && !(other.block() instanceof Conveyor && Vars.state.teams.get(core.getTeam()).ai.tag(other) == item)));
    }

    @Override
    public void begin(WorkerDrone drone){

    }

    @Override
    public void update(WorkerDrone drone){
        if(result.isDone()){
            Tile tile = result.get();
            drone.finishTask();

            if(tile != null){
                drone.beginTask(new PathfindTask(tile, item));
                drone.beginTask(new BuildBlockTask(new BuildRequest(tile.x, tile.y, 0, Recipe.getByResult(ProductionBlocks.mechanicalDrill))));
            }else{
                drone.beginTask(new MineTask(item, 50));
            }
        }
    }

}
