package io.anuke.mindustry.ai.control.tasks;

import io.anuke.mindustry.Vars;
import io.anuke.mindustry.ai.control.WorkTask;
import io.anuke.mindustry.content.blocks.Blocks;
import io.anuke.mindustry.entities.units.types.WorkerDrone;
import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.world.Tile;

public class MineTask implements WorkTask{
    private final Item item;
    private final int amount;
    private Tile tile;

    public MineTask(Item item, int amount) {
        this.item = item;
        this.amount = amount;
    }

    @Override
    public void begin(WorkerDrone drone){
        drone.getPlaceQueue().clear();
        tile = Vars.world.indexer.findClosestOre(drone.x, drone.y, item);
    }

    @Override
    public void update(WorkerDrone drone){

        drone.moveTo(tile, 20f);
        drone.setMineTile(tile);

        if(tile.block() != Blocks.air){
            drone.finishTask();
        }

        if(drone.getInventory().isFull() || drone.getClosestCore().items.has(item, amount)){
            drone.finishTask();
            drone.beginTask(new DepositTask());
        }
    }
}
