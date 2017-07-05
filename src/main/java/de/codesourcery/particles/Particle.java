/**
 * Copyright 2017 Tobias Gierke <tobias.gierke@code-sourcery.de>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.codesourcery.particles;

public class Particle
{
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
    
    public Particle pos(float x,float y) {
        this.posx = x;
        this.posy = y;
        return this;
    }
    
    public void reset() 
    {
        posx=posy=0;
        ax=ay=0;
        vx=vy=0;
        color=0xffffffff;
        age=0;
        mark=false;
        state = State.DEAD;
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
        return "Particle ["+state+"], age "+age+" @ ("+posx+","+posy+")";
    }
}