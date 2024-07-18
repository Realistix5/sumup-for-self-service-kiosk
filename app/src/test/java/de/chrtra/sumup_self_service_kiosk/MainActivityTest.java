package de.chrtra.sumup_self_service_kiosk;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MainActivityTest {

    @Mock
    Context mockContext;

    @Mock
    SharedPreferences mockSharedPreferences;

    @Mock
    SharedPreferences.Editor mockEditor;

    MainActivity mainActivity;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mainActivity = new MainActivity();
        mainActivity.sharedPreferences = mockSharedPreferences;
    }

    @Test
    public void testAreSettingsValid_SettingsNotSet() {
        when(mockSharedPreferences.getString("custom_url_schema", "")).thenReturn("");
        when(mockSharedPreferences.getString("custom_url_host", "")).thenReturn("");
        when(mockSharedPreferences.getString("start_url", "")).thenReturn("");
        when(mockSharedPreferences.getString("success_url", "")).thenReturn("");
        when(mockSharedPreferences.getString("error_url", "")).thenReturn("");
        when(mockSharedPreferences.getString("currency", "")).thenReturn("");

        assertFalse(mainActivity.areSettingsValid());
    }

    @Test
    public void testAreSettingsValid_SettingsSet() {
        when(mockSharedPreferences.getString("custom_url_schema", "")).thenReturn("schema");
        when(mockSharedPreferences.getString("custom_url_host", "")).thenReturn("host");
        when(mockSharedPreferences.getString("start_url", "")).thenReturn("url");
        when(mockSharedPreferences.getString("success_url", "")).thenReturn("success");
        when(mockSharedPreferences.getString("error_url", "")).thenReturn("error");
        when(mockSharedPreferences.getString("currency", "")).thenReturn("EUR");

        assertTrue(mainActivity.areSettingsValid());
    }
}
