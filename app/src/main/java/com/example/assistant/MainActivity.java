package com.example.assistant;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    ImageView micro;
    TextView txtSpeechInput;
    Integer sizeOfSearch;

    ArrayList<String> requests;

    //переменная для проверки возможности
    //распознавания голоса в телефоне
    private static final int VR_REQUEST = 999;

    //ListView для отображения запросов
    private ListView wordList;

    //Log для вывода вспомогательной информации
    private final String LOG_TAG = "SpeechActivity";

    //переменные для работы TTS

    //переменная для проверки данных для TTS
    private int MY_DATA_CHECK_CODE = 0;

    //Text To Speech интерфейс
    private TextToSpeech repeatTTS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txtSpeechInput = (TextView) findViewById(R.id.txtSpeechInput);
        micro = (ImageButton) findViewById(R.id.btnSpeak);
        wordList = (ListView) findViewById(R.id.word_list);

        requests = new ArrayList<>();

        //проверяем, поддерживается ли распознование речи
        PackageManager packManager = getPackageManager();
        List<ResolveInfo> intActivities = packManager.queryIntentActivities(new
                Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (intActivities.size() != 0) {
            // распознавание поддерживается, будем отслеживать событие щелчка по кнопке
            micro.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // отслеживаем результат
                    listenToSpeech();
                }
            });
        } else {
            // распознавание не работает. Заблокируем кнопку и выведем соответствующее предупреждение.
            micro.setEnabled(false);
            Toast.makeText(this, "Oops - Speech recognition not supported!", Toast.LENGTH_LONG).show();
        }


    }

    private void listenToSpeech() {

        //запускаем интент, распознающий речь и передаем ему требуемые данные
        Intent listenIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //указываем пакет
        listenIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
        //В процессе распознования выводим сообщение
        listenIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a word!");
        //устанавливаем модель речи
        listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //указываем число результатов, которые могут быть получены
        listenIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);

        //начинаем прослушивание
        startActivityForResult(listenIntent, VR_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //проверяем результат распознавания речи
        if (requestCode == VR_REQUEST && resultCode == RESULT_OK) {
            //Добавляем распознанные слова в список результатов
            requests.addAll(data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS));
            checkResult(requests.get(requests.size()-1).toLowerCase());
//            requests = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            //Передаем список возможных слов через ArrayAdapter компоненту ListView
            //wordList.setAdapter(new ArrayAdapter<String>(this, R.layout.word, suggestedWords));
        }

        //tss код здесь

        //вызываем метод родительского класса
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        wordList.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.word, requests));
    }

    void checkResult(String row){
        boolean findRu = row.contains("найти"), findEn = row.contains("find");
        if (findRu){
            forResearch(row, "найти");
        }
        if (findEn){
            forResearch(row, "find");
        }

    }

    void forResearch(String row, String find){
        int indexFindEn = row.indexOf(find);
        if(indexFindEn <= 3){
            String searchRequest = String.copyValueOf(row.toCharArray()) ;
            Uri address = Uri.parse(searchRequest);
            Intent openLinkIntent = new Intent(Intent.ACTION_VIEW, address);
            if (openLinkIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(openLinkIntent);
            } else {
                Log.d("Intent", "Не получается обработать намерение!");
            }
        }
    }
}
