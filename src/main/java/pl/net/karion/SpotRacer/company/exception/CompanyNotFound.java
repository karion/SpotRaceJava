package pl.net.karion.SpotRacer.company.exception;

public class CompanyNotFound extends RuntimeException {

    public static final String COMPANY_NOT_FOUND = "Company not found";
    public CompanyNotFound(String message) {
        super(message);
    }

    public CompanyNotFound() {
        super(COMPANY_NOT_FOUND);
    }
}
