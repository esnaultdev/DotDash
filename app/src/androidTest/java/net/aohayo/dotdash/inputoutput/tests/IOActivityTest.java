package net.aohayo.dotdash.inputoutput.tests;

import android.support.test.InstrumentationRegistry;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.SmallTest;

import net.aohayo.dotdash.R;
import net.aohayo.dotdash.inputoutput.IOActivity;

import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressBack;

public class IOActivityTest extends ActivityInstrumentationTestCase2<IOActivity> {

    public IOActivityTest() {
        super(IOActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        getActivity();
    }

    @SmallTest
    public void testInputSelectionAtStart() {
        onView(withId(R.id.inputSelection))
                .check(matches(isDisplayed()));
    }

    @SmallTest
    public void testOutputSelectionAtStart() {
        onView(withId(R.id.fab_input_layout))
                .perform(click());
        onView(withId(R.id.outputSelection))
                .check(matches(isDisplayed()));
    }

    @SmallTest
    public void testOutputSelectionPreviousAtStart() {
        onView(withId(R.id.fab_input_layout))
                .perform(click());
        onView(withId(R.id.outputSelection))
                .perform(pressBack());
        onView(withId(R.id.inputSelection))
                .check(matches(isDisplayed()));
    }

    @SmallTest
    public void testFABInput() {
        onView(withId(R.id.fab_input_layout))
                .perform(click());
        onView(withId(R.id.sound_output_layout))
                .perform(click());
        onView(withText("Select"))
                .perform(click());
        onView(withId(R.id.morse_input_fab))
                .check(matches(isDisplayed()));
    }

    @SmallTest
    public void testLargeButtonInput() {
        onView(withId(R.id.large_button_input_layout))
                .perform(click());
        onView(withId(R.id.sound_output_layout))
                .perform(click());
        onView(withText("Select"))
                .perform(click());
        onView(withId(R.id.morse_input_large_button))
                .check(matches(isDisplayed()));
    }

    @SmallTest
    public void testTextInput() {
        onView(withId(R.id.text_input_layout))
                .perform(click());
        onView(withId(R.id.sound_output_layout))
                .perform(click());
        onView(withText("Select"))
                .perform(click());
        onView(withId(R.id.morse_input_text_card))
                .check(matches(isDisplayed()));
    }
}
