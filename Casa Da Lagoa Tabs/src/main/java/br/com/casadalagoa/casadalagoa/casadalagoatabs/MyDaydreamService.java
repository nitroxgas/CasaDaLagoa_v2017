package br.com.casadalagoa.casadalagoa.casadalagoatabs;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.service.dreams.DreamService;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.LinearInterpolator;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


/**
 * This class is a sample implementation of a DreamService. When activated, a
 * TextView will repeatedly, move from the left to the right of screen, at a
 * random y-value.
 * <p />
 * Daydreams are only available on devices running API v17+.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class MyDaydreamService extends DreamService {
    boolean showConfig = false;
    boolean showGrafico = false;
    boolean syncAbas = false;
    boolean conectar = true;
    Timer timer;
    MyTimerTask myTimerTask;

    public int procurandoServidor = 0;
    public String servidor = "https://docs.google.com/spreadsheet/pub?key=0AthpB0DCO-YadE5tcC1BVWRzSnNBRkRmLTJfaGhTOFE&single=true&gid=0&range=A1&output=csv";
    public String strPlanilha = "https://docs.google.com/spreadsheet/pub?key=0AthpB0DCO-YadE5tcC1BVWRzSnNBRkRmLTJfaGhTOFE&single=true&gid=0&range=A1&output=csv";
    public SharedPreferences mPrefs;
    public String LOG_TAG = "FILTRAR";

    private static final TimeInterpolator sInterpolator = new LinearInterpolator();

    private final AnimatorListener mAnimListener = new AnimatorListenerAdapter() {

        @Override
        public void onAnimationEnd(Animator animation) {
            // Start animation again
            startTextViewScrollAnimation();
        }

    };

    private final Random mRandom = new Random();
    private final Point mPointSize = new Point();

    private TextView mDreamTextView;
    private ViewPropertyAnimator mAnimator;

    public String getCurrentSsid(Context context) {

        String ssid = null;
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !(connectionInfo.getSSID().equals(""))) {
                //if (connectionInfo != null && !StringUtil.isBlank(connectionInfo.getSSID())) {
                ssid = connectionInfo.getSSID();
            }
        }
        return ssid;
    }

    private void Conectar() {
        // Verifica se há conexão e faz solicitação inicial
        if (isConnected()) {
            ConnectivityManager connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
            // NetworkInfo mobNetInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (activeNetInfo != null) {
                if (activeNetInfo.getTypeName().toString().contains("WIFI")) {
                    if (showConfig)
                        Toast.makeText(getBaseContext(), "WIFI SSID (" + getCurrentSsid(this) + ")", Toast.LENGTH_LONG).show();
                    if (getCurrentSsid(this).contains("GeorgeHome")) {
                        servidor = mPrefs.getString("servidor_casa", "http://192.168.1.220/");
                        new HttpAsyncTask().execute(servidor);
                        if (showConfig)
                            Toast.makeText(getBaseContext(), "Acessando rede local...", Toast.LENGTH_LONG).show();
                        conectar = false;
                    }
                } else {
                    servidor = mPrefs.getString("servidor", strPlanilha);
                    Toast.makeText(this, "Rede Móvel", Toast.LENGTH_SHORT).show();
                    new HttpAsyncTask().execute(servidor);
                    conectar = false;
                }
            }
        } else {
            Toast.makeText(getBaseContext(), "Não Conectado!", Toast.LENGTH_LONG).show();
        }
        //if (showConfig) Log.v(LOG_TAG,"Conectar");
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Exit dream upon user touch?
        setInteractive(true);

        // Hide system UI?
        setFullscreen(true);

        // Keep screen at full brightness?
        setScreenBright(true);

        // Set the content view, just like you would with an Activity.
        setContentView(R.layout.my_daydream);

        mDreamTextView = (TextView) findViewById(R.id.dream_text);
        mDreamTextView.setText(getTextFromPreferences());
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        showConfig = mPrefs.getBoolean("showConfig", false);
        showGrafico = mPrefs.getBoolean("showGrafico", false);
        syncAbas = mPrefs.getBoolean("syncAbas", false);
        Conectar();
    }

    @Override
    public void onDreamingStarted() {
        super.onDreamingStarted();

        // TODO: Begin animations or other behaviors here.

        startTextViewScrollAnimation();
        String atualizar = mPrefs.getString("sync_frequency", "-1");
        if (!atualizar.equals("-1")) {
            timer = new Timer();
            myTimerTask = new MyTimerTask();

            timer.schedule(myTimerTask, 30000 * Integer.valueOf(atualizar));
            if (showConfig)
                Toast.makeText(getBaseContext(), "(Iniciou Timer " + atualizar + " minutos)", Toast.LENGTH_SHORT).show();
        }
    }


    /*
        Timer para atualizar o conteúdo efetuando nova consulta.
    */
    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
                    new HttpAsyncTask().execute("http://api.openweathermap.org/data/2.5/forecast/daily?q=Florian%C3%B3polis&lang=pt&cnt=2&mode=html");
        }
    }



    @Override
    public void onDreamingStopped() {
        super.onDreamingStopped();

        // TODO: Stop anything that was started in onDreamingStarted()

        mAnimator.cancel();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // TODO: Dismantle resources
        // (for example, detach from handlers and listeners).
    }

    private String getTextFromPreferences() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        return prefs.getString("servidor", //getString(R.string.pref_dream_text_key),
                getString(R.string.pref_dream_text_default));
    }

    private void startTextViewScrollAnimation() {
        // Refresh Size of Window
        getWindowManager().getDefaultDisplay().getSize(mPointSize);

        final int windowWidth = mPointSize.x;
        final int windowHeight = mPointSize.y;

        // Move TextView so it's moved all the way to the left
        mDreamTextView.setTranslationX(-mDreamTextView.getWidth());

        // Move TextView to random y value
        final int yRange = windowHeight - mDreamTextView.getHeight();
        mDreamTextView.setTranslationY(mRandom.nextInt(yRange));

        // Create an Animator and keep a reference to it
        mAnimator = mDreamTextView.animate().translationX(windowWidth)
            .setDuration(5000)
            .setStartDelay(500)
            .setListener(mAnimListener)
            .setInterpolator(sInterpolator);

        // Start the animation
        mAnimator.start();
    }

    //
    // Sessão para tratar das comunicações:
    //
    public static String GET(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            // create HttpClient
            HttpClient httpclient = new DefaultHttpClient();

            // make GET request to the given URL
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));

            // receive response as inputStream
            inputStream = httpResponse.getEntity().getContent();

            // convert inputstream to string
            if(inputStream != null) {
                result = convertInputStreamToString(inputStream);
            }
            else
                result = "Não funcionou";


        } catch (Exception e) {
            result = "Exception no recebimento..." + e.toString();
            // this.servidor = "http://186.222.50.176:8080";
        }

        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;
    }

    public boolean isConnected(){
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected())

            return true;
        else
            return false;
    }

    public void ajustacor( TextView textView, String retorno){
        try {
            int temperatura = 0;

           /*
            temperatura = Long.parseLong(retorno);
            */

            temperatura = Integer.parseInt(retorno); //.substring(0,retorno.indexOf(".")

            if (temperatura < 20)
                textView.setTextColor(textView.getResources().getColor(R.color.c_azul));
            else if ((temperatura >= 20) && (temperatura < 30))
                textView.setTextColor(textView.getResources().getColor(R.color.c_verde));
            else if ((temperatura >= 30) && (temperatura < 40))
                textView.setTextColor(textView.getResources().getColor(R.color.c_laranja));
            else if (temperatura >= 40) {
                textView.setTextColor(textView.getResources().getColor(R.color.c_vermelho));
            }
        } catch (NumberFormatException nfe) {
            System.out.println("Não foi possível decodificar " + nfe);
        }

    }

    public void implementaClick(Switch sw, final String rele){
        sw.setOnClickListener(
                new Switch.OnClickListener() {
                    public void onClick(View view) {
                        new HttpAsyncTask().execute(servidor+rele);
                    }
                });

    }

    public class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return GET(urls[0]);
        }
        public String[] estado = {"0","0","0","0","0","0","0","0","0","0","0","0","0"};

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
            /*
             *  Verifica se o retorno não é nulo e se tem o tamanho esperado.
             */

             Toast.makeText(getBaseContext(), "(" + result.toString() + ") Conexão", Toast.LENGTH_LONG).show();
            mDreamTextView = (TextView) findViewById(R.id.dream_text);


            if (result.matches("^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
                    "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$")){

                servidor = "http://"+result.toString()+":8080/";
                new HttpAsyncTask().execute(servidor);
                Toast.makeText(getBaseContext(), "Novo IP Identificado:(" + servidor.toString() + ")", Toast.LENGTH_SHORT).show();
                // Salva valor localmente...
                SharedPreferences.Editor mEditor = mPrefs.edit();
                mEditor.putString("servidor", servidor.toString()).commit();
                procurandoServidor=0;
            } else
            if (result.contains("</DADOS>")) {
                // result = result +":19:25";

                result = result.substring(result.indexOf("<DADOS>")+7, result.indexOf("</DADOS>"));

                if (showConfig) Toast.makeText(getBaseContext(), "(" + result.toString() + ")", Toast.LENGTH_SHORT).show();


                View tempView = findViewById(R.id.lay_temp);

                estado = result.split(":");
                if (estado.length<21) {
                    Toast.makeText(getBaseContext(), "(" + result.toString() + ") Muito Curto", Toast.LENGTH_SHORT).show();
                }

                /*
                Estados
                1  -> Temperatura Circuitos
                2  -> Temperatura Externa
                3  -> Temperatura Interna
                4  -> Área de serviço
                5  -> Fundos
                6  -> Relê
                7  -> Relê
                8  -> Relê
                9  ->
                10 ->

                 */

                    /*
                    // Caso haja o fragment de temperaturas, preenche com o resultado
                     */

                if (tempView!=null) {

                    TextView tmpPlacas = (TextView) findViewById(R.id.tmpPlaca); //  mViewPager.getChildAt(0).getRootView().
                    if (tmpPlacas != null) {
                        tmpPlacas.setText("Circuitos\n" + estado[1] + "º C");
                        ajustacor(tmpPlacas, estado[1]);  //result.substring(17, 19)
                    }

                    TextView tmpExt = (TextView) findViewById(R.id.tmpExt); //  mViewPager.getChildAt(0).getRootView().
                    if (tmpExt != null) {
                        tmpExt.setText("Externa\n" + estado[2] + "º C");
                        ajustacor(tmpExt, estado[2]);
                    }

                    TextView tmpInt = (TextView) findViewById(R.id.tmpInt); //  mViewPager.getChildAt(0).getRootView().
                    if (tmpInt != null) {
                        tmpInt.setText("Casa\n" + estado[3] + "º C");
                        ajustacor(tmpInt,  estado[3]);
                        mDreamTextView.setText("Dentro de casa : " + estado[3] + "º C");
                        ajustacor(mDreamTextView, estado[3]);
                    }
                        /*
                        WebView tela = (WebView) findViewById(1001);

                        if ((tela!=null)&&(tela.getVisibility()==View.VISIBLE)){
                            tela.loadUrl(servidorGrafico);
                        }
                        */
                }

                /*
                for (int i=1;i<estado.length;i++) {
                    Toast.makeText(getBaseContext(), "(" + estado[i] + ") Estado "+ i, Toast.LENGTH_SHORT).show();
                }
                 */

            } else {
                Toast.makeText(getBaseContext(), "Resultado Não Esperado: (" + result.toString() + ") Buscando novo servidor...", Toast.LENGTH_LONG).show();
                if (procurandoServidor<=3) {
                    new HttpAsyncTask().execute(strPlanilha);
                    procurandoServidor++;
                }
            }
        }
    }

}
