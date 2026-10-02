package com.example.myludo;

import android.app.*;import android.os.*;import android.graphics.*;import android.view.*;import java.net.*;import java.util.*;

public class MainActivity extends Activity{
 GameView v;
 public void onCreate(Bundle b){super.onCreate(b);v=new GameView();setContentView(v);new Thread(this::listen).start();}
 String ip(){try{for(Enumeration<NetworkInterface> e=NetworkInterface.getNetworkInterfaces();e.hasMoreElements();){for(Enumeration<InetAddress>a=e.nextElement().getInetAddresses();a.hasMoreElements();){InetAddress x=a.nextElement();String s=x.getHostAddress();if(!x.isLoopbackAddress()&&s.indexOf(':')<0)return s;}}}catch(Exception e){}return "unknown";}
 void listen(){try{DatagramSocket s=new DatagramSocket(45454);byte[]b=new byte[64];while(true){DatagramPacket q=new DatagramPacket(b,b.length);s.receive(q);String[]a=new String(q.getData(),0,q.getLength()).trim().split(",");if(a.length==2){int p=Integer.parseInt(a[0]),d=Integer.parseInt(a[1]);if(p>=1&&p<=4&&d>=1&&d<=6)runOnUiThread(()->v.controllerRoll(p-1,d));}}}catch(Exception e){}}
 class GameView extends View{
  Paint p=new Paint(1);Random r=new Random();int screen=0,players=4,turn=0,dice=0;boolean rolling=false;
  int[][] token=new int[4][4];int[] home={0,0,0,0};int[] safe={0,8,13,21,26,34,39,47};int[] start={0,13,26,39};
  int[] snakeA={99,95,92,89,74,64,62,49,47,25},snakeB={80,75,88,68,53,44,19,11,26,5};
  int[] ladA={2,7,8,15,21,28,36,51,71,78},ladB={38,14,31,26,42,84,44,67,91,98};
  int[] colors={0xffe53935,0xff1e88e5,0xff43a047,0xfffbc02d};
  GameView(){super(MainActivity.this);p.setTypeface(Typeface.DEFAULT_BOLD);setFocusable(true);}
  void txt(Canvas c,String s,float x,float y,float z,int col){p.setTextSize(z);p.setColor(col);p.setStyle(Paint.Style.FILL);c.drawText(s,x,y,p);}
  void rect(Canvas c,float l,float t,float rr,float bb,int col){p.setColor(col);p.setStyle(Paint.Style.FILL);c.drawRoundRect(l,t,rr,bb,18,18,p);}
  protected void onDraw(Canvas c){c.drawColor(0xff10131c);if(screen==0)menu(c);else if(screen==1)ludo(c);else if(screen==2)snake(c);else friends(c);}
  void menu(Canvas c){txt(c,"MY LUDO",28,65,40,Color.WHITE);txt(c,"Proper Ludo + Saanp-Seedi",30,94,16,0xffbbbbbb);button(c,125,"LUDO");button(c,205,"SAANP-SEEDI");button(c,285,"PLAY WITH FRIENDS");txt(c,"Wi-Fi: "+ip(),30,395,14,0xffdddddd);txt(c,"Dice Controller: same Wi-Fi",30,420,13,0xff888888);}
  void button(Canvas c,int y,String s){rect(c,25,y,getWidth()-25,y+60,0xff3f3570);txt(c,s,48,y+39,19,Color.WHITE);}
  void top(Canvas c,String s){txt(c,"<  "+s,20,48,27,Color.WHITE);}
  void ludo(Canvas c){
   top(c,"LUDO");txt(c,"Players: "+players+"    Turn: P"+(turn+1),20,78,16,0xffdddddd);
   int S=Math.min(getWidth()-24,getHeight()-230),L=(getWidth()-S)/2,T=92;float q=S/15f;
   p.setColor(Color.WHITE);c.drawRect(L,T,L+S,T+S,p);
   for(int k=0;k<4;k++){int x=(k==1||k==2)?10:0,y=(k>=2)?10:0;p.setColor(colors[k]);c.drawRoundRect(L+x*q+2,T+y*q+2,L+(x+5)*q-2,T+(y+5)*q-2,14,14,p);}
   p.setColor(0xffeeeeee);for(int i=0;i<15;i++){c.drawRect(L+5*q,T+i*q,L+10*q,T+(i+1)*q,p);c.drawRect(L+i*q,T+5*q,L+(i+1)*q,T+10*q,p);}
   for(int i=0;i<=15;i++){p.setColor(0xff999999);p.setStyle(Paint.Style.STROKE);c.drawLine(L,T+i*q,L+S,T+i*q,p);c.drawLine(L+i*q,T,L+i*q,T+S,p);}p.setStyle(Paint.Style.FILL);
   drawTokens(c,L,T,q);
   txt(c,"P1 "+home[0]+"/4    P2 "+home[1]+"/4",20,T+S+25,13,0xffeeeeee);txt(c,"P3 "+home[2]+"/4    P4 "+home[3]+"/4",20,T+S+46,13,0xffeeeeee);
   rect(c,25,T+S+60,getWidth()-25,T+S+120,0xff3f3570);txt(c,dice==0?"ROLL DICE":"DICE: "+dice,55,T+S+98,21,Color.WHITE);
   txt(c,"6 = extra turn • exact roll required for home",25,T+S+145,12,0xff888888);
  }
  void drawTokens(Canvas c,int L,int T,float q){for(int pl=0;pl<players;pl++)for(int k=0;k<4;k++){int pos=token[pl][k];float x,y;if(pos==0){int bx=pl%2==0?2:11,by=pl<2?2:11;x=L+(bx+(k%2))*q; y=T+(by+(k/2))*q;}else if(pos>=57){x=L+7.5f*q;y=T+7.5f*q;}else{int cell=(start[pl]+pos-1)%52;int row=cell<26?12-(cell/13):cell/13;int col=cell<13?cell:cell<26?12-(cell-13):cell<39?cell-26:12-(cell-39);x=L+(col+.5f)*q;y=T+(row+.5f)*q;}p.setColor(colors[pl]);c.drawCircle(x,y,q*.28f,p);p.setStyle(Paint.Style.STROKE);p.setColor(Color.WHITE);c.drawCircle(x,y,q*.28f,p);p.setStyle(Paint.Style.FILL);}}
  void rollLudo(int d){dice=d;rolling=false;boolean moved=false;for(int k=0;k<4;k++){int v=token[turn][k];if(v>0&&v<57&&v+d<=57){token[turn][k]=v+d;moved=true;break;}}if(!moved&&d==6){for(int k=0;k<4;k++)if(token[turn][k]==0){token[turn][k]=1;moved=true;break;}}for(int k=0;k<4;k++)if(token[turn][k]==57){home[turn]++;token[turn][k]=58;}if(d!=6)turn=(turn+1)%players;invalidate();}
  void controllerRoll(int p0,int d){if(p0>=players)return;turn=p0;rollLudo(d);}
  void snake(Canvas c){
   top(c,"SAANP-SEEDI");int S=Math.min(getWidth()-18,getHeight()-145),L=9,T=62;float q=S/10f;
   for(int r=0;r<10;r++)for(int cc=0;cc<10;cc++){p.setColor(((r+cc)&1)==0?0xffffe7a8:0xfff2cf8d);c.drawRect(L+cc*q,T+r*q,L+(cc+1)*q,T+(r+1)*q,p);int n=(9-r)*10+(r%2==0?cc+1:10-cc);txt(c,""+n,L+cc*q+3,T+r*q+14,10,0xff5b4a35);}
   p.setStrokeWidth(7);for(int i=0;i<snakeA.length;i++)line(c,snakeA[i],snakeB[i],0xffd32f2f,L,T,q);for(int i=0;i<ladA.length;i++)line(c,ladA[i],ladB[i],0xff2e7d32,L,T,q);
   txt(c,"P1 "+pos(0)+"   P2 "+pos(1),15,T+S+24,13,Color.WHITE);txt(c,"P3 "+pos(2)+"   P4 "+pos(3),15,T+S+45,13,Color.WHITE);
   rect(c,20,T+S+58,getWidth()-20,T+S+118,0xff3f3570);txt(c,"DICE: "+(dice==0?"ROLL":dice),50,T+S+96,21,Color.WHITE);
  }
  int[] ss={1,1,1,1};int pos(int i){return ss[i];}
  float[] cell(int n,int L,int T,float q){int z=n-1,r=9-z/10,c=z%10;if(r%2==1)c=9-c;return new float[]{L+(c+.5f)*q,T+(9-r+.5f)*q};}
  void line(Canvas c,int a,int b,int col,int L,int T,float q){float[]x=cell(a,L,T,q),y=cell(b,L,T,q);p.setColor(col);c.drawLine(x[0],x[1],y[0],y[1],p);}
  void rollSnake(int d){dice=d;int n=Math.min(100,ss[turn]+d);for(int i=0;i<snakeA.length;i++)if(n==snakeA[i])n=snakeB[i];for(int i=0;i<ladA.length;i++)if(n==ladA[i])n=ladB[i];ss[turn]=n;if(n<100||d!=6)turn=(turn+1)%players;invalidate();}
  void friends(Canvas c){top(c,"PLAY WITH FRIENDS");txt(c,"LOCAL FRIENDS",25,110,25,Color.WHITE);txt(c,"Ek phone par 2–4 players",25,140,15,0xffcccccc);button(c,175,"2 PLAYERS");button(c,245,"3 PLAYERS");button(c,315,"4 PLAYERS");txt(c,"Online internet room ke liye server/backend",25,410,13,0xffffcc66);txt(c,"chahiye; is build me local friends ready hain.",25,433,13,0xffaaaaaa);}
  public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=1)return true;float y=e.getY();if(screen==0){if(y>120&&y<195)screen=1;else if(y>195&&y<275)screen=2;else if(y>275&&y<360)screen=3;}else if(y<65)screen=0;else if(screen==1&&y>getHeight()-180)rollLudo(1+r.nextInt(6));else if(screen==2&&y>getHeight()-140)rollSnake(1+r.nextInt(6));else if(screen==3){if(y>170&&y<240)players=2;else if(y<315&&y>240)players=3;else if(y<390&&y>315)players=4;}invalidate();return true;}
 }
}