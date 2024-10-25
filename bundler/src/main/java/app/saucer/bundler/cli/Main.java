package app.saucer.bundler.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import xyz.e3ndr.fastloggingframework.FastLoggingFramework;

@Command(name = "sbnd", mixinStandardHelpOptions = true, subcommands = {
        CommandBundle.class,
})
public class Main implements Runnable {

    public static void main(String[] args) throws Exception {
        FastLoggingFramework.setColorEnabled(false);
        System.exit(
            new CommandLine(new Main())
                .execute(args)
        );
    }

    @Override
    public void run() {
        CommandLine.usage(this, System.err);
    }

}
