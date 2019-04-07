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
import java.util.Arrays;
import java.util.Collection;
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
            ArrayList<String> result = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            txtSpeechInput.setText(result.get(0));
            requests.addAll(result);
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
        boolean calcEn = row.contains("calculate"), calcRu = row.contains("решить");
        if (findRu){
            forResearch(row, "найти ");
        }
        if (findEn){
            forResearch(row, "find ");
        }
        if (calcRu){
//            row = row.replace(',', '.');
            calc(row, "решить ");
        }
        if (calcEn){
            row = row.replace(',', '.');
            calc(row, "calculate ");
        }

    }

    void forResearch(String row, String find){
        int indexFindEn = row.indexOf(find);
        if(indexFindEn <= 3){
            char tempRow [];
            tempRow = deleteKeyWord(row, find);
            String searchRequest = String.copyValueOf(tempRow);
            Uri address = Uri.parse("https://www.google.com/search?q= " + searchRequest);
            Intent openLinkIntent = new Intent(Intent.ACTION_VIEW, address);
            if (openLinkIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(openLinkIntent);
            } else {
                Log.d("Intent", "Не получается обработать намерение!");
            }
        }
    }

    char [] deleteKeyWord(String row, String keyWord){
        char tempRow [] = new char[row.length()-4];
        for (int i = keyWord.length(); i < row.length(); i++) {
            tempRow[i-keyWord.length()] = row.toCharArray()[i];
        }
        return tempRow;
    }

    void calc(String row, String word){
        double answer = 0.0;
        char [] tempCh;
        tempCh = (deleteKeyWord(row, word));
        String tempRow = "";

        for (int i = 0; i < tempCh.length; i++) {
            tempRow += Character.toString(tempCh[i]);
        }

        tempRow.replace("x", "*");
        Log.d("AADADAD", tempRow);
//        try {
            tempRow = actionWithRow(tempRow);
            Log.d("AADADAD", tempRow);
        answer = eval(tempRow);

            Toast.makeText(getApplicationContext(),Double.toString(answer), Toast.LENGTH_LONG).show();

//        }catch (Exception e){
//            Toast.makeText(getApplicationContext(),"Не могу посчитать...", Toast.LENGTH_LONG).show();
//            Log.d("Error", e.toString());
//        }
    }

    String actionWithRow(String row){
        String result = "";
        String legalSymbols = "0l23456789*+-/1";
        Log.d("AADADAD4", Integer.toString(row.length()));
        for (int i = 0; i < row.length() ; i++) {
                if(legalSymbols.contains(Character.toString(row.toCharArray()[i]))){
                    result += Character.toString(row.toCharArray()[i]);
                    Log.d("AADADAD3",Character.toString(row.toCharArray()[i]));
                }
        }
        return result;
    }


    public static double eval(final String str) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < str.length()) ? str.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < str.length()){
                    Log.d("AADADAD", "True");
                    Log.d("AADADAD", Character.toString((char) ch));
                    Log.d("AADADAD", Integer.toString(ch));

                    throw new RuntimeException("Unexpected: " + (char) ch);
                    }
                return x;
            }

            // Grammar:
            // expression = term | expression `+` term | expression `-` term
            // term = factor | term `*` factor | term `/` factor
            // factor = `+` factor | `-` factor | `(` expression `)`
            //        | number | functionName factor | factor `^` factor

            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm(); // addition
                    else if (eat('-')) x -= parseTerm(); // subtraction
                    else return x;
                }
            }

            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor(); // multiplication
                    else if (eat('/')) x /= parseFactor(); // division
                    else return x;
                }
            }

            double parseFactor() {
                if (eat('+')) return parseFactor(); // unary plus
                if (eat('-')) return -parseFactor(); // unary minus

                double x;
                int startPos = this.pos;
                if (eat('(')) { // parentheses
                    x = parseExpression();
                    eat(')');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(str.substring(startPos, this.pos));
                } else if (ch >= 'a' && ch <= 'z') { // functions
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = str.substring(startPos, this.pos);
                    x = parseFactor();
                    if (func.equals("sqrt")) x = Math.sqrt(x);
                    else if (func.equals("sin")) x = Math.sin(Math.toRadians(x));
                    else if (func.equals("cos")) x = Math.cos(Math.toRadians(x));
                    else if (func.equals("tan")) x = Math.tan(Math.toRadians(x));
                    else throw new RuntimeException("Unknown function: " + func);
                }
                else {
//                    Log.d("AADADAD", Integer.toString(ch));
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }

                if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

                return x;
            }
        }.parse();
    }

}
