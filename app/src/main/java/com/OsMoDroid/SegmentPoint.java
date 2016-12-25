package com.OsMoDroid;
import android.graphics.Point;
/**
 * Created by 1 on 25.12.2016.
 */
public class SegmentPoint extends Point
    {
        public int segment;
        public SegmentPoint(int x, int y, int segment) {
            this.x = x;
            this.y = y;
            this.segment = segment;
        }
    }
