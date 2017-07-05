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

import org.apache.commons.lang3.Validate;

import de.codesourcery.particles.Particle.State;

public class ParticleSystem
{
    private final Particle[] particles;
    private final Particle[] alive;
    private final Particle[] dead;
    
    private int lastDeadIdx=-1;
    private int aliveCounter;
    private IAnimator animator;
    
    public interface IAnimator 
    {
        public void beforeVisitingParticles(float deltaSeconds);
        
        public void tick(Particle p,float deltaSeconds);
    }
    
    public void reset() 
    {
        Arrays.fill(alive , null );
        Arrays.fill(dead , null );
        aliveCounter=0;
        lastDeadIdx=-1;
        for ( int i = particles.length-1; i >= 0 ; i-- ) 
        {
            this.particles[i].reset();
        }        
    }
    
    public ParticleSystem(int size) 
    {
        if ( size < 1 ) {
            throw new IllegalArgumentException("Size must be >= 1");
        }
        this.particles = new Particle[ size ];
        this.alive = new Particle[ size+1 ];
        this.dead = new Particle[ size+1 ];
        for ( int i = size-1 ; i >= 0 ; i-- ) 
        {
            this.particles[i] = new Particle();
        }
    }
    
    public void setAnimator(IAnimator animator) 
    {
        Validate.notNull(animator,"animator must not be NULL");
        this.animator = animator;
    }
    
    public void tick(float deltaSeconds) 
    {
        synchronized(particles) 
        {
            this.animator.beforeVisitingParticles( deltaSeconds );
            
            int alivePtr=0,deadPtr=0;
            for (int i = particles.length-1 ; i >= 0 ; i--) 
            {
                final Particle p = particles[i];
                final State currentState = p.getState();
                if ( currentState.isTransient() )
                {
                    p.doStateTransition();
                    if ( p.isDead() ) 
                    {
                        p.mark( false ).age(100);
                    } 
                }
                
                if ( p.isAlive() ) 
                {
                    this.alive[ alivePtr++ ] = p;  
                    this.animator.tick( p , deltaSeconds );
                } else {
                    this.dead[ deadPtr++ ] = p;                    
                }
            }
            // write end marker (only save because we allocated the arrays with size+1 in the constructor)
            this.lastDeadIdx = deadPtr-1;
            this.dead[ deadPtr ] = null;
            this.alive[ alivePtr ] = null;
            this.aliveCounter = alivePtr;
        }
    }
    
    public boolean allDead() 
    {
        return getAliveCount() == 0 ;
    }

    public int getAliveCount()
    {
        synchronized(particles) 
        {
            return aliveCounter;
        }
    }
    
    public interface IVisitor {
        
        public void visit(Particle particle);
    }
    
    public void visitDeadParticles(IVisitor visitor,int maxVisits)
    {
        if ( maxVisits < 1 ) {
            throw new IllegalArgumentException("maxVisits must be >= 1");
        }
        synchronized(particles) 
        {
            for (int i = particles.length-1 , ptr=0 , visitsRemaining = maxVisits ; i >= 0; i--,ptr++) 
            {
                final Particle particle = dead[ptr];
                if ( particle == null ) {
                    break;
                }
                visitsRemaining--;
                visitor.visit( particle ); 
                if ( visitsRemaining == 0 ) 
                { 
                    break;
                }
            }
        }
    }
    
    public Particle claimDeadParticle() 
    {
        synchronized(particles) 
        {
            if ( lastDeadIdx >= 0 ) 
            { 
                return dead[lastDeadIdx--];
            }
        }
        return null;
    }

    public void visitAliveParticles(IVisitor visitor)
    {
        synchronized(particles) 
        {
            for (int i = 0,len=alive.length ; i < len ; i++ )
            {
                final Particle particle = alive[i];
                if ( particle == null ) {
                    return;
                }
                visitor.visit( particle );
            }
        }
    }
    
    public void visitDeadParticles(IVisitor visitor)
    {
        synchronized(particles) 
        {
            for (int i = 0 , len = dead.length ; i < len ; i++) 
            {
                final Particle particle = particles[i];
                if ( particle == null ) 
                { 
                    return;
                }
                visitor.visit( particle );
            }
        }
    }    
}