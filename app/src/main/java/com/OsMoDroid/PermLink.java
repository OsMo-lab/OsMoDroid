package com.OsMoDroid;
public class PermLink
    {
        int u;
        String url;
        String description;
        int timeout;
        long start;
        long finish;
        long time;
        int count;
        boolean active;
        @Override
        public String toString()
            {

                if(description.equals(""))
                    {
                        return url;
                    }
                else
                    {
                        return description + ":\n" + url;
                    }
            }
    }
