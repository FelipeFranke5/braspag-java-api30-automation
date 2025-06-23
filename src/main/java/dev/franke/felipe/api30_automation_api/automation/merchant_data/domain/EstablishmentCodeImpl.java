package dev.franke.felipe.api30_automation_api.automation.merchant_data.domain;

import dev.franke.felipe.api30_automation_api.automation.merchant_data.exception.InvalidEstablishmentCodeException;

public record EstablishmentCodeImpl(String establishmentNumber) implements EstablishmentCode {

    public EstablishmentCodeImpl(String establishmentNumber) {
        this.establishmentNumber = establishmentNumber;
        validate();
    }

    @Override
    public void validate() {
        checkEstablishmentNumberIsNullOrBlank();
        checkEstablishmentNumberIsValidLong();
        checkEstablishmentNumberLengthEquals10();
    }

    private void checkEstablishmentNumberIsNullOrBlank() {
        if (establishmentNumberIsNullOrBlank()) {
            throw new InvalidEstablishmentCodeException("Establishment Code is Null or Blank");
        }
    }

    private void checkEstablishmentNumberIsValidLong() {
        if (!establishmentNumberIsValidLong()) {
            throw new InvalidEstablishmentCodeException("Establishment Code is not a valid long");
        }
    }

    private void checkEstablishmentNumberLengthEquals10() {
        if (!establishmentNumberLengthEquals10()) {
            throw new InvalidEstablishmentCodeException("Establishment Code length is not valid");
        }
    }

    private boolean establishmentNumberIsNullOrBlank() {
        return establishmentNumber == null || establishmentNumber.isBlank();
    }

    private boolean establishmentNumberIsValidLong() {
        try {
            return Long.parseLong(establishmentNumber) > 0;
        } catch (NumberFormatException unparsableException) {
            return false;
        }
    }

    private boolean establishmentNumberLengthEquals10() {
        return establishmentNumber.length() == 10;
    }
}
