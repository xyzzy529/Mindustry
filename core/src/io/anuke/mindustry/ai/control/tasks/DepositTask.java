package io.anuke.mindustry.ai.control.tasks;

import io.anuke.mindustry.ai.control.WorkTask;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.entities.UnitInventory;
import io.anuke.mindustry.entities.units.types.WorkerDrone;
import io.anuke.mindustry.gen.Call;

public class DepositTask implements WorkTask {

    @Override
    public void update(WorkerDrone drone) {
        TileEntity tile = drone.getClosestCore();
        drone.circleTo(tile, 20f);

        if(drone.distanceTo(tile) < 30f){
            UnitInventory inventory = drone.inventory;
            Call.transferItemTo(inventory.getItem().item, inventory.getItem().amount, drone.x, drone.y, tile.tile);
            inventory.clearItem();
            drone.finishTask();
        }
    }
}
