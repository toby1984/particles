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

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

public class PerformanceTest 
{
    private ParticleSystem system;
    private OutOfBoundsVisitor animator;
    
    @Test
    public void test() {

        system = new ParticleSystem( 500000 );
        animator = new OutOfBoundsVisitor( system );
        system.setAnimator( animator );        
        animator.setBounds(640, 480);
        
        for ( int i = 0 ; i < 3 ; i++ ) 
        {
            system.reset();
            animator.rnd.setSeed( 0xdeadbeef );
            animator.resetStatistics();
            long time1 = System.currentTimeMillis();
            final int tickLimit = 200;
            for ( long tick = 0 ; tick < tickLimit ; tick++) 
            {
                system.tick( 0.016f );
                if ( system.allDead() ) 
                {
                    System.out.println("Init @ "+tick);
                    animator.init( system );
                }                
            }
            long time2 = System.currentTimeMillis();
            final long ms = (time2-time1) / tickLimit;
            System.out.println("ms per tick: "+ms);
            System.out.println("spawned / killed: "+animator.particlesSpawned+" / "+animator.particlesKilled);
        }
    }
}
