package com.example.fridgewise.ui.viewmodel;

import com.example.fridgewise.R;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class OnboardingViewModel extends ViewModel {
    private final MutableLiveData<Boolean> shouldSaveRegistration = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> registrationSuccess = new MutableLiveData<>(false);

    public void triggerSave() {
        shouldSaveRegistration.setValue(true);
    }

    public LiveData<Boolean> getShouldSaveRegistration() {
        return shouldSaveRegistration;
    }

    public void onRegistrationProcessed(boolean success) {
        shouldSaveRegistration.setValue(false);
        if (success) {
            registrationSuccess.setValue(true);
        }
    }

    public LiveData<Boolean> getRegistrationSuccess() {
        return registrationSuccess;
    }
}
