package com.example.courierapp.presentation;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.courierapp.R;
import com.example.courierapp.data.DBWorker;

import java.sql.*;

public class MainActivity extends AppCompatActivity {
    private Button loginButton;
    private Button registerButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        checkDatabaseConnection();
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
    private void init()
    {
        loginButton = findViewById(R.id.btn_login);
        registerButton = findViewById(R.id.btn_register);

    }
    private void checkDatabaseConnection() {
        new Thread(() -> {
            Connection conn = DBWorker.getConnection();
            runOnUiThread(() -> {
                if (conn != null) {
                    Toast.makeText(this, "Подключение к БД успешно!", Toast.LENGTH_SHORT).show();
                    try {
                        conn.close();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(this, "Не удалось подключиться к БД", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }
}