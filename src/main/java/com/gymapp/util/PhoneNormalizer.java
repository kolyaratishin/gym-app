package com.gymapp.util;

public final class PhoneNormalizer {

    private PhoneNormalizer() {
    }

    public static String normalize(String phone) {
        if (phone == null || phone.isBlank()) {
            return null;
        }

        String digits = phone.replaceAll("\\D", "");

        if (digits.startsWith("380") && digits.length() == 12) {
            return digits;
        }

        if (digits.startsWith("0") && digits.length() == 10) {
            return "38" + digits;
        }

        return digits;
    }
}