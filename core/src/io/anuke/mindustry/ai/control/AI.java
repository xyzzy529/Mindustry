package io.anuke.mindustry.ai.control;

import io.anuke.mindustry.game.Team;
import io.anuke.ucore.entities.EntityGroup;
import io.anuke.ucore.entities.impl.BaseEntity;

import static io.anuke.mindustry.Vars.unitGroups;

public class AI{

    public void update(Team team){
        EntityGroup<? extends BaseEntity> group = unitGroups[team.ordinal()];
        for(BaseEntity entity : group.all()){

        }
    }

}
