package com.example.async;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private TextView textInfo;  // объявляем переменную хранения textView
    private ExecutorService executorService; // объявляем объект ExecutorService для управления потоками (для фоновой задачи)
    private Handler handler; // объявляем объект Handler для взаимодействия с овновным потоком из других
    private int currentFloor; // объявляем переменную для отслеживания этажа, где находится кот
    private boolean isButtonVisible = true; // объявлекние и инициализация переменной для видимости / невидимости
    private ProgressBar progressBar; // объявление переменной для progressBar

    private Button startButton; // объявляем переменную button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textInfo = findViewById(R.id.TextInfo); // поиск textView по ID

        startButton = findViewById(R.id.start_button); // инициализируем по ID startButton

        executorService = Executors.newSingleThreadExecutor(); // создаем один поток для выполнения фоновых задач. Однопоточный пул потоков, все задачи
        // последовательно в фоновом потоке

        handler = new Handler(Looper.getMainLooper()); // создаем поток для обновления интерфейса из основого потока. Looper - механизм обеспечения обработки сообщений в потоке.

        currentFloor = 0;  // инициализируем 0-вой этаж

        progressBar = findViewById(R.id.progressBar); // инициализируем переменную progressBar

        startUpdatingFloors(); // запуск фоновой задачи для увеличения этажей каждые 3 секунды
    }

    public void onClick(View view) { // метод нажатия на кнопку
        if (isButtonVisible) {
            startButton.setVisibility(View.INVISIBLE); // Скрываем кнопку
            progressBar.setVisibility(View.VISIBLE);  // Показываем прогресс при первом нажатии
        }
        textInfo.setText("Кот полез на крышу. Этаж: " + (currentFloor)); // установка начального текста после нажатия на кнопку
        progressBar.setProgress(currentFloor);
        executorService.execute(new Runnable() { // запуск фоновой задачи
            @Override
            public void run() {
                // Выполнение фоновой задачи. Добавляем задачу в очередь выполнения.
                try {
                    TimeUnit.SECONDS.sleep(5); // установка задержки в 5 секунд с имитацией фоновой работы

                } catch (InterruptedException e) { // ловим исключение, выводим в ошибку
                    e.printStackTrace(); // ошибка
                }

                // Обновляем пользовательский интерфейс в основном потоке
                handler.post(new Runnable() { // запуск Runnable основного потока
                    @Override
                    public void run() { // выполнение задачи в основном потоке
                        textInfo.setText("Кот залез на крышу. Этаж " + currentFloor);
                        if (currentFloor == 23) {
                            startButton.setVisibility(View.VISIBLE);
                            isButtonVisible = true;
                            progressBar.setVisibility(View.INVISIBLE);  // Скрываем прогресс при достижении этажа 23
                        }
                    }
                });
            }
        });
    }

    private void startUpdatingFloors() { // метод для увеличения номера этажа на экране
        executorService.execute(new Runnable() { // запускаем новую задачу в фоновый поток, добавляем в очередь
            @Override
            public void run() {
                while (currentFloor < 23) { // пока количество этаже меньше 23 увеличиваем
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) { // ловим ошибку и откидываем ее
                        e.printStackTrace();
                    }

                    currentFloor++; // увеличение кол ва этажа

                    handler.post(new Runnable() { // запуск задачи в основной поток по изменению текста
                        @Override
                        public void run() {
                            textInfo.setText("Кот полез на крышу. Этаж " + currentFloor); // смена текста
                        }
                    });
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() { // запуск задачи изменения текста в основной поток, срабатывает после 23 этажей.
                        textInfo.setText("Кот залез на крышу. Этаж " + currentFloor); // смена текста
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() { // метод при закрытии приложения
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {  // Завершаем фоновоеФ executorService при уничтожении активности
            executorService.shutdown();
        }
    }
}
