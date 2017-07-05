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

import java.util.Arrays;

import de.codesourcery.particles.Particle.State;

public class ParticleArray 
{
    public final float[] posx;
    public final float[] posy;
    public final float[] ax;
    public final float[] ay;
    public final float[] vx;
    public final float[] vy;
    public final int[] color;
    public final int[] age;
    public final boolean[] mark;
    public final State[] state;
    
    public ParticleArray(int size) 
    {
        this.posx = new float[size];
        this.posy = new float[size];
        this.ax = new float[size];
        this.ay = new float[size];
        this.vx = new float[size];
        this.vy = new float[size];
        this.color = new int[size];
        this.age = new int[size];
        this.mark = new boolean[size];
        this.state = new State[size];
        Arrays.fill( state , State.DEAD );
    }
}
