package com.zmy.next.emotionwithcharacter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.jpg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ImageActivity.class);
                intent.putExtra("url", "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1499154417917&di=0cfc720f3a8b7638dd50d5d502039d07&imgtype=0&src=http%3A%2F%2Fv1.qzone.cc%2Fskin%2F201510%2F12%2F19%2F02%2F561b935de45a9240.jpg%2521600x600.jpg");
                startActivity(intent);
            }
        });

        findViewById(R.id.gif).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ImageActivity.class);
                intent.putExtra("url", "http://i1.mhimg.com/M00/08/89/CgAAhlSrXbeAdstNAB_I1UWm0PA280.gif");
                startActivity(intent);
            }
        });
    }
}
