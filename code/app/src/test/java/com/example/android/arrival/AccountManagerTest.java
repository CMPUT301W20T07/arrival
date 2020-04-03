package com.example.android.arrival;

import android.net.Uri;

import com.example.android.arrival.Model.Driver;
import com.example.android.arrival.Model.Rider;
import com.example.android.arrival.Util.AccountCallbackListener;
import com.example.android.arrival.Util.AccountManager;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *
 */
public class AccountManagerTest {

    private static final String TAG = "AccountManagerTest";
    private static AccountManager mockAccountManager;
    private static final String MOCKED_UID = "1Uqq9sCOQdaSTBcgcILZFiaLwX22";
    private Rider rider = new Rider("Naan", "naan@test.ca", "4165165", "aUbejendaks");
    private String password = "Hellooo";
    private static TestAccountCallbackListener mockAccountCallbackListener;


    @BeforeAll
    static void setUp() {
        mockAccountManager = mock(AccountManager.class);
        setMock(mockAccountManager);
        mockAccountCallbackListener = new TestAccountCallbackListener();

    }

    static private void setMock(AccountManager mock) {
        try {
            Field instance = AccountManager.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(instance, mock);
        } catch (Exception e) {
           throw new RuntimeException(e);
        }
    }

    @Test
    void addRider() throws InterruptedException {
        assertNull(mockAccountManager.getUID());
        mockAccountManager.createRiderAccount(rider, password, mockAccountCallbackListener);

    }

    @Test
    void getData(){
        assertNull(mockAccountManager.getUID());
        mockAccountManager.getUserData(mockAccountCallbackListener);
    }

    @AfterAll
    static void resetSingleton() throws Exception{
        Field instance = AccountManager.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

}
class TestAccountCallbackListener implements AccountCallbackListener {

    @Override
    public void onAccountTypeRetrieved(String userType) {

    }

    @Override
    public void onAccountTypeRetrieveFailure(String e) {

    }

    @Override
    public void onAccountCreated(String accountType) {
        assertEquals("rider", accountType);
    }

    @Override
    public void onAccountCreationFailure(String e) {

    }

    @Override
    public void onRiderDataRetrieved(Rider rider) {

    }

    @Override
    public void onDriverDataRetrieved(Driver driver) {

    }

    @Override
    public void onDataRetrieveFail(String e) {

    }

    @Override
    public void onAccountDeleted() {

    }

    @Override
    public void onAccountDeleteFailure(String e) {

    }

    @Override
    public void onImageUpload() {

    }

    @Override
    public void onImageUploadFailure(String e) {

    }

    @Override
    public void onPhotoReceived(Uri uri) {

    }

    @Override
    public void onPhotoReceiveFailure(String e) {

    }

    @Override
    public void onAccountUpdated() {

    }

    @Override
    public void onAccountUpdateFailure(String e) {

    }
}
