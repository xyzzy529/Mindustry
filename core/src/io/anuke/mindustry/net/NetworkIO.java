package io.anuke.mindustry.net;

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.badlogic.gdx.utils.TimeUtils;
import io.anuke.mindustry.entities.Player;
import io.anuke.mindustry.game.GameMode;
import io.anuke.mindustry.game.Team;
import io.anuke.mindustry.game.Teams;
import io.anuke.mindustry.game.Teams.TeamData;
import io.anuke.mindustry.game.Version;
import io.anuke.mindustry.io.SaveIO;
import io.anuke.mindustry.world.Tile;
import io.anuke.ucore.core.Core;
import io.anuke.ucore.core.Timers;
import io.anuke.ucore.entities.Entities;
import io.anuke.ucore.util.Bits;

import java.io.*;
import java.nio.ByteBuffer;

import static io.anuke.mindustry.Vars.*;

public class NetworkIO{

    public static void writeWorld(Player player, OutputStream os){

        try(DataOutputStream stream = new DataOutputStream(os)){

            stream.writeFloat(Timers.time()); //timer time
            stream.writeLong(TimeUtils.millis()); //timestamp

            //--GENERAL STATE--
            stream.writeByte(state.mode.ordinal()); //gamemode
            stream.writeInt(world.getSector() == null ? invalidSector : world.getSector().packedPosition()); //sector ID
            stream.writeInt(world.getSector() == null ? 0 : world.getSector().completedMissions);

            //write tags
            ObjectMap<String, String> tags = world.getMap().tags;
            stream.writeByte(tags.size);
            for(Entry<String, String> entry : tags.entries()){
                stream.writeUTF(entry.key);
                stream.writeUTF(entry.value);
            }

            stream.writeInt(state.wave); //wave
            stream.writeFloat(state.wavetime); //wave countdown

            stream.writeInt(player.id);
            player.write(stream);

            //--MAP DATA--
            SaveIO.getSaveWriter().write(stream);

            stream.write(Team.all.length);

            //write team data
            for(Team team : Team.all){
                TeamData data = state.teams.get(team);
                stream.writeByte(team.ordinal());

                stream.writeByte(data.enemies.size());
                for(Team enemy : data.enemies){
                    stream.writeByte(enemy.ordinal());
                }

                stream.writeByte(data.cores.size);
                for(Tile tile : data.cores){
                    stream.writeInt(tile.packedPosition());
                }
            }

            //now write a snapshot.
            player.con.viewX = world.width() * tilesize/2f;
            player.con.viewY = world.height() * tilesize/2f;
            player.con.viewWidth = world.width() * tilesize;
            player.con.viewHeight = world.height() * tilesize;
            netServer.writeSnapshot(player, stream);

        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    /**
     * Return whether a custom map is expected, and thus whether the client should wait for additional data.
     */
    public static void loadWorld(InputStream is){

        Player player = players[0];

        try(DataInputStream stream = new DataInputStream(is)){
            float timerTime = stream.readFloat();
            long timestamp = stream.readLong();

            Timers.clear();
            Timers.resetTime(timerTime + (TimeUtils.timeSinceMillis(timestamp) / 1000f) * 60f);

            //general state
            byte mode = stream.readByte();
            int sector = stream.readInt();
            int missions = stream.readInt();

            if(sector != invalidSector){
                world.sectors.createSector(Bits.getLeftShort(sector), Bits.getRightShort(sector));
                world.setSector(world.sectors.get(sector));
                world.getSector().completedMissions = missions;
            }

            ObjectMap<String, String> tags = new ObjectMap<>();

            byte tagSize = stream.readByte();
            for(int i = 0; i < tagSize; i++){
                String key = stream.readUTF();
                String value = stream.readUTF();
                tags.put(key, value);
            }

            int wave = stream.readInt();
            float wavetime = stream.readFloat();

            state.wave = wave;
            state.wavetime = wavetime;
            state.mode = GameMode.values()[mode];

            Entities.clear();
            int id = stream.readInt();
            player.resetNoAdd();
            player.read(stream, TimeUtils.millis());
            player.resetID(id);
            player.add();

            world.beginMapLoad();
            SaveIO.getSaveWriter().readMap(stream);

            state.teams = new Teams();

            byte teams = stream.readByte();
            for(int i = 0; i < teams; i++){
                Team team = Team.all[stream.readByte()];

                byte enemies = stream.readByte();
                Team[] enemyArr = new Team[enemies];
                for(int j = 0; j < enemies; j++){
                    enemyArr[j] = Team.all[stream.readByte()];
                }

                state.teams.add(team, enemyArr);

                byte cores = stream.readByte();

                for(int j = 0; j < cores; j++){
                    state.teams.get(team).cores.add(world.tile(stream.readInt()));
                }

                if(team == players[0].getTeam() && cores > 0){
                    Core.camera.position.set(state.teams.get(team).cores.first().drawx(), state.teams.get(team).cores.first().drawy(), 0);
                }
            }

            world.endMapLoad();

            //read raw snapshot
            netClient.readSnapshot(stream);

        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    public static ByteBuffer writeServerData(){
        int maxlen = 32;

        String host = (headless ? "Server" : players[0].name);
        String map = world.getMap().name;

        host = host.substring(0, Math.min(host.length(), maxlen));
        map = map.substring(0, Math.min(map.length(), maxlen));

        ByteBuffer buffer = ByteBuffer.allocate(128);

        buffer.put((byte) host.getBytes().length);
        buffer.put(host.getBytes());

        buffer.put((byte) map.getBytes().length);
        buffer.put(map.getBytes());

        buffer.putInt(playerGroup.size());
        buffer.putInt(state.wave);
        buffer.putInt(Version.build);
        buffer.put((byte)Version.type.getBytes().length);
        buffer.put(Version.type.getBytes());
        return buffer;
    }

    public static Host readServerData(String hostAddress, ByteBuffer buffer){
        byte hlength = buffer.get();
        byte[] hb = new byte[hlength];
        buffer.get(hb);

        byte mlength = buffer.get();
        byte[] mb = new byte[mlength];
        buffer.get(mb);

        String host = new String(hb);
        String map = new String(mb);

        int players = buffer.getInt();
        int wave = buffer.getInt();
        int version = buffer.getInt();
        byte tlength = buffer.get();
        byte[] tb = new byte[tlength];
        buffer.get(tb);
        String vertype = new String(tb);

        return new Host(host, hostAddress, map, wave, players, version, vertype);
    }
}
