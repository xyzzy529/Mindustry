package io.anuke.mindustry.world.blocks.distribution;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.entities.Unit;
import io.anuke.mindustry.graphics.Layer;
import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.meta.BlockGroup;
import io.anuke.mindustry.world.meta.BlockStat;
import io.anuke.mindustry.world.meta.StatUnit;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.graphics.Draw;
import io.anuke.ucore.util.Geometry;
import io.anuke.ucore.util.Mathf;
import io.anuke.ucore.util.Translator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import static io.anuke.mindustry.Vars.tilesize;

public class Conveyor extends Block{
    private static final float itemSpace = 0.135f * 2.2f;
    private static final float offsetScl = 128f * 3f;
    private static final float minmove = 1f / (Short.MAX_VALUE - 2);
    private final Translator tr1 = new Translator();
    private final Translator tr2 = new Translator();

    private TextureRegion[][] regions = new TextureRegion[7][4];

    protected float speed = 0f;

    protected Conveyor(String name){
        super(name);
        rotate = true;
        update = true;
        layer = Layer.overlay;
        group = BlockGroup.transportation;
        hasItems = true;
        autoSleep = true;
        itemCapacity = 4;
    }

    @Override
    public void setBars(){}

    @Override
    public void setStats(){
        super.setStats();
        stats.add(BlockStat.itemSpeed, speed * 60, StatUnit.pixelsSecond);
    }

    @Override
    public void load(){
        super.load();

        for(int i = 0; i < regions.length; i++){
            for(int j = 0; j < 4; j++){
                regions[i][j] = Draw.region(name + "-" + i + "-" + j);
            }
        }
    }

    @Override
    public void drawShadow(Tile tile){
        //fixes build block crash
        if(!(tile.entity instanceof ConveyorEntity)){
            super.drawShadow(tile);
            return;
        }

        ConveyorEntity entity = tile.entity();

        if(entity.blendshadowrot == -1){
            super.drawShadow(tile);
        }else{
            Draw.rect("shadow-corner", tile.drawx(), tile.drawy(), (tile.getRotation() + 3 + entity.blendshadowrot) * 90);
        }
    }

    @Override
    public void draw(Tile tile){
        ConveyorEntity entity = tile.entity();
        byte rotation = tile.getRotation();
        //TODO clog display
        boolean clogged = false;

        int frame = !clogged ? (int) (((Timers.time() * speed * 8f * entity.timeScale)) % 4) : 0;
        Draw.rect(regions[Mathf.clamp(entity.blendbits, 0, regions.length - 1)][Mathf.clamp(frame, 0, regions[0].length - 1)], tile.drawx(), tile.drawy(),
            tilesize * entity.blendsclx, tilesize * entity.blendscly, rotation*90);
    }

    @Override
    public void onProximityUpdate(Tile tile){
        super.onProximityUpdate(tile);

        ConveyorEntity entity = tile.entity();
        entity.blendbits = 0;
        entity.blendsclx = entity.blendscly = 1;
        entity.blendshadowrot = -1;

        if(blends(tile, 2) && blends(tile, 1) && blends(tile, 3)){
            entity.blendbits = 3;
        }else if(blends(tile, 1) && blends(tile, 3)){
            entity.blendbits = 4;
        }else if(blends(tile, 1) && blends(tile, 2)){
            entity.blendbits = 2;
        }else if(blends(tile, 3) && blends(tile, 2)){
            entity.blendbits = 2;
            entity.blendscly = -1;
        }else if(blends(tile, 1)){
            entity.blendbits = 1;
            entity.blendscly = -1;
            entity.blendshadowrot = 0;
        }else if(blends(tile, 3)){
            entity.blendbits = 1;
            entity.blendshadowrot = 1;
        }
    }

    private boolean blends(Tile tile, int direction){
        Tile other = tile.getNearby(Mathf.mod(tile.getRotation() - direction, 4));
        if(other != null) other = other.target();

        return other != null && other.block().outputsItems()
        && ((tile.getNearby(tile.getRotation()) == other) || (!other.block().rotate || other.getNearby(other.getRotation()) == tile));
    }

    @Override
    public TextureRegion[] getIcon(){
        if(icon == null){
            icon = new TextureRegion[]{Draw.region(name + "-0-0")};
        }
        return super.getIcon();
    }

    @Override
    public void drawLayer(Tile tile){
        ConveyorEntity entity = tile.entity();

        byte rotation = tile.getRotation();

        //TODO
        /*
        for(int i = 0; i < entity.convey.size; i++){
            ItemPos pos = drawpos.set(entity.convey.get(i), ItemPos.drawShorts);

            if(pos.item == null) continue;

            tr1.trns(rotation * 90, tilesize, 0);
            tr2.trns(rotation * 90, -tilesize / 2f, pos.x * tilesize / 2);

            Draw.rect(pos.item.region,
                    (int) (tile.x * tilesize + tr1.x * pos.y + tr2.x),
                    (int) (tile.y * tilesize + tr1.y * pos.y + tr2.y), itemSize, itemSize);
        }*/
    }

    @Override
    public void unitOn(Tile tile, Unit unit){
        ConveyorEntity entity = tile.entity();

        entity.noSleep();

        float speed = this.speed * tilesize / 2.3f;
        float centerSpeed = 0.1f;
        float centerDstScl = 3f;
        float tx = Geometry.d4[tile.getRotation()].x, ty = Geometry.d4[tile.getRotation()].y;

        float centerx = 0f, centery = 0f;

        if(Math.abs(tx) > Math.abs(ty)){
            centery = Mathf.clamp((tile.worldy() - unit.y) / centerDstScl, -centerSpeed, centerSpeed);
            if(Math.abs(tile.worldy() - unit.y) < 1f) centery = 0f;
        }else{
            centerx = Mathf.clamp((tile.worldx() - unit.x) / centerDstScl, -centerSpeed, centerSpeed);
            if(Math.abs(tile.worldx() - unit.x) < 1f) centerx = 0f;
        }

        //TODO check if not clogged
        //if(entity.convey.size * itemSpace < 0.9f){
            unit.getVelocity().add((tx * speed + centerx) * entity.delta(), (ty * speed + centery) * entity.delta());
        //}
    }

    @Override
    public void onProximityAdded(Tile tile){
        ConveyorEntity entity = tile.entity();
        Tile front = tile.getNearby(tile.getRotation());
        Tile back = tile.getNearby((tile.getRotation() + 2) % 4);
        if(front.getRotation() == tile.getRotation() && front.block() instanceof Conveyor) front.<ConveyorEntity>entity().line.add(tile);
        if(back.getRotation() == tile.getRotation() && back.block() instanceof Conveyor) back.<ConveyorEntity>entity().line.add(tile);

        //create new line if nothing was added to this one
        if(entity.line == null){
            entity.line = new ConveyorLine(tile);
        }
    }

    @Override
    public void onProximityRemoved(Tile tile){
        super.onProximityRemoved(tile);
    }

    @Override
    public void update(Tile tile){

        ConveyorEntity entity = tile.entity();
        entity.line.update(tile);
    }

    @Override
    public boolean isAccessible(){
        return true;
    }

    @Override
    public int removeStack(Tile tile, Item item, int amount){
        /*
        ConveyorEntity entity = tile.entity();
        entity.noSleep();
        int removed = 0;


        for(int j = 0; j < amount; j++){
            for(int i = 0; i < entity.convey.size; i++){
                long val = entity.convey.get(i);
                ItemPos pos = pos1.set(val, ItemPos.drawShorts);
                if(pos.item == item){
                    entity.convey.removeValue(val);
                    entity.items.remove(item, 1);
                    removed++;
                    break;
                }
            }
        }
        return removed;*/
        return 0;
        //TODO
    }

    @Override
    public void getStackOffset(Item item, Tile tile, Translator trns){
        trns.trns(tile.getRotation() * 90 + 180f, tilesize / 2f);
    }

    @Override
    public int acceptStack(Item item, int amount, Tile tile, Unit source){
        return 0;
        //TODO
        /*ConveyorEntity entity = tile.entity();
        return Math.min((int)(entity.minitem / itemSpace), amount);*/
    }

    @Override
    public void handleStack(Item item, int amount, Tile tile, Unit source){
        //TODO
        /*
        ConveyorEntity entity = tile.entity();

        for(int i = amount - 1; i >= 0; i--){
            long result = ItemPos.packItem(item, 0f, i * itemSpace, (byte) Mathf.random(255));
            entity.convey.insert(0, result);
            entity.items.add(item, 1);
        }

        entity.noSleep();*/
    }

    @Override
    public boolean acceptItem(Item item, Tile tile, Tile source){
        //TODO
        return false;
        /*
        int direction = source == null ? 0 : Math.abs(source.relativeTo(tile.x, tile.y) - tile.getRotation());
        float minitem = tile.<ConveyorEntity>entity().minitem;
        return (((direction == 0) && minitem > itemSpace) ||
                ((direction % 2 == 1) && minitem > 0.52f)) && (source == null || !(source.block().rotate && (source.getRotation() + 2) % 4 == tile.getRotation()));*/
    }

    @Override
    public void handleItem(Item item, Tile tile, Tile source){
        //TODO
        /*
        byte rotation = tile.getRotation();

        int ch = Math.abs(source.relativeTo(tile.x, tile.y) - rotation);
        int ang = ((source.relativeTo(tile.x, tile.y) - rotation));

        float pos = ch == 0 ? 0 : ch % 2 == 1 ? 0.5f : 1f;
        float y = (ang == -1 || ang == 3) ? 1 : (ang == 1 || ang == -3) ? -1 : 0;

        ConveyorEntity entity = tile.entity();
        entity.noSleep();
        long result = ItemPos.packItem(item, y * 0.9f, pos, (byte) Mathf.random(255));

        tile.entity.items.add(item, 1);

        for(int i = 0; i < entity.convey.size; i++){
            if(compareItems(result, entity.convey.get(i)) < 0){
                entity.convey.insert(i, result);
                entity.lastInserted = (byte)i;
                return;
            }
        }

        //this item must be greater than anything there...
        entity.convey.add(result);
        entity.lastInserted = (byte)(entity.convey.size-1);*/
    }

    @Override
    public TileEntity newEntity(){
        return new ConveyorEntity();
    }

    public static class ConveyorEntity extends TileEntity{
        ConveyorLine line;

        int blendshadowrot = -1;
        int blendbits;
        int blendsclx, blendscly;

        @Override
        public void write(DataOutputStream stream) throws IOException{
            //TODO write line if line seed == this tile
        }

        @Override
        public void read(DataInputStream stream) throws IOException{
            //TODO write line if line seed == this tile
        }
    }


}