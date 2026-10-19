package android.content;public abstract class BroadcastReceiver {public static int uid=42;public int getSentFromUid(){return uid;}public abstract void onReceive(Context c,Intent i);}
