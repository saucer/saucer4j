package co.casterlabs.saucer.impl;

import java.io.PrintWriter;
import java.io.StringWriter;

import lombok.NonNull;

class SaucerUtils {

    public static String getExceptionStack(@NonNull Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        e.printStackTrace(pw);

        String out = sw.toString();

        pw.flush();
        pw.close();
        sw.flush();

        return out
            .substring(0, out.length() - 2)
            .replace("\r", "");
    }

}
