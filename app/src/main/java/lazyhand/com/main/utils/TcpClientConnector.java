package lazyhand.com.main.utils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TcpClientConnector {
    private static TcpClientConnector mTcpClientConnector;
    private Socket mClient;
    private Thread mConnectThread;


    public static TcpClientConnector getInstance() {
        if (mTcpClientConnector == null)
            mTcpClientConnector = new TcpClientConnector();
        return mTcpClientConnector;
    }

    public void creatConnect(final String mSerIP, final int mSerPort) {
        if (mConnectThread == null) {
            mConnectThread = new Thread(()->{
                try {
                    connect(mSerIP, mSerPort);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            mConnectThread.start();
        }
    }

    /**
     * 与服务端进行连接
     * @throws IOException
     */
    private void connect(String mSerIP, int mSerPort) throws IOException {
        if (mClient == null) {
            mClient = new Socket(mSerIP, mSerPort);
        }
        InputStream inputStream = mClient.getInputStream();
        byte[] buffer = new byte[2048];
        int len = -1;
        while ((len = inputStream.read(buffer)) != -1) {
            String data = new String(buffer, 0, len);
            Message message = new Message();
            message.what = 100;
            Bundle bundle = new Bundle();
            bundle.putString("data", data);
            message.setData(bundle);
            EventBus.getDefault().post(new MessageEvent(data));
        }
    }

    /**
     * 发送数据
     * @param data 需要发送的内容
     */
    public void send(String data) throws IOException {
        Log.d("data", "send: "+data);
        OutputStream outputStream = mClient.getOutputStream();
        Log.d("data", "send: 2");

        outputStream.write(data.getBytes());
        outputStream.flush();

    }

    /**
     * 断开连接
     * @throws IOException
     */
    public void disconnect() throws IOException {
        if (mClient != null) {
            mClient.close();
            mClient = null;
        }
    }
}
