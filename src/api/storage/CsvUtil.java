package api.storage;

import java.util.ArrayList;
import java.util.List;

public final class CsvUtil {
    private CsvUtil() {}

    public static List<String> parseLine(String line) {
        List<String> out = new ArrayList<>();
        if (line == null) return out;

        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    sb.append('"'); // escaped quote inside quotes
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                out.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }

        out.add(sb.toString());
        return out;
    }

    public static String esc(String value) {
        if (value == null) return "";

        boolean needsQuotes =
                value.indexOf(',') >= 0 ||
                        value.indexOf('"') >= 0 ||
                        value.indexOf('\n') >= 0 ||
                        value.indexOf('\r') >= 0;

        String v = value.replace("\"", "\"\""); // double quotes inside CSV
        return needsQuotes ? ("\"" + v + "\"") : v;
    }
}
