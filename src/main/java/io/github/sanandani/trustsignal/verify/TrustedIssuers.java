package io.github.sanandani.trustsignal.verify;
@FunctionalInterface public interface TrustedIssuers { boolean trusts(String keyId); }
