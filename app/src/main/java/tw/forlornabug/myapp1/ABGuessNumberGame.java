package tw.forlornabug.myapp1;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Random;


public class ABGuessNumberGame extends Activity {


    public static final String PREF = "GameScore";

    public static final String PREF_SCORE[] = {"NoneUse", "1DigitsScore", "2DigitsScore",
            "3DigitsScore", "4DigitsScore", "5DigitsScore",

            "7DigitsScore", "7DigitsScore", "8DigitsScore",

            "9DigitsScore"};

    private EditText edittextGuessDigits, edittextGuessNumber;

    private Button btnPlayorNew, btnExit, btnGuess, btnGiveUp, btnClear;

    private TextView textviewMessage, textviewThisScore, textviewBestScore;

    private ListView listviewResult;

    private ListAdapter laSimple1;

    private ArrayList<String> listItems = new ArrayList<String>();

    private boolean playFlag = false, bingoFlag;

    private int playDigits, n, m, intBestScore;

    private String strAnswerNumber, strOutputString;




    @Override

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        findViews();

        initialSetup();

        setButtonListeners();

    }


    private void findViews() {

        // 設定 Widgets 的 findViewById

        edittextGuessDigits = (EditText) findViewById(R.id.guessdigitsEditText);

        edittextGuessNumber = (EditText) findViewById(R.id.guessnumberEditText);

        btnPlayorNew = (Button) findViewById(R.id.playornewBtn);

        btnGuess = (Button) findViewById(R.id.guessBtn);

        btnGiveUp = (Button) findViewById(R.id.giveupBtn);

        btnExit = (Button) findViewById(R.id.exitBtn);

        btnClear = (Button) findViewById(R.id.clearBtn);

        textviewMessage = (TextView) findViewById(R.id.messageTextView);

        textviewThisScore = (TextView) findViewById(R.id.thisscoreTextView);

        textviewBestScore = (TextView) findViewById(R.id.bestscoreTextView);

        listviewResult = (ListView) findViewById(R.id.resultListView);

    }


    private void initialSetup() {

        // 設定 edittextGuessDigits, edittextGuessNumber 屬性

        edittextGuessDigits.setText("");

        edittextGuessDigits.setTextColor(Color.BLUE);

        edittextGuessDigits.setEnabled(true);

        edittextGuessDigits.requestFocus();

        edittextGuessNumber.setText("");

        edittextGuessNumber.setEnabled(false);


        // 設定  btnPlayorNew, btnGuess, btnGiveUp 屬性

        btnPlayorNew.setText(R.string.strPlay);

        btnGuess.setEnabled(false);

        btnGiveUp.setEnabled(false);


        // 清除 ListView 資料

        clearResults();


        // 隱藏 SoftKeyboard

        hideSoftKeyboard();

    }


    private void setButtonListeners() {

        // set Buttons Listeners


        btnPlayorNew.setOnClickListener(new View.OnClickListener() {

            // 執行 【開始玩】 或 【重玩】

            public void onClick(View v) {

                //上傳 0 , 0
                FirebaseDatabase db2 = FirebaseDatabase.getInstance();
                DatabaseReference usersRef2 = db2.getReference("myapp1-b1072");
                usersRef2.child("win").setValue(0);
                FirebaseDatabase db = FirebaseDatabase.getInstance();
                DatabaseReference usersRef = db.getReference("myapp1-b1072");
                usersRef.child("num").setValue(0);
                //


                if (edittextGuessDigits.length() != 1) // 沒輸入猜幾位數時，按鈕無效

                    return;


                playFlag = (!playFlag);


                if (playFlag) {

                    // 切換【猜數字模式】

                    // 設定 edittextGuessDigits , btnPlayorNew , btnGuess 屬性

                    edittextGuessDigits.setEnabled(false);

                    btnPlayorNew.setText(R.string.strNew);

                    btnGuess.setEnabled(true);

                    btnGiveUp.setEnabled(true);

                    btnClear.setEnabled(false);


                    // 設定 edittextGuessNumber 的輸入長度

                    playDigits = Integer.valueOf(edittextGuessDigits.getText().toString());

                    InputFilter[] FilterArray = new InputFilter[1];

                    FilterArray[0] = new InputFilter.LengthFilter(playDigits);

                    edittextGuessNumber.setFilters(FilterArray);
                    edittextGuessNumber.setHint("請輸入(1..9)不同數字的" + playDigits + "位數數字");


                    // 設定 edittextGuessNumber 屬性

                    edittextGuessNumber.setText("");
                    edittextGuessNumber.setEnabled(true);

                    edittextGuessNumber.requestFocus();


                    // 讀取最佳紀錄

                    intBestScore = getSharedPreferences(playDigits);


                    if (intBestScore == 999) // 999 代表尚苂最佳紀錄

                        textviewBestScore.setText(getString(R.string.strBestScore) + "<尚無紀錄>");

                    else

                        textviewBestScore.setText(getString(R.string.strBestScore) +

                                "猜數 " + intBestScore + " 次");


                    // 利用亂數產生一組數字, 儲放於 strAnswerNUmber
                    generateAnswerNumber();
                } else {
                    // 【輸入猜數位數模式】
                    // 設定 edittextGuessDigits, edittextGuessNumber 屬性
                    edittextGuessDigits.setText("");
                    edittextGuessDigits.setEnabled(true);
                    edittextGuessDigits.requestFocus();
                    edittextGuessNumber.setText("");
                    edittextGuessNumber.setEnabled(false);
                    // 設定 btnPlayorNew, btnGuess, btnGiveUp, btnClear 屬性
                    btnPlayorNew.setText(R.string.strPlay);
                    btnGuess.setEnabled(false);
                    btnGiveUp.setEnabled(false);
                    btnClear.setEnabled(true);

                    // 清除 ListView 資料
                    clearResults();
                }
            }
        });

        btnGuess.setOnClickListener(new OnClickListener() {
            // 執行 【猜看看】
            public void onClick(View v) {
                if (edittextGuessNumber.getText().toString().length() == playDigits) {
                    if (validateGuessNumber(edittextGuessNumber.getText().toString()))
                    // 輸入數字正確時
                    {
                        // 檢查猜對幾個數字
                        checkMatchResult();

                        // 顯示猜數結果
                        showMatchResult();
                    } else {
                        // 輸入數字錯誤時
                        textviewMessage.setText(getString(R.string.strMessage) +
                                edittextGuessNumber.getText().toString() +
                                " 輸入錯誤！");
                        edittextGuessNumber.setText("");
                    }
                }
            }
        });

        btnGiveUp.setOnClickListener(new OnClickListener() {
            // 執行 【看答案】
            public void onClick(View v) {
                // 顯示謎底答案
                textviewMessage.setText(getString(R.string.strMessage) + strAnswerNumber);

                // 設定 edittextGuessNumber 屬性
                edittextGuessNumber.setEnabled(false);
                edittextGuessNumber.clearFocus();

                // 設定 btnGuess, btnGiveUp 屬性
                btnGuess.setEnabled(false);
                btnGiveUp.setEnabled(false);

                // 隱藏 SoftKeyboard
                hideSoftKeyboard();
            }
        });


        btnClear.setOnClickListener(new OnClickListener() {
            // 執行 【清除紀錄】
            public void onClick(View v) {
                // 清除 SharedPreferences 所有資料
                clearSharedPreferences();
            }
        });

        btnExit.setOnClickListener(new OnClickListener() {
            // 執行 【離開】
            public void onClick(View v) {
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
    }

    private void generateAnswerNumber() {
        // 產生一組 playDigits 位數的數字，並且將產生的數寀儲放於 strAnswerNumber
        strAnswerNumber = "";
        for (int i = 1; i <= playDigits; i++) {
            int randomNo;
            boolean exitFlag;
            do {
                exitFlag = true;
                Random generator = new Random(System.currentTimeMillis());

                // 產生 1~9 之間的一個亂數整數
                do {
                    randomNo = generator.nextInt(10);
                } while (randomNo == 0);
                // 檢查這一個亂數整數與前面已產生的亂數整數是否重複
                if (i > 1) {
                    for (int j = 0; j <= (i - 2); j++) {
                        String chkDigitNo = strAnswerNumber.substring(j, j + 1);
                        if (chkDigitNo.equals(String.valueOf(randomNo))) {
                            // 數字重複時，將 exitFlag 設為 false
                            exitFlag = false;
                        }
                    }
                }
            } while (i > 1 && (!exitFlag));

            // 依序組合系統自動所產生的亂數整數
            if (i == 1)
                strAnswerNumber = String.valueOf(randomNo);
            else
                strAnswerNumber += String.valueOf(randomNo);
        }
    }


    private boolean validateGuessNumber(String guessNumber) {

        // 檢查輸入猜的數字中是否有重複？有重複時傳回 false;沒有重複時傳回 true


        for (int i = 0; i < guessNumber.length() - 1; i++) {

            String digit1 = guessNumber.substring(i, i + 1);

            for (int j = i + 1; j < guessNumber.length(); j++) {

                String digit2 = guessNumber.substring(j, j + 1);

                if (digit1.equals(digit2))

                    return false;

            }

        }

        return true;

    }


    private void checkMatchResult() {

        // 檢查猜中幾個數字，傳回  n A , m B 結果

        n = 0;

        m = 0;

        String strInputNumber = edittextGuessNumber.getText().toString();

        for (int i = 0; i < playDigits; i++) {

            String strInputDigit = strInputNumber.substring(i, i + 1);

            for (int j = 0; j < playDigits; j++) {

                String strAnswerDigit = strAnswerNumber.substring(j, j + 1);

                if (strAnswerDigit.equals(strInputDigit)) {
                    if (i == j)    // 數字與位置都正確時，累計 n 值
                        n++;
                    else {            // 數字正確但位置不對時，累計 m 值
                        m++;
                    }
                }
            }
        }

        // 將猜數字結果放入 strOutputString
        if (n == playDigits) {
            // BINGO
            bingoFlag = true;
            strOutputString = strInputNumber + "   " + "BINGO !!";
        } else {
            // n A m B
            bingoFlag = false;
            strOutputString = strInputNumber + "     " + n + " A " + m + " B ";
        }
        // 將 strOutputString 放入 ListView 的 listItems
        listItems.add(strOutputString);
    }


    private void showMatchResult() {

        // 顯示 listItems 的所有資料

        laSimple1 = new ArrayAdapter<String>(this,

                R.layout.my_list_row,

                listItems);

        listviewResult.setAdapter(laSimple1);


        // 顯示訊息區

        textviewMessage.setText(getString(R.string.strMessage) + strOutputString);


        // 顥示猜數總次數

        textviewThisScore.setText(getString(R.string.strScore) + listItems.size() + " 次");

        //firebase 傳次數
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = db.getReference("myapp1-b1072");
        usersRef.child("num").setValue(listItems.size());
        //


        // 清除 edittextGuessNumber 欄位

        edittextGuessNumber.setText("");


        if (bingoFlag)

        // 全部數字猜對時 ( BINGO )

        {

            // 設定 edittextGuessNumber 屬性

            edittextGuessNumber.setEnabled(false);

            edittextGuessNumber.clearFocus();


            // 設定 btnGuess, btnGiveUp 屬性

            btnGuess.setEnabled(false);

            btnGiveUp.setEnabled(false);

            //win 上傳勝利

            FirebaseDatabase db2 = FirebaseDatabase.getInstance();
            DatabaseReference usersRef2 = db2.getReference("myapp1-b1072");
            usersRef2.child("win").setValue(1);
            //


            if (listItems.size() < intBestScore) // 本次紀錄是最佳紀錄

            {

                // 顯示最佳紀錄

                textviewBestScore.setText(getString(R.string.strBestScore) +

                        listItems.size() + " 次" +

                        "  <破紀錄!>");

                // 儲存最佳紀錄

                SharedPreferences settings = getSharedPreferences("PREF", 0);

                SharedPreferences.Editor preEdit = settings.edit();

                preEdit.putInt(PREF_SCORE[playDigits], listItems.size());

                preEdit.commit();

            }


            if (listItems.size() == intBestScore) // 本次紀錄平最佳紀錄

            {

                // 顯示最佳紀錄

                textviewBestScore.setText(getString(R.string.strBestScore) +

                        listItems.size() + " 次" +

                        "  <平紀錄!>");

            }


        }


        // 隱藏 SoftKeyboard

        hideSoftKeyboard();

    }


    private void clearResults() {

        // 清除 listItems 的所有資料

        listItems.clear();

        laSimple1 = new ArrayAdapter<String>(this,

                R.layout.my_list_row,

                listItems);

        listviewResult.setAdapter(laSimple1);


        // 清除訊息區

        textviewMessage.setText(getString(R.string.strMessage));


        // 清除紀錄

        textviewThisScore.setText(getString(R.string.strScore));

        textviewBestScore.setText(getString(R.string.strBestScore));






    }


    private void hideSoftKeyboard() {

        // 隱藏 SoftKeyboard


        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        imm.hideSoftInputFromWindow(getWindow().peekDecorView().getWindowToken(), 0);

    }


    private int getSharedPreferences(int level) {

        // 讀取 SharedPreferences 資料

        SharedPreferences settings = getSharedPreferences("PREF", 0);

        int levelScore = settings.getInt(PREF_SCORE[level], 999);

        return levelScore;

    }


    private void clearSharedPreferences() {

        // 清除 SharedPreferences 資料

        SharedPreferences settings = getSharedPreferences("PREF", 0);

        settings.edit().clear().commit();

    }


}
