package de.codesourcery.particles;

public class Particle
{
    public final int id;
    
    public enum State 
    {
        DYING 
        {
            @Override
            public State create()
            {
                return INITIALIZING;
            }

            @Override
            public State transition()
            {
                return DEAD;
            }
        },
        DEAD {
            @Override
            public State create()
            {
                return State.INITIALIZING;
            }
        },
        INITIALIZING {

            @Override
            public State die()
            {
                return DYING;
            }

            @Override
            public State transition()
            {
                return ALIVE;
            }
        },
        ALIVE {

            @Override
            public State die()
            {
                return State.DYING;
            }
        };
        
        public boolean isTransient() {
            return this == INITIALIZING || this == State.DYING;
        }
        
        public State die() {
            return this;
        }
        
        public State create() {
            return this;
        }
        
        public State transition() {
            return this;
        }
    }
    
    public float posx;
    public float posy;
    public float ax;
    public float ay;
    public float vx;
    public float vy;
    public int color = 0xffffff;
    public int age;
    private boolean mark;
    
    private State state = State.DEAD;
    
    public Particle(int id) {
        this.id = id;
    }
    
    public Particle pos(float x,float y) {
        this.posx = x;
        this.posy = y;
        return this;
    }
    
    public boolean isMarked() {
        return mark;
    }
    
    public Particle mark(boolean yesNo) {
        this.mark = yesNo;
        return this;
    }
    
    public Particle age(int age) {
        this.age = age;
        return this;
    }
    
    public Particle speed(float x,float y) {
        this.vx = x;
        this.vy = y;
        return this;
    }    
    
    public Particle acceleration(float x,float y) {
        this.ax = x;
        this.ay = y;
        return this;
    }      
    
    public Particle color(int c) {
        this.color = c;
        return this;
    }      
    
    public boolean isAlive()
    {
        return state == State.ALIVE;
    }
    
    public boolean isDead() {
        return state == State.DEAD;
    }
    
    public Particle instantiate() 
    {
        this.state = this.state.create();
        return this;
    }
    
    public void kill() 
    {
        this.state = this.state.die();
    }
    
    public State getState() 
    {
        return this.state;
    }
    
    public void doStateTransition() {
        this.state = this.state.transition();
    }
    
    public void move(float deltaMs) 
    {
        vx += ax*deltaMs;
        vy += ay*deltaMs;
        
        posx += vx * deltaMs; 
        posy += vy * deltaMs; 
    }
    
    @Override
    public String toString()
    {
        return "Particle #"+id+"["+state+"], age "+age+" @ ("+posx+","+posy+")";
    }
}