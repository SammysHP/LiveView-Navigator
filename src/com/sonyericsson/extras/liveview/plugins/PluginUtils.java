/*
 * Copyright (c) 2010 Sony Ericsson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * 
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.sonyericsson.extras.liveview.plugins;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;

/**
 * Utils.
 */
public final class PluginUtils {
    
    private PluginUtils() {
    }
    
    /**
     * Stores icon to phone file system
     * 
     * @param resources Reference to project resources
     * @param resource Reference to specific resource
     * @param fileName The icon file name
     */
    public static String storeIconToFile(Context ctx, Resources resources, int resource, String fileName) {
        Log.d(PluginConstants.LOG_TAG, "Store icon to file.");
        
        if(resources == null) {
            return "";
        }
        
        Bitmap bitmap = BitmapFactory.decodeStream(resources.openRawResource(resource));
        
        try {
            FileOutputStream fos = ctx.openFileOutput(fileName, Context.MODE_WORLD_READABLE);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close(); 
        } 
        catch (IOException e) { 
            Log.e(PluginConstants.LOG_TAG, "Failed to store to device", e);
        }
        
        File iconFile = ctx.getFileStreamPath(fileName);
        Log.d(PluginConstants.LOG_TAG, "Icon stored. " + iconFile.getAbsolutePath());
        
        return iconFile.getAbsolutePath();
    }
    
    /**
     * Rotates and stores image to device
     *  
     * @param bitmap
     * @param degrees
     * @return
     */
    public static void rotateAndSend(LiveViewAdapter liveView, int pluginId, Bitmap bitmap, int degrees) {
        Bitmap newBitmap = null;
        try
        {
            Matrix matrix = new Matrix();
            matrix.postRotate(degrees);
            newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch(Exception e) {
            Log.e(PluginConstants.LOG_TAG, "Failed to rotate bitmap.", e);
            return;
        }
        
        sendScaledImage(liveView, pluginId, newBitmap);
    }
    
    public static void sendTextBitmap(LiveViewAdapter liveView, int pluginId, String text) {
        sendTextBitmap(liveView, pluginId, text, 64, 15);
    }
    
	/**
	 * Draws the lveview c:geo screen.
	 * 
	 * @param liveView
	 * @param pluginId
	 * @param dist
	 *            Distance
	 * @param dir
	 *            Direction
	 */

	public static void drawAndSendScreen(LiveViewAdapter liveView, int pluginId, Bitmap arrow, String dist, int dir) {
		Log.d(PluginConstants.LOG_TAG, "Sending Textbitmap " + dist + " " + dir);

		// rotateAndSend(liveView, pluginId, arrow, dir);
		// liveView.sendImageAsBitmap(pluginId, centerX(arrow), centerY(arrow),
		// arrow);
		sendArrow(liveView, pluginId, dir);

		sendTextBitmap(liveView, pluginId, dist, 128, 12, 0, 10);
		sendTextBitmap(liveView, pluginId, dir + " °", 128, 12, 0, 110);
	}

	/**
	 * Turns out rotating Image is too slow.
	 * 
	 * @param liveView
	 * @param pluginId
	 * @param dir
	 */

	private static void sendArrow(LiveViewAdapter liveView, int pluginId, int dir) {

		Bitmap bitmap = null;
		try {
			bitmap = Bitmap.createBitmap(80, 80, Bitmap.Config.RGB_565);
		} catch (IllegalArgumentException e) {
			return;
		}

		Canvas canvas = new Canvas(bitmap);
		double dirRad = Math.toRadians(dir + 90);
		Point origin = new Point(40, 40);
		Point p1 = rotateByRadians(origin, new Point(10, 40), dirRad);
		Point p2 = rotateByRadians(origin, new Point(70, 35), dirRad);
		Point p3 = rotateByRadians(origin, new Point(70, 45), dirRad);
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

		paint.setStrokeWidth(2);
		paint.setColor(android.graphics.Color.RED);
		paint.setStyle(Paint.Style.FILL_AND_STROKE);
		paint.setAntiAlias(true);

		Path path = new Path();
		path.setFillType(Path.FillType.EVEN_ODD);
		path.moveTo(p1.x, p1.y);
		path.lineTo(p2.x, p2.y);
		path.lineTo(p3.x, p3.y);
		path.close();

		canvas.drawPath(path, paint);
		try {
			liveView.sendImageAsBitmap(pluginId, centerX(bitmap), centerY(bitmap), bitmap);
		} catch (Exception e) {
			Log.d(PluginConstants.LOG_TAG, "Failed to send bitmap", e);
		}
	}

	/**
	 * Rotates the point by a specific number of radians about a specific origin
	 * point.
	 * 
	 * @param origin
	 *            The origin point about which to rotate the point
	 * @param degrees
	 *            The number of radians to rotate the point
	 * @return
	 */
	public static Point rotateByRadians(Point origin, Point p, double radians) {
		double cosVal = Math.cos(radians);
		double sinVal = Math.sin(radians);

		double ox = p.x - origin.x;
		double oy = p.y - origin.y;

		p.x = (int) (origin.x + ox * cosVal - oy * sinVal);
		p.y = (int) (origin.y + ox * sinVal + oy * cosVal);
		return p;
	}

	/**
	 * Stores text to an image on file.
	 * 
	 * @param liveView
	 *            Reference to LiveView connection
	 * @param pluginId
	 *            Id of the plugin
	 * @param text
	 *            The text string
	 * @param bitmapSizeX
	 *            Bitmap size X
	 * @param fontSize
	 *            Font size
	 * @return Absolute path to file
	 */
	public static void sendTextBitmap(LiveViewAdapter liveView, int pluginId, String text, int bitmapSizeX, int fontSize, int x, int y) {
		Log.d(PluginConstants.LOG_TAG, "Sending Textbitmap " + text);
		// Empty bitmap and link the canvas to it
		Bitmap bitmap = null;
		try {
			bitmap = Bitmap.createBitmap(bitmapSizeX, fontSize, Bitmap.Config.RGB_565);
		} catch (IllegalArgumentException e) {
			return;
		}

		Canvas canvas = new Canvas(bitmap);

		// Set the text properties in the canvas
		TextPaint textPaint = new TextPaint();
		textPaint.setTextSize(fontSize);
		textPaint.setColor(Color.WHITE);

		// Create the text layout and draw it to the canvas
		Layout textLayout = new StaticLayout(text, textPaint, bitmapSizeX, Layout.Alignment.ALIGN_CENTER, 1, 1, false);
		textLayout.draw(canvas);

		try {
			liveView.sendImageAsBitmap(pluginId, x, y, bitmap);
		} catch (Exception e) {
			Log.d(PluginConstants.LOG_TAG, "Failed to send bitmap", e);
		}
	}
    /**
     * Stores text to an image on file.
     * 
     * @param liveView Reference to LiveView connection
     * @param pluginId Id of the plugin
     * @param text The text string
     * @param bitmapSizeX Bitmap size X
     * @param fontSize Font size
     * @return Absolute path to file
     */
    public static void sendTextBitmap(LiveViewAdapter liveView, int pluginId, String text, int bitmapSizeX, int fontSize) {
		Log.d(PluginConstants.LOG_TAG, "Sending Textbitmap " + text);
        // Empty bitmap and link the canvas to it
        Bitmap bitmap = null;
        try {
            bitmap = Bitmap.createBitmap(bitmapSizeX, fontSize, Bitmap.Config.RGB_565);
        }
        catch(IllegalArgumentException  e) {
            return;
        }
        
        Canvas canvas = new Canvas(bitmap);

        // Set the text properties in the canvas
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(fontSize);
        textPaint.setColor(Color.WHITE);

        // Create the text layout and draw it to the canvas
        Layout textLayout = new StaticLayout(text, textPaint, bitmapSizeX, Layout.Alignment.ALIGN_CENTER, 1, 1, false);
        textLayout.draw(canvas);
        
        try
        { 
            liveView.sendImageAsBitmap(pluginId, centerX(bitmap), centerY(bitmap), bitmap);
        } catch(Exception e) {
            Log.d(PluginConstants.LOG_TAG, "Failed to send bitmap", e);
        }
    }
    
    /**
     * Gets resource id dynamically
     * 
     * @param context
     * @param resourceName
     * @param resourceType
     * @return
     */
    public static int getDynamicResourceId(Context context, String resourceName, String resourceType) {
        return context.getResources().getIdentifier(resourceName, resourceType, context.getPackageName());
    }
    
    /**
     * Gets resource string dynamically
     * 
     * @param context
     * @param resourceName
     * @return
     */
    public static String getDynamicResourceString(Context context, String resourceName) {
        int resourceId = getDynamicResourceId(context, resourceName, "string");
        return context.getString(resourceId);
    }
    
    /**
     * Sends an image to LiveView and puts it in the middle of the screen
     * 
     * @param liveView
     * @param pluginId
     * @param bitmap
     * @param path
     */
    public static void sendScaledImage(LiveViewAdapter liveView, int pluginId, Bitmap bitmap) {
        try {
            if(liveView != null) {
                liveView.sendImageAsBitmap(pluginId, centerX(bitmap), centerY(bitmap), bitmap);
            }
        } catch(Exception e) {
            Log.e(PluginConstants.LOG_TAG, "Failed to send image.", e);
        }
    }
    
    /**
     * Get centered X axle
     * 
     * @param bitmap
     * @return
     */
    private static int centerX(Bitmap bitmap) {
        return (PluginConstants.LIVEVIEW_SCREEN_X/2) - (bitmap.getWidth()/2);
    }
    
    /**
     * Get centered Y axle
     * 
     * @param bitmap
     * @return
     */
    private static int centerY(Bitmap bitmap) {
        return (PluginConstants.LIVEVIEW_SCREEN_Y/2) - (bitmap.getHeight()/2);
    }

	public static Bitmap convertToRGB565(Bitmap arrow) {
		Bitmap b = Bitmap.createBitmap(arrow.getWidth(), arrow.getHeight(), Bitmap.Config.RGB_565);
		Paint p = new Paint();
		Canvas c = new Canvas(b);
		c.drawBitmap(arrow, 0, 0, p);
		return b;
	}

}