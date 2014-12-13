package com.OsMoDroid;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapView;
import org.osmdroid.util.BoundingBoxE6;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.OverlayItem;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.provider.Settings.Global;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ChannelsOverlay extends Overlay implements RotationGestureDetector.RotationListener {
	//private final float mScale;
	Paint paint=new Paint();
	private Paint pathpaint;
	private final Point mTempPoint1 = new Point();
	private final Point mTempPoint2 = new Point();
	private final Point mCurScreenCoords = new Point();
	MapView map;
	private RotationGestureDetector mRotationDetector;
	private float currentAngle=0f;
	int ten;
	int twenty;
	private MapFragment mapFragment;
	int followdev=-1;
	public ChannelsOverlay(ResourceProxy pResourceProxy, MapView map) {
		super(pResourceProxy);
		this.map=map;
		this.mapFragment=mapFragment;
		mRotationDetector = new RotationGestureDetector(this);
		// mScale = OsMoDroid.context.getResources().getDisplayMetrics().density;
		pathpaint = new Paint();
		pathpaint.setStyle(Style.STROKE);
		pathpaint.setAlpha(128);
		pathpaint.setAntiAlias(true);
		pathpaint.setStrokeCap(Cap.ROUND);
		pathpaint.setStrokeJoin(Join.ROUND);
		

		// TODO Auto-generated constructor stub
	}
	
	int getSP(int px)
	{
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, px, map.getResources().getDisplayMetrics());
		
	}
	
	@Override
    public boolean onTouchEvent(MotionEvent event, MapView mapView)
    {
        if (this.isEnabled()) {
            mRotationDetector.onTouch(event);
        }
        return super.onTouchEvent(event, mapView);
    }

	

	@Override
	protected void draw(Canvas canvas, MapView mapView, boolean shadow) {
		ten=getSP(OsMoDroid.settings.getInt("pointsize", 8));
		twenty=getSP(OsMoDroid.settings.getInt("pointsize", 8)*2);
		pathpaint.setStrokeWidth(OsMoDroid.settings.getInt("pointsize", 8));
		if(shadow){return;}
		final BoundingBoxE6 theBoundingBox = mapView.getBoundingBox();
		final Projection pj = mapView.getProjection();
        final Point scrPoint = new Point();
        for(ColoredGPX gpx : LocalService.showedgpxList)
        	{
        		drawGPX(canvas, pj, gpx , theBoundingBox, scrPoint, mapView);
        	}
        for(Device dev:LocalService.deviceList)
		{
			
			 if(OsMoDroid.settings.getBoolean("traces", true)){
			 if(dev.devicePath.size()>2)
			 	{
				 pathpaint.setColor( dev.color);
				 Path path = new Path();
				 pj.toPixels((GeoPoint) dev.devicePath.get(0), scrPoint);
				 path.moveTo(scrPoint.x, scrPoint.y);
				 for (IGeoPoint geo: dev.devicePath)
				 	{
					 pj.toPixels((GeoPoint) geo, scrPoint);
					 path.lineTo(scrPoint.x, scrPoint.y);
					 path.moveTo(scrPoint.x, scrPoint.y);
				 	}
				 	canvas.drawPath(path, pathpaint);
			 	}
		}
			 if(dev.lat!=0f&&dev.lon!=0f)
				{ 
				if (theBoundingBox.contains(new GeoPoint(dev.lat, dev.lon))) 
				 {
					pj.toPixels(new GeoPoint(dev.lat, dev.lon), scrPoint);
					paint.setDither(true);
					paint.setAntiAlias(true);
					paint.setTextSize(twenty);
					paint.setTypeface(Typeface.DEFAULT_BOLD);
					paint.setTextAlign(Paint.Align.CENTER);
					paint.setColor(Color.parseColor("#013220"));
					canvas.save();
			        canvas.rotate(-mapView.getMapOrientation(), scrPoint.x, scrPoint.y);
					if(dev.updatated<(System.currentTimeMillis()-30000))
						{
							paint.setColor(Color.parseColor("#F0FFFF"));
						}
					canvas.drawText(dev.name, scrPoint.x, scrPoint.y-ten, paint);
					canvas.drawText(dev.speed, scrPoint.x,scrPoint.y-twenty, paint);
					paint.setColor(dev.color);
					canvas.drawCircle(scrPoint.x, scrPoint.y, ten, paint);
					if(dev.u==followdev)
						{	
							paint.setColor(Color.RED);
							canvas.drawCircle(scrPoint.x, scrPoint.y, ten/3, paint);
						}
					canvas.restore();
				 }
				}
		}
		for (Channel ch :LocalService.channelList){
			for (ColoredGPX gpx :ch.gpxList)
				{
				
				drawGPX(canvas, pj, gpx, theBoundingBox , scrPoint , mapView);
			}
			
			for(Device dev:ch.deviceList)
			{
				
				 if(OsMoDroid.settings.getBoolean("traces", true)){
				 if(dev.devicePath.size()>2)
				 	{
					 pathpaint.setColor( dev.color);
					 Path path = new Path();
					 pj.toPixels((GeoPoint) dev.devicePath.get(0), scrPoint);
					 path.moveTo(scrPoint.x, scrPoint.y);
					 for (IGeoPoint geo: dev.devicePath)
					 	{
						 pj.toPixels((GeoPoint) geo, scrPoint);
						 path.lineTo(scrPoint.x, scrPoint.y);
						 path.moveTo(scrPoint.x, scrPoint.y);
					 	}
					 	canvas.drawPath(path, pathpaint);
				 	}
			}
				 if(dev.lat!=0f&&dev.lon!=0f)
					{ 
					if (theBoundingBox.contains(new GeoPoint(dev.lat, dev.lon))) 
					 {
						pj.toPixels(new GeoPoint(dev.lat, dev.lon), scrPoint);
						paint.setDither(true);
						paint.setAntiAlias(true);
						paint.setTextSize(twenty);
						paint.setTypeface(Typeface.DEFAULT_BOLD);
						paint.setTextAlign(Paint.Align.CENTER);
						paint.setColor(Color.parseColor("#013220"));
						canvas.save();
				        canvas.rotate(-mapView.getMapOrientation(), scrPoint.x, scrPoint.y);
				        if(dev.updatated<(System.currentTimeMillis()-30000))
							{
								paint.setColor(Color.parseColor("#F0FFFF"));
							}
				        canvas.drawText(dev.name, scrPoint.x, scrPoint.y-ten, paint);
						canvas.drawText(dev.speed, scrPoint.x,scrPoint.y-twenty, paint);
						paint.setColor(dev.color);
						canvas.drawCircle(scrPoint.x, scrPoint.y, ten, paint);
						if(dev.u==followdev)
							{	
								paint.setColor(Color.RED);
								canvas.drawCircle(scrPoint.x, scrPoint.y, ten/3, paint);
							}
						canvas.restore();
					 }
					}
			}
			for(com.OsMoDroid.Channel.Point p: ch.pointList){
				if (theBoundingBox.contains(new GeoPoint(p.lat, p.lon))) 
				 {
					pj.toPixels(new GeoPoint(p.lat, p.lon), scrPoint);
					paint.setDither(true);
					paint.setAntiAlias(true);
					paint.setTextSize(twenty);
					paint.setTypeface(Typeface.DEFAULT_BOLD);
					paint.setTextAlign(Paint.Align.CENTER);
					paint.setColor(Color.parseColor("#013220"));
					canvas.save();
			        canvas.rotate(-mapView.getMapOrientation(), scrPoint.x, scrPoint.y);
					canvas.drawText(p.name, scrPoint.x, scrPoint.y-ten, paint);
					try {
						paint.setColor(Color.parseColor(p.color));
					} catch (Exception e) {
						paint.setColor(Color.RED);
					}
					canvas.drawRect(scrPoint.x-ten, scrPoint.y-ten, scrPoint.x+ten, scrPoint.y+ten, paint);
					canvas.restore();
				 }
			}
			
		}
		
	}

	private void drawGPX(Canvas canvas, final Projection pj, ColoredGPX gpx, BoundingBoxE6 theBoundingBox, Point scrPoint, MapView mapView)
		{
			int size=gpx.points.size();
			if(size>2)
			{
				//
				

				// precompute new points to the intermediate projection.
				while (gpx.mPointsPrecomputed < size) {
					final Point pt = gpx.points.get(gpx.mPointsPrecomputed);
					pj.toProjectedPixels(pt.x, pt.y, pt);

					gpx.mPointsPrecomputed++;
				}

				Point screenPoint0 = null; // points on screen
				Point screenPoint1;
				Point projectedPoint0; // points from the points list
				Point projectedPoint1;

				// clipping rectangle in the intermediate projection, to avoid performing projection.
				BoundingBoxE6 boundingBox = pj.getBoundingBox();
				Point topLeft = pj.toProjectedPixels(boundingBox.getLatNorthE6(),
				boundingBox.getLonWestE6(), null);
				Point bottomRight = pj.toProjectedPixels(boundingBox.getLatSouthE6(),
				boundingBox.getLonEastE6(), null);
				final Rect clipBounds = new Rect(topLeft.x, topLeft.y, bottomRight.x, bottomRight.y);

				gpx.mPath.rewind();
				projectedPoint0 = gpx.points.get(size - 1);
				gpx.mLineBounds.set(projectedPoint0.x, projectedPoint0.y, projectedPoint0.x, projectedPoint0.y);

				for (int i = size - 2; i >= 0; i--) {
					// compute next points
					projectedPoint1 = gpx.points.get(i);
					gpx.mLineBounds.union(projectedPoint1.x, projectedPoint1.y);

					if (!Rect.intersects(clipBounds, gpx.mLineBounds)) {
						// skip this line, move to next point
						projectedPoint0 = projectedPoint1;
						screenPoint0 = null;
						continue;
					}

					// the starting point may be not calculated, because previous segment was out of clip
					// bounds
					if (screenPoint0 == null) {
						screenPoint0 = pj.toPixelsFromProjected(projectedPoint0, this.mTempPoint1);
						gpx.mPath.moveTo(screenPoint0.x, screenPoint0.y);
					}

					screenPoint1 = pj.toPixelsFromProjected(projectedPoint1, this.mTempPoint2);

					// skip this point, too close to previous point
					if (Math.abs(screenPoint1.x - screenPoint0.x) + Math.abs(screenPoint1.y - screenPoint0.y) <= 1) {
						continue;
					}

					gpx.mPath.lineTo(screenPoint1.x, screenPoint1.y);

					// update starting point to next position
					projectedPoint0 = projectedPoint1;
					screenPoint0.x = screenPoint1.x;
					screenPoint0.y = screenPoint1.y;
					gpx.mLineBounds.set(projectedPoint0.x, projectedPoint0.y, projectedPoint0.x, projectedPoint0.y);
				}
				pathpaint.setColor(gpx.color);
				canvas.drawPath(gpx.mPath, this.pathpaint);
				
				///
				
//					Path path = new Path();
//					pj.toMapPixels(gpx.points.get(0), scrPoint);
//					path.moveTo(scrPoint.x, scrPoint.y);
//					for (IGeoPoint geo: gpx.points)
//				 		{
//							pj.toMapPixels(geo, scrPoint);
//							path.lineTo(scrPoint.x, scrPoint.y);
//							path.moveTo(scrPoint.x, scrPoint.y);
//				 		}
//				 	canvas.drawPath(path, pathpaint);
			}
			for(Channel.Point p: gpx.waypoints){
				if (theBoundingBox.contains(new GeoPoint(p.lat, p.lon))) 
				 {
					pj.toPixels(new GeoPoint(p.lat, p.lon), scrPoint);
					paint.setDither(true);
					paint.setAntiAlias(true);
					paint.setTextSize(twenty);
					paint.setTypeface(Typeface.DEFAULT_BOLD);
					paint.setTextAlign(Paint.Align.CENTER);
					paint.setColor(Color.parseColor("#013220"));
					canvas.save();
			        canvas.rotate(-mapView.getMapOrientation(), scrPoint.x, scrPoint.y);
					//canvas.drawText(p.name, scrPoint.x, scrPoint.y-ten, paint);
					try {
						paint.setColor(gpx.color);
					} catch (Exception e) {
						paint.setColor(Color.RED);
					}
					canvas.drawRect(scrPoint.x-ten, scrPoint.y-ten, scrPoint.x+ten, scrPoint.y+ten, paint);
					canvas.restore();
				 }
			}
		}

	@Override
	public void onRotate(float deltaAngle)
		{
			if(OsMoDroid.settings.getBoolean("rotation", false))
				{
					map.setMapOrientation(map.getMapOrientation()+deltaAngle);
				}
			
		}

	@Override
	public boolean onLongPress(MotionEvent e, MapView mapView)
		{
			 Projection proj = mapView.getProjection();
			 IGeoPoint p = proj.fromPixels((int)e.getX(), (int)e.getY());
			 final JSONObject jo = new JSONObject();
			 try
				{
					jo.put("lat", p.getLatitude());
					jo.put("lon", p.getLongitude());
				} catch (JSONException e1)
				{
					e1.printStackTrace();
				}
			 	LinearLayout layout = new LinearLayout(map.getContext());
				layout.setOrientation(LinearLayout.VERTICAL);
				final TextView txv5 = new TextView(map.getContext());
				txv5.setText(R.string.point_name_);
				layout.addView(txv5);
				final EditText pointName = new EditText(map.getContext());
				layout.addView(pointName);
				final TextView txv6 = new TextView(map.getContext());
				txv6.setText(R.string.chanal);
				layout.addView(txv6);
				final Spinner groupSpinner = new Spinner(map.getContext());
				layout.addView(groupSpinner);
				ArrayAdapter<Channel> dataAdapter = new ArrayAdapter<Channel>(map.getContext(),R.layout.spinneritem, LocalService.channelList);
				groupSpinner.setAdapter(dataAdapter);
			 	AlertDialog alertdialog1 = new AlertDialog.Builder(map.getContext()).create();
			 	alertdialog1.setView(layout);
				alertdialog1.setTitle(map.getContext().getString(R.string.point_create));
				alertdialog1.setMessage(map.getContext().getString(R.string.point_create_description));
				alertdialog1.setButton(map.getContext().getString(R.string.yes),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								 try
										{
											jo.put("name",pointName.getText().toString());
											jo.put("group", ((Channel)groupSpinner.getSelectedItem()).u);
										} catch (JSONException e1)
										{
											e1.printStackTrace();
										}
								LocalService.myIM.sendToServer("POINT_ADD|"+jo.toString());
								return;
							}
						});
				alertdialog1.setButton2(map.getContext().getString(R.string.No),
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								return;
							}
						});
				alertdialog1.show();
			return super.onLongPress(e, mapView);
		}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e, MapView mapView)
		{
			final Projection pj = mapView.getProjection();
			final Rect screenRect = pj.getIntrinsicScreenRect();
			final int size = LocalService.deviceList.size();
			for (Channel ch :LocalService.channelList)
				{
					for(Device dev:ch.deviceList)
						{
							pj.toPixels(new GeoPoint(dev.lat, dev.lon), mCurScreenCoords);
							if (mCurScreenCoords.x<=(e.getX()+2*ten) && mCurScreenCoords.x>=(e.getX()-2*ten) 
									&& mCurScreenCoords.y<=(e.getY()+ten)&&mCurScreenCoords.y>=(e.getY()-ten))
					 
								{
									
									if (followdev!=dev.u)
										{
											Toast.makeText(mapView.getContext(),map.getContext().getString(R.string.follow_) + dev.name, Toast.LENGTH_SHORT).show();
											followdev=dev.u;
										}
									else
										{
											Toast.makeText(mapView.getContext(),map.getContext().getString(R.string.no_follow_) + dev.name, Toast.LENGTH_SHORT).show();
											followdev=-1;
										}
									mapView.invalidate();
									return true;
								}
						}
					for (ColoredGPX cg : ch.gpxList)
						{
							for (Channel.Point p : cg.waypoints)
								{
									pj.toPixels(new GeoPoint(p.lat, p.lon), mCurScreenCoords);
									if (mCurScreenCoords.x<=(e.getX()+2*ten) && mCurScreenCoords.x>=(e.getX()-2*ten) 
											&& mCurScreenCoords.y<=(e.getY()+ten)&&mCurScreenCoords.y>=(e.getY()-ten))
							 
										{
											Toast.makeText(mapView.getContext(),p.name, Toast.LENGTH_SHORT).show();
										}
								}
						}
				}
			for (int i = 0; i < size; i++) {
				final Device dev = LocalService.deviceList.get(i);
				if (dev.lat == 0f) {
					continue;
				}

				pj.toPixels(new GeoPoint(dev.lat, dev.lon), mCurScreenCoords);
				if (mCurScreenCoords.x<=(e.getX()+2*ten) && mCurScreenCoords.x>=(e.getX()-2*ten) 
						&& mCurScreenCoords.y<=(e.getY()+ten)&&mCurScreenCoords.y>=(e.getY()-ten))
		 
					{
						
						if (followdev!=dev.u)
							{
								Toast.makeText(mapView.getContext(),map.getContext().getString(R.string.follow_) + dev.name, Toast.LENGTH_SHORT).show();
								followdev=dev.u;
							}
						else
							{
								Toast.makeText(mapView.getContext(),map.getContext().getString(R.string.no_follow_) + dev.name, Toast.LENGTH_SHORT).show();
								followdev=-1;
							}
						mapView.invalidate();
						return true;
					}
				}
			
			
			return super.onSingleTapConfirmed(e, mapView);
		}


	
	
	
	
}
