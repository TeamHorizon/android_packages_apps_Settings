
package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.io.InputStream;
import java.net.HttpURLConnection;

public class DeveloperPreference extends Preference {

    private static final String TAG = "DeveloperPreference";

    private String nameDev;
    private String username;
    private String title;
    private static final String ANDROIDNS = "http://schemas.android.com/apk/res/android";
    private static final String SETTINGS = "http://schemas.android.com/apk/res/com.android.settings";

    public DeveloperPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
	setValuesFromXml(context,attrs);
    }

    private void setValuesFromXml(Context context,AttributeSet attrs) {
         nameDev = context.getString(attrs.getAttributeResourceValue(SETTINGS, "nameDev", 0));
         username = context.getString(attrs.getAttributeResourceValue(SETTINGS, "userName", 0));
	 title = getAttributeStringValue(attrs, ANDROIDNS, "key", "");
         Log.i(TAG,"initialization: "+nameDev+","+username+","+title);
     }

     private String getAttributeStringValue(AttributeSet attrs, String namespace, String name, String defaultValue) {
         String value = attrs.getAttributeValue(namespace, name);
         if(value == null)
             value = defaultValue;

         return value;
     }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
	TextView devName = (TextView) view.findViewById(R.id.name);
	devName.setText(nameDev);
	ImageView photoView = (ImageView) view.findViewById(R.id.photo);
	ImageView plusButton = (ImageView) view.findViewById(R.id.plus_button);

	int id = getContext().getResources().getIdentifier("com.android.settings:drawable/"+title, null, null);
	photoView.setImageResource(id);

        Log.i(TAG,"onBindView: "+nameDev+","+username+","+title);


        if (username != null) {
            final OnPreferenceClickListener openUser = new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Uri userURL = Uri.parse("http://plus.google.com/" + username);
                    final Intent intent = new Intent(Intent.ACTION_VIEW, userURL);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    getContext().startActivity(intent);
                    return true;
                }
            };

        this.setOnPreferenceClickListener(openUser);

        }
    }
}
