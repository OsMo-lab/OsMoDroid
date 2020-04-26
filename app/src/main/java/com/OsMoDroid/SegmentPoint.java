package com.OsMoDroid;

import org.osmdroid.util.PointL;

/**
 * Created by 1 on 25.12.2016.
 */
public class SegmentPoint extends PointL
    {
        public int segment;
        public SegmentPoint(long x, long y, int segment) {
            this.x = x;
            this.y = y;
            this.segment = segment;
        }
    }
