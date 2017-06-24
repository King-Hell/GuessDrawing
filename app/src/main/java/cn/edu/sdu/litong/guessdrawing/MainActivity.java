package cn.edu.sdu.litong.guessdrawing;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {
    private MySurfaceView mySurfaceView;
    private Button clearButton;
    private EditText IPText;
    private Button buttonConnect;
    private Button buttonServer;

    protected static boolean isServer = false;
    protected static boolean isLogin=false;

    protected static Socket socket;
    private ServerSocket server;

    private final int setIPAddress = 1;
    private final int IPError = 2;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case setIPAddress:
                    IPText.setText("本机IP：" + (String) msg.obj);
                    IPText.setEnabled(false);

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        clearButton = (Button) findViewById(R.id.clear_button);
        mySurfaceView = (MySurfaceView) findViewById(R.id.mySurfaceView);
        IPText = (EditText) findViewById(R.id.editText);
        buttonServer = (Button) findViewById(R.id.button_server);
        buttonServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isServer) {
                    Toast.makeText(getApplication(), "作为服务端等待连接中……", Toast.LENGTH_LONG).show();
                    buttonServer.setText("断开连接");
                    buttonConnect.setEnabled(false);
                    IPText.setHint("等待连接……");
                    isServer=true;
                    new Thread() {
                        @Override
                        public void run() {
                            super.run();
                            try {
                                Message message = new Message();
                                message.what = setIPAddress;
                                message.obj = server.getInetAddress().getHostAddress();
                                handler.sendMessage(message);
                                server = new ServerSocket(49401);
                                socket=server.accept();
                                BufferedWriter out=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                                BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                                String data=null;
                                while((data=in.readLine())!=null){
                                    if(data.equals("LOGIN")){
                                        out.write("YES");
                                        out.newLine();
                                        out.flush();
                                        isLogin=true;
                                        break;
                                    }
                                }
                                out.close();
                                in.close();
                                DataInputStream is=new DataInputStream(socket.getInputStream());

                                int x=0,y=0,type=0;
                                while(!socket.isInputShutdown()){
                                    x=is.readInt();
                                    y=is.readInt();
                                    type=is.readInt();
                                    mySurfaceView.net(x,y,type);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();

                }else {
                    buttonConnect.setEnabled(true);
                    IPText.setEnabled(true);
                    Toast.makeText(getApplication(), "已断开连接", Toast.LENGTH_LONG).show();
                    IPText.setHint("请输入服务端IP地址");
                    isServer=false;
                    new Thread(){
                        @Override
                        public void run() {
                           try{
                            server.close();}catch(Exception e){
                               e.printStackTrace();
                           }
                        }
                    }.start();
                }
            }

        });
        buttonConnect = (Button) findViewById(R.id.button_connect);
        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(){
                    @Override
                    public void run() {
                        try{
                            socket=new Socket(IPText.getText().toString(),49401);
                            BufferedWriter out=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                            out.write("LOGIN");
                            out.newLine();
                            out.flush();
                            BufferedReader in=new BufferedReader(new InputStreamReader(socket.getInputStream()));
                            String data=null;
                            while((data=in.readLine())!=null){
                                if(data.equals("YES")){
                                    isLogin=true;
                                    break;
                                }
                            }
                            out.close();
                            in.close();
                        }catch (IllegalArgumentException e) {
                            Message message=new Message();
                            message.what = IPError;
                            message.obj = "请输入正确的IP地址格式";
                            handler.sendMessage(message);
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mySurfaceView.reset();
            }
        });
    }

}
