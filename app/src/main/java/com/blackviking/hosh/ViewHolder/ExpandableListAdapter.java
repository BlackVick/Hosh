package com.blackviking.hosh.ViewHolder;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.blackviking.hosh.R;

import java.util.HashMap;
import java.util.List;

public class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> listFaqTitle;
    private HashMap<String, List<String>> listHashMap;

    public ExpandableListAdapter(Context context, List<String> listFaqTitle, HashMap<String, List<String>> listHashMap) {
        this.context = context;
        this.listFaqTitle = listFaqTitle;
        this.listHashMap = listHashMap;
    }

    @Override
    public int getGroupCount() {
        return listFaqTitle.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return listHashMap.get(listFaqTitle.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listFaqTitle.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return listHashMap.get(listFaqTitle.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String faqTitle = (String)getGroup(groupPosition);
        if (convertView == null){

            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.faq_group, null);


        }
        TextView faqTitleTxt = (TextView)convertView.findViewById(R.id.faqTitle);
        faqTitleTxt.setTypeface(null, Typeface.BOLD);
        faqTitleTxt.setText(faqTitle);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String childText = (String)getChild(groupPosition, childPosition);
        if (convertView == null){

            LayoutInflater inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.faq_item, null);

        }

        TextView textListChild = (TextView)convertView.findViewById(R.id.faqItem);
        textListChild.setText(childText);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
