package de.codesourcery.particles;

public class ColorHelper {

    public static int r(int color) {
        return (color & 0xff0000) >> 16;
    }
    
    public static int g(int color) {
        return (color & 0xff00) >> 8;
    }
    
    public static int b(int color) {
        return color & 0xff;
    }
    
    private static int clamp(int c) 
    {
        return c < 0 ? 0 : c > 255 ? 255 : c;
    }
    
    public static int toColor(int r,int g,int b) 
    {
        return 0xff000000 | clamp(r) << 16 | clamp(g) << 8 | clamp(b);
    }
} 
