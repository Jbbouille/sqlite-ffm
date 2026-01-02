package top.petit.sqlite.object;

public record SemVer(int major, int minor, int patch) {

    public static SemVer fromVersionString(String version) {
        var parts = version.split("\\.");
        var major = Integer.parseInt(parts[0]);
        var minor = Integer.parseInt(parts[1]);
        var patch = Integer.parseInt(parts[2]);
        return new SemVer(major, minor, patch);
    }

    @Override
    public String toString() {
        return "%d.%d.%d".formatted(major, minor, patch);
    }
}
