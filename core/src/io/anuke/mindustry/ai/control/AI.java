package io.anuke.mindustry.ai.control;

import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;
import io.anuke.mindustry.ai.control.tasks.BuildBlockTask;
import io.anuke.mindustry.ai.control.tasks.MineTask;
import io.anuke.mindustry.ai.control.tasks.PathfindTask;
import io.anuke.mindustry.content.Items;
import io.anuke.mindustry.content.blocks.CraftingBlocks;
import io.anuke.mindustry.content.blocks.ProductionBlocks;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.entities.traits.BuilderTrait.BuildRequest;
import io.anuke.mindustry.entities.units.types.WorkerDrone;
import io.anuke.mindustry.game.EventType.TileChangeEvent;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.type.Recipe;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;
import io.anuke.ucore.core.Events;
import io.anuke.ucore.entities.EntityGroup;
import io.anuke.ucore.entities.impl.BaseEntity;
import io.anuke.ucore.util.Tmp;

import static io.anuke.mindustry.Vars.unitGroups;
import static io.anuke.mindustry.Vars.world;

public class AI{
    private final Team team;
    private ObjectMap<Block, ObjectSet<Tile>> blocks = new ObjectMap<>();
    private IntMap<Item> tags = new IntMap<>();

    private final Block drillBlock = ProductionBlocks.mechanicalDrill;

    public AI(Team team) {
        this.team = team;

        Events.on(TileChangeEvent.class, event -> {
            for(ObjectSet<Tile> set : blocks.values()){
                set.remove(event.tile);
            }

            if(event.tile.getTeam() == team){
                if(!blocks.containsKey(event.tile.block())) blocks.put(event.tile.block(), new ObjectSet<>());
                blocks.get(event.tile.block()).add(event.tile);
            }
        });
    }

    public void update(){
        EntityGroup<? extends BaseEntity> group = unitGroups[team.ordinal()];
        for(BaseEntity entity : group.all()){
            if(!(entity instanceof WorkerDrone)) continue;

            WorkerDrone drone = (WorkerDrone)entity;
            if(drone.getTask() == null){
                assignTask(drone);
            }
        }
    }

    public void tagTile(Tile tile, Item item){
        tags.put(tile.id(), item);
    }

    public Item tag(Tile tile){
        return tags.get(tile.id());
    }

    ObjectSet<Tile> getBlock(Block block){
        if(!blocks.containsKey(block)) blocks.put(block, new ObjectSet<>());
        return blocks.get(block);
    }

    void assignTask(WorkerDrone drone){
        TileEntity core = drone.getClosestCore();

        Item toMine = Items.copper;
        int amount = core.items.get(toMine);

        if(amount >= 300){
            createSmelter(drone);
        }else if(amount >= 200){
            createDrill(drone, Items.lead);
        }else if(amount >= 60){
            createDrill(drone, toMine);
        }else{
            mineItem(drone, toMine, 50);
        }
    }

    void createSmelter(WorkerDrone drone){
        TileEntity core = drone.getClosestCore();

        Tile tile;
        do{
            Tmp.v1.setToRandomDirection().scl(100f);
            tile = world.tileWorld(core.x + Tmp.v1.x, core.y + Tmp.v1.y);
        }while(tile == null || !tile.block().alwaysReplace);

        drone.beginTask(new BuildBlockTask(new BuildRequest(tile.x, tile.y, 0, Recipe.getByResult(CraftingBlocks.smelter))));
    }

    void mineItem(WorkerDrone drone, Item item, int amount){
        drone.beginTask(new MineTask(item, amount));
    }

    void createDrill(WorkerDrone drone, Item item){
        BuildRequest req = getClosestDrillReq(drone, item);
        drone.beginTask(new PathfindTask(world.tile(req.x + 2, req.y), item));
        drone.beginTask(new BuildBlockTask(req));
    }

    BuildRequest getClosestDrillReq(WorkerDrone drone, Item item){
        Tile tile = world.indexer.findClosestOre(drone.x, drone.y, item, drillBlock, team);
        if(tile == null) return null;
        return new BuildRequest(tile.x, tile.y, 0, Recipe.getByResult(ProductionBlocks.mechanicalDrill));
    }

}
