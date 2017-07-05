package de.codesourcery.particles;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;
import java.util.function.Consumer;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class Main extends JFrame 
{
    private static final boolean SPAWN_NEW = true;
    
    private long lastCall;

    private final Random rnd = new Random(System.currentTimeMillis());

    private class MyPanel extends JPanel 
    {
        private final OutOfBoundsVisitor outOfBoundsCheck;
        private final ParticleSystem system;
        private final Consumer<ParticleSystem> initializer;

        private int cx;
        private int cy;

        private BufferedImage buffer;
        private Graphics2D bufferGfx; 
        private int[] pixels;
        private int width;
        private int maxOffset;        
        
        private int frames;
        
        public class OutOfBoundsVisitor extends ParticleSystem.Animator 
        {
            private float pxmin,pxmax,pymin,pymax;

            public void setup()
            {
                pxmin = -getWidth()/2;
                pxmax =  getWidth()/2;

                pymin = -getHeight()/2;
                pymax =  getHeight()/2;                
            }

            public boolean isOutOfBounds(Particle p) {
                return p.posx < pxmin || p.posy < pymin || p.posx > pxmax || p.posy > pymax; 
            }

            @Override
            public void tick(Particle particle, float deltaMs)
            {
                particle.age--;
                particle.move( deltaMs );
                
                if ( particle.age < 0 || isOutOfBounds( particle ) )
                {
                    particle.kill();
                }  
                else if ( SPAWN_NEW && particle.age >= 30 && ! particle.isMarked() ) 
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
        }

        public MyPanel(Consumer<ParticleSystem> consumer) 
        {
            this.initializer = consumer;
            this.outOfBoundsCheck = new OutOfBoundsVisitor();
            this.system = new ParticleSystem(outOfBoundsCheck,500000);            
        }

        private void setupBuffer() 
        {
            if ( buffer == null || buffer.getWidth() != getWidth() || buffer.getHeight() != getHeight() ) 
            {
                this.width = getWidth();
                this.maxOffset = getHeight()*width;
                if ( bufferGfx != null ) {
                    bufferGfx.dispose();
                    bufferGfx = null;
                }
                buffer = new BufferedImage( getWidth() , getHeight() , BufferedImage.TYPE_INT_ARGB );
                bufferGfx = buffer.createGraphics();
                pixels = ((DataBufferInt) buffer.getRaster().getDataBuffer()).getData();
            }
            bufferGfx.clearRect( 0 , 0 , buffer.getWidth() , buffer.getHeight() );
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            frames++;
            long now = System.currentTimeMillis();
            
            cx = getWidth()/2;
            cy = getHeight()/2;

            setupBuffer();

            outOfBoundsCheck.setup();

            long now2 = System.currentTimeMillis();
            
            if ( system.allDead() ) {

                System.out.println("All dead");
                initializer.accept( system );
            } 

            system.visitAliveParticles( this::renderParticle );

            g.drawImage(buffer , 0 , 0 , null );
            
            if ( (frames%60) == 0 ) {
                long now3 = System.currentTimeMillis();
                System.out.println("Timing: total "+(now3-now)+" ms, rendering "+(now3-now2)+" ms, OOB check: "+(now2-now)+" ms , alive: "+system.getAliveCount());
            }
            //            g.setColor(Color.RED);
            //            g.drawString("Alive: "+system.getAliveCount() , 15 ,25 );
        }

        public boolean renderParticle(Particle p) 
        {
            int px = (int) (p.posx + cx);
            int py = (int) (cy - p.posy);

            final int offset = px + py * width;
            if ( offset > 0 && offset < maxOffset ) {
                pixels[offset] = p.color;
            }
            return true;
        }
    }

    public static void main(String[] args) throws InvocationTargetException, InterruptedException
    {
        SwingUtilities.invokeAndWait( () -> new Main().run() ); 
    }

    private void spawnChild(Particle parent,Particle child) 
    {
        initParticle( child );
        child.posx = parent.posx;
        child.posy = parent.posy;
        child.vx += parent.vx;
        child.vy += parent.vy;
    }
    
    private void init(ParticleSystem system) 
    {
        system.visitDeadParticles( particle -> {
            this.initParticle( particle );
            particle.age( 600 );
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
        final int color = 0xff000000 | ( rnd.nextInt() & 0x00ffffff);
        final int maxAge = 100 + (int) (rnd.nextFloat()*20);
        
        particle.instantiate().pos( px , py ).acceleration( ax,  ay ).color( color ).speed( vx , vy ).age( maxAge );
    }

    private int frames;

    public void run() 
    {
        final MyPanel panel = new MyPanel( this::init );
        panel.setPreferredSize( new Dimension(320,200));

        final Main frame = new Main();
        frame.setPreferredSize( new Dimension(320,200));
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.getContentPane().add( panel );
        frame.pack();
        frame.setVisible( true );

        final Timer timer = new Timer(16, ev -> 
        {
            final long now = System.currentTimeMillis();
            if ( lastCall != 0 ) 
            {
                final float deltaMs = (now - lastCall)/1000f;
                panel.system.tick( deltaMs );
                long now2 = System.currentTimeMillis();
                frames++;
                if ( ( frames % 60 ) == 0 ) {
                    System.out.println("system tick(): "+(now2-now)+" , delta: "+(now-lastCall)+" ms");
                }
                panel.repaint();
//                Toolkit.getDefaultToolkit().sync();
            }
            lastCall = now;
        });
        timer.start();
}
}