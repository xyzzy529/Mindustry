package io.anuke.mindustry.ai.control;

import io.anuke.mindustry.entities.units.types.WorkerDrone;

public interface WorkTask {
    void update(WorkerDrone drone);
    default void completed(WorkerDrone drone){}
}
