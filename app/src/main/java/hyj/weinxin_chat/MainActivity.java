package hyj.weinxin_chat;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import hyj.weinxin_chat.activity.AccountSetActivity;
import hyj.weinxin_chat.flowWindow.MyWindowManager;
import hyj.weinxin_chat.util.GetPermissionUtil;

public class MainActivity extends AppCompatActivity {
    SharedPreferences sharedPreferences;

    private static final String[] m={"1","2","3","4","5","6","7","8","9","10"};
    private TextView view ;
    private EditText intevalTime;
    private Spinner spinner;
    private ArrayAdapter<String> adapter;

    @Override
    @TargetApi(23)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GetPermissionUtil.getReadAndWriteContactPermision(this,MainActivity.this);
        if(Build.VERSION.SDK_INT>19){
            if (!Settings.canDrawOverlays(MainActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent,10);
            }
        }

        MyWindowManager.createSmallWindow(getApplicationContext());
        sharedPreferences = GlobalApplication.getContext().getSharedPreferences("url",MODE_PRIVATE);

        setContentView(R.layout.activity_main);
        Button btn = (Button)findViewById(R.id.open_assist);
        btn.setOnClickListener(tbnListen);

        Button accountSetBtn = (Button)this.findViewById(R.id.login_set);
        accountSetBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent account = new Intent(MainActivity.this, AccountSetActivity.class);
                startActivity(account);
            }
        });

        view = (TextView) findViewById(R.id.spinnerText);

        spinner = (Spinner) findViewById(R.id.Spinner01);
        //将可选内容与ArrayAdapter连接起来
        adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,m);
        //设置下拉列表的风格
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //将adapter 添加到spinner中
        spinner.setAdapter(adapter);
        //添加事件Spinner事件监听
        spinner.setOnItemSelectedListener(new SpinnerSelectedListener());
        //设置默认值
        spinner.setVisibility(View.VISIBLE);

        String defaultValue = sharedPreferences.getString("qNum","");
        if(defaultValue==null||defaultValue.equals("")){
            defaultValue = "1";
        }
        spinner.setSelection(Integer.parseInt(defaultValue)-1);

        intevalTime = (EditText)findViewById(R.id.interval_time);
        String defaultintevalTime = sharedPreferences.getString("intervalTime","");
        if(defaultintevalTime==null||defaultintevalTime.equals("")){
            defaultintevalTime = "3";
        }
        intevalTime.setText(defaultintevalTime);

    }
    private View.OnClickListener tbnListen = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            saveParams();
            System.out.println("--->dd");
            Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
            Toast.makeText(MainActivity.this, "放开辅助权限", Toast.LENGTH_LONG).show();
        }
    };
    private void saveParams(){
        SharedPreferences.Editor editor= sharedPreferences.edit();
        editor.putString("intervalTime",intevalTime.getText()+"");
        editor.commit();
    }

    //使用数组形式操作
    class SpinnerSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
            view.setText("发送个数："+m[arg2]);
            SharedPreferences.Editor editor= sharedPreferences.edit();
            editor.putString("qNum",m[arg2]);
            editor.commit();
        }
        public void onNothingSelected(AdapterView<?> arg0) {
        }
    }
}
