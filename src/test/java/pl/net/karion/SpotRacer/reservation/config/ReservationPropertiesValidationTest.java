package pl.net.karion.SpotRacer.reservation.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.validation.autoconfigure.ValidationAutoConfiguration;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.*;

public class ReservationPropertiesValidationTest {


    @Configuration(proxyBeanMethods = false)
    @EnableConfigurationProperties(ReservationProperties.class)
    static class TestConfig {
    }

    @Test
    void shouldNotAllowNegativeStandardWindowDays() throws Exception {
        ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class)
            .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class));

        runner.withPropertyValues(
            "reservation.release-assigned-spots-at=07:00",
            "reservation.standard-window-days=-1",
            "reservation.assigned-window-days=7"
        ).run(context -> {
            assertThat(context.getStartupFailure()).rootCause().hasMessageContaining("standardWindowDays");
        });
    }

    @Test
    void shouldNotAllowNegativeAssignedWindowDays() throws Exception {
        ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class)
            .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class));

        runner.withPropertyValues(
            "reservation.release-assigned-spots-at=07:00",
            "reservation.standard-window-days=1",
            "reservation.assigned-window-days=-1"
        ).run(context -> {

            assertThat(context.getStartupFailure()).rootCause().hasMessageContaining("assignedWindowDays");
        });
    }

    @Test
    void shouldNotAllowAssignedWindowDaysSmallerThanStandardWindowDays() throws Exception {
        ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class)
            .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class));

        runner.withPropertyValues(
            "reservation.release-assigned-spots-at=07:00",
            "reservation.standard-window-days=3",
            "reservation.assigned-window-days=1"
        ).run(context -> {
//            assertThat(context).getFailure();
            assertThat(context.getStartupFailure()).rootCause()
                .hasMessageContaining("assignedWindowDays must be greater than or equal to standardWindowDays");
        });
    }

    @Test
    void shouldNotAllowNullOnReleaseAssignedSpotsAt() throws Exception {
        ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(TestConfig.class)
            .withConfiguration(AutoConfigurations.of(ValidationAutoConfiguration.class));

        runner.withPropertyValues(
            "reservation.standard-window-days=1",
            "reservation.assigned-window-days=3"
        ).run(context -> {

            assertThat(context.getStartupFailure()).rootCause().hasMessageContaining("releaseAssignedSpotsAt");
        });
    }
}
