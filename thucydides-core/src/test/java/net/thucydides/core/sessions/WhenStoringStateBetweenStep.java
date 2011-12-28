package net.thucydides.core.sessions;

import net.thucydides.core.Thucydides;
import net.thucydides.core.annotations.Step;
import net.thucydides.core.pages.Pages;
import net.thucydides.core.steps.ScenarioSteps;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class WhenStoringStateBetweenStep {

    @Mock
    Pages pages;
    
    class SampleSteps extends ScenarioSteps {

        public SampleSteps(final Pages pages) {
            super(pages);
        }
        
        @Step
        public void storeName(String value) {
            Thucydides.getCurrentSession().put("name", value);
        }


        @Step
        public String retrieveName() {
            return (String) Thucydides.getCurrentSession().get("name");
        }

        @Step
        public boolean checkName() {
            return Thucydides.getCurrentSession().containsKey("name");
        }

    }
    
    
    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
        Thucydides.initializeTestSession();
    }

    @Test
    public void should_be_able_to_store_variables_between_steps() {
        SampleSteps steps = new SampleSteps(pages);
        
        steps.storeName("joe");

        assertThat(steps.retrieveName(), is("joe"));
    }

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void should_throw_an_exception_if_no_variable_is_found() {
        expectedException.expect(AssertionError.class);
        expectedException.expectMessage("Session variable name expected but not found.");
        SampleSteps steps = new SampleSteps(pages);
        steps.retrieveName();
    }

    @Test
    public void should_be_able_to_ask_if_a_session_variable_has_been_set() {
        SampleSteps steps = new SampleSteps(pages);

        assertThat(steps.checkName(), is(false));

        steps.storeName("joe");

        assertThat(steps.checkName(), is(true));
    }

    @Test
    public void should_clear_session_at_the_start_of_each_test() {
        SampleSteps steps = new SampleSteps(pages);

        steps.storeName("joe");
        assertThat(steps.retrieveName(), is("joe"));

        Thucydides.initializeTestSession();

        assertThat(steps.checkName(), is(false));
    }

}