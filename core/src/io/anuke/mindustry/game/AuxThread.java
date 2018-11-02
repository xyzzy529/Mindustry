package io.anuke.mindustry.game;

import com.badlogic.gdx.utils.Array;
import io.anuke.mindustry.Vars;
import io.anuke.ucore.util.Threads;

public abstract class AuxThread implements Runnable{
    protected final Array<Runnable> runnables = new Array<>();
    protected final Array<Runnable> executedRunnables = new Array<>();
    protected final Thread thread = new Thread(this);

    protected boolean update;

    private final Object lock = new Object();
    private boolean alert;

    public AuxThread(){
        thread.setPriority(4);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run(){
        try{
            while(true){
                synchronized(lock){
                    while(!alert){
                        Threads.wait(lock);
                    }

                    synchronized(runnables){
                        for(int i = runnables.size - 1; i >= 0; i--)
                            executedRunnables.add(runnables.get(i));
                        runnables.clear();
                    }
                    if(executedRunnables.size == 0) return;
                    for(int i = executedRunnables.size - 1; i >= 0; i--)
                        executedRunnables.removeIndex(i).run();

                    alert = false;

                    update();
                }
            }
        }catch(Throwable t){
            Vars.control.setError(t);
        }
    }

    protected void update(){

    }

    public void post(Runnable runnable){
        synchronized (runnables){
            runnables.add(runnable);
        }

        synchronized(lock){
            alert = true;
            lock.notify();
        }
    }
}
