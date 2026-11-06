package pl.net.karion.SpotRacer.company.exception;

public class CompanyNameCollision extends RuntimeException {

    public static final String COMPANY_NAME_COLLISION_MSG = "Company name collision";
    public CompanyNameCollision(String message) {
        super(message);
    }

    public CompanyNameCollision() {
        super(COMPANY_NAME_COLLISION_MSG);
    }
}
