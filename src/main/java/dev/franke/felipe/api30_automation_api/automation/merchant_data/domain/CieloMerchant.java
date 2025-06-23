package dev.franke.felipe.api30_automation_api.automation.merchant_data.domain;

import java.util.UUID;

public record CieloMerchant(
        String establishmentCode,
        UUID merchantId,
        String documentType,
        String documentNumber,
        String name,
        boolean blocked,
        boolean pixEnabled,
        boolean antifraudEnabled,
        boolean tokenizationEnabled,
        boolean velocityEnabled,
        boolean smartRecurrencyEnabled,
        boolean zeroAuthEnabled,
        boolean binQueryEnabled,
        boolean selectiveAuthEnabled,
        boolean automaticCancelationEnabled,
        boolean forceBraspagAuthEnabled,
        boolean mtlsEnabled,
        boolean webhookEnabled,
        byte whiteListIpCount
) {}
