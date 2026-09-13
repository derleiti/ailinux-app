package me.ailinux.workspace;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

/** Small Android Keystore-backed secret store for AILinux session material. */
final class SecureStore {
    private static final String ALIAS="ailinux-app-session-v1";
    private static final String PREFS="ailinux_app_secure";
    private final SharedPreferences prefs;
    SecureStore(Context c){prefs=c.getSharedPreferences(PREFS,Context.MODE_PRIVATE);}

    private SecretKey key() throws Exception {
        KeyStore ks=KeyStore.getInstance("AndroidKeyStore"); ks.load(null);
        java.security.Key existing=ks.getKey(ALIAS,null);
        if(existing instanceof SecretKey)return (SecretKey)existing;
        KeyGenerator gen=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");
        gen.init(new KeyGenParameterSpec.Builder(ALIAS,KeyProperties.PURPOSE_ENCRYPT|KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build());
        return gen.generateKey();
    }
    void put(String name,String value){
        try{
            Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,key());
            byte[] enc=c.doFinal((value==null?"":value).getBytes(StandardCharsets.UTF_8));
            String packed=Base64.encodeToString(c.getIV(),Base64.NO_WRAP)+"."+Base64.encodeToString(enc,Base64.NO_WRAP);
            prefs.edit().putString(name,packed).apply();
        }catch(Exception e){throw new IllegalStateException("secure storage failed",e);}
    }
    String get(String name){
        String packed=prefs.getString(name,""); if(packed==null||packed.isEmpty())return "";
        try{
            String[] parts=packed.split("\\.",2); if(parts.length!=2)return "";
            byte[] iv=Base64.decode(parts[0],Base64.NO_WRAP),enc=Base64.decode(parts[1],Base64.NO_WRAP);
            Cipher c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,key(),new GCMParameterSpec(128,iv));
            return new String(c.doFinal(enc),StandardCharsets.UTF_8);
        }catch(Exception e){return "";}
    }
    void remove(String name){prefs.edit().remove(name).apply();}
    void clearSession(){prefs.edit().clear().apply();}
}
