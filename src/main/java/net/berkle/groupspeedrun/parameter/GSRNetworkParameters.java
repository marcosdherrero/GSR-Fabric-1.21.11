package net.berkle.groupspeedrun.parameter;

/**
 * Parameters for network payloads: size limits and validation.
 * Used by payload receivers in {@link net.berkle.groupspeedrun.GSRNetworking} and {@link net.berkle.groupspeedrun.GSRClient}.
 */
public final class GSRNetworkParameters {

    private GSRNetworkParameters() {}

    /** Max NBT payload size in bytes for run data. Reject larger payloads to prevent memory exhaustion. */
    public static final int RUN_DATA_MAX_NBT_BYTES = 1024 * 1024;
}
