package com.mpagliaro98.mysubscriptions.model;

import android.content.Context;
import com.mpagliaro98.mysubscriptions.R;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the SettingsManager class.
 */
public class SettingsManagerTest {

    // The component under test
    private SettingsManager CuT;

    private Context context;
    private static final String filepath = "src\\test\\java\\com\\mpagliaro98\\mysubscriptions";
    private static final String filename = "settings.dat";

    /**
     * Run before each test, make sure no test settings file exists and set mock method calls.
     */
    @Before
    public void setup() {
        context = mock(Context.class);
        when(context.getString(R.string.currency_default)).thenReturn("$");
        when(context.getString(R.string.date_format_default)).thenReturn("MM/dd/yyyy");
        File dir = new File(filepath);
        File file = new File(filepath + "\\" + filename);
        if (file.delete()) {
            System.out.println("[SETUP] Settings file deleted");
        } else {
            System.out.println("[SETUP] Failure to delete settings file, likely doesn't exist yet");
        }
        when(context.getFilesDir()).thenReturn(dir);
    }

    /**
     * Run after each test, remove the settings file if one was created.
     */
    @After
    public void cleanup() {
        File file = new File(filepath + "\\" + filename);
        if (file.delete()) {
            System.out.println("[CLEANUP] Settings file deleted");
        } else {
            System.out.println("[CLEANUP] Failure to delete settings file, IO stream is still open or file didn't exist");
        }
    }

    /**
     * Test loading the settings manager and that each field is its default value.
     */
    @Test
    public void test_default_settings() {
        try {
            CuT = new SettingsManager(context);
        } catch (IOException e) {
            fail();
        }

        assertTrue(CuT.getNotificationsOn());
        assertEquals("$", CuT.getCurrencySymbol());
        assertEquals("MM/dd/yyyy", CuT.getDateFormat());

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 6);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        assertEquals(calendar.getTime(), CuT.getNotificationTime());
    }

    /**
     * Test setting each setting to new values.
     */
    @Test
    public void test_set_settings() {
        try {
            CuT = new SettingsManager(context);
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 7);
            calendar.set(Calendar.MINUTE, 24);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            // Open the output stream
            File file = new File(filepath + "\\" + filename);
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(file);
                when(context.openFileOutput(filename, Context.MODE_PRIVATE)).thenReturn(fos);
            } catch (FileNotFoundException e) {
                fos = null;
            }

            // Don't do anything on IOException since we aren't focused on saving settings
            try {
                CuT.setSettings(false, calendar.getTime(), "£",
                        "dd/MM/yyyy", context);
            } catch (IOException e) {}

            // Close the output stream
            assert fos != null;
            fos.close();
        } catch (IOException e) {
            fail();
        }

        assertFalse(CuT.getNotificationsOn());
        assertEquals("£", CuT.getCurrencySymbol());
        assertEquals("dd/MM/yyyy", CuT.getDateFormat());

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 7);
        calendar.set(Calendar.MINUTE, 24);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        assertEquals(calendar.getTime(), CuT.getNotificationTime());
    }
}
