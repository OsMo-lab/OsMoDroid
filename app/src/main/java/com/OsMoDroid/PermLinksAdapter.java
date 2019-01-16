package com.OsMoDroid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

public class PermLinksAdapter extends ArrayAdapter<PermLink>

{
    private final LocalService localservice;

    public PermLinksAdapter(Context context, int textViewResourceId, List<PermLink> objects, LocalService localservice)
    {
        super(context, textViewResourceId, objects);
        this.localservice = localservice;
    }
    View.OnClickListener myCheckChangList = new View.OnClickListener()
    {
        public void onClick(View v)
        {
            ((CheckBox) v).toggle();
            PermLink permLink = getItem((Integer) v.getTag());

            if (permLink.active)
            {
                localservice.myIM.sendToServer("LD:" + permLink.u, true);
            }
            else
            {
                localservice.myIM.sendToServer("LA:" + permLink.u, true);
            }
        }
    };
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        View row = convertView;
        if (row == null)
        {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.channelsitem, parent, false);
        }
        PermLink permLink = getItem(position);
        TextView channelName = (TextView) row.findViewById(R.id.txtName);
        TextView channelCreated = (TextView) row.findViewById(R.id.txtCreated);
        CheckBox tg = (CheckBox) row.findViewById(R.id.checkBox);
        tg.setOnClickListener(myCheckChangList);
        tg.setTag(position);
        channelName.setText(permLink.toString());
        tg.setChecked(permLink.active);
        //channelName.setTextColor(Color.BLACK);
        return row;
    }
}
