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
        
        public int r() { return ColorUtils.r( this.color ); }
        public int g() { return ColorUtils.g( this.color ); }
        public int b() { return ColorUtils.b( this.color ); }
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
                    final int color = ColorUtils.toColor( (int) Math.round( r0 ) , (int) Math.round( g0 ),(int) Math.round( b0 ) );
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
