package com.google.android.libraries.performance.primes.transmitter;

public interface AccountProvider {
    public static final AccountProvider NOOP_PROVIDER = new AccountProvider() {
        public String getAccountName() {
            return null;
        }
    };

    String getAccountName();
}
