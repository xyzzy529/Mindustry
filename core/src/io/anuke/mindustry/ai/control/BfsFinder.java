package io.anuke.mindustry.ai.control;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.Queue;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.world.Tile;
import io.anuke.ucore.function.Predicate;
import io.anuke.ucore.function.Supplier;

public class BfsFinder{

    public static Array<Tile> find(Tile start, Predicate<Tile> goal){
        Queue<Tile> queue = new Queue<>();
        //maps tile -> parent of tile
        IntIntMap parents = new IntIntMap();

        queue.addFirst(start);

        //yay, local functions?
        Tile result = ((Supplier<Tile>)() -> {
            while (queue.size > 0) {
                Tile tile = queue.removeFirst();
                for (int i = 0; i < 4; i++) {
                    Tile other = tile.getNearby(i);

                    if(other == null) continue;

                    if(goal.test(other) || (!other.solid() && !other.floor().isLiquid && parents.get(other.packedPosition(), -1) == -1 && other != start)){
                        queue.addLast(other);
                        parents.put(other.packedPosition(), tile.packedPosition());

                        if(goal.test(other)){
                            return other;
                        }
                    }
                }
            }
            return null;
        }).get();

        if(result == null) return null;

        Array<Tile> array = new Array<>();

        array.add(result);

        while(parents.get(result.packedPosition(), -1) != -1){
            array.add(result);
            result = Vars.world.tile(parents.get(result.packedPosition(), -1));
        }

        array.add(start);
        array.reverse();

        return array;

    }
}
