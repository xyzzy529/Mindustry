package io.anuke.mindustry.ai.control.tasks;

import io.anuke.mindustry.ai.control.WorkTask;
import io.anuke.mindustry.entities.units.types.WorkerDrone;
import io.anuke.mindustry.world.Tile;

public class MineTask implements WorkTask{
    private final Tile tile;

    public MineTask(Tile tile) {
        this.tile = tile;
    }

    @Override
    public void begin(WorkerDrone drone){
        drone.clearBuilding();
        drone.setMineTile(tile);
    }

    @Override
    public void update(WorkerDrone drone){
        if(drone.getInventory().isFull()){

        }
    }
}
