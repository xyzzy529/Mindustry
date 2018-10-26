package io.anuke.mindustry.entities.units.types;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Queue;
import io.anuke.mindustry.ai.control.WorkTask;
import io.anuke.mindustry.entities.traits.BuilderTrait;
import io.anuke.mindustry.entities.traits.TargetTrait;
import io.anuke.mindustry.entities.units.FlyingUnit;
import io.anuke.mindustry.entities.units.UnitState;
import io.anuke.mindustry.world.Tile;
import io.anuke.ucore.util.Log;
import io.anuke.ucore.util.Mathf;
import io.anuke.ucore.util.ThreadQueue;

/**Drone controlled by AI.*/
public class WorkerDrone extends FlyingUnit implements BuilderTrait{
    protected Array<WorkTask> tasks = new Array<>();
    protected Tile mineTile;
    protected Queue<BuildRequest> placeQueue = new ThreadQueue<>();

    public final UnitState work = new UnitState(){
        @Override
        public void update() {
            if(getTask() != null){
                getTask().update(WorkerDrone.this);
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
        return work;
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

    @Override
    public void updateRotation(){
        if(target != null && (getMineTile() != null || isBuilding())){
            rotation = Mathf.slerpDelta(rotation, angleTo(target), 0.3f);
        }else{
            rotation = Mathf.slerpDelta(rotation, velocity.angle(), 0.3f);
        }
    }

    @Override
    public void behavior(){}

    public void circleTo(TargetTrait trait, float range){
        target = trait;
        circle(range);
    }

    public void moveTo(TargetTrait trait, float range){
        target = trait;
        moveTo(range);
    }

    public WorkTask getTask(){
        return tasks.size == 0 ? null : tasks.peek();
    }

    public void beginTask(WorkTask task){
        Log.info("{0}: begin {1}", id, task.getClass().getSimpleName());
        //new RuntimeException().printStackTrace();
        tasks.add(task);
        task.begin(this);
    }

    /**Completes the current task.*/
    public void finishTask(){
        if(getTask() != null) getTask().completed(this);
        tasks.pop();
        if(getTask() != null){
            getTask().begin(this);
        }
    }
}
