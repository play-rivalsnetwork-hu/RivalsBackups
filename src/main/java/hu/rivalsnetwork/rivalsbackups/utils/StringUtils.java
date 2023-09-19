package hu.rivalsnetwork.rivalsbackups.utils;

import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class StringUtils {

    public static String createProgressBar(String incomplete, String complete, int bars, int progress) {
        StringBuilder builder = new StringBuilder();
        if (progress == 100) {
            builder.append(complete.repeat(bars));
            return builder.toString();
        }

        int completeBars = progress / 100 * bars;
        int incompleteBars = bars - completeBars;

        if (completeBars > 0) {
            builder.append(complete.repeat(completeBars));
        }

        if (incompleteBars > 0) {
            builder.append(incomplete.repeat(incompleteBars));
        }

        return builder.toString();
    }

    public static String fancyTime(long time) {
        Duration remainingTime = Duration.ofMillis(time);
        long total = remainingTime.getSeconds();
        long days = total / 84600;
        long hours = (total % 84600) / 3600;
        long minutes = (total % 3600) / 60;
        long seconds = total % 60;

        if (days > 0) return String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds);
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
