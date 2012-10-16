package com.jdonlan.stockmaster;

import java.util.ArrayList;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

public class FavDisplay extends LinearLayout{

	Button _add;
	Button _remove;
	Spinner _list;
	Context _context;
	ArrayList<String> _stocks = new ArrayList<String>();
	
	public FavDisplay(Context context){
		super(context);
		setupDisplay(context);
	}
	
	public FavDisplay(Context context, ArrayList<String> items){
		super(context);
		setupDisplay(context);
		
		for(int i=0, q=items.size(); i<q; i++){
			_stocks.add(items.get(i));
		}
	}
	
	private void setupDisplay(Context context){
		_context = context;
		LayoutParams lp;
		lp = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);
		this.setLayoutParams(lp);
		_stocks.add("Select Favorite");
		
		//CREATE SPINNER
		_list = new Spinner(_context);
		lp = new LayoutParams(0,LayoutParams.WRAP_CONTENT,1.0f);
		_list.setLayoutParams(lp);
		ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(_context, android.R.layout.simple_spinner_item, _stocks);
		listAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
		_list.setAdapter(listAdapter);
		
		
		//ADD BUTTONS
		_add = new Button(_context);
		_add.setText("+");
		_remove = new Button(_context);
		_remove.setText("-");
	
		//ADD VIEWS TO FAVORITE DISPLAY
		this.addView(_list);
		this.addView(_add);
		this.addView(_remove);
	}
	
	

}
