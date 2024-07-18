package de.chrtra.sumup_self_service_kiosk;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;


import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MainActivityInstrumentedTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    private SharedPreferences sharedPreferences;

    @Before
    public void setUp() {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext());
        sharedPreferences.edit().putString("affiliate_key", "test_key").apply();
    }

    @Test
    public void testLoginButton() {
        onView(withId(R.id.button_login)).perform(click());
        // Here you would need to handle the login activity intent
    }

    @Test
    public void testLogMerchantButton_NotLoggedIn() {
        onView(withId(R.id.button_log_merchant)).perform(click());
        onView(withId(R.id.message)).check(matches(withText(R.string.not_logged_in_message)));
    }

    @Test
    public void testLogMerchantButton_LoggedIn() {
        // Mock login state and merchant details
        // Here you would mock SumUpAPI.isLoggedIn() and SumUpAPI.getCurrentMerchant()

        // Example:
        // when(SumUpAPI.isLoggedIn()).thenReturn(true);
        // when(SumUpAPI.getCurrentMerchant()).thenReturn(mockMerchant);

        onView(withId(R.id.button_log_merchant)).perform(click());
        // Check that the message is updated with merchant details
    }

    @Test
    public void testCardReaderPageButton() {
        onView(withId(R.id.button_card_reader_page)).perform(click());
        // Here you would need to handle the card reader page intent
    }

    @Test
    public void testLogoutButton() {
        onView(withId(R.id.button_logout)).perform(click());
        // Verify logout action
    }

    @Test
    public void testOpenWebViewButton_SettingsNotSet() {
        sharedPreferences.edit().clear().apply();
        onView(withId(R.id.btn_open_webview)).perform(click());
        onView(withText(R.string.settings_not_set_message)).inRoot(new ToastMatcher())
                .check(matches(isDisplayed()));
    }

    @Test
    public void testOpenWebViewButton_SettingsSet_NotLoggedIn() {
        sharedPreferences.edit().putString("custom_url_schema", "schema")
                .putString("custom_url_host", "host")
                .putString("start_url", "url")
                .putString("success_url", "success")
                .putString("error_url", "error")
                .putString("currency", "EUR")
                .apply();
        onView(withId(R.id.btn_open_webview)).perform(click());
        // Verify that login activity is opened
    }
}
