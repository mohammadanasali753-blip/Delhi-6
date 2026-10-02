package com.example.myludo;

import android.app.*;import android.os.*;import android.graphics.*;import android.graphics.drawable.*;import android.view.*;import java.net.*;import java.util.*;

public class MainActivity extends Activity{
 GameView v;
 public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(Color.rgb(13,12,20));getWindow().setNavigationBarColor(Color.rgb(13,12,20));v=new GameView();setContentView(v);new Thread(this::listen).start();}
 String ip(){try{for(Enumeration<NetworkInterface>e=NetworkInterface.getNetworkInterfaces();e.hasMoreElements();)for(Enumeration<InetAddress>a=e.nextElement().getInetAddresses();a.hasMoreElements();){InetAddress x=a.nextElement();String s=x.getHostAddress();if(!x.isLoopbackAddress()&&s.indexOf(':')<0)return s;}}catch(Exception e){}return "Not connected";}
 void listen(){try{DatagramSocket s=new DatagramSocket(45454);byte[]b=new byte[64];while(true){DatagramPacket q=new DatagramPacket(b,b.length);s.receive(q);String[]a=new String(q.getData(),0,q.getLength()).trim().split(",");if(a.length==2){int pl=Integer.parseInt(a[0]),d=Integer.parseInt(a[1]);if(pl>=1&&pl<=4&&d>=1&&d<=6)runOnUiThread(()->v.controllerRoll(pl-1,d));}}}catch(Exception e){}}
 class GameView extends View{
  Paint p=new Paint(3);Random rnd=new Random();int screen=0,players=4,turn=0,dice=0;long pulse=0;
  int[][] token=new int[4][4];int[] home={0,0,0,0};int[] ss={1,1,1,1};
  int[] start={0,13,26,39};int[] colors={0xffef4444,0xff3b82f6,0xff22c55e,0xfffacc15};
  int[] snakeA={99,95,92,89,74,64,62,49,47,25},snakeB={80,75,88,68,53,44,19,11,26,5};
  int[] ladA={2,7,8,15,21,28,36,51,71,78},ladB={38,14,31,26,42,84,44,67,91,98};
  GameView(){super(MainActivity.this);p.setTypeface(Typeface.create("sans",Typeface.BOLD));setFocusable(true);}
  void color(int c){p.setColor(c);p.setStyle(Paint.Style.FILL);}
  void txt(Canvas c,String s,float x,float y,float size,int col){p.setTypeface(Typeface.create("sans",Typeface.BOLD));p.setTextSize(size);p.setColor(col);p.setStyle(Paint.Style.FILL);c.drawText(s,x,y,p);}
  void center(Canvas c,String s,float x,float y,float size,int col){p.setTextSize(size);p.setColor(col);p.setTypeface(Typeface.create("sans",Typeface.BOLD));c.drawText(s,x-p.measureText(s)/2,y,p);}
  void rr(Canvas c,float l,float t,float r,float b,float rad,int col){color(col);c.drawRoundRect(l,t,r,b,rad,rad,p);}
  protected void onDraw(Canvas c){super.onDraw(c);color(0xff0d0c14);c.drawRect(0,0,getWidth(),getHeight(),p);if(screen==0)menu(c);else if(screen==1)ludo(c);else if(screen==2)snake(c);else friends(c);}
  void header(Canvas c,String title,String sub){
   txt(c,"‹",22,50,38,Color.WHITE);txt(c,title,62,40,22,Color.WHITE);txt(c,sub,62,61,11,0xffaaa7b7);
   rr(c,getWidth()-58,18,getWidth()-18,58,20,0xff201d2c);center(c,"⋮",getWidth()-38,47,24,0xffd7d3e0);
  }
  void logo(Canvas c,float cx,float cy,float r){
   color(0xff7c3aed);c.drawCircle(cx,cy,r+7,p);color(0xff17131f);c.drawCircle(cx,cy,r,p);
   color(0xfff7d154);c.save();c.rotate(45,cx,cy);c.drawRoundRect(cx-r*.55f,cy-r*.55f,cx+r*.55f,cy+r*.55f,12,12,p);c.restore();
   color(Color.WHITE);for(int i=0;i<6;i++){float a=(float)(Math.PI/3*i);c.drawCircle(cx+(float)Math.cos(a)*r*.25f,cy+(float)Math.sin(a)*r*.25f,5,p);}
   center(c,"L",cx,cy+r*.9f,18,Color.WHITE);
  }
  void menu(Canvas c){
   logo(c,getWidth()/2f,100,45);center(c,"MY LUDO",getWidth()/2f,177,31,Color.WHITE);center(c,"LUDO  •  SAANP-SEEDI",getWidth()/2f,202,13,0xffaaa6b7);
   gameCard(c,30,238,getWidth()-30,345,0xff6d28d9,"LUDO","Classic 2–4 player board game","♟");
   gameCard(c,30,365,getWidth()-30,472,0xff0f766e,"SAANP-SEEDI","Race to 100 • snakes & ladders","◆");
   gameCard(c,30,492,getWidth()-30,599,0xff334155,"FRIENDS","Play together on one phone","♣");
   rr(c,30,625,getWidth()-30,677,26,0xff181621);txt(c,"🎲  DICE CONTROLLER",50,657,14,0xffd7d2e5);txt(c,"Wi-Fi ready",getWidth()-116,657,11,0xff777383);
   center(c,"Professional board • smooth touch controls",getWidth()/2f,718,11,0xff777383);
  }
  void gameCard(Canvas c,float l,float t,float r,float b,int accent,String title,String sub,String icon){
   rr(c,l+2,t+5,r+2,b+5,22,0x55000000);rr(c,l,t,r,b,22,0xff1a1723);rr(c,l,t,l+8,b,8,accent);
   rr(c,l+22,t+21,l+72,t+71,15,accent);center(c,icon,l+47,t+55,24,Color.WHITE);
   txt(c,title,l+88,t+39,19,Color.WHITE);txt(c,sub,l+88,t+62,11,0xffaaa6b7);txt(c,"PLAY  ›",r-83,t+39,11,0xffddd8e8);
  }
  void ludo(Canvas c){
   header(c,"LUDO","Classic • 2–4 players");txt(c,"PLAYER "+(turn+1)+"'S TURN",22,91,11,0xffa9a4b5);
   rr(c,getWidth()-145,72,getWidth()-22,101,15,0xff201c2b);center(c,""+players+" PLAYERS",getWidth()-83,92,10,0xffddd8e8);
   int bottom=170;int S=Math.min(getWidth()-28,getHeight()-bottom-150);float q=S/15f;int L=(getWidth()-S)/2,T=110;
   drawLudoBoard(c,L,T,S,q);drawLudoTokens(c,L,T,q);
   float cy=T+S+42;center(c,"P1  "+home[0]+"   P2  "+home[1]+"   P3  "+home[2]+"   P4  "+home[3],getWidth()/2f,cy,10,0xffa9a4b5);
   rr(c,18,cy+18,getWidth()-18,cy+98,25,0xff191622);txt(c,"DICE",35,cy+47,11,0xff918b9e);drawDice(c,getWidth()/2f,cy+57,58,dice==0?1:dice);
   rr(c,getWidth()-128,cy+30,getWidth()-32,cy+82,20,0xff7c3aed);center(c,"ROLL",getWidth()-80,cy+63,15,Color.WHITE);
   txt(c,"6 = extra turn  •  exact roll to finish",35,cy+119,10,0xff777383);
  }
  void drawLudoBoard(Canvas c,int L,int T,int S,float q){
   rr(c,L-5,T-5,L+S+5,T+S+5,20,0xff25212e);color(Color.WHITE);c.drawRoundRect(L,T,L+S,T+S,15,15,p);
   int[] pos={{0,0},{10,0},{0,10},{10,10}};for(int k=0;k<4;k++){color(colors[k]);c.drawRect(L+pos[k][0]*q,T+pos[k][1]*q,L+(pos[k][0]+5)*q,T+(pos[k][1]+5)*q,p);color(0x44ffffff);c.drawCircle(L+(pos[k][0]+2.5f)*q,T+(pos[k][1]+2.5f)*q,1.7f*q,p);}
   color(0xfff5f5f7);c.drawRect(L+5*q,T,L+10*q,T+S,p);c.drawRect(L,T+5*q,L+S,T+10*q,p);
   for(int i=0;i<15;i++){for(int j=0;j<15;j++){if((i>=5&&i<10)&&(j>=5&&j<10))continue;color(0xffd8d6dc);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);c.drawRect(L+j*q,T+i*q,L+(j+1)*q,T+(i+1)*q,p);p.setStyle(Paint.Style.FILL);}}
   // home lanes
   for(int i=0;i<5;i++){color(colors[0]);c.drawRect(L+(5+i)*q,T+(i)*q,L+(6+i)*q,T+(i+1)*q,p);color(colors[1]);c.drawRect(L+(9-i)*q,T+(i)*q,L+(10-i)*q,T+(i+1)*q,p);color(colors[2]);c.drawRect(L+(i)*q,T+(9-i)*q,L+(i+1)*q,T+(10-i)*q,p);color(colors[3]);c.drawRect(L+(14-i)*q,T+(9+i)*q,L+(15-i)*q,T+(10+i)*q,p);}
   Path tri=new Path();tri.moveTo(L+5*q,T+5*q);tri.lineTo(L+10*q,T+5*q);tri.lineTo(L+7.5f*q,T+7.5f*q);tri.close();color(0xffef4444);c.drawPath(tri,p);tri.reset();tri.moveTo(L+10*q,T+5*q);tri.lineTo(L+10*q,T+10*q);tri.lineTo(L+7.5f*q,T+7.5f*q);tri.close();color(0xff3b82f6);c.drawPath(tri,p);tri.reset();tri.moveTo(L+10*q,T+10*q);tri.lineTo(L+5*q,T+10*q);tri.lineTo(L+7.5f*q,T+7.5f*q);tri.close();color(0xff22c55e);c.drawPath(tri,p);tri.reset();tri.moveTo(L+5*q,T+10*q);tri.lineTo(L+5*q,T+5*q);tri.lineTo(L+7.5f*q,T+7.5f*q);tri.close();color(0xfffacc15);c.drawPath(tri,p);
   color(0x55ffffff);c.drawCircle(L+7.5f*q,T+7.5f*q,1.8f*q,p);
  }
  void drawLudoTokens(Canvas c,int L,int T,float q){
   for(int pl=0;pl<players;pl++)for(int k=0;k<4;k++){int pos=token[pl][k];float x,y;if(pos==0){int bx=pl%2==0?1:11,by=pl<2?1:11;x=L+(bx+(k%2)*2)*q;y=T+(by+(k/2)*2)*q;}else{int cell=(start[pl]+pos-1)%52;float a=(float)(cell%52)/52f*6.283f;x=L+7.5f*q+(float)Math.cos(a)*4.1f*q;y=T+7.5f*q+(float)Math.sin(a)*4.1f*q;}color(0x55000000);c.drawCircle(x+2,y+3,q*.29f,p);color(colors[pl]);c.drawCircle(x,y,q*.29f,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(Color.WHITE);c.drawCircle(x,y,q*.29f,p);p.setStyle(Paint.Style.FILL);color(0xaaFFFFFF);c.drawCircle(x-q*.09f,y-q*.1f,q*.07f,p);}
  }
  void drawDice(Canvas c,float cx,float cy,float s,int n){
   color(0x55000000);c.drawRoundRect(cx-s/2+3,cy-s/2+5,cx+s/2+3,cy+s/2+5,13,13,p);color(Color.WHITE);c.drawRoundRect(cx-s/2,cy-s/2,cx+s/2,cy+s/2,13,13,p);
   color(0xff20202a);float d=s*.21f;float[][]xy={{-1,-1},{1,-1},{0,0},{-1,1},{1,1},{-1,0},{1,0}};int[][] map={{2},{0,4},{0,2,4},{0,1,3,4},{0,1,2,3,4},{0,1,2,3,4,5},{0,1,2,3,4,5,6}};int[]inds=map[Math.max(0,Math.min(6,n-1))];for(int i:inds){float xx=cx+xy[i][0]*d,yy=cy+xy[i][1]*d;if(n==6&&i>=5)yy=cy+xy[i][1]*d;c.drawCircle(xx,yy,s*.075f,p);}
  }
  void rollLudo(int d){dice=d;boolean moved=false;for(int k=0;k<4;k++){int v=token[turn][k];if(v>0&&v<57&&v+d<=57){token[turn][k]=v+d;moved=true;break;}}if(!moved&&d==6)for(int k=0;k<4;k++)if(token[turn][k]==0){token[turn][k]=1;moved=true;break;}for(int k=0;k<4;k++)if(token[turn][k]==57){home[turn]++;token[turn][k]=58;}if(d!=6)turn=(turn+1)%players;invalidate();}
  void controllerRoll(int pl,int d){if(pl<players){turn=pl;rollLudo(d);}}
  void snake(Canvas c){
   header(c,"SAANP-SEEDI","Snakes & Ladders • 2–4 players");txt(c,"PLAYER "+(turn+1)+"'S TURN",22,91,11,0xffa9a4b5);
   int S=Math.min(getWidth()-26,getHeight()-255),L=13,T=108;float q=S/10f;rr(c,L-4,T-4,L+S+4,T+S+4,16,0xff25212e);
   for(int r=0;r<10;r++)for(int cc=0;cc<10;cc++){int n=(9-r)*10+(r%2==0?cc+1:10-cc);color(((r+cc)&1)==0?0xfff6d98e:0xffedc875);c.drawRect(L+cc*q,T+r*q,L+(cc+1)*q,T+(r+1)*q,p);txt(c,""+n,L+cc*q+4,T+r*q+15,9,0xff4b3d2a);}
   p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeWidth(7);for(int i=0;i<snakeA.length;i++)line(c,snakeA[i],snakeB[i],0xffdc3545,L,T,q);for(int i=0;i<ladA.length;i++)line(c,ladA[i],ladB[i],0xff198754,L,T,q);p.setStrokeCap(Paint.Cap.BUTT);
   for(int i=0;i<players;i++){float[]z=cell(ss[i],L,T,q);color(colors[i]);c.drawCircle(z[0],z[1],q*.22f,p);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(Color.WHITE);c.drawCircle(z[0],z[1],q*.22f,p);p.setStyle(Paint.Style.FILL);}
   float y=T+S+28;center(c,"P1 "+ss[0]+"     P2 "+ss[1]+"     P3 "+ss[2]+"     P4 "+ss[3],getWidth()/2f,y,10,0xffaaa6b7);
   rr(c,18,y+18,getWidth()-18,y+100,25,0xff191622);txt(c,"DICE",35,y+48,11,0xff918b9e);drawDice(c,getWidth()/2f,y+58,58,dice==0?1:dice);rr(c,getWidth()-128,y+31,getWidth()-32,y+83,20,0xff0f766e);center(c,"ROLL",getWidth()-80,y+64,15,Color.WHITE);
  }
  float[]cell(int n,int L,int T,float q){int z=n-1,r=9-z/10,c=z%10;if(r%2==1)c=9-c;return new float[]{L+(c+.5f)*q,T+(9-r+.5f)*q};}
  void line(Canvas c,int a,int b,int col,int L,int T,float q){float[]x=cell(a,L,T,q),y=cell(b,L,T,q);p.setColor(col);c.drawLine(x[0],x[1],y[0],y[1],p);}
  void rollSnake(int d){dice=d;int n=Math.min(100,ss[turn]+d);for(int i=0;i<snakeA.length;i++)if(n==snakeA[i])n=snakeB[i];for(int i=0;i<ladA.length;i++)if(n==ladA[i])n=ladB[i];ss[turn]=n;if(n<100&&d!=6)turn=(turn+1)%players;invalidate();}
  void friends(Canvas c){header(c,"FRIENDS","Local multiplayer");center(c,"PLAY TOGETHER",getWidth()/2f,135,25,Color.WHITE);center(c,"Same phone • 2 to 4 players",getWidth()/2f,161,13,0xffaaa6b7);for(int i=2;i<=4;i++){float y=205+(i-2)*82;rr(c,30,y,getWidth()-30,y+62,20,0xff1b1824);txt(c,i+" PLAYERS",55,y+39,17,Color.WHITE);txt(c,"SELECT  ›",getWidth()-116,y+39,12,0xffb7a5ff);}txt(c,"Internet friends need a backend/server.",30,475,12,0xffffd166);txt(c,"Local play is ready in this build.",30,497,12,0xff8f899b);}
  public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_UP)return true;float x=e.getX(),y=e.getY();
   if(screen==0){if(y>225&&y<355)screen=1;else if(y>355&&y<482)screen=2;else if(y>482&&y<610)screen=3;}
   else if(y<70){screen=0;}
   else if(screen==1&&y>getHeight()-210)rollLudo(1+rnd.nextInt(6));
   else if(screen==2&&y>getHeight()-150)rollSnake(1+rnd.nextInt(6));
   else if(screen==3&&y>190&&y<500){int n=2+(int)((y-190)/82);if(n>=2&&n<=4){players=n;screen=1;}}
   invalidate();return true;}
 }
}