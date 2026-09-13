package me.ailinux.workspace;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import okhttp3.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/** Native API client shared by Android account, AI and @handle network UI. */
final class AppApiClient {
    static final String BASE="https://api.ailinux.me";
    interface JsonCallback { void done(JSONObject data, Exception error); }
    private final OkHttpClient http=new OkHttpClient.Builder().connectTimeout(15,TimeUnit.SECONDS).readTimeout(320,TimeUnit.SECONDS).writeTimeout(30,TimeUnit.SECONDS).build();
    private final SecureStore secrets;
    AppApiClient(Context c){secrets=new SecureStore(c);}
    String token(){return secrets.get("token");}
    boolean loggedIn(){return !token().isEmpty();}
    void logout(){secrets.clearSession();}
    private void request(String method,String path,JSONObject body,boolean auth,JsonCallback cb){
        try{
            Request.Builder b=new Request.Builder().url(BASE+path).header("Accept","application/json");
            if(auth&&!token().isEmpty())b.header("Authorization","Bearer "+token());
            RequestBody rb=body==null?null:RequestBody.create(body.toString(),MediaType.parse("application/json"));
            if("GET".equals(method))b.get();else if("DELETE".equals(method))b.delete();else b.method(method,rb==null?RequestBody.create(new byte[0],null):rb);
            http.newCall(b.build()).enqueue(new Callback(){
                public void onFailure(Call call,IOException e){cb.done(null,e);}
                public void onResponse(Call call,Response response)throws IOException{
                    try(Response r=response){String text=r.body()==null?"":r.body().string();JSONObject out;
                        try{out=text.isEmpty()?new JSONObject():new JSONObject(text);}catch(Exception ex){out=new JSONObject().put("raw",text);}
                        if(!r.isSuccessful()){cb.done(out,new IOException("HTTP "+r.code()+" · "+out.optString("detail",out.optString("raw","request failed"))));return;}
                        cb.done(out,null);
                    }catch(Exception e){cb.done(null,e);}
                }
            });
        }catch(Exception e){cb.done(null,e);}
    }
    private void saveSession(JSONObject data){
        String t=data.optString("token","");if(t.isEmpty())throw new IllegalArgumentException("login returned no token");
        secrets.put("token",t);secrets.put("email",data.optString("email",""));secrets.put("name",data.optString("name",""));secrets.put("client_id",data.optString("client_id",""));secrets.put("tier",data.optString("tier",""));
    }
    String email(){return secrets.get("email");} String name(){return secrets.get("name");} String clientId(){return secrets.get("client_id");}
    void login(String email,String password,JsonCallback cb){try{request("POST","/v1/auth/login",new JSONObject().put("email",email).put("password",password),false,(d,e)->{if(e==null)try{saveSession(d);}catch(Exception x){e=x;}cb.done(d,e);});}catch(Exception e){cb.done(null,e);}}
    void exchangeBrowser(String code,String verifier,String redirect,JsonCallback cb){try{JSONObject p=new JSONObject().put("code",code).put("purpose","app").put("app_id","ailinux-client").put("redirect_uri",redirect).put("code_verifier",verifier);request("POST","/v1/auth/browser/exchange",p,false,(d,e)->{if(e==null)try{saveSession(d);}catch(Exception x){e=x;}cb.done(d,e);});}catch(Exception e){cb.done(null,e);}}
    void verify(JsonCallback cb){request("GET","/v1/auth/verify",null,true,cb);}
    void models(JsonCallback cb){request("GET","/v1/client/models",null,loggedIn(),cb);}
    void chat(String text,String model,int maxTokens,JsonCallback cb){try{JSONObject p=new JSONObject().put("message",text).put("temperature",0.7).put("max_tokens",maxTokens);if(model!=null&&!model.isEmpty())p.put("model",model);request("POST","/v1/client/chat",p,loggedIn(),cb);}catch(Exception e){cb.done(null,e);}}
    void registerEndpoint(String deviceId,String handle,String endpointId,JsonCallback cb){try{JSONObject p=new JSONObject().put("device_id",deviceId).put("handle",handle).put("endpoint_id",endpointId==null?"":endpointId).put("kind","client").put("label","AILinux App Android").put("visibility","account").put("transport","mailbox").put("ttl_seconds",300).put("capabilities",new JSONArray().put("chat").put("ai").put("notify").put("workspace").put("mcp"));request("POST","/v1/notify-network/endpoints",p,true,cb);}catch(Exception e){cb.done(null,e);}}
    void presence(String endpointId,JsonCallback cb){try{request("POST","/v1/notify-network/presence",new JSONObject().put("endpoint_id",endpointId).put("availability","available").put("activity","idle").put("accept_human_chat",true).put("accept_ai_chat",true).put("accept_tasks",true).put("ttl_seconds",300),true,cb);}catch(Exception e){cb.done(null,e);}}
    void directory(JsonCallback cb){request("GET","/v1/notify-network/directory?include_offline=true",null,true,cb);}
    void renameHandle(String endpointId,String handle,JsonCallback cb){try{request("POST","/v1/notify-network/handles/rename",new JSONObject().put("endpoint_id",endpointId).put("handle",handle),true,cb);}catch(Exception e){cb.done(null,e);}}
}
