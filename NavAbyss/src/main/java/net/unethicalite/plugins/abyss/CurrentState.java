package net.unethicalite.plugins.abyss;

public class CurrentState {

    private int state;

    public CurrentState(){
        this.state = 0;
    }

    public int getState(){
        return state;
    }

    public int setState(int state){
        return this.state = state;
    }

    public void resetState(){
        state = 0;
    }
}
