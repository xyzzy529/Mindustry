package io.anuke.mindustry.ai.control.tasks;

import io.anuke.mindustry.Vars;
import io.anuke.mindustry.ai.control.WorkTask;
import io.anuke.mindustry.entities.traits.BuilderTrait.BuildRequest;
import io.anuke.mindustry.entities.units.types.WorkerDrone;

public class BuildBlockTask implements WorkTask{
    private final BuildRequest request;

    public BuildBlockTask(BuildRequest request){
        this.request = request;
    }

    @Override
    public void begin(WorkerDrone drone){
        drone.getPlaceQueue().clear();
        drone.getPlaceQueue().addLast(request);
    }

    @Override
    public void update(WorkerDrone drone){
        drone.circleTo(Vars.world.tile(request.x, request.y), 30f);

        if(!drone.isBuilding()){
            drone.complete();
        }
    }
}
