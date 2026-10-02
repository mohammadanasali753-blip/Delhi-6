package com.example.myludo;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.*;
import android.view.*;
import java.net.*;
import java.util.*;

public class MainActivity extends Activity {
    GameView game;
    public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(Color.rgb(10,8,18));
        getWindow().setNavigationBarColor(Color.rgb(10,8,18));
        game=new GameView();
        setContentView(game);
        new Thread(this::listenController).start();
    }
    void listenController(){
        try{
            DatagramSocket s=new DatagramSocket(45454);
            byte[] buf=new byte[64];
            while(true){
                DatagramPacket p=new DatagramPacket(buf,buf.length);
                s.receive(p);
                String[] a=new String(p.getData(),0,p.getLength()).trim().split(",");
                if(a.length==2){
                    int pl=Integer.parseInt(a[0])-1, d=Integer.parseInt(a[1]);
                    if(pl>=0&&pl<4&&d>=1&&d<=6) runOnUiThread(()->game.controllerDice(pl,d));
                }
            }
        }catch(Exception ignored){}
    }

    class GameView extends View {
        Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        Random random=new Random();
        int page=0, players=4, turn=0, dice=0;
        boolean rolled=false, choosing=false, gameOver=false;
        int winner=-1;
        int[][] tok=new int[4][4]; // -1 yard, 0..51 track, 52..56 home lane, 57 finished
        int[] finished={0,0,0,0};
        int[] col={0xffe53935,0xff1976d2,0xff2e9d59,0xffffc107};
        int[] dark={0xffa91d2a,0xff12549b,0xff1d7040,0xffb18400};
        int[] start={0,13,26,39};
        int[] safe={0,8,13,21,26,34,39,47};
        int[][] path=new int[52][2];

        GameView(){
            super(MainActivity.this);
            p.setTypeface(Typeface.create("sans",Typeface.BOLD));
            Arrays.stream(tok).forEach(a->Arrays.fill(a,-1));
            buildPath();
            setFocusable(true);
            setClickable(true);
        }
        void buildPath(){
            // clockwise 52-cell ring around the 15x15 board
            int[][] pts={
                {6,0},{7,0},{8,0},{9,0},{10,0},{11,0},{12,0},
                {14,1},{14,2},{14,3},{14,4},{14,5},{14,6},
                {13,7},{14,7},{14,8},{14,9},{14,10},{14,11},{14,12},{13,14},
                {12,14},{11,14},{10,14},{9,14},{8,14},{7,14},{6,14},
                {5,13},{4,14},{3,14},{2,14},{1,14},{0,13},
                {0,12},{0,11},{0,10},{0,9},{0,8},{0,7},{0,6},
                {1,5},{0,5},{0,4},{0,3},{0,2},{0,1},{1,0},
                {2,0},{3,0},{4,0},{5,0}
            };
            for(int i=0;i<52;i++){path[i][0]=pts[i][0];path[i][1]=pts[i][1];}
        }
        void bg(Canvas c){p.setStyle(Paint.Style.FILL);p.setColor(0xff0b0912);c.drawRect(0,0,getWidth(),getHeight(),p);}
        void text(Canvas c,String s,float x,float y,float size,int color){
            p.setStyle(Paint.Style.FILL);p.setTypeface(Typeface.create("sans",Typeface.BOLD));p.setTextSize(size);p.setColor(color);c.drawText(s,x,y,p);
        }
        void center(Canvas c,String s,float x,float y,float size,int color){
            p.setTextSize(size);p.setTypeface(Typeface.create("sans",Typeface.BOLD));p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawText(s,x-p.measureText(s)/2,y,p);
        }
        void round(Canvas c,float l,float t,float r,float b,float rad,int color){
            p.setStyle(Paint.Style.FILL);p.setColor(color);c.drawRoundRect(l,t,r,b,rad,rad,p);
        }
        @Override protected void onDraw(Canvas c){
            bg(c);
            if(page==0) home(c);
            else if(page==1) ludo(c);
            else if(page==2) snakes(c);
            else if(page==4) playerSelect(c);
            else friends(c);
        }
        void logo(Canvas c,float cx,float cy,float s){
            p.setShader(new LinearGradient(cx-s,cy-s,cx+s,cy+s,0xff8b5cf6,0xffec4899,Shader.TileMode.CLAMP));
            c.drawCircle(cx,cy,s+8,p);p.setShader(null);
            round(c,cx-s,cy-s,cx+s,cy+s,22,0xff171322);
            round(c,cx-s*.62f,cy-s*.62f,cx+s*.62f,cy+s*.62f,14,0xfff5c542);
            for(int i=0;i<5;i++) c.drawCircle(cx-s*.27f+i*s*.135f,cy-s*.27f,s*.065f,p);
            center(c,"LUDO",cx,cy+s*.96f,Math.max(12,s*.28f),Color.WHITE);
        }
        void home(Canvas c){
            logo(c,getWidth()/2f,105,50);
            center(c,"MY LUDO",getWidth()/2f,190,34,Color.WHITE);
            center(c,"THE CLASSIC BOARD GAME",getWidth()/2f,214,11,0xffaaa5b5);
            card(c,28,250,getWidth()-28,365,col[0],"LUDO","Classic 2–4 players","PLAY NOW");
            card(c,28,382,getWidth()-28,497,0xff0f766e,"SAANP-SEEDI","Race to 100","PLAY NOW");
            card(c,28,514,getWidth()-28,629,0xff4f46e5,"FRIENDS","Play together on one phone","PLAY NOW");
            round(c,28,655,getWidth()-28,705,24,0xff17131f);
            text(c,"DICE CONTROLLER",48,686,13,0xffddd7e8);
            text(c,"Wi-Fi",getWidth()-83,686,11,0xff8e879b);
        }
        void card(Canvas c,float l,float t,float r,float b,int accent,String title,String sub,String action){
            round(c,l+3,t+5,r+3,b+5,22,0x66000000);
            round(c,l,t,r,b,22,0xff19151f);
            round(c,l,t,l+9,b,9,accent);
            round(c,l+24,t+23,l+70,t+69,14,accent);
            center(c,title.substring(0,1),l+47,t+54,23,Color.WHITE);
            text(c,title,l+86,t+40,19,Color.WHITE);
            text(c,sub,l+86,t+63,11,0xffa9a2b4);
            text(c,action,r-92,t+40,10,0xffd9d0e7);
        }
        void header(Canvas c,String title,String sub){
            text(c,"‹",18,46,40,Color.WHITE);
            text(c,title,59,36,21,Color.WHITE);
            text(c,sub,59,57,10,0xffa29baa);
            round(c,getWidth()-112,17,getWidth()-18,47,15,0xff17131f);
            center(c,"P"+(turn+1)+" ACTIVE",getWidth()-65,37,9,col[Math.min(turn,3)]);
        }
        void playerSelect(Canvas c){
            header(c,"LUDO","CHOOSE PLAYERS");
            center(c,"HOW MANY PLAYERS?",getWidth()/2f,135,25,Color.WHITE);
            center(c,"Choose 2, 3 or 4 players",getWidth()/2f,160,12,0xffaaa5b5);
            for(int n=2;n<=4;n++){
                float y=205+(n-2)*105;
                round(c,30,y,getWidth()-30,y+78,24,0xff19151f);
                round(c,48,y+15,110,y+63,18,col[n-2]);
                center(c,""+n,79,y+48,22,Color.WHITE);
                text(c,n+" PLAYERS",132,y+37,19,Color.WHITE);
                text(c,"PLAY LUDO  ›",getWidth()-135,y+37,11,0xffc9b8ff);
            }
            center(c,"4 gotiyan per player • local pass & play",getWidth()/2f,560,11,0xff777181);
        }
        void ludo(Canvas c){
            header(c,"LUDO",""+players+" PLAYERS  •  PLAYER "+(turn+1)+" TURN");
            float top=76;
            float s=Math.min(getWidth()-22,getHeight()-270);
            float left=(getWidth()-s)/2f;
            drawBoard(c,left,top,s);
            drawTokens(c,left,top,s);
            float info=top+s+17;
            center(c,"RED   BLUE   GREEN   YELLOW",getWidth()/2f,info+10,9,0xff9f98aa);
            for(int i=0;i<4;i++){
                round(c,14+i*(getWidth()-28)/4,info+25,14+(i+1)*(getWidth()-28)/4-4,info+58,14,0xff17131f);
                center(c,"P"+(i+1)+"  "+finished[i]+"/4",14+(i+.5f)*(getWidth()-28)/4,info+47,10,col[i]);
            }
            round(c,18,info+70,getWidth()-18,info+154,26,0xff18141f);
            drawDice(c,getWidth()/2f,info+112,58,dice==0?1:dice);
            round(c,getWidth()-125,info+87,getWidth()-31,info+137,20,0xff7c3aed);
            center(c,rolled?"SELECT GOTI":"ROLL DICE",getWidth()-78,info+118,11,Color.WHITE);
            if(gameOver){
                round(c,45,210,getWidth()-45,400,28,0xff17121f);
                center(c,"WINNER!",getWidth()/2f,270,29,Color.WHITE);
                center(c,"PLAYER "+(winner+1),getWidth()/2f,315,23,col[winner]);
                round(c,90,340,getWidth()-90,385,20,0xff7c3aed);
                center(c,"PLAY AGAIN",getWidth()/2f,369,12,Color.WHITE);
            }
        }
        void drawBoard(Canvas c,float l,float t,float s){
            float q=s/15f;
            round(c,l-5,t-5,l+s+5,t+s+5,22,0xff28232f);
            round(c,l,t,l+s,t+s,16,0xfff4f1e8);
            // four home bases
            base(c,l,t,q,0,0,col[0]);base(c,l,t,q,10,0,col[1]);base(c,l,t,q,0,10,col[2]);base(c,l,t,q,10,10,col[3]);
            // track cells
            for(int i=0;i<52;i++){
                float x=l+path[i][0]*q,y=t+path[i][1]*q;
                p.setStyle(Paint.Style.FILL);p.setColor(0xfff8f6ef);c.drawRect(x,y,x+q,y+q,p);
                p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(0xffc9c5c9);c.drawRect(x,y,x+q,y+q,p);
            }
            p.setStyle(Paint.Style.FILL);
            // colored start cells
            for(int pl=0;pl<4;pl++){
                int idx=start[pl];float x=l+path[idx][0]*q,y=t+path[idx][1]*q;
                p.setColor(col[pl]);c.drawRect(x+1,y+1,x+q-1,y+q-1,p);
            }
            // home lanes
            for(int i=0;i<6;i++){
                lane(c,l,t,q,6,1+i,col[0]);
                lane(c,l,t,q,8,1+i,col[1]);
                lane(c,l,t,q,1+i,6,col[2]);
                lane(c,l,t,q,1+i,8,col[3]);
            }
            // center four-color home
            Path a=new Path();
            a.moveTo(l+5*q,t+5*q);a.lineTo(l+10*q,t+5*q);a.lineTo(l+7.5f*q,t+7.5f*q);a.close();p.setColor(col[0]);c.drawPath(a,p);
            a.reset();a.moveTo(l+10*q,t+5*q);a.lineTo(l+10*q,t+10*q);a.lineTo(l+7.5f*q,t+7.5f*q);a.close();p.setColor(col[1]);c.drawPath(a,p);
            a.reset();a.moveTo(l+10*q,t+10*q);a.lineTo(l+5*q,t+10*q);a.lineTo(l+7.5f*q,t+7.5f*q);a.close();p.setColor(col[2]);c.drawPath(a,p);
            a.reset();a.moveTo(l+5*q,t+10*q);a.lineTo(l+5*q,t+5*q);a.lineTo(l+7.5f*q,t+7.5f*q);a.close();p.setColor(col[3]);c.drawPath(a,p);
        }
        void base(Canvas c,float l,float t,float q,int x,int y,int color){
            round(c,l+x*q,t+y*q,l+(x+5)*q,t+(y+5)*q,13,color);
            for(int k=0;k<4;k++){
                float xx=l+(x+(k%2==0?1.45f:3.55f))*q;
                float yy=t+(y+(k<2?1.45f:3.55f))*q;
                p.setColor(0x55ffffff);c.drawCircle(xx,yy,q*.65f,p);
                p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(Color.WHITE);c.drawCircle(xx,yy,q*.65f,p);p.setStyle(Paint.Style.FILL);
            }
        }
        void lane(Canvas c,float l,float t,float q,int x,int y,int color){
            p.setColor(color);c.drawRect(l+x*q+1,t+y*q+1,l+(x+1)*q-1,t+(y+1)*q-1,p);
        }
        void drawTokens(Canvas c,float l,float t,float s){
            float q=s/15f;
            for(int pl=0;pl<players;pl++)for(int k=0;k<4;k++){
                int pos=tok[pl][k];float x,y;
                if(pos==-1){
                    int bx=pl%2==0?1:11,by=pl<2?1:11;
                    x=l+(bx+(k%2==0?1.45f:3.55f))*q;y=t+(by+(k<2?1.45f:3.55f))*q;
                }else if(pos>=52){
                    int step=pos-52;
                    int[][] lane={{7,1},{7,2},{7,3},{7,4},{7,5},{7,6}};
                    int[] z=lane[Math.min(5,step)];
                    if(pl==1){z=new int[]{8,7-step};}
                    if(pl==2){z=new int[]{7-step,8};}
                    if(pl==3){z=new int[]{6+step,7};}
                    x=l+(z[0]+.5f)*q;y=t+(z[1]+.5f)*q;
                }else if(pos>=0){
                    int g=(start[pl]+pos)%52;
                    x=l+(path[g][0]+.5f)*q;y=t+(path[g][1]+.5f)*q;
                }else continue;
                p.setColor(0x55000000);c.drawCircle(x+2,y+3,q*.32f,p);
                p.setColor(col[pl]);c.drawCircle(x,y,q*.31f,p);
                p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);p.setColor(Color.WHITE);c.drawCircle(x,y,q*.31f,p);p.setStyle(Paint.Style.FILL);
                p.setColor(0xaaFFFFFF);c.drawCircle(x-q*.09f,y-q*.11f,q*.07f,p);
                center(c,""+(k+1),x,y+q*.12f,Math.max(7,q*.18f),Color.WHITE);
            }
        }
        void drawDice(Canvas c,float cx,float cy,float size,int n){
            p.setShadowLayer(14,0,5,0x99000000);setLayerType(View.LAYER_TYPE_SOFTWARE,p);
            round(c,cx-size/2,cy-size/2,cx+size/2,cy+size/2,14,Color.WHITE);p.clearShadowLayer();
            float d=size*.22f;
            int[][] pts={{-1,-1},{0,-1},{1,-1},{-1,0},{0,0},{1,0},{-1,1},{0,1},{1,1}};
            int[][] map={{4},{0,8},{0,4,8},{0,2,6,8},{0,2,4,6,8},{0,2,3,5,6,8},{0,2,3,4,5,6,8}};
            p.setColor(0xff202027);
            for(int idx:map[Math.max(0,Math.min(6,n-1))]){
                float x=cx+pts[idx][0]*d,y=cy+pts[idx][1]*d;c.drawCircle(x,y,size*.075f,p);
            }
        }
        boolean isSafe(int g){for(int s:safe)if(s==g)return true;return false;}
        int global(int pl,int pos){return (start[pl]+pos)%52;}
        boolean canMove(int pl,int k,int d){
            int v=tok[pl][k];
            if(v==57)return false;
            if(v==-1)return d==6;
            return v+d<=57;
        }
        void roll(int d){
            if(gameOver||rolled)return;
            dice=d;rolled=true;choosing=true;invalidate();
        }
        void controllerDice(int pl,int d){
            if(page!=1||gameOver)return;
            turn=pl;roll(d);
            // controller selects the first legal token if only one exists
            int count=0,last=-1;for(int k=0;k<4;k++)if(canMove(turn,k,d)){count++;last=k;}
            if(count==1){moveToken(last);}
        }
        void moveToken(int k){
            if(!rolled||!canMove(turn,k,dice))return;
            int v=tok[turn][k];
            int nv=(v==-1)?0:v+dice;
            tok[turn][k]=nv;
            if(nv==57)finished[turn]++;
            // cut opponents on non-safe track
            if(nv>=0&&nv<52){
                int g=global(turn,nv);
                if(!isSafe(g))for(int op=0;op<players;op++)if(op!=turn)for(int j=0;j<4;j++){
                    int ov=tok[op][j];
                    if(ov>=0&&ov<52&&global(op,ov)==g)tok[op][j]=-1;
                }
            }
            if(finished[turn]>=4){gameOver=true;winner=turn;}
            boolean extra=dice==6;
            rolled=false;choosing=false;int old=turn;
            if(!gameOver&&!extra)turn=(turn+1)%players;
            invalidate();
        }
        void reset(){
            for(int[] a:tok)Arrays.fill(a,-1);
            Arrays.fill(finished,0);turn=0;dice=0;rolled=false;choosing=false;gameOver=false;winner=-1;
        }
        void snakes(Canvas c){
            header(c,"SAANP-SEEDI","2–4 PLAYERS  •  PLAYER "+(turn+1)+" TURN");
            float s=Math.min(getWidth()-20,getHeight()-285),l=(getWidth()-s)/2f,t=72,q=s/10f;
            round(c,l-4,t-4,l+s+4,t+s+4,18,0xff29232f);
            for(int r=0;r<10;r++)for(int cc=0;cc<10;cc++){
                int n=(9-r)*10+(r%2==0?cc+1:10-cc);
                p.setColor(((r+cc)&1)==0?0xffffe8a8:0xfff1cf84);c.drawRect(l+cc*q,t+r*q,l+(cc+1)*q,t+(r+1)*q,p);
                text(c,""+n,l+cc*q+4,t+r*q+15,9,0xff5c4a32);
            }
            p.setStrokeWidth(6);p.setStrokeCap(Paint.Cap.ROUND);
            int[] sa={99,95,92,89,74,64,62,49,47,25},sb={80,75,88,68,53,44,19,11,26,5};
            int[] la={2,7,8,15,21,28,36,51,71,78},lb={38,14,31,26,42,84,44,67,91,98};
            for(int i=0;i<sa.length;i++)snakeLine(c,sa[i],sb[i],0xffd9363e,l,t,q);
            for(int i=0;i<la.length;i++)snakeLine(c,la[i],lb[i],0xff23965a,l,t,q);
            p.setStrokeCap(Paint.Cap.BUTT);
            for(int pl=0;pl<players;pl++){float[] z=cell(ss(pl),l,t,q);p.setColor(col[pl]);c.drawCircle(z[0],z[1],q*.22f,p);p.setStyle(Paint.Style.STROKE);p.setColor(Color.WHITE);p.setStrokeWidth(2);c.drawCircle(z[0],z[1],q*.22f,p);p.setStyle(Paint.Style.FILL);}
            float y=t+s+28;center(c,"P1 "+ss(0)+"   P2 "+ss(1)+"   P3 "+ss(2)+"   P4 "+ss(3),getWidth()/2f,y,10,0xffaaa2b1);
            round(c,18,y+18,getWidth()-18,y+100,25,0xff18141f);drawDice(c,getWidth()/2f,y+58,56,dice==0?1:dice);
            round(c,getWidth()-125,y+31,getWidth()-31,y+83,20,0xff0f766e);center(c,"ROLL",getWidth()-78,y+63,11,Color.WHITE);
        }
        int[] snakePos={1,1,1,1};
        int ss(int i){return snakePos[i];}
        float[] cell(int n,float l,float t,float q){
            int z=n-1,row=9-z/10,cc=z%10;if(row%2==1)cc=9-cc;
            return new float[]{l+(cc+.5f)*q,t+(9-row+.5f)*q};
        }
        void snakeLine(Canvas c,int a,int b,int color,float l,float t,float q){
            float[] x=cell(a,l,t,q),y=cell(b,l,t,q);p.setColor(color);c.drawLine(x[0],x[1],y[0],y[1],p);
        }
        void rollSnake(){
            int d=1+random.nextInt(6);dice=d;int n=Math.min(100,snakePos[turn]+d);
            int[] sa={99,95,92,89,74,64,62,49,47,25},sb={80,75,88,68,53,44,19,11,26,5};
            int[] la={2,7,8,15,21,28,36,51,71,78},lb={38,14,31,26,42,84,44,67,91,98};
            for(int i=0;i<sa.length;i++)if(n==sa[i])n=sb[i];
            for(int i=0;i<la.length;i++)if(n==la[i])n=lb[i];
            snakePos[turn]=n;if(n<100&&d!=6)turn=(turn+1)%players;invalidate();
        }
        void friends(Canvas c){
            header(c,"FRIENDS","LOCAL MULTIPLAYER");
            center(c,"CHOOSE PLAYERS",getWidth()/2f,125,25,Color.WHITE);
            for(int n=2;n<=4;n++){float y=165+(n-2)*78;round(c,28,y,getWidth()-28,y+58,20,0xff19151f);text(c,n+" PLAYERS",55,y+37,17,Color.WHITE);text(c,"PLAY",getWidth()-88,y+36,11,0xffb8a4ff);}
            text(c,"Internet multiplayer requires a server.",28,430,12,0xffffd166);
        }
        @Override public boolean performClick(){
            super.performClick();
            return true;
        }
        @Override public boolean onTouchEvent(MotionEvent e){
            if(e.getAction()!=MotionEvent.ACTION_UP)return true;
            float x=e.getX(),y=e.getY();
            if(page==0){if(y>=240&&y<380)page=4;else if(y<515&&y>=375)page=2;else if(y>=510&&y<645)page=3;}
            else if(page==4){
                if(y<65){page=0;return true;}
                if(y>=190&&y<535){int n=2+(int)((y-190)/105);if(n>=2&&n<=4){players=n;reset();page=1;}}
            }
            else if(page==1){
                if(gameOver){
                    if(y>=330&&y<=430){reset();invalidate();return true;}
                    if(y<70){page=0;return true;}
                    invalidate();return true;
                }
                if(y<70){page=0;return true;}

                // Use the exact same board geometry as onDraw().
                float top=76f;
                float s=Math.min(getWidth()-22,getHeight()-270);
                float l=(getWidth()-s)/2f;
                float q=s/15f;
                float info=top+s+17f;

                // Large, forgiving dice/control hit zone.
                boolean diceZone = y>=info+62 && y<=info+166;
                if(diceZone){
                    if(!rolled){
                        roll(1+random.nextInt(6));
                    }
                    invalidate();
                    performClick();
                    return true;
                }

                // While a dice result is active, tapping a token selects it.
                if(rolled){
                    for(int k=0;k<4;k++)if(canMove(turn,k,dice)){
                        int v=tok[turn][k];
                        float tx,ty;
                        if(v==-1){
                            int bx=turn%2==0?1:11,by=turn<2?1:11;
                            tx=l+(bx+(k%2==0?1.45f:3.55f))*q;
                            ty=top+(by+(k<2?1.45f:3.55f))*q;
                        }else if(v<52){
                            int g=global(turn,v);
                            tx=l+(path[g][0]+.5f)*q;
                            ty=top+(path[g][1]+.5f)*q;
                        }else{
                            tx=l+7.5f*q;
                            ty=top+7.5f*q;
                        }
                        if(Math.hypot(x-tx,y-ty)<q*1.05f){
                            moveToken(k);
                            invalidate();
                            performClick();
                            return true;
                        }
                    }
                }
                invalidate();
            }else if(page==2){
                if(y<65)page=0;else if(y>getHeight()-160)rollSnake();
            }else if(page==3){
                if(y<65)page=0;else if(y>150&&y<420){players=Math.max(2,Math.min(4,2+(int)((y-150)/78)));page=1;reset();}
            }
            invalidate();return true;
        }
    }
}
