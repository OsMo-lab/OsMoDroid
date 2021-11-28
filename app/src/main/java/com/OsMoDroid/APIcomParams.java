package com.OsMoDroid;

import androidx.core.app.NotificationCompat.Builder;

import java.io.File;
public class APIcomParams
    {
        //	String[] params = {"http://apim.esya.ru/?query="+action +";&key="+settings.getString("key", ""),"false","","APIM"};
        String action;
        String post;
        String command;
        File uploadfile;
        Builder notificationBuilder;
        int notification;
        ColoredGPX load;
        String downloadfilename;
        public APIcomParams(String action, String post, String command, File uploadfile, Builder notificationBuilder, int notificationid)
            {
                this.action = action;
                this.post = post;
                this.command = command;
                this.uploadfile = uploadfile;
                this.notificationBuilder = notificationBuilder;
                this.notification = notificationid;
            }
        public APIcomParams(String action, String post, String command)
            {
                this.action = action;
                this.post = post;
                this.command = command;
            }
        public APIcomParams(String url, ColoredGPX load)
            {
                this.action = url;
                this.load = load;
            }
        public APIcomParams(String url, String filename)
        {
            this.action = url;
            this.downloadfilename = filename;
        }
    }
