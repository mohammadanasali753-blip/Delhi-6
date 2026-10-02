package com.example.dicecontroller;
import android.app.*; import android.os.*; import android.graphics.Color; import android.view.*; import android.widget.*; import java.net.*;
public class MainActivity extends Activity {
 EditText ip; TextView msg;
 public void onCreate(Bundle b){super.onCreate(b); LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(24,24,24,24);root.setBackgroundColor(Color.rgb(16,16,20));
 TextView h=new TextView(this);h.setText("🎛️  DICE CONTROLLER");h.setTextColor(Color.WHITE);h.setTextSize(28);root.addView(h);
 TextView l=new TextView(this);l.setText("My Ludo का IP address डालें (same Wi‑Fi पर). Same phone हो तो 127.0.0.1");l.setTextColor(Color.WHITE);l.setTextSize(15);root.addView(l);
 ip=new EditText(this);ip.setText("127.0.0.1");ip.setTextColor(Color.WHITE);ip.setHintTextColor(Color.GRAY);root.addView(ip);
 GridLayout g=new GridLayout(this);g.setColumnCount(2);root.addView(g);
 for(int n=1;n<=6;n++){Button x=new Button(this);x.setText("DICE "+n);x.setTextSize(20);g.addView(x,new ViewGroup.LayoutParams(-1,140));final int v=n;x.setOnClickListener(z->send(v));}
 Button r=new Button(this);r.setText("🎲 RANDOM");root.addView(r);r.setOnClickListener(v->send(1+(int)(Math.random()*6)));
 msg=new TextView(this);msg.setTextColor(Color.LTGRAY);msg.setTextSize(16);msg.setPadding(0,20,0,0);root.addView(msg);setContentView(root);}
 void send(int n){new Thread(()->{try{DatagramSocket s=new DatagramSocket();byte[] b=String.valueOf(n).getBytes();s.send(new DatagramPacket(b,b.length,InetAddress.getByName(ip.getText().toString().trim()),45454));s.close();runOnUiThread(()->msg.setText("Sent dice: "+n));}catch(Exception e){runOnUiThread(()->msg.setText("Connection error: "+e.getMessage()));}}).start();}
}
