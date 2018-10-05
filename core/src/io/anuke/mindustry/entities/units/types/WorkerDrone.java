package io.anuke.mindustry.entities.units.types;

import com.badlogic.gdx.utils.Queue;
import io.anuke.mindustry.ai.control.WorkTask;
import io.anuke.mindustry.entities.traits.BuilderTrait;
import io.anuke.mindustry.entities.traits.TargetTrait;
import io.anuke.mindustry.entities.units.FlyingUnit;
import io.anuke.mindustry.entities.units.UnitState;
import io.anuke.mindustry.world.Tile;
import io.anuke.ucore.util.ThreadQueue;

/**Drone controlled by AI.*/
public class WorkerDrone extends FlyingUnit implements BuilderTrait{
    protected WorkTask task;
    protected Tile mineTile;
    protected Queue<BuildRequest> placeQueue = new ThreadQueue<>();

    public final UnitState work = new UnitState(){
        @Override
        public void update() {
            if(task != null){
                task.update(WorkerDrone.this);
            }
        }
    };

    @Override
    public void update(){
        super.update();
        updateBuilding();
    }

    @Override
    public void drawOver(){
        super.drawOver();
        drawBuilding();
    }

    @Override
    public UnitState getStartState() {
        return super.getStartState();
    }

    @Override
    public Queue<BuildRequest> getPlaceQueue() {
        return placeQueue;
    }

    @Override
    public Tile getMineTile() {
        return mineTile;
    }

    @Override
    public void setMineTile(Tile tile) {
        this.mineTile = tile;
    }

    @Override
    public float getMinePower() {
        return 1f;
    }

    @Override
    public float getBuildPower(Tile tile) {
        return 1f;
    }

    public void circleTo(TargetTrait trait, float range){
        target = trait;
        circle(range);
    }

    public WorkTask getTask() {
        return task;
    }

    public void beginTask(WorkTask task){
        this.task = task;
        task.begin(this);
    }

    /**Completes the current task.*/
    public void complete(){
        if(task != null) task.completed(this);
        task = null;
    }
}
