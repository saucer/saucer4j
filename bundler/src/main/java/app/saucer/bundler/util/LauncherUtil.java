package app.saucer.bundler.util;

import java.io.InputStream;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import app.saucer.bundler.config.BuildTargetArch;
import app.saucer.bundler.config.BuildTargetOS;

public class LauncherUtil {
    private static final Map<BuildTargetArch, String> ARCH_MAPPING = Map.of(
    // @formatter:off
//        BuildTargetArch.x86,     "x86",
        BuildTargetArch.x86_64,  "x86_64",
//        BuildTargetArch.ppc64,   "ppc64",
        BuildTargetArch.ppc64le, "powerpc64le",
//        BuildTargetArch.s390x,   "s390x",
        BuildTargetArch.aarch64, "aarch64",
        BuildTargetArch.arm,     "arm"
//        BuildTargetArch.sparcv9, "sparcv9",
//        BuildTargetArch.riscv64, "riscv64"
        // @formatter:on
    );

    private static final Map<BuildTargetOS, String> OS_MAPPING = Map.of(
    // @formatter:off
        BuildTargetOS.windows,   "windows-msvc",
        BuildTargetOS.macos,     "macos-none",
        BuildTargetOS.gnulinux,  "linux-gnu.2.36"
//        BuildTargetOS.musllinux, "alpine-linux",
//        BuildTargetOS.solaris,   "solaris",
//        BuildTargetOS.aix,       "aix"
        // @formatter:on
    );

    private static final Map<BuildTargetOS, String> OS_EXT_MAPPING = Map.of(
    // @formatter:off
        BuildTargetOS.windows,   ".exe",
        BuildTargetOS.macos,     "",
        BuildTargetOS.gnulinux,  ""
//        BuildTargetOS.musllinux, "",
//        BuildTargetOS.solaris,   "",
//        BuildTargetOS.aix,       ""
        // @formatter:on
    );

    public static @Nullable InputStream get(BuildTargetArch arch, BuildTargetOS os) {
        String fileName = String.format("/%s-%s/launcher%s", ARCH_MAPPING.get(arch), OS_MAPPING.get(os), OS_EXT_MAPPING.get(os));
        return LauncherUtil.class.getResourceAsStream(fileName);
    }

    public static String fileExtension(BuildTargetOS os) {
        return OS_EXT_MAPPING.get(os);
    }

    public static final String MACOS_BUNDLE_PLIST_TEMPLATE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<!DOCTYPE plist PUBLIC \"-//Apple Computer//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">\n"
        + "<plist version=\"1.0\">\n"
        + "<dict>\n"
        + "  <key>CFBundleGetInfoString</key>\n"
        + "  <string>{name}</string>\n"
        + "  <key>CFBundleExecutable</key>\n"
        + "  <string>{name}</string>\n"
        + "  <key>CFBundleIdentifier</key>\n"
        + "  <string>{id}</string>\n"
        + "  <key>CFBundleName</key>\n"
        + "  <string>{name}</string>\n"
        + "  <key>CFBundleIconFile</key>\n"
        + "  <string>icons.icns</string>\n"
        + "  <key>CFBundleShortVersionString</key>\n"
        + "  <string>1.0</string>\n"
        + "  <key>CFBundleInfoDictionaryVersion</key>\n"
        + "  <string>6.0</string>\n"
        + "  <key>CFBundlePackageType</key>\n"
        + "  <string>APPL</string>\n"
        + "  <key>IFMajorVersion</key>\n"
        + "  <integer>0</integer>\n"
        + "  <key>IFMinorVersion</key>\n"
        + "  <integer>1</integer>\n"
        + "  <key>NSHighResolutionCapable</key>\n"
        + "  <true/>\n"
        + "  <key>NSAppTransportSecurity</key>\n"
        + "  <dict>\n"
        + "    <key>NSAllowsArbitraryLoads</key>\n"
        + "    <true/>\n"
        + "    <key>NSExceptionDomains</key>\n"
        + "    <dict>\n"
        + "      <key>127.0.0.1</key>\n"
        + "      <dict>\n"
        + "        <key>NSExceptionAllowsInsecureHTTPLoads</key>\n"
        + "        <true/>\n"
        + "        <key>NSIncludesSubdomains</key>\n"
        + "        <false/>\n"
        + "      </dict>\n"
        + "      <key>::1</key>\n"
        + "      <dict>\n"
        + "        <key>NSExceptionAllowsInsecureHTTPLoads</key>\n"
        + "        <true/>\n"
        + "        <key>NSIncludesSubdomains</key>\n"
        + "        <false/>\n"
        + "      </dict>\n"
        + "      <key>localhost</key>\n"
        + "      <dict>\n"
        + "        <key>NSExceptionAllowsInsecureHTTPLoads</key>\n"
        + "        <true/>\n"
        + "        <key>NSIncludesSubdomains</key>\n"
        + "        <false/>\n"
        + "      </dict>\n"
        + "    </dict>\n"
        + "  </dict>\n"
        + "</dict>\n"
        + "</plist>";

}
