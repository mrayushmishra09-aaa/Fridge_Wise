package com.example.fridgewise.ui.viewmodel;

import com.example.fridgewise.R;
import com.example.fridgewise.data.AppDatabase;
import com.example.fridgewise.data.PreferenceManager;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.Executors;

public class ProfileViewModel extends AndroidViewModel {

    private final PreferenceManager prefManager;
    private final MutableLiveData<String> userName = new MutableLiveData<>();
    private final MutableLiveData<String> userEmail = new MutableLiveData<>();
    private final MutableLiveData<String> userDOB = new MutableLiveData<>();
    private final MutableLiveData<String> userId = new MutableLiveData<>();
    private final MutableLiveData<String> authProvider = new MutableLiveData<>();
    private final MutableLiveData<String> userPassword = new MutableLiveData<>();
    private final MutableLiveData<String> profileImageUri = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isGuest = new MutableLiveData<>();
    private final MutableLiveData<Boolean> logoutCompleted = new MutableLiveData<>(false);

    public ProfileViewModel(@NonNull Application application) {
        super(application);
        prefManager = new PreferenceManager(application);
        loadUserData();
    }

    private void loadUserData() {
        userName.setValue(prefManager.getUserName());
        userEmail.setValue(prefManager.getUserEmail());
        userDOB.setValue(prefManager.getUserDOB());
        userId.setValue(prefManager.getUserId());
        authProvider.setValue(prefManager.getAuthProvider());
        userPassword.setValue(prefManager.getUserPassword());
        profileImageUri.setValue(prefManager.getProfileImageUri());
        isLoggedIn.setValue(prefManager.isLoggedIn());
        isGuest.setValue(prefManager.isGuest());
    }

    public LiveData<Boolean> getIsLoggedIn() {
        return isLoggedIn;
    }

    public LiveData<Boolean> getIsGuest() {
        return isGuest;
    }

    public LiveData<String> getUserName() {
        return userName;
    }

    public LiveData<String> getUserEmail() {
        return userEmail;
    }

    public LiveData<String> getUserDOB() {
        return userDOB;
    }

    public LiveData<String> getUserId() {
        return userId;
    }

    public LiveData<String> getAuthProvider() {
        return authProvider;
    }

    public LiveData<String> getUserPassword() {
        return userPassword;
    }

    public LiveData<String> getProfileImageUri() {
        return profileImageUri;
    }

    public void updateProfile(String name, String email, String dob) {
        prefManager.setUserName(name);
        prefManager.setUserEmail(email);
        prefManager.setUserDOB(dob);
        userName.setValue(name);
        userEmail.setValue(email);
        userDOB.setValue(dob);
    }

    public void updateCredentials(String email, String password) {
        prefManager.setUserEmail(email);
        prefManager.setUserPassword(password);
        userEmail.setValue(email);
        userPassword.setValue(password);
    }

    public void updateProfileImage(String uri) {
        prefManager.setProfileImageUri(uri);
        profileImageUri.setValue(uri);
    }

    public LiveData<Boolean> getLogoutCompleted() {
        return logoutCompleted;
    }

    public void logout() {
        logoutCompleted.setValue(false);
        prefManager.clearAll();
        // Perform a full database wipe on a background thread
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(getApplication()).clearAllTables();
            logoutCompleted.postValue(true);
        });
    }
}
