package de.codesourcery.particles;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GradientBuilder 
{
    public interface GradientEntry { }
    
    public final class GradientColor implements GradientEntry
    {
        public final int color;
        public GradientColor(int color) {
            this.color = color;
        }
        
        public int r() { return ColorHelper.r( this.color ); }
        public int g() { return ColorHelper.g( this.color ); }
        public int b() { return ColorHelper.b( this.color ); }
    }
    
    public final class GradientSteps implements GradientEntry
    {
        public final int stepCount;
        public GradientSteps(int stepCount) {
            if ( stepCount < 1 ) {
                throw new IllegalArgumentException("StepCount must be >= 1");
            }
            this.stepCount = stepCount;
        }
    }    
    
    private final List<GradientEntry> entries = new ArrayList<>();

    public GradientBuilder color(Color color) {
        return color( color.getRGB() );
    }
    
    public GradientBuilder color(int color) {
        this.entries.add( new GradientColor(color));
        return this;
    }
    
    public GradientBuilder steps(int stepCount) 
    {
        this.entries.add( new GradientSteps(stepCount));
        return this;
    }    
    
    public GradientBuilder reverse() {
        Collections.reverse( this.entries );
        return this;
    }
    
    public int[] build() 
    {
        final List<Integer> result = new ArrayList<>();
        GradientColor previousColor = null;
        for (int i = 0; i < entries.size(); i++) 
        {
            final GradientEntry entry = entries.get(i);
            if ( entry instanceof GradientColor) 
            {
                previousColor = (GradientColor) entry;
            }
            else if ( entry instanceof GradientSteps ) 
            {
                if ( previousColor == null ) {
                    throw new IllegalStateException("Encountered steps() without source color");
                }
                Optional<GradientColor> targetColor = Optional.empty(); 
                if ( i+1 < entries.size() ) 
                {
                    targetColor = entries.subList( i+1 , entries.size() ).stream().filter( e -> e instanceof GradientColor).map( e -> (GradientColor) e).findFirst();
                }
                if ( ! targetColor.isPresent() ) {
                    throw new IllegalStateException("Encountered steps() without target color");
                }
                float r0 = previousColor.r();
                float g0 = previousColor.g();
                float b0 = previousColor.b();
                
                final int r1 = targetColor.get().r();
                final int g1 = targetColor.get().g();
                final int b1 = targetColor.get().b();
                
                final float maxSteps = ((GradientSteps) entry).stepCount; 
                float dr = (r1 - r0) / maxSteps;
                float dg = (g1 - g0) / maxSteps;
                float db = (b1 - b0) / maxSteps;
                
                for ( int step = 0 ; step < maxSteps ; step++ ) 
                {
                    r0 += dr;
                    g0 += dg;
                    b0 += db;
                    final int color = ColorHelper.toColor( (int) Math.round( r0 ) , (int) Math.round( g0 ),(int) Math.round( b0 ) );
                    result.add( color);
                    previousColor = new GradientColor( color );
                }                
            } else {
                throw new RuntimeException("Internal error,unhandled entry: "+entry);
            }
        }
        return result.stream().mapToInt( Integer::intValue ).toArray();
    }
}
