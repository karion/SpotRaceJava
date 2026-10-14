package pl.net.karion.SpotRacer.company.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class CompanyTest {

    public static Stream<Arguments> wrongNames() {
        return Stream.of(

            // overlap
            Arguments.of((String)null),
            Arguments.of(""),
            Arguments.of("  "),
            Arguments.of("vLorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc molestie, turpis sed dignissim vehicula, tellus nulla auctor ipsum, vitae ullamcorper arcu enim egestas nibh. Duis varius iaculis tempor. Class aptent taciti sociosqu ad litora massa nunc.")
            );
    }

    @ParameterizedTest
    @MethodSource("wrongNames")
    void shouldThrowExceptionOnWrongName(String name) {
        assertThrows(IllegalArgumentException.class, () -> {
            new Company(name, true);
        });
    }

    public static Stream<Arguments> stringToNormalize() {
        return Stream.of(

            // overlap
            Arguments.of(
                " AA  AA ",
                "AA AA",
                "aa aa"
            ),
            Arguments.of(
                " Lorem     ipsum    dolor sit amet, consectetur adipiscing elit. Nunc molestie, turpis sed dignissim vehicula, tellus nulla auctor ipsum, vitae ullamcorper arcu enim egestas nibh. Duis varius iaculis tempor. Class aptent taciti sociosqu ad litora massa nunc.",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nunc molestie, turpis sed dignissim vehicula, tellus nulla auctor ipsum, vitae ullamcorper arcu enim egestas nibh. Duis varius iaculis tempor. Class aptent taciti sociosqu ad litora massa nunc.",
                "lorem ipsum dolor sit amet, consectetur adipiscing elit. nunc molestie, turpis sed dignissim vehicula, tellus nulla auctor ipsum, vitae ullamcorper arcu enim egestas nibh. duis varius iaculis tempor. class aptent taciti sociosqu ad litora massa nunc."
            )
        );
    }

    @ParameterizedTest
    @MethodSource("stringToNormalize")
    void shouldCreateCompanyWithNormalizedName(String before, String after, String normalized) {
        Company company = new Company(before, true);

        assertThat(company.getName()).isEqualTo(after);
        assertThat(company.getNormalizedName()).isEqualTo(normalized);
    }

    @ParameterizedTest
    @MethodSource("wrongNames")
    void shouldThrowExceptionOnSettingWrongName(String wrongName) {
        Company company = new Company("Correct name", true);

        assertThatThrownBy(() -> company.setName(wrongName))
            .isInstanceOf(IllegalArgumentException.class);

        assertThat(company.getName()).isEqualTo("Correct name");
        assertThat(company.getNormalizedName()).isEqualTo("correct name");
    }

}