package com.jdonlan.stockmaster;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StockDisplay extends LinearLayout {
	
	String _current = "";
	StockInfo _symbol;
	StockInfo _price;
	StockInfo _time;
	StockInfo _high;
	StockInfo _low;
	StockInfo _change;
	StockInfo _open;
	StockInfo _volume;  
	JSONObject _stockdata;
	
	public StockDisplay(Context context){
		super(context);
		
		this.setOrientation(LinearLayout.VERTICAL);

		_symbol = new StockInfo(context, "Symbol");
		_price = new StockInfo(context, "Price");
		_time = new StockInfo(context, "Updated");
		_high = new StockInfo(context, "High");
		_low = new StockInfo(context, "Low");
		_change = new StockInfo(context, "Change");
		_open = new StockInfo(context, "Open");
		_volume = new StockInfo(context, "Volume");
		
		this.addView(_symbol);
		this.addView(_price);
		this.addView(_time);
		this.addView(_high);
		this.addView(_low);
		this.addView(_change);
		this.addView(_open);
		this.addView(_volume);
	}
	
	public void updateData(JSONObject data){
		_stockdata = data;
		try{
			_current = data.getString("symbol");
			_symbol.setInfo(data.getString("symbol"));
			_price.setInfo(data.getString("price"));
			_time.setInfo(data.getString("date") + " " + data.getString("time"));
			_high.setInfo(data.getString("high"));
			_low.setInfo(data.getString("low"));
			_change.setInfo(data.getString("change"));
			_open.setInfo(data.getString("col1"));
			_volume.setInfo(data.getString("col2"));
		} catch (JSONException e){
			Log.i("JSON EXCEPTION", data.toString());
		}
	}

	private class StockInfo extends LinearLayout {
	
		TextView _info;
		
		public StockInfo(Context context){
			super(context);
		}
		
		public StockInfo(Context context, String labelText){
			super(context);
			
			this.setOrientation(LinearLayout.HORIZONTAL);
			LayoutParams lp;
			
			TextView label = new TextView(context);
			label.setText(labelText + ":");
			label.setTextAppearance(context, R.style.StockLabel);
			lp = new LayoutParams(0,LayoutParams.WRAP_CONTENT,1.0f);
			label.setLayoutParams(lp);
			label.setPadding(10, 0, 0, 0);
			this.addView(label);
			
			_info = new TextView(context);
			_info.setTextAppearance(context, R.style.StockInfo);
			lp = new LayoutParams(0,LayoutParams.WRAP_CONTENT,3.0f);
			_info.setLayoutParams(lp);
			_info.setPadding(5, 0, 0, 0);
			this.addView(_info);
		}
		
		public void setInfo(String data){
			_info.setText(data);
		}
	}

}



