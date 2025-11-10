package dianafriptuleac.socialMediaCompany.exceptions;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(Long id) {
        super("The record with id: \" + id + \" was not found!");
    }
}
