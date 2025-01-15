package fr.enseeiht.l5.easy.ui.authenticate;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import fr.enseeiht.l5.easy.R;
import fr.enseeiht.l5.easy.databinding.FragmentAuthenticateBinding;

/**
 * Fragment to let the user authenticate in order to deactivate the alarm
 */

/*CHOCOLAT*/
public class AuthenticateFragment extends Fragment {

    private FragmentAuthenticateBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentAuthenticateBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SharedPreferences sharedPref = getActivity().getSharedPreferences(getString(R.string.sharedPrefName), Context.MODE_PRIVATE);
        LinearLayout authenticateLayout = binding.authenticateLayout;


        final TextView textAuthenticate = binding.textAuthenticate;

        if(sharedPref.getBoolean(getString(R.string.needAuthentication), false)){
            textAuthenticate.setText(getString(R.string.authenticationTextNeed));
            authenticateLayout.setVisibility(View.VISIBLE);
        } else {
            textAuthenticate.setText(getString(R.string.authenticationTextNoNeed));
        }

        final Button authenticateButton = binding.authenticateButton;

        authenticateButton.setOnClickListener(view -> {
            //Set an internal variable to say there is no need to authenticate anymore
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(getString(R.string.needAuthentication), false);
            editor.apply();
            //Change UI
            authenticateLayout.setVisibility(View.INVISIBLE);
            textAuthenticate.setText(getString(R.string.authenticationTextNoNeed));

            // Send authentication to server
            String json = "{\"key\":\"" + sharedPref.getString(getString(R.string.deviceId),"") + "\",\"message\":\"set_alarm_off\",\"timestamp\":\"" + (System.currentTimeMillis()/1000L) + "\"}";
            new SendOffAlarm().execute(json);
        });

        // button to let the user call the police if needed
        Button callThePoliceButton = binding.policeButton;
        callThePoliceButton.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:112"));
            startActivity(intent);
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Send the HTTP Post to the server to deactivate the alarm
     */
    class SendOffAlarm extends AsyncTask<String, Void, String>{

        public final MediaType JSON = MediaType.get("application/json");
        OkHttpClient client = new OkHttpClient();

        @Override
        protected String doInBackground(String... strings) {
            String json = strings[0];

            //RequestBody body = RequestBody.create(JSON,json.getBytes());
            RequestBody body = RequestBody.create(json,JSON);
            Request request = new Request.Builder()
                    .url("https://easy.kazzad.fr")
                    /*.addHeader("Content-Type", "application/json")*/
                    .post(body)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                Log.d("Response HTTP POST (Status code)", String.valueOf(response.code()));
                return response.body().string();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.d("Response HTTP POST", s);
        }
    }
}