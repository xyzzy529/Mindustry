package io.anuke.mindustry.ai.control.tasks;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.async.AsyncResult;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.ai.control.AI;
import io.anuke.mindustry.ai.control.BfsFinder;
import io.anuke.mindustry.ai.control.WorkTask;
import io.anuke.mindustry.content.blocks.DistributionBlocks;
import io.anuke.mindustry.entities.traits.BuilderTrait.BuildRequest;
import io.anuke.mindustry.entities.units.types.WorkerDrone;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.type.ItemStack;
import io.anuke.mindustry.type.Recipe;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.distribution.Conveyor;
import io.anuke.mindustry.world.blocks.storage.CoreBlock;
import io.anuke.mindustry.world.modules.ItemModule;
import io.anuke.ucore.function.Predicate;

public class PathfindTask implements WorkTask{
    private AsyncResult<Array<Tile>> result;
    private boolean placed;

    private final Tile tile;
    private final Item tag;
    private final Predicate<Tile> goal;
    private final Block block = DistributionBlocks.conveyor;

    public PathfindTask(Tile tile, Item tag){
        //todo only red currently
        this(tile, tag, other -> other.target().block() instanceof CoreBlock
                || (other.target().block() instanceof Conveyor) && Vars.state.teams.get(Team.red).ai.tag(other) == tag);
    }

    public PathfindTask(Tile tile, Item tag, Predicate<Tile> goal){
        this.tile = tile;
        this.tag = tag;
        this.goal = goal;
    }

    @Override
    public void update(WorkerDrone drone){
        if(result == null){
            result = AI.executor.submit(() -> BfsFinder.find(tile, goal));
        }else if(result.isDone()){
            Array<Tile> out = result.get();

            if(out == null){
                drone.finishTask();
                return;
            }

            Recipe recipe = Recipe.getByResult(block);
            ItemModule items = drone.getClosestCore().items;
            boolean wait = false;

            for(ItemStack stack : recipe.requirements){
                if(!items.has(stack.item, stack.amount * out.size)){
                    drone.beginTask(new MineTask(stack.item, stack.amount * out.size));
                    wait = true;
                }
            }

            if(wait) return;

            if(!placed) {
                for (int i = 0; i < out.size - 1; i++) {
                    Tile current = out.get(i);
                    Tile next = out.get(i + 1);
                    drone.beginTask(new BuildBlockTask(new BuildRequest(current.x, current.y, current.relativeTo(next.x, next.y), recipe)));
                    Vars.state.teams.get(drone.getTeam()).ai.tagTile(current, tag);
                }
                placed = true;
            }

            drone.finishTask();
        }
    }
}
