package com.google.android.libraries.performance.primes.transmitter;

public interface AccountProvider {
    AccountProvider NOOP_PROVIDER = new AccountProvider() {
        public String getAccountName() {
            return null;
        }
    };

    String getAccountName();
}
