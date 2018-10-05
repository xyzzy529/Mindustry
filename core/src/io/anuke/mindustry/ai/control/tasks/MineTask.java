package io.anuke.mindustry.ai.control.tasks;

import io.anuke.mindustry.ai.control.WorkTask;
import io.anuke.mindustry.content.blocks.Blocks;
import io.anuke.mindustry.entities.units.types.WorkerDrone;
import io.anuke.mindustry.world.Tile;
import io.anuke.ucore.core.Timers;

public class MineTask implements WorkTask{
    private final Tile tile;
    private final float duration;

    private float time;

    public MineTask(Tile tile, float duration) {
        this.tile = tile;
        this.duration = duration;
    }

    @Override
    public void begin(WorkerDrone drone){
        drone.clearBuilding();
        drone.setMineTile(tile);
    }

    @Override
    public void update(WorkerDrone drone){
        time += Timers.delta();

        drone.moveTo(tile, 20f);
        drone.setMineTile(tile);

        if(tile.block() != Blocks.air || time >= duration){
            drone.finishTask();
        }

        if(drone.getInventory().isFull()){
            drone.finishTask();
            drone.beginTask(new DepositTask());
        }
    }
}
