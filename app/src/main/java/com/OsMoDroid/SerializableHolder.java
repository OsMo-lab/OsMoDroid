package com.OsMoDroid;
import java.io.Serializable;
public class SerializableHolder implements Serializable
    {
        private Serializable content;
        public SerializableHolder(Serializable content)
            {
                this.content = content;
            }
        public Serializable get()
            {
                return content;
            }
    }