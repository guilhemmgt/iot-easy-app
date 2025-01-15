package fr.enseeiht.l5.easy.ui.connect;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.io.IOException;
import java.util.Objects;

import fr.enseeiht.l5.easy.MainActivity;
import fr.enseeiht.l5.easy.R;
import fr.enseeiht.l5.easy.databinding.FragmentConnectBinding;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ConnectFragment extends Fragment {

    private FragmentConnectBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentConnectBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final EditText tokenEditText = binding.tokenText;

        // Set token to the TextField to a manual access
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(Objects.requireNonNull(getActivity()), new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                tokenEditText.setText(instanceIdResult.getToken());
            }
        });

        // button to send token associated to the device id
        final EditText idEditText = binding.idConnectField;
        final TextView idConnectedText = binding.connectedIdText;
        Button connectButton = binding.connectButton;
        connectButton.setOnClickListener(view -> FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(Objects.requireNonNull(getActivity()), instanceIdResult ->{
            new SendId().execute(idEditText.getText().toString(), instanceIdResult.getToken());
            idConnectedText.setText(idEditText.getText().toString());
        }));
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    /**
     * Send the HTTP Post to send the notification token to the server
     */
    class SendId extends AsyncTask<String, Void, String> {

        public final MediaType JSON = MediaType.get("application/json");
        OkHttpClient client = new OkHttpClient();

        @Override
        protected String doInBackground(String... strings) {
            String deviceId = strings[0];
            String token = strings[1];

            String json = "{\"key\":\"" + deviceId + "\",\"message\":\"add_phone\",\"timestamp\":\"" + (System.currentTimeMillis()/1000L) + "\", \"data\":\"" + token +"\"}";

            //RequestBody body = RequestBody.create(JSON,json.getBytes());
            RequestBody body = RequestBody.create(json,JSON);
            Request request = new Request.Builder()
                    .url("https://easy.kazzad.fr")
                    .post(body)
                    .build();
            try (Response response = client.newCall(request).execute()) {
                Log.d("Response HTTP POST (Status code)", String.valueOf(response.code()));
                if (response.code() == 200){
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences(getString(R.string.sharedPrefName), Context.MODE_PRIVATE).edit();
                    editor.putString(getString(R.string.deviceId), deviceId);
                    editor.apply();
                }
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