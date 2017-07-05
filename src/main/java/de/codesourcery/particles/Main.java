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
    static final boolean SPAWN_NEW = true;

    private long lastCall;

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

        public MyPanel() 
        {
            this.system = new ParticleSystem(500000);
            this.outOfBoundsCheck = new OutOfBoundsVisitor(system,new Random(0xdeadbeef));
            system.setAnimator( this.outOfBoundsCheck );
            this.initializer = this.outOfBoundsCheck::init;
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
            outOfBoundsCheck.setBounds( getWidth(), getHeight() );

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

    private int frames;

    public void run() 
    {
        final MyPanel panel = new MyPanel();
        panel.setPreferredSize( new Dimension(640,480));

        final Main frame = new Main();
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        frame.getContentPane().add( panel );
        frame.pack();
        frame.setLocationRelativeTo( null );
        frame.setVisible( true );

        final Timer timer = new Timer(16, ev -> 
        {
            final long now = System.currentTimeMillis();
            if ( lastCall != 0 ) 
            {
                final float deltaInSeconds = (now - lastCall)/1000f;
                panel.system.tick( deltaInSeconds );
                long now2 = System.currentTimeMillis();
                frames++;
                if ( ( frames % 60 ) == 0 ) {
                    System.out.println("system tick(): "+(now2-now)+" , delta: "+(now-lastCall)+" ms, delta/seconds: "+deltaInSeconds);
                }
                panel.repaint();
            }
            lastCall = now;
        });
        timer.start();
    }
}