package com.hebiao.carcontrol;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import com.hebiao.net.AsyncHttpClient;
import com.hebiao.net.BinaryHttpResponseHandler;
import com.hebiao.net.JsonHttpResponseHandler;
import com.hebiao.net.TextHttpResponseHandler;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

public class MainActivity extends Activity implements OnTouchListener, Runnable {

	Button button_forword, button_backoff, button_left, button_right,
			button_lock, button_connect, button_disconnect;

	EditText editText = null;

	int count = 0;

	int imgarr[] = { R.drawable.status_1, R.drawable.status_2,
			R.drawable.status_3, R.drawable.status_4, R.drawable.status_5,
			R.drawable.status_4, R.drawable.status_3, R.drawable.status_2,
			R.drawable.status_1 };

	int button_arr[] = { R.id.button_forword, R.id.button_backoff,
			R.id.button_right, R.id.button_left, R.id.lock, R.id.connect,
			R.id.disconnect };

	ImageView statusImageView = null;

	Timer timer = new Timer();

	String ipAddress;

	boolean flag, switchFlag;

	// String url="http://192.168.1.159:8080/?action=stream";
	String url = "http://192.168.1.159:8080/?action=snapshot";
	SurfaceHolder holder;
	Thread mythread;
	Canvas canvas;
	URL videoUrl;
	int w;
	int h;
	HttpURLConnection conn;
	Bitmap bmp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.activity_main);

		button_forword = (Button) findViewById(R.id.button_forword);
		button_backoff = (Button) findViewById(R.id.button_backoff);
		button_right = (Button) findViewById(R.id.button_right);
		button_left = (Button) findViewById(R.id.button_left);
		button_lock = (Button) findViewById(R.id.lock);
		button_connect = (Button) findViewById(R.id.connect);
		button_disconnect = (Button) findViewById(R.id.disconnect);

		button_forword.setOnTouchListener(this);
		button_backoff.setOnTouchListener(this);
		button_right.setOnTouchListener(this);
		button_left.setOnTouchListener(this);

		editText = (EditText) findViewById(R.id.edittext);
		// editText.setFocusable(false);
		SharedPreferences mySharedPreferences = getSharedPreferences("Car",
				Activity.MODE_PRIVATE);
		String s = mySharedPreferences.getString("ip", "");
		if (s.length() > 0) {
			editText.setText(s);
			ipAddress = s;
		}

		statusImageView = (ImageView) findViewById(R.id.net_status);

		/*
		 * new Thread() { public void run() { messageTransfer = new
		 * MessageTransfer("192.168.1.218", 8000); }; }.start();
		 */

		timer.schedule(timerTask, 1000, 1000);

		SurfaceView surface = (SurfaceView) findViewById(R.id.surfaceview);
		surface.setKeepScreenOn(true);// 保持屏幕常亮
		mythread = new Thread(this);
		holder = surface.getHolder();
		holder.addCallback(new Callback() {

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				// TODO Auto-generated method stub

			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				// TODO Auto-generated method stub
				mythread.start();
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format,
					int width, int height) {
				// TODO Auto-generated method stub

			}
		});

	}

	TimerTask timerTask = new TimerTask() {

		@Override
		public void run() {
			// TODO Auto-generated method stub

			if (count == 7) {
				count = 0;
			} else {
				count++;
			}

			handler.sendEmptyMessage(count);

		}
	};

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			if (flag && switchFlag) {
				statusImageView.setBackgroundResource(imgarr[msg.what]);
			} else {
				statusImageView.setBackgroundResource(R.drawable.status_6);
			}

			if ((msg.what == 6 || msg.what == 2) && switchFlag) {

				AsyncHttpClient client = new AsyncHttpClient();
				String url = IPTools.connectUrl(ipAddress);
				client.get(url, new TextHttpResponseHandler() {

					@Override
					public void onFailure(int statusCode, Header[] headers,
							String responseString, Throwable throwable) {
						// TODO Auto-generated method stub
						flag = false;
					}

					@Override
					public void onSuccess(int statusCode, Header[] headers,
							String responseString) {
						// TODO Auto-generated method stub
						// System.out.println("-----------------" +
						// responseString);
						if ("ok".equalsIgnoreCase(responseString)) {
							flag = true;
						} else {
							flag = false;
						}

					}

				});
			}

		};
	};

	public void lockAction(View v) {

		String st = editText.getText().toString();

		if (checkIp(st)) {
			SharedPreferences mySharedPreferences = getSharedPreferences("Car",
					Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = mySharedPreferences.edit();
			editor.putString("ip", st);
			ipAddress = st;
			editor.commit();

			Toast.makeText(MainActivity.this, "IP保存成功", Toast.LENGTH_LONG)
					.show();
		} else {
			Toast.makeText(MainActivity.this, "IP地址输入错误", Toast.LENGTH_LONG)
					.show();
		}

		/*
		 * 
		 * AsyncHttpClient client = new AsyncHttpClient(); client.get(
		 * "http://192.168.1.218:8080/carcontrol/control/sendCmd.action?cmd=(*@***!!!!!!!"
		 * , new TextHttpResponseHandler() {
		 * 
		 * @Override public void onSuccess(int statusCode, Header[] headers,
		 * String responseString) { // TODO Auto-generated method stub
		 * 
		 * }
		 * 
		 * @Override public void onFailure(int statusCode, Header[] headers,
		 * String responseString, Throwable throwable) { // TODO Auto-generated
		 * method stub
		 * 
		 * } });
		 */
	}

	private boolean checkIp(String ip) {

		if (!ip.startsWith("192.168.")) {
			return false;
		}
		return true;
	}

	public void connectAction(View v) {

		Toast.makeText(MainActivity.this, "连接设备中...", Toast.LENGTH_LONG).show();

		switchFlag = true;

		AsyncHttpClient client = new AsyncHttpClient();
		String url = IPTools.connectUrl(ipAddress);
		client.get(url, new TextHttpResponseHandler() {

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					String responseString) {
				// TODO Auto-generated method stub
				// System.out.println("-----------------" + responseString);
				if ("ok".equalsIgnoreCase(responseString)) {
					flag = true;
					Toast.makeText(MainActivity.this, "连接已经建立",
							Toast.LENGTH_LONG).show();

				}

			}

		});

	}

	@SuppressWarnings("deprecation")
	public void disconnectAction(View v) {

		Toast.makeText(MainActivity.this, "断开连接中...", Toast.LENGTH_LONG).show();

		switchFlag = false;

		if (flag) {
			Toast.makeText(MainActivity.this, "连接已断开", Toast.LENGTH_LONG)
					.show();
			flag = false;

		}

	}

	public void forword(View v) {

	}

	public void back(View v) {

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.button_forword) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				button_forword.setBackgroundResource(R.drawable.a1_01);
				sendCommand(ConstantPart.COMMAND_E_STOP);
			}
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				button_forword.setBackgroundResource(R.drawable.a1_02);
				sendCommand(ConstantPart.COMMAND_A_FORWARD);
			}

		} else if (v.getId() == R.id.button_backoff) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				button_backoff.setBackgroundResource(R.drawable.a1_03);
				sendCommand(ConstantPart.COMMAND_E_STOP);
			}
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				button_backoff.setBackgroundResource(R.drawable.a1_04);
				sendCommand(ConstantPart.COMMAND_C_BACKOFF);
			}

		} else if (v.getId() == R.id.button_left) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				button_left.setBackgroundResource(R.drawable.a1_05);
				sendCommand(ConstantPart.COMMAND_E_STOP);
			}
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				button_left.setBackgroundResource(R.drawable.a1_06);
				sendCommand(ConstantPart.COMMAND_F_TURNLEFT);
			}

		} else if (v.getId() == R.id.button_right) {
			if (event.getAction() == MotionEvent.ACTION_UP) {
				button_right.setBackgroundResource(R.drawable.a1_07);
				sendCommand(ConstantPart.COMMAND_E_STOP);
			}
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				button_right.setBackgroundResource(R.drawable.a1_08);
				sendCommand(ConstantPart.COMMAND_H_TURNRIGHT);
			}
		}

		return false;
	}

	private void sendCommand(String cmd) {

		if (!flag) {
			Toast.makeText(MainActivity.this, "连接未建立或断开", Toast.LENGTH_LONG)
					.show();
			return;
		}

		AsyncHttpClient client = new AsyncHttpClient();
		String url = IPTools.cmdUrl(ipAddress) + "?cmd=" + cmd;
		client.get(url, new TextHttpResponseHandler() {

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					String responseString) {
				// TODO Auto-generated method stub
				// System.out.println("-----------------" + responseString);

			}

		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void draw() {
		// TODO Auto-generated method stub
		try {
			InputStream inputstream = null;
			// 创建一个URL对象
			videoUrl = new URL(url);
			// 利用HttpURLConnection对象从网络中获取网页数据
			conn = (HttpURLConnection) videoUrl.openConnection();
			// 设置输入流
			conn.setDoInput(true);
			// 连接
			conn.connect();
			// 得到网络返回的输入流
			inputstream = conn.getInputStream();
			// 创建出一个bitmap
			bmp = BitmapFactory.decodeStream(inputstream);
			canvas = holder.lockCanvas();
			canvas.drawColor(Color.WHITE);
			RectF rectf = new RectF(0, 0, 165 * 4, 135 * 4);
			canvas.drawBitmap(bmp, null, rectf, null);
			holder.unlockCanvasAndPost(canvas);
			// 关闭HttpURLConnection连接
			conn.disconnect();
		} catch (Exception ex) {

		} finally {
			
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			draw();
		}
	}

}
