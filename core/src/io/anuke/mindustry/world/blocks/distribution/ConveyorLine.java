package io.anuke.mindustry.world.blocks.distribution;

import com.badlogic.gdx.utils.LongArray;
import com.badlogic.gdx.utils.ObjectSet;
import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.distribution.Conveyor.ConveyorEntity;
import io.anuke.ucore.util.Bits;

import static io.anuke.mindustry.Vars.content;

public class ConveyorLine{
    private static final ItemPos drawpos = new ItemPos();
    private static final ItemPos pos1 = new ItemPos();
    private static final ItemPos pos2 = new ItemPos();

    private final Tile seed;
    private final LongArray items = new LongArray();
    private final ObjectSet<Tile> tiles = new ObjectSet<>();

    public ConveyorLine(Tile seed){
        this.seed = seed;
        add(seed);
    }

    public void update(Tile tile){
        //TODO
    }

    public void add(Tile tile){
        if(!tiles.add(tile)) return;

        ConveyorEntity entity = tile.entity();
        //merge lines when needed
        if(entity.line != null){
            //TODO merge line entities
            for(Tile other : entity.line.tiles){
                ConveyorEntity oe = other.entity();
                oe.line = this;
                tiles.add(other);
            }
        }
    }

    public void remove(Tile tile){
        if(!tiles.remove(tile)) return;

        //TODO split into multiple lines, remove items
    }

    private static int compareItems(long a, long b){
        return Float.compare(pos1.set(a, ItemPos.packShorts).y, pos2.set(b, ItemPos.packShorts).y);
    }

    //Container class. Do not instantiate.
    static class ItemPos{
        private static short[] packShorts = new short[4];
        private static short[] drawShorts = new short[4];
        private static short[] updateShorts = new short[4];

        Item item;
        float x, y;
        byte seed;

        private ItemPos(){
        }

        static long packItem(Item item, float x, float y, byte seed){
            short[] shorts = packShorts;
            shorts[0] = (short) item.id;
            shorts[1] = (short) (x * Short.MAX_VALUE);
            shorts[2] = (short) ((y - 1f) * Short.MAX_VALUE);
            shorts[3] = seed;
            return Bits.packLong(shorts);
        }

        ItemPos set(long lvalue, short[] values){
            Bits.getShorts(lvalue, values);

            if(values[0] >= content.items().size || values[0] < 0)
                item = null;
            else
                item = content.items().get(values[0]);

            x = values[1] / (float) Short.MAX_VALUE;
            y = ((float) values[2]) / Short.MAX_VALUE + 1f;
            seed = (byte) values[3];
            return this;
        }

        long pack(){
            return packItem(item, x, y, seed);
        }
    }
}
