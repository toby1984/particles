package de.codesourcery.particles;

import de.codesourcery.particles.Particle.State;

public class ParticleSystem
{
    private final Particle[] particles;
    private final Particle[] alive;
    private final Particle[] dead;
    
    private int lastDeadIdx;
    private int aliveCounter;
    private final Animator animator;
    
    public static abstract class Animator 
    {
        public abstract void tick(Particle p,float deltaMs);
    }
    
    public ParticleSystem(Animator animator,int size) 
    {
        if ( size < 1 ) {
            throw new IllegalArgumentException("Size must be >= 1");
        }
        this.particles = new Particle[ size ];
        this.alive = new Particle[ size+1 ];
        this.dead = new Particle[ size+1 ];
        for ( int i = size-1 ; i >= 0 ; i-- ) 
        {
            this.particles[i] = new Particle(i);
        }
        this.animator = animator;
    }
    
    public void tick(float deltaMs) 
    {
        synchronized(particles) 
        {
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
                    this.animator.tick( p , deltaMs );
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