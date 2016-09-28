package com.OsMoDroid;
public class PermLink
    {
        int u;
        String url;
        String description;
        @Override
        public String toString()
            {
                // TODO Auto-generated method stub
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
