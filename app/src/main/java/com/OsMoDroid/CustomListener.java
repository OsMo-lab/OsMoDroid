package com.OsMoDroid;
import android.app.Dialog;
import android.view.View;
/**
 * Created by 1 on 23.10.2015.
 */
class CustomListener implements View.OnClickListener {
    final Dialog dialog;
    public CustomListener(Dialog dialog) {
        this.dialog = dialog;
    }
    @Override
    public void onClick(View v) {

        // Do whatever you want here

        // If tou want to close the dialog, uncomment the line below
        //dialog.dismiss();
    }
}