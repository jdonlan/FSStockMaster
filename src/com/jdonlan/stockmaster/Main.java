package com.jdonlan.stockmaster;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.jdonlan.lib.FileStuff;
import com.jdonlan.lib.WebStuff;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class Main extends Activity {

	Context _context;
	LinearLayout _appLayout;
	SearchForm _search;
	StockDisplay _stock;
	FavDisplay _favorites;
	HashMap<String, String> _history;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        _context = this;
        _appLayout = new LinearLayout(this);
        _history = getHistory();
        Log.i("HISTORY READ",_history.toString());
        
        _search = new SearchForm(_context, "Enter Text Symbol", "GO");
        
        //ADD SEARCH HANDLER
        Button searchButton = _search.getButton();
        
        searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String symbol = _search.getField().getText().toString().toUpperCase();
				_search.getField().setText(symbol);
				getQuote(symbol);
			}
		});   
        
        //ADD STOCK DISPLAY
        _stock = new StockDisplay(_context);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f);
        _stock.setLayoutParams(lp);
        
        //ADD FAVORITES DISPLAY AND FUNCTIONALITY
        ArrayList<String> favs = new ArrayList<String>(Arrays.<String>asList(FileStuff.readStringFile(_context, "favorites", true).split(",")));
        _favorites = new FavDisplay(_context, favs);
        _favorites._list.setOnItemSelectedListener(new OnItemSelectedListener() {
        	@Override
        	public void onItemSelected(AdapterView<?> parent, View v, int pos, long id){
        		if(pos > 0){
        			String symbol = parent.getItemAtPosition(pos).toString();				
        			_search.getField().setText(symbol);
        			getQuote(symbol);
        		}
        	}
		
        	@Override
        	public void onNothingSelected(AdapterView<?> parent){
        		Log.i("FAVORITE SELECTED", "NONE");
        	}
        });   
  		_favorites._add.setOnClickListener(new OnClickListener() {
  			@Override
  			public void onClick(View v) {
  				String symbol = _stock._current;
  				if(symbol.length() > 0){
  					Boolean found = false;
  					int foundpos = 0;
  					for(int i=1, q=_favorites._stocks.size(); i<q; i++){
  						if(_favorites._stocks.get(i).compareTo(symbol)==0){
  							found = true;
  							foundpos = i;
  							break;
  						}
  					}
  					if(!found){
  						_favorites._stocks.add(symbol);
  						writeFavs(symbol);
  						_favorites._list.setSelection(_favorites._stocks.size()-1);
  					} else {
  						_favorites._list.setSelection(foundpos);
  					}
  				} else {
  					Toast toast =  Toast.makeText(_context, "Invalid stock symbol.", Toast.LENGTH_SHORT);
  					toast.show();
  				}
  			}
  		});
  		_favorites._remove.setOnClickListener(new OnClickListener() {
  			@Override
  			public void onClick(View v) {
  				String symbol = _favorites._list.getSelectedItem().toString();
  				_favorites._stocks.remove(symbol);
  				writeFavs(symbol);
  				_favorites._list.setSelection(0);
  			}
  		});
        
        //ADD VIEWS TO MAIN LAYOUT
        _appLayout.addView(_search);
        _appLayout.addView(_stock);
        _appLayout.addView(_favorites);
        
        _appLayout.setOrientation(LinearLayout.VERTICAL);
        
        setContentView(_appLayout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    private void writeFavs(String symbol){
    	StringBuilder sb = new StringBuilder();
			for(int i=1, q=_favorites._stocks.size(); i<q; i++){
				sb.append(_favorites._stocks.get(i));
				sb.append(",");
			}
			String favString = sb.toString().substring(0, sb.toString().length()-1);
			Boolean stored = FileStuff.storeStringFile(_context,"favorites", favString, true);
			Toast toast;
			if(stored){
				toast = Toast.makeText(_context, symbol + " added to favorites.", Toast.LENGTH_SHORT);
				toast.show();
			} else {
				toast = Toast.makeText(_context, "Unable to add " + symbol + " to favorites.", Toast.LENGTH_SHORT);
				toast.show();
			}
    }
    
    private void getQuote(String symbol){
    	Boolean connected = WebStuff.getConnectionStatus(_context);
    	if(connected){
	    	String baseURL = "http://query.yahooapis.com/v1/public/yql";
	    	String yql = "select * from csv where url='http://download.finance.yahoo.com/d/quotes.csv?s=" + symbol + "&f=sl1d1t1c1ohgv&e=.csv' and columns='symbol,price,date,time,change,col1,high,low,col2'";
	    	String qs;
	    	try{
	    		qs = URLEncoder.encode(yql, "UTF-8");
	    	} catch (Exception e){
	    		Log.e("BAD URL","ENCODING PROBLEM");
	    		qs = "";
	    	}
	    	URL finalURL;
	    	try{
	    		finalURL = new URL(baseURL + "?q=" + qs + "&format=json");
	    		QuoteRequest qr = new QuoteRequest();
	    		qr.execute(finalURL);
	    	} catch (MalformedURLException e){
	    		Log.e("BAD URL", "MALFORMED URL");
	    		finalURL = null;
	    	}
    	} else if(_history.containsKey(symbol)){
    		String jsonString = (String) _history.get(symbol);
    		_stock.updateData(buildJSON(jsonString));
    	} else {
    		Toast toast = Toast.makeText(_context, "No network connection or history available.", Toast.LENGTH_SHORT);
    		toast.show();
    	}
    }
    
    private JSONObject buildJSON(String jsonString){
    	JSONObject data;
    	try{
			data = new JSONObject(jsonString);
		} catch (JSONException e){
			data = null;
		}
    	return data;
    }
    
    @SuppressWarnings("unchecked")
	private HashMap<String, String> getHistory(){
    	Object stored = FileStuff.readObjectFile(_context, "history", false);
    	
    	HashMap<String, String> history;
    	if(stored == null){
    		Log.i("HISTORY","NO HISTORY FILE FOUND");
    		history = new HashMap<String, String>(); 
    	} else {
    		history = (HashMap<String, String>) stored;
    	}
    	return history;
    }
    
    private class QuoteRequest extends AsyncTask<URL, Void, String>{
    	@Override
    	protected String doInBackground(URL... urls){
    		String response = "";
    		for(URL url: urls){
    			response = WebStuff.getURLStringResponse(url);
    		}
    		return response;
    	}
    	
    	@Override
    	protected void onPostExecute(String result){
    		Log.i("URL RESPONSE", result);
			JSONObject json = buildJSON(result);
			try{
				JSONObject results = json.getJSONObject("query").getJSONObject("results").getJSONObject("row");
				if(results.getString("col1").compareTo("N/A")==0){
					Toast toast = Toast.makeText(_context, "Invalid Symbol", Toast.LENGTH_SHORT);
					toast.show();
				} else {
					_stock.updateData(results);
					_history.put(results.getString("symbol"), results.toString());
					FileStuff.storeObjectFile(_context, "history", _history, false);
				}
			} catch (JSONException e){
				Log.e("JSON EXCEPTION","ERROR PARSING RESPONSE");
			}
    	}
    }

    
}
