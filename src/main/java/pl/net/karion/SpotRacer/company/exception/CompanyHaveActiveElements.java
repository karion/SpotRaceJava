package pl.net.karion.SpotRacer.company.exception;

public class CompanyHaveActiveElements extends RuntimeException {

    public static final String COMPANY_HAVE_ACTIVE_ELEMENTS = "Company have active elements found";
    public CompanyHaveActiveElements(String message) {
        super(message);
    }

    public CompanyHaveActiveElements() {
        super(COMPANY_HAVE_ACTIVE_ELEMENTS);
    }
}
