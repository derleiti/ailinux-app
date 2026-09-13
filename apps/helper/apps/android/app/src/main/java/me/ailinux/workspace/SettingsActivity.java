package me.ailinux.workspace;

import android.app.Activity;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Platform-neutral AICoder settings registry rendered natively on Android. */
public class SettingsActivity extends Activity {
    private final Map<String,EditText> fields=new LinkedHashMap<>();
    private SharedPreferences prefs;
    @Override public void onCreate(Bundle b){super.onCreate(b);prefs=getSharedPreferences("aicoder_android_settings",MODE_PRIVATE);build();}
    private TextView text(String s,int size,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextColor(Color.WHITE);v.setTextSize(size);if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);v.setPadding(0,7,0,7);return v;}
    private void build(){
        ScrollView scroll=new ScrollView(this);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(32,32,32,56);scroll.addView(root);scroll.setBackgroundColor(0xff0d1117);
        root.addView(text("AICoder Settings",28,true));root.addView(text("Canonical settings schema shared with desktop AICoder. Security-impacting values remain explicit and local to this device.",14,false));
        try{
            String raw=readAsset("aicoder-settings.json");JSONArray rows=new JSONObject(raw).getJSONArray("settings");String lastGroup="";
            for(int i=0;i<rows.length();i++){
                JSONObject spec=rows.getJSONObject(i);if(!spec.optBoolean("mutable",true))continue;
                String group=spec.optString("group","other");if(!group.equals(lastGroup)){TextView g=text(group.toUpperCase(Locale.ROOT),18,true);g.setTextColor(0xff79c0ff);g.setPadding(0,24,0,8);root.addView(g);lastGroup=group;}
                String key=spec.getString("key");String type=spec.optString("type","str");boolean impact=spec.optBoolean("security_impact",false);
                root.addView(text(key+(impact?" · security-sensitive":""),15,true));
                TextView desc=text(spec.optString("description",""),12,false);desc.setTextColor(0xff8b949e);root.addView(desc);
                EditText e=new EditText(this);e.setTextColor(Color.WHITE);e.setHintTextColor(0xff6e7681);e.setBackgroundColor(0xff161b22);e.setPadding(16,12,16,12);e.setSingleLine(true);
                Object def=spec.isNull("default")?"":spec.opt("default");String defaultValue=def instanceof JSONArray?def.toString():String.valueOf(def);
                e.setText(prefs.getString(key,defaultValue));
                JSONArray choices=spec.optJSONArray("choices");if(choices!=null&&choices.length()>0)e.setHint("Allowed: "+choices.toString());
                if("int".equals(type))e.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED);else e.setInputType(InputType.TYPE_CLASS_TEXT);
                fields.put(key,e);root.addView(e);
            }
        }catch(Exception e){root.addView(text("Settings schema could not be loaded: "+e.getMessage(),14,false));}
        Button save=new Button(this);save.setText("Save all settings");save.setAllCaps(false);save.setOnClickListener(v->save());root.addView(save);
        Button reset=new Button(this);reset.setText("Reset Android settings to AICoder defaults");reset.setAllCaps(false);reset.setOnClickListener(v->{prefs.edit().clear().apply();fields.clear();build();});root.addView(reset);
        setContentView(scroll);
    }
    private String readAsset(String name)throws Exception{try(InputStream in=getAssets().open(name);ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=in.read(b))>=0)out.write(b,0,n);return out.toString(StandardCharsets.UTF_8.name());}}
    private void save(){SharedPreferences.Editor ed=prefs.edit();for(Map.Entry<String,EditText> row:fields.entrySet())ed.putString(row.getKey(),row.getValue().getText().toString().trim());ed.apply();Toast.makeText(this,"AICoder settings saved",Toast.LENGTH_SHORT).show();}
}
