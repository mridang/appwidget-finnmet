package com.mridang.finnmet;

import java.util.Date;
import java.util.Random;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.RemoteViews;

import com.mridang.widgets.BaseWidget;
import com.mridang.widgets.SavedSettings;
import com.mridang.widgets.WidgetHelpers;
import com.mridang.widgets.utils.GzippedClient;
import com.mridang.widgets.utils.LocationUtils;

/**
 * This class is the provider for the widget and updates the data
 */
public class LauncherWidget extends BaseWidget {

	/*
	 * @see com.mridang.widgets.BaseWidget#fetchContent(android.content.Context, java.lang.Integer,
	 * com.mridang.widgets.SavedSettings)
	 */
	@Override
	public String fetchContent(Context ctxContext, Integer intInstance, final SavedSettings objSettings)
			throws Exception {

		final DefaultHttpClient dhcClient = GzippedClient.createClient();

		Log.d("LauncherWidget", "Fetching the weather information");
		final Location locWhere = LocationUtils.getLocation(ctxContext);
		final HttpGet getProjects = new HttpGet("http://m.fmi.fi/mobile/interfaces/weatherdata.php?locations=" + locWhere.getLatitude() + "%2C" + locWhere.getLongitude());
		final HttpResponse resProjects = dhcClient.execute(getProjects);

		final Integer intForecasts = resProjects.getStatusLine().getStatusCode();
		if (intForecasts != HttpStatus.SC_OK) {
			throw new HttpResponseException(intForecasts, "Server responded with code " + intForecasts);
		}

		Log.d("LauncherWidget", "Fetched the weather information");
		final String strForecasts = EntityUtils.toString(resProjects.getEntity(), "UTF-8");

		return new JSONObject(strForecasts).getJSONArray("forecasts").getJSONObject(0).getJSONArray("forecast").toString(2);

	}

	/*
	 * @see com.mridang.widgets.BaseWidget#getIcon()
	 */
	@Override
	public Integer getIcon() {

		return R.drawable.ic_notification;

	}

	/*
	 * @see com.mridang.widgets.BaseWidget#getKlass()
	 */
	@Override
	protected Class<?> getKlass() {

		return getClass();

	}

	/*
	 * @see com.mridang.BaseWidget#getToken()
	 */
	@Override
	public String getToken() {

		return "a1b2c3d4";

	}

	/*
	 * @see com.mridang.widgets.BaseWidget#updateWidget(android.content.Context, java.lang.Integer,
	 * com.mridang.widgets.SavedSettings, java.lang.String)
	 */
	@Override
	public void updateWidget(Context ctxContext, Integer intInstance, SavedSettings objSettings, String strContent)
			throws Exception {

		final RemoteViews remView = new RemoteViews(ctxContext.getPackageName(), R.layout.widget);
		final JSONArray jsoData = new JSONArray(strContent);

		final Intent ittSlides = new Intent(ctxContext, SlideService.class);
		ittSlides.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, intInstance);
		ittSlides.setData(Uri.fromParts("content", String.valueOf(new Random().nextInt()), null));
		ittSlides.putExtra("data", jsoData.toString());

		final PendingIntent pitOptions = WidgetHelpers.getIntent(ctxContext, WidgetSettings.class, intInstance);
		remView.setTextViewText(R.id.location_name, jsoData.getJSONObject(0).getString("name"));
		remView.setTextViewText(R.id.location_region, jsoData.getJSONObject(0).getString("region"));
		remView.setTextViewText(R.id.last_update, DateFormat.format("kk:mm", new Date()));
		remView.setOnClickPendingIntent(R.id.settings_button, pitOptions);
		remView.setRemoteAdapter(R.id.widget_cards, ittSlides);

		AppWidgetManager.getInstance(ctxContext).updateAppWidget(intInstance, remView);

	}

}
