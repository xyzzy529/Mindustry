package io.anuke.mindustry.ai.control.tasks;

import io.anuke.mindustry.Vars;
import io.anuke.mindustry.ai.control.WorkTask;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.entities.traits.BuilderTrait.BuildRequest;
import io.anuke.mindustry.entities.units.types.WorkerDrone;
import io.anuke.mindustry.type.ItemStack;
import io.anuke.mindustry.world.Tile;

public class BuildBlockTask implements WorkTask{
    private final BuildRequest request;

    public BuildBlockTask(BuildRequest request){
        this.request = request;
    }

    @Override
    public void begin(WorkerDrone drone){
        drone.setMineTile(null);
        drone.clearBuilding();
        drone.getPlaceQueue().addLast(request);
    }

    @Override
    public void update(WorkerDrone drone){
        drone.moveTo(Vars.world.tile(request.x, request.y), 30f);

        TileEntity core = drone.getClosestCore();

        if(core != null){
            for(ItemStack stack : request.recipe.requirements){
                if(!core.items.has(stack.item, stack.amount) && stack.item.genOre){
                    Tile tile = Vars.world.indexer().findClosestOre(drone.x, drone.y, stack.item);
                    drone.beginTask(new MineTask(tile, 60*20));
                    return;
                }
            }
        }

        if(request.progress >= 1f){
            drone.finishTask();
        }
    }
}
