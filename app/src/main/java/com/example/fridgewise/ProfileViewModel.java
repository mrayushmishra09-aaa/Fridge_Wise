package com.example.fridgewise;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class ProfileViewModel extends AndroidViewModel {

    private final PreferenceManager prefManager;
    private final MutableLiveData<String> userName = new MutableLiveData<>();
    private final MutableLiveData<Integer> userAge = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        prefManager = new PreferenceManager(application);
        loadUserData();
    }

    private void loadUserData() {
        userName.setValue(prefManager.getUserName());
        userAge.setValue(prefManager.getUserAge());
    }

    public LiveData<String> getUserName() {
        return userName;
    }

    public LiveData<Integer> getUserAge() {
        return userAge;
    }

    public void updateProfile(String name, int age) {
        prefManager.setUserName(name);
        prefManager.setUserAge(age);
        userName.setValue(name);
        userAge.setValue(age);
    }

    public void logout() {
        prefManager.clearAll();
    }
}
