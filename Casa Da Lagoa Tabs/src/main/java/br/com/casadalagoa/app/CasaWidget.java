package br.com.casadalagoa.app;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;


/**
 * Implementation of App Widget functionality.
 */
public class CasaWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[i]);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.casa_widget);
       // views.setTextViewText(R.id.appwidget_text, widgetText);
        //new HttpAsyncTask.execute("http://georgesilva.ddns.net:8080/");
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}

    /*
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

    */
    /*

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;
        inputStream.close();
        return result;
    }

    public void ajustacor( TextView textView, String retorno){
        try {
            int temperatura = 0;



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

*/
 /*


    public class HttpAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {

            return GET(urls[0]);
        }
        public String[] estado = {"0","0","0","0","0","0","0","0","0","0","0","0","0"};

        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(String result) {
      */
    /*

            // Toast.makeText(getBaseContext(), "(" + result.toString() + ") Conexão", Toast.LENGTH_LONG).show();
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

                RemoteViews tempViews = new RemoteViews(getBaseContext().getPackageName(), R.layout.casa_widget);
                tempViews.setTextViewText(R.id.appwidget_text, "Mudou...");
                View tempView = findViewById(R.id.lay_temp);

                estado = result.split(":");
                if (estado.length<21) {
                    Toast.makeText(getBaseContext(), "(" + result.toString() + ") Muito Curto", Toast.LENGTH_SHORT).show();
                }

                if (tempView!=null) {

                    TextView tmpPlacas = (TextView) findViewById(401); //  mViewPager.getChildAt(0).getRootView().
                    if (tmpPlacas != null) {
                        tmpPlacas.setText("Circuitos\n" + estado[1] + "º C");
                        ajustacor(tmpPlacas, estado[1]);  //result.substring(17, 19)
                    }

                    TextView tmpExt = (TextView) findViewById(402); //  mViewPager.getChildAt(0).getRootView().
                    if (tmpExt != null) {
                        tmpExt.setText("Externa\n" + estado[2] + "º C");
                        ajustacor(tmpExt, estado[2]);
                    }

                    TextView tmpInt = (TextView) findViewById(403); //  mViewPager.getChildAt(0).getRootView().
                    if (tmpInt != null) {
                        tmpInt.setText("Casa\n" + estado[3] + "º C");
                        ajustacor(tmpInt,  estado[3]);
                    }
                        /*
                        WebView tela = (WebView) findViewById(1001);

                        if ((tela!=null)&&(tela.getVisibility()==View.VISIBLE)){
                            tela.loadUrl(servidorGrafico);
                        }
                        */

                    //CharSequence widgetText = getBaseContext().getString(R.string.appwidget_text);
                    // Construct the RemoteViews object



/*
            } else {
                Toast.makeText(getBaseContext(), "Resultado Não Esperado: (" + result.toString() + ") Buscando novo servidor...", Toast.LENGTH_LONG).show();
                if (procurandoServidor<=3) {
                    new HttpAsyncTask().execute(strPlanilha);
                    procurandoServidor++;
                }
            }
        }
    }

*/



