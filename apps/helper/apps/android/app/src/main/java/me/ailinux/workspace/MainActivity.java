package me.ailinux.workspace;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.text.InputType;
import android.util.Base64;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.*;

/** Unified AILinux App: AICoder + Helper account, AI, network and workspace UI. */
public class MainActivity extends Activity {
    private static final String REDIRECT="https://api.ailinux.me/v1/auth/browser/app-callback/ailinux-client";
    private AppApiClient api; private SecureStore secrets; private SharedPreferences local; private StateStore workspaceState; private TermuxShell termux;
    private TextView accountStatus,aiOutput,networkOutput,termuxOutput;private EditText email,password,prompt,handle,termuxCommand;private Spinner models;private ArrayList<String> modelIds=new ArrayList<>();
    @Override public void onCreate(Bundle b){super.onCreate(b);api=new AppApiClient(this);secrets=new SecureStore(this);local=getSharedPreferences("ailinux_app",MODE_PRIVATE);workspaceState=new StateStore(this);termux=new TermuxShell(this,workspaceState);buildUi();handleIntent(getIntent());refreshAccount();}
    @Override protected void onNewIntent(Intent i){super.onNewIntent(i);setIntent(i);handleIntent(i);}
    private TextView text(String s,int size,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextColor(Color.WHITE);v.setTextSize(size);if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);v.setPadding(0,7,0,7);return v;}
    private Button button(String s){Button b=new Button(this);b.setText(s);b.setAllCaps(false);return b;}
    private EditText input(String hint,boolean secret){EditText e=new EditText(this);e.setHint(hint);e.setTextColor(Color.WHITE);e.setHintTextColor(0xff8b949e);e.setBackgroundColor(0xff161b22);e.setPadding(16,14,16,14);if(secret)e.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);return e;}
    private void heading(LinearLayout root,String title){TextView h=text(title,21,true);h.setTextColor(0xff79c0ff);h.setPadding(0,28,0,8);root.addView(h);}
    private void buildUi(){
        ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(0xff0d1117);LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(34,34,34,64);scroll.addView(root);
        root.addView(text("AILinux App",31,true));TextView sub=text("AICoder + Helper · one account, one @handle network, one capability fabric",14,false);sub.setTextColor(0xffb8c1cc);root.addView(sub);

        heading(root,"Account");accountStatus=text("Not signed in",14,false);root.addView(accountStatus);email=input("WordPress / AILinux email",false);root.addView(email);password=input("Password",true);root.addView(password);
        Button login=button("Sign in with WordPress / password");login.setOnClickListener(v->manualLogin());root.addView(login);
        Button google=button("Continue with Google in browser");google.setOnClickListener(v->browserLogin());root.addView(google);
        Button logout=button("Sign out");logout.setOnClickListener(v->{api.logout();local.edit().remove("endpoint_id").apply();refreshAccount();});root.addView(logout);

        heading(root,"AI / AICoder");
        models=new Spinner(this);root.addView(models);Button load=button("Load available models");load.setOnClickListener(v->loadModels());root.addView(load);
        prompt=input("Ask AICoder / TriForce…",false);prompt.setMinLines(3);prompt.setSingleLine(false);root.addView(prompt);Button ask=button("Send to AI");ask.setOnClickListener(v->chat());root.addView(ask);
        aiOutput=text("",14,false);aiOutput.setTextIsSelectable(true);root.addView(aiOutput);

        heading(root,"AI Network · @handle");
        handle=input("username handle, e.g. markus-android",false);handle.setText(local.getString("handle",""));root.addView(handle);
        Button publish=button("Publish / refresh this device");publish.setOnClickListener(v->publishHandle());root.addView(publish);
        Button directory=button("Open network directory");directory.setOnClickListener(v->directory());root.addView(directory);
        networkOutput=text("",13,false);networkOutput.setTextIsSelectable(true);root.addView(networkOutput);

        heading(root,"Workspace & Device Sharing");
        root.addView(text("The original Helper executor remains intact: Android SAF workspace, read/write grants, reconnectable leases, Termux special function and device resource preferences.",13,false));
        Button workspace=button("Open Workspace & Device Sharing");workspace.setOnClickListener(v->startActivity(new Intent(this,WorkspaceActivity.class)));root.addView(workspace);
        Button mcp=button("Open public MCP / pairing page");mcp.setOnClickListener(v->startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://api.ailinux.me/v1/mcp"))));root.addView(mcp);

        heading(root,"Full AICoder runtime · Termux");
        root.addView(text("Runs the complete imported AICoder CLI/agent runtime inside your user-controlled Termux installation. This local shell is never advertised to the AI network unless you explicitly share a separate capability.",13,false));
        Button runtimeAccess=button("Open Termux access / workspace controls");runtimeAccess.setOnClickListener(v->startActivity(new Intent(this,WorkspaceActivity.class)));root.addView(runtimeAccess);
        Button installRuntime=button("Install / update full AICoder runtime");installRuntime.setOnClickListener(v->installAICoderRuntime());root.addView(installRuntime);
        termuxCommand=input("AICoder command, e.g. aicoder status",false);termuxCommand.setText("aicoder status");root.addView(termuxCommand);
        Button runRuntime=button("Run AICoder command in Termux");runRuntime.setOnClickListener(v->runAICoderCommand());root.addView(runRuntime);
        Button agentRuntime=button("Run current AI prompt as full AICoder agent");agentRuntime.setOnClickListener(v->runPromptAsAgent());root.addView(agentRuntime);
        termuxOutput=text("Termux: "+termux.detail(),12,false);termuxOutput.setTextIsSelectable(true);root.addView(termuxOutput);

        heading(root,"Settings");
        root.addView(text("All canonical AICoder settings are rendered from the same registry contract used by desktop AICoder.",13,false));
        Button settings=button("AICoder settings");settings.setOnClickListener(v->startActivity(new Intent(this,SettingsActivity.class)));root.addView(settings);
        Button provider=button("Provider account runtimes / Termux");provider.setOnClickListener(v->showProviderInfo());root.addView(provider);
        setContentView(scroll);
    }
    private void ui(Runnable r){runOnUiThread(r);}
    private void refreshAccount(){String label=api.loggedIn()?"Signed in: "+api.email()+" · "+api.name():"Not signed in";accountStatus.setText(label);if(api.loggedIn()&&handle.getText().toString().trim().isEmpty()){String base=api.email().split("@")[0].toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]","-");handle.setText(base+"-android");}}
    private void manualLogin(){String e=email.getText().toString().trim(),p=password.getText().toString();if(e.isEmpty()||p.isEmpty()){accountStatus.setText("Email and password required");return;}accountStatus.setText("Signing in…");api.login(e,p,(d,err)->ui(()->{if(err!=null)accountStatus.setText("Login failed: "+err.getMessage());else{password.setText("");refreshAccount();publishHandle();}}));}
    private static String b64(byte[] b){return Base64.encodeToString(b,Base64.URL_SAFE|Base64.NO_WRAP|Base64.NO_PADDING);}
    private void browserLogin(){try{byte[] raw=new byte[64];new SecureRandom().nextBytes(raw);String verifier=b64(raw);String challenge=b64(MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII)));String state=b64(random(32));secrets.put("pkce_verifier",verifier);secrets.put("pkce_state",state);Uri url=Uri.parse("https://login.ailinux.me/").buildUpon().appendQueryParameter("google","1").appendQueryParameter("app_login","1").appendQueryParameter("app_id","ailinux-client").appendQueryParameter("redirect_uri",REDIRECT).appendQueryParameter("code_challenge",challenge).appendQueryParameter("code_challenge_method","S256").appendQueryParameter("state",state).build();accountStatus.setText("Complete Google / AILinux login in browser…");startActivity(new Intent(Intent.ACTION_VIEW,url));}catch(Exception e){accountStatus.setText("Browser login error: "+e.getMessage());}}
    private byte[] random(int n){byte[] b=new byte[n];new SecureRandom().nextBytes(b);return b;}
    private void handleIntent(Intent intent){if(intent==null||intent.getData()==null)return;Uri u=intent.getData();if(u.toString().startsWith(REDIRECT)){String fragment=u.getFragment();if(fragment==null)fragment="";Uri parsed=Uri.parse("https://callback/?"+fragment);String code=parsed.getQueryParameter("code"),state=parsed.getQueryParameter("state");String expected=secrets.get("pkce_state"),verifier=secrets.get("pkce_verifier");if(code!=null&&!code.isEmpty()&&state!=null&&state.equals(expected)&&!verifier.isEmpty()){accountStatus.setText("Completing browser login…");api.exchangeBrowser(code,verifier,REDIRECT,(d,err)->ui(()->{secrets.remove("pkce_state");secrets.remove("pkce_verifier");if(err!=null)accountStatus.setText("Browser login failed: "+err.getMessage());else{refreshAccount();publishHandle();}}));}return;}
        String scheme=u.getScheme()==null?"":u.getScheme();if((scheme.equals("ailinux-helper")||scheme.equals("ailinux-workspace")||"api.ailinux.me".equals(u.getHost()))){Intent w=new Intent(this,WorkspaceActivity.class).setData(u);startActivity(w);}}
    private void loadModels(){aiOutput.setText("Loading model catalog…");api.models((d,e)->ui(()->{if(e!=null){aiOutput.setText("Models failed: "+e.getMessage());return;}modelIds.clear();JSONArray a=d.optJSONArray("models");if(a==null&&d.optJSONObject("data")!=null)a=d.optJSONObject("data").optJSONArray("models");if(a!=null)for(int i=0;i<a.length();i++){Object row=a.opt(i);if(row instanceof JSONObject){JSONObject o=(JSONObject)row;String id=o.optString("id",o.optString("name",""));if(!id.isEmpty())modelIds.add(id);}else if(row!=null)modelIds.add(String.valueOf(row));}if(modelIds.isEmpty())modelIds.add("");ArrayAdapter<String> ad=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,modelIds);models.setAdapter(ad);aiOutput.setText("Loaded "+Math.max(0,modelIds.size()-(modelIds.size()==1&&modelIds.get(0).isEmpty()?1:0))+" models");}));}
    private void chat(){String q=prompt.getText().toString().trim();if(q.isEmpty())return;String model=models.getSelectedItem()==null?"":String.valueOf(models.getSelectedItem());int max=16384;try{max=Integer.parseInt(getSharedPreferences("aicoder_android_settings",MODE_PRIVATE).getString("max_output_tokens","16384"));}catch(Exception ignored){}aiOutput.setText("Thinking with "+(model.isEmpty()?"backend default":model)+"…");api.chat(q,model,max,(d,e)->ui(()->{if(e!=null){aiOutput.setText("AI failed: "+e.getMessage());return;}String out=d.optString("response",d.optString("content",d.optString("message","")));if(out.isEmpty()){JSONObject msg=d.optJSONObject("message");if(msg!=null)out=msg.optString("content",msg.toString());}if(out.isEmpty())out=d.toString();aiOutput.setText(out);}));}
    private String deviceId(){String saved=local.getString("device_id","");if(!saved.isEmpty())return saved;String android=Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);String id="android-"+(android==null||android.isEmpty()?UUID.randomUUID().toString():android);local.edit().putString("device_id",id).apply();return id;}
    private void publishHandle(){if(!api.loggedIn()){networkOutput.setText("Sign in first");return;}String h=handle.getText().toString().trim().toLowerCase(Locale.ROOT).replaceFirst("^@","");if(h.length()<2){networkOutput.setText("Handle must be at least 2 characters");return;}String eid=local.getString("endpoint_id","");networkOutput.setText("Publishing @"+h+"…");api.registerEndpoint(deviceId(),h,eid,(d,e)->ui(()->{if(e!=null){networkOutput.setText("Publish failed: "+e.getMessage());return;}JSONObject ep=d.optJSONObject("endpoint");if(ep==null){networkOutput.setText("Invalid endpoint response");return;}String id=ep.optString("endpoint_id","");String actual=ep.optString("handle",h);local.edit().putString("endpoint_id",id).putString("handle",actual.replaceFirst("^@","")).apply();handle.setText(actual.replaceFirst("^@",""));api.presence(id,(pd,pe)->{});networkOutput.setText("Online as "+(actual.startsWith("@")?actual:"@"+actual)+" · endpoint "+id);}));}
    private void directory(){if(!api.loggedIn()){networkOutput.setText("Sign in first");return;}networkOutput.setText("Loading network…");api.directory((d,e)->ui(()->{if(e!=null){networkOutput.setText("Directory failed: "+e.getMessage());return;}JSONArray a=d.optJSONArray("endpoints");if(a==null){networkOutput.setText(d.toString());return;}StringBuilder b=new StringBuilder();for(int i=0;i<a.length();i++){JSONObject ep=a.optJSONObject(i);if(ep==null)continue;b.append(ep.optString("handle","?")).append(" · ").append(ep.optString("kind","client")).append(" · ").append(ep.optBoolean("online",false)?"online":"offline").append('\n');}networkOutput.setText(b.length()==0?"No visible endpoints":b.toString());}));}
    private String shellQuote(String value){return "'"+(value==null?"":value).replace("'","'\\''")+"'";}
    private String termuxText(JSONObject result){try{JSONArray content=result.optJSONArray("content");if(content!=null&&content.length()>0){JSONObject row=content.optJSONObject(0);if(row!=null)return row.optString("text",result.toString());}}catch(Exception ignored){}return result==null?"No result":result.toString();}
    private void runTermux(String command,String label){if(!termux.released()){termuxOutput.setText("Termux is closed: "+termux.detail()+". Open Workspace & Device Sharing and explicitly enable local Termux access first.");return;}termuxOutput.setText(label+"…");new Thread(()->{try{JSONObject out=termux.run(command,".",300,workspaceState.tree());ui(()->termuxOutput.setText(termuxText(out)));}catch(Exception e){ui(()->termuxOutput.setText(label+" failed: "+e.getMessage()));}},"ailinux-app-termux").start();}
    private void installAICoderRuntime(){String cmd="pkg install -y python git && python -m pip install --upgrade pip && python -m pip install --upgrade --force-reinstall 'git+https://github.com/derleiti/ailinux-app.git#subdirectory=core/aicoder' && aicoder --version";runTermux(cmd,"Installing AICoder runtime");}
    private void runAICoderCommand(){String cmd=termuxCommand.getText().toString().trim();if(cmd.isEmpty())cmd="aicoder status";if(!(cmd.equals("aicoder")||cmd.startsWith("aicoder "))){termuxOutput.setText("Only AICoder commands are accepted here. Use the Workspace Termux function for arbitrary local shell commands.");return;}runTermux(cmd,"Running AICoder");}
    private void runPromptAsAgent(){String q=prompt.getText().toString().trim();if(q.isEmpty()){termuxOutput.setText("Enter an AI/agent prompt first.");return;}runTermux("aicoder agent "+shellQuote(q),"Running full AICoder agent");}
    private void showProviderInfo(){new AlertDialog.Builder(this).setTitle("Provider account runtimes").setMessage("AILinux account login is native on Android: WordPress/password or Google browser PKCE. Provider-owned CLI sessions used by desktop AICoder (ChatGPT/Codex, Claude Code, Mistral Vibe, Google Antigravity, Grok) stay owned by those official clients. On Android they are executed through the user-released Termux runtime where available; credentials are never copied into AILinux App state.").setPositiveButton("Open Workspace / Termux",(d,w)->startActivity(new Intent(this,WorkspaceActivity.class))).setNegativeButton("Close",null).show();}
}
