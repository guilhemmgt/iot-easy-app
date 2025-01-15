package fr.enseeiht.l5.easy.ui.authenticate;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AuthenticateViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public AuthenticateViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is slideshow fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}