package io.anuke.mindustry.maps;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.ObjectMap;
import io.anuke.ucore.function.Supplier;

import java.io.InputStream;

public class Map{
    /** Internal map name. This is the filename, without any extensions.*/
    public final String name;
    /** Whether this is a custom map.*/
    public final boolean custom;
    /** Metadata. Author description, display name, etc.*/
    public final ObjectMap<String, String> tags;
    /** Supplies a new input stream with the data of this map.*/
    public final Supplier<InputStream> stream;
    /** Preview texture.*/
    public Texture texture;

    public Map(String name, ObjectMap<String, String> tags, boolean custom, Supplier<InputStream> streamSupplier){
        this.name = name;
        this.custom = custom;
        this.tags = tags;
        this.stream = streamSupplier;
    }

    public Map(String unknownName){
        this(unknownName, new ObjectMap<>(), true, () -> null);
    }

    public String description(){
        return tags.get("description", "");
    }

    public String author(){
        return tags.get("author", "");
    }

    public boolean hasOreGen(){
        return tags.get("oregen", "0").equals("1");
    }

    public String getDisplayName(){
        return tags.get("name", name);
    }
}
