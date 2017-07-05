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

import java.awt.Color;
import java.util.List;
import java.util.Random;

public final class OutOfBoundsVisitor implements ParticleSystem.IAnimator 
{
    public static final int[] colors = new GradientBuilder().color( Color.WHITE ).steps( 30 ).color( Color.RED ).steps( 30 ).color( Color.YELLOW ).steps( 50 ).color( Color.BLACK ).reverse().build();
    
    public final Random rnd;
    
    public int particlesSpawned = 0;
    public int particlesKilled = 0;
    
    private int shapeIdx;
    public List<List<Vec2d>> customShapes = null;
    
    private final ParticleSystem system;
    private float pxmin,pxmax,pymin,pymax;
    
    private int spawnCount;
    private float elapsedTime;
    
    private long particleCount;
    
    public OutOfBoundsVisitor(ParticleSystem system) {
        this(system,new Random() );
    }
    
    public OutOfBoundsVisitor(ParticleSystem system,Random rnd) {
        this.system = system;
        this.rnd = rnd;
    }    
    
    public void resetStatistics() {
        particlesSpawned = 0;
        particlesKilled = 0;
    }
    
    public void setBounds(int width,int height) 
    {
        pxmin = -width/2;
        pxmax =  width/2;
        pymin = -height/2;
        pymax =  height/2;             
    }
    
    public boolean isOutOfBounds(Particle p) {
        return p.posx < pxmin || p.posy < pymin || p.posx > pxmax || p.posy > pymax; 
    }
    
    @Override
    public void beforeVisitingParticles(float deltaSeconds) 
    {
        elapsedTime+=deltaSeconds;
        if ( elapsedTime > 0.15f) {
            elapsedTime -= 0.15f;
            if ( spawnCount > 0 ) {
                spawnCount--;
            }
        }
    }

    @Override
    public void tick(Particle particle, float deltaSeconds)
    {
        particleCount++;
        particle.move( deltaSeconds );
        particle.age--;
        if ( particle.age < 0 || isOutOfBounds( particle ) )
        {
            particlesKilled++;
            particle.kill();
            return;
        } 
        if ( particle.age > 0 ) {
            particle.color = colors[ particle.age ];
        }
        if ( (particleCount%10000) == 0 && spawnCount < 10) 
        {
            if ( spawnChildren(particle) ) {
                spawnCount++;
            }
        }
    }

    protected boolean spawnChildren(Particle parent) 
    {
        boolean success = false;
        if ( customShapes != null ) 
        {
            final List<Vec2d> customShape = customShapes.get( shapeIdx );
            shapeIdx = (shapeIdx+1) % customShapes.size();
            for ( int count = customShape.size()-1 ; count >= 0 ; count-- ) 
            {
                final Vec2d displacement = customShape.get(count);
                final Particle child = system.claimDeadParticle();
                if ( child == null ) 
                {
                    break;
                }
                
                success = true;
                
                final float px = parent.posx;
                final float py = parent.posy;
                final float ax = 0;
                final float ay = -9.81f*5;
                final float vx = parent.vx+displacement.x;
                final float vy = parent.vy+displacement.y;
                final int maxAge = colors.length-1;
                
                particlesSpawned++;
                child.instantiate().pos( px , py ).acceleration( ax,  ay ).color( colors[0] ).speed( vx , vy ).age( maxAge );                
            }
        } 
        else 
        {
            System.out.println("No custom shape");
            for ( int count = 50 ; count >= 0 ; count-- ) 
            {
                final Particle child = system.claimDeadParticle();
                if ( child == null ) 
                {
                    break;
                }
                spawnChild( parent , child );
                success = true;
            }
        }
        return success;
    }

    private void spawnChild(Particle parent,Particle child) 
    {
        initParticle( child );
        child.posx = parent.posx;
        child.posy = parent.posy;
        child.vx += parent.vx;
        child.vy += parent.vy;
    }
    
    public void init(ParticleSystem system) 
    {
        system.visitDeadParticles( particle -> 
        {
            initParticle( particle );
            particle.age( colors.length - 1  );
        }, 10000 );
    }

    private void initParticle(Particle particle)
    {
        final float px = (rnd.nextFloat()-0.5f)*20;
        final float py = (rnd.nextFloat()-0.5f)*20;
        final float ax = 0;
        final float ay = -9.81f*5;
        final float vx = (rnd.nextFloat()-0.5f)*200;
        final float vy = (rnd.nextFloat()-0.5f)*200;    
        final int maxAge = colors.length-1;
        
        particlesSpawned++;
        particle.instantiate().pos( px , py ).acceleration( ax,  ay ).color( colors[0] ).speed( vx , vy ).age( maxAge );
    }    
}