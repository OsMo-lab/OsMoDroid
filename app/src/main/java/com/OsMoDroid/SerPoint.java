package com.OsMoDroid;
import android.graphics.Point;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
/**
 * Created by 1 on 23.12.2015.
 */
class SerPoint implements Serializable
    {


        private int x;
         private int y;
       transient Point point;

        SerPoint(Point p)
            {
                this.x=p.x;
                this.y=p.y;
                this.point=new Point(p);
            }
        private void readObject(ObjectInputStream input)
                throws IOException, ClassNotFoundException {
            // deserialize the non-transient data members first;
            input.defaultReadObject();
            // Read the color
            point= new Point(x,y);
        }

    }
