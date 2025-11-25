package com.example.carbon;

import static org.mockito.Mockito.*;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

/**
 * Test to verify that the updateBanStatusUI() method updates
 * the UI elements correctly when a user is banned or not banned.
 */
public class AllUsersAdapterTest {

    private AllUsersAdapter adapter;
    private AllUsersAdapter.ViewHolder mockHolder;

    @Before
    public void setup() {
        adapter = mock(AllUsersAdapter.class, CALLS_REAL_METHODS);

        // Mock UI
        mockHolder = mock(AllUsersAdapter.ViewHolder.class);
        mockHolder.bannedLabel = mock(TextView.class);
        mockHolder.banButton = mock(Button.class);
    }

    @Test
    public void testUpdateBanStatusUI_UserBanned() {
        adapter.updateBanStatusUI(mockHolder, true);

        // Expected: BANNED-label visible, button gone
        verify(mockHolder.bannedLabel).setVisibility(View.VISIBLE);
        verify(mockHolder.banButton).setVisibility(View.GONE);
    }

    @Test
    public void testUpdateBanStatusUI_UserNotBanned() {
        adapter.updateBanStatusUI(mockHolder, false);

        // Expected: BANNED-label gone, knop visible
        verify(mockHolder.bannedLabel).setVisibility(View.GONE);
        verify(mockHolder.banButton).setVisibility(View.VISIBLE);
    }
}
