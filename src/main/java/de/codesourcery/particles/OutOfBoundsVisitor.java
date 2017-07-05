package de.codesourcery.particles;

import java.awt.Color;
import java.util.Random;

public final class OutOfBoundsVisitor implements ParticleSystem.IAnimator 
{
    public static final int[] colors = new GradientBuilder().color( Color.WHITE ).steps( 30 ).color( Color.RED ).steps( 30 ).color( Color.YELLOW ).steps( 50 ).color( Color.BLACK ).reverse().build();
    
    public final Random rnd;
    
    public int particlesSpawned = 0;
    public int particlesKilled = 0;
    
    private final ParticleSystem system;
    private float pxmin,pxmax,pymin,pymax;
    
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
    public void tick(Particle particle, float deltaSeconds)
    {
        particle.move( deltaSeconds );
        particle.age--;
        if ( particle.age < 0 || isOutOfBounds( particle ) )
        {
            particlesKilled++;
            particle.kill();
        }
        if ( particle.age > 0 ) {
            particle.color = colors[ particle.age ];
        }
        if ( Main.SPAWN_NEW && particle.age >= 30 && ! particle.isMarked() ) 
        {
            final boolean fork = particle.age%3 == 0;
            if ( fork ) 
            {
                boolean success = false;
                for ( int count = 50 ; count >= 0 ; count-- ) 
                {
                    final Particle child = system.claimDeadParticle();
                    if ( child == null ) 
                    {
                        break;
                    }
                    spawnChild( particle , child );
                    success = true;
                }
                if ( success ) 
                {
                    particle.mark(true);
                }
            } 
        }
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
        }, 10 );
    }

    private void initParticle(Particle particle)
    {
        final float px = (rnd.nextFloat()-0.5f)*20;
        final float py = (rnd.nextFloat()-0.5f)*20;
        final float ax = 0;
        final float ay = -9.81f*5;
        final float vx = (rnd.nextFloat()-0.5f)*100;
        final float vy = (rnd.nextFloat()-0.5f)*100;    
        final int maxAge = colors.length-1;
        
        particlesSpawned++;
        particle.instantiate().pos( px , py ).acceleration( ax,  ay ).color( colors[0] ).speed( vx , vy ).age( maxAge );
    }    
}