package hyj.weinxin_chat.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import hyj.weinxin_chat.GlobalApplication;
import hyj.weinxin_chat.R;


public class AccountSetActivity extends AppCompatActivity {
    private SharedPreferences.Editor shPrefEdit;
    private SharedPreferences shPref;
    EditText sendMsg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_set);
        shPref = GlobalApplication.getContext().getSharedPreferences("url",MODE_PRIVATE);
        sendMsg = (EditText)findViewById(R.id.send_msg);
        Button save = (Button)findViewById(R.id.account_save);
        Button back = (Button)findViewById(R.id.account_back);
        String text = shPref.getString("sendMsg","");
        sendMsg.setText(text);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = sendMsg.getText().toString().trim();
                shPrefEdit = shPref.edit();
                shPrefEdit.putString("sendMsg",text);
                shPrefEdit.commit();
                finish();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
