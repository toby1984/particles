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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ImageToVectors {

    public static List<Vec2d> load(File file,float timeToDisplay)
    {
        final BufferedImage image;
        try {
            image = ImageIO.read( file );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final List<Vec2d> result =  scanImage(image,timeToDisplay);
        System.out.println("Shape has "+result.size()+" points");
        return result;
    }

    private static List<Vec2d> scanImage(BufferedImage image,float timeToDisplay) 
    {
        final List<Vec2d> result = new ArrayList<>();
        final int cx = image.getWidth()/2;
        final int cy = image.getHeight()/2;

        final float yMovement = -9.81f*timeToDisplay;
        for ( int x = 0,maxX = image.getWidth() ; x < maxX; x++ ) 
        {
            for ( int y = 0,maxY = image.getHeight() ; y < maxY; y++ ) 
            {
                final int color = image.getRGB(x,y) & 0xffffff;
                if ( color == 0 ) 
                {
                    float dx = x - cx;
                    float dy = cy - y-2*yMovement;
                    float vx = dx/timeToDisplay;
                    float vy = dy/timeToDisplay;
                    result.add( new Vec2d(vx,vy) );
                }
            }
        }
        return result;
    }

    public static List<Vec2d> load(String text,float timeToDisplay)
    {
        final BufferedImage image = render( text , 128 , 64 );
        final List<Vec2d> result =  scanImage(image,timeToDisplay);
        System.out.println("Shape has "+result.size()+" points");
        return result;
    }
    
    private static BufferedImage render(String s,int width,int height) 
    {
        final BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_BYTE_BINARY);
        Graphics2D g2 = image.createGraphics();

        g2.setColor( Color.WHITE );
        g2.fillRect( 0 , 0 , width, height );
        
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

        final FontRenderContext frc = g2.getFontRenderContext();
        final Font f = new Font("Helvetica", 1, 28);
        final TextLayout textTl = new TextLayout(s, f, frc);
        AffineTransform transform = new AffineTransform();
        final Shape outline = textTl.getOutline(null);
        final Rectangle outlineBounds = outline.getBounds();
        transform = g2.getTransform();
        transform.translate(width / 2 - (outlineBounds.width / 2), height / 2 + (outlineBounds.height / 2));
        g2.transform(transform);
        g2.setColor(Color.BLACK);
        g2.draw(outline);
        g2.setClip(outline);
        g2.dispose();
        return image;
    }
    
    public static void main(String[] args) {
        
        final JPanel panel = new JPanel() {
            
            private final BufferedImage image;
            
            {
                image = render( "TEST", 128,64 );
            }
            
            @Override
            protected void paintComponent(Graphics g) 
            {
                super.paintComponent( g );
                g.drawImage( image  , 0 , 0 , null );
            }
        };
        
        panel.setPreferredSize( new Dimension(640,480));
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add( panel );
        frame.pack();
        frame.setLocationRelativeTo( null );
        frame.setVisible(true);
    }
}
