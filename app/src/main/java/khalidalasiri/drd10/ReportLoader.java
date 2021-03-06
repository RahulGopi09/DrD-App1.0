package khalidalasiri.drd10;


import android.support.v4.content.AsyncTaskLoader;
import android.content.Context;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ReportLoader extends AsyncTaskLoader<List<Report>> {

    private String tableURL;
    private URL url;
    private String primaryKey;
    private String columnName;
    private int index;
    private String tableName;
    private String filter;
    private HttpURLConnection httpURLConnection;
    private List<HttpCookie> token;

    public ReportLoader(Context context, String tableURL, String primaryKey, String columnName, String tableName,String filter, List<HttpCookie> token) {
        super(context);
        this.tableURL = tableURL;
        this.primaryKey = primaryKey;
        this.columnName = columnName;
        this.tableName = tableName;
        this.filter = filter;
        this.token = token;
    }


    @Override
    public List<Report> loadInBackground() {
        List<Report> reportList = new ArrayList<>();

        try {
            String key = token.get(1).toString().replace("XSRF-TOKEN=","");
            url = new URL(tableURL+"?csrf="+key+"&include="+tableName+"&filter[]="+columnName+",eq,"+ primaryKey+filter);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Content-Type", "application/json");
            httpURLConnection.setRequestProperty("Accept", "application/json");
            httpURLConnection.setRequestProperty("Cookie", TextUtils.join(";", token));

            // Receive the Content of the URL and build the String
            InputStream inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder builder = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }

            JSONObject root = new JSONObject(builder.toString());
            JSONObject jsonObject1 = root.getJSONObject("Patient_History");
            JSONArray records1 = jsonObject1.getJSONArray("records");
            JSONObject jsonObject2 = root.getJSONObject(tableName);
            JSONArray records2 = jsonObject2.getJSONArray("records");

            for (int i=0; i < records2.length();i++)
            {
                Report report = new Report(records1.getJSONArray(i).get(0).toString(),
                        records2.getJSONArray(i).get(0).toString(),
                        records2.getJSONArray(i).get(1).toString(),
                        records2.getJSONArray(i).get(2).toString(),
                        records2.getJSONArray(i).get(3).toString(),
                        records2.getJSONArray(i).get(4).toString());

                reportList.add(report);
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return reportList;
    }
}
