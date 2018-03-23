package testing;


import java.util.HashSet;
import java.util.Set;

import javafx.geometry.Bounds;
import javafx.scene.image.PixelReader;

public class Utils
{
    private static final String COMMA = ",";
    
    public static void fillPixelArray(int[][] pixelArray, PixelReader pixelReader)
    {
        for(int h = 0; h < pixelArray.length; h++)
        {
            for(int w = 0; w < pixelArray[h].length; w++)
            {
                pixelArray[h][w] = pixelReader.getArgb(w, h);
            }
        }
    }
    
    public static boolean hitDetecton(Bounds bounds1, Bounds bounds2, int[][] pixels1, int[][] pixels2)
    {
        boolean hit = bounds1.intersects(bounds2);
        
        if(hit)
        {
            hit = false;
            Set<String> set = new HashSet<>();
            int x1 = (int)bounds1.getMinX();
            int x2 = (int)bounds2.getMinX();
            int y1 = (int)bounds1.getMinY();
            int y2 = (int)bounds2.getMinY();
            int pixel;
            String point;
            
            for(int h = 0; h < pixels2.length; h++)
            {
                for(int w = 0; w < pixels2[h].length; w++)
                {
                    pixel = pixels2[h][w];
                    
                    if(pixel != 0)
                    {
                        point = (x2 + w) + COMMA + (y2 + h);
                        set.add(point);
                    }
                }
            }
            
            detecton: for(int h = 0; h < pixels1.length; h++)
            {
                for(int w = 0; w < pixels1[h].length; w++)
                {
                    pixel = pixels1[h][w];

                    if(pixel != 0)
                    {
                        point = (x1 + w) + COMMA + (y1 + h);

                        if(set.contains(point))
                        {
                            hit = true;
                            break detecton;
                        }
                    }
                }
            }
        }
        
        return hit;
    }
}