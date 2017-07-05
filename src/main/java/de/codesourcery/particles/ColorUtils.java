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

public class ColorUtils {

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
