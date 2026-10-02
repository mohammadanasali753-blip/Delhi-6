package com.example.myludo;
import android.app.*; import android.os.*; import android.graphics.Color; import android.view.*; import android.widget.*; import java.net.*; import java.io.*;
public class MainActivity extends Activity {
 TextView dice,status; volatile boolean running=true;
 public void onCreate(Bundle b){super.onCreate(b); LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setPadding(18,18,18,18); root.setBackgroundColor(Color.rgb(23,21,31));
 TextView title=t("🎲  MY LUDO",28); root.addView(title); TextView info=t("Dice Controller connected via local network.\nPort: 45454",14);root.addView(info);
 dice=t("–",72);dice.setGravity(17);root.addView(dice,new LinearLayout.LayoutParams(-1,180)); status=t("Player 1 की बारी",18);status.setGravity(17);root.addView(status);
 Button roll=new Button(this);roll.setText("ROLL DICE");root.addView(roll);roll.setOnClickListener(v->{int n=1+(int)(Math.random()*6);show(n);});
 Button reset=new Button(this);reset.setText("RESET");root.addView(reset);reset.setOnClickListener(v->{dice.setText("–");status.setText("Player 1 की बारी");});
 setContentView(root); new Thread(this::listen).start();}
 TextView t(String s,int z){TextView v=new TextView(this);v.setText(s);v.setTextColor(Color.WHITE);v.setTextSize(z);v.setPadding(8,14,8,14);return v;}
 void show(int n){runOnUiThread(()->{dice.setText(String.valueOf(n));status.setText("Dice: "+n);});}
 void listen(){try{DatagramSocket s=new DatagramSocket(45454);byte[] b=new byte[32];while(running){DatagramPacket p=new DatagramPacket(b,b.length);s.receive(p);int n=Integer.parseInt(new String(p.getData(),0,p.getLength()).trim());if(n>=1&&n<=6)show(n);}}catch(Exception e){}}
 protected void onDestroy(){running=false;super.onDestroy();}
}
