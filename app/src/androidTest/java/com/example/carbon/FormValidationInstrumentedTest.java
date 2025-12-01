package com.example.carbon;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasErrorText;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.IBinder;
import android.view.WindowManager;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.espresso.Root;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class FormValidationInstrumentedTest {

    @Test
    public void createEventShowsErrorWhenRequiredFieldsMissing() {
        try (ActivityScenario<CreateEventActivity> ignored = ActivityScenario.launch(CreateEventActivity.class)) {
            onView(withId(R.id.create_event_btn)).perform(click());
            onView(withId(R.id.create_event_title_input))
                    .check(matches(hasErrorText("Event Title is required")));
        }
    }

    @Test
    public void logInShowsToastWhenFieldsEmpty() {
        try (ActivityScenario<LogInActivity> ignored = ActivityScenario.launch(LogInActivity.class)) {
            onView(withId(R.id.sign_up_btn)).perform(click());
            onView(withText("Please fill in all fields"))
                    .inRoot(new ToastMatcher())
                    .check(matches(isDisplayed()));
        }
    }

    /**
     * Matches Toast windows so we can assert toast visibility in Espresso.
     */
    private static class ToastMatcher extends TypeSafeMatcher<Root> {
        @Override
        public void describeTo(Description description) {
            description.appendText("is toast");
        }

        @Override
        protected boolean matchesSafely(Root root) {
            int type = root.getWindowLayoutParams().get().type;
            if (type == WindowManager.LayoutParams.TYPE_TOAST) {
                IBinder windowToken = root.getDecorView().getWindowToken();
                IBinder appToken = root.getDecorView().getApplicationWindowToken();
                return windowToken == appToken;
            }
            return false;
        }
    }
}
