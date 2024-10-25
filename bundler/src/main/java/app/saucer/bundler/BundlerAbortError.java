package app.saucer.bundler;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class BundlerAbortError extends Error {
    private static final long serialVersionUID = 8896145816501522569L;

    public final int desiredExitCode;

}
