package com.mridang.finnmet;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.acra.ACRA;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService.RemoteViewsFactory;

/**
 * This class is used to provide the stacks for the widget
 */
@SuppressLint("SimpleDateFormat")
public class SlideFactory implements RemoteViewsFactory {

	/* This is the array containing the the list of forecasts */
	private JSONArray jsoForecasts = new JSONArray();
	/* The context of the calling activity */
	private final Context ctxContext;
	/* The view that is used for each of the slides */
	private RemoteViews remView;
	/* The date formatter to parse the date and times */
	private SimpleDateFormat sdfParse = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
	/* The date formatter to format the date and times */
	private SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
	/* The date formatter to format the date and times */
	private SimpleDateFormat sdfWday = new SimpleDateFormat("EEE");

	/*
	 * 
	 */
	public SlideFactory(Context ctxContext, Intent ittIntent) {

		sdfParse.setTimeZone(TimeZone.getTimeZone("UTC"));
		remView = new RemoteViews(ctxContext.getPackageName(), R.layout.slide);
		this.ctxContext = ctxContext;
		try {
			jsoForecasts = new JSONArray(ittIntent.getStringExtra("data"));
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}

	}

	/*
	 * @see android.widget.RemoteViewsService.RemoteViewsFactory#getCount()
	 */
	@Override
	public int getCount() {

		return jsoForecasts != null ? jsoForecasts.length() : 0;

	}

	/*
	 * @see android.widget.RemoteViewsService.RemoteViewsFactory#getItemId(int)
	 */
	@Override
	public long getItemId(int intPosition) {

		return intPosition;

	}

	/*
	 * @see android.widget.RemoteViewsService.RemoteViewsFactory#getLoadingView()
	 */
	@Override
	public RemoteViews getLoadingView() {

		return new RemoteViews(ctxContext.getPackageName(), R.layout.loading);

	}

	/*
	 * @see android.widget.RemoteViewsService.RemoteViewsFactory#getViewAt(int)
	 */
	@SuppressLint("DefaultLocale")
	@Override
	public RemoteViews getViewAt(int intPosition) {

		try {

			JSONObject jsoSlide = jsoForecasts.getJSONObject(intPosition);
			remView.setTextViewText(R.id.current_temp, jsoSlide.getString("Temperature") + "\u00b0");
			remView.setTextViewText(R.id.current_rain, jsoSlide.getString("Precipitation1h") + "mm");
			remView.setTextViewText(R.id.rain_chances, jsoSlide.getString("PoP") + "%");
			remView.setTextViewText(R.id.weather_when, sdfTime.format(sdfParse.parse(jsoSlide.getString("utctime"))));
			remView.setTextViewText(R.id.weather_wday, sdfWday.format(sdfParse.parse(jsoSlide.getString("utctime"))));
			remView.setTextViewText(R.id.wind_speed, jsoSlide.getString("WindSpeedMS") + "m/s");
			remView.setTextViewText(R.id.wind_direc, jsoSlide.getString("WindCompass8"));
			remView.setImageViewResource(R.id.wind_icon, this.ctxContext.getResources().getIdentifier("ic_wind_" + jsoSlide.getString("WindCompass8").toLowerCase(), "drawable", this.ctxContext.getPackageName()));
			remView.setImageViewResource(R.id.weather_icon, this.ctxContext.getResources().getIdentifier("ic_symb_" + jsoSlide.getString("WeatherSymbol3").toLowerCase(), "drawable", this.ctxContext.getPackageName()));

		} catch (final JSONException e) {
			Log.e("SlideFactory", "Unknown error encountered", e);
			ACRA.getErrorReporter().handleSilentException(e);
		} catch (final Exception e) {
			Log.e("SlideFactory", "Unknown error encountered", e);
			ACRA.getErrorReporter().handleSilentException(e);
		}

		return remView;

	}

	/*
	 * @see android.widget.RemoteViewsService.RemoteViewsFactory#getViewTypeCount()
	 */
	@Override
	public int getViewTypeCount() {

		return 1;

	}

	/*
	 * @see android.widget.RemoteViewsService.RemoteViewsFactory#hasStableIds()
	 */
	@Override
	public boolean hasStableIds() {

		return true;

	}

	/*
	 * @see android.widget.RemoteViewsService.RemoteViewsFactory#onCreate()
	 */
	@Override
	public void onCreate() {

		return;

	}

	/*
	 * @see android.widget.RemoteViewsService.RemoteViewsFactory#onDataSetChanged()
	 */
	@Override
	public void onDataSetChanged() {

		return;

	}

	/*
	 * @see android.widget.RemoteViewsService.RemoteViewsFactory#onDestroy()
	 */
	@Override
	public void onDestroy() {

		jsoForecasts = null;

	}

}
