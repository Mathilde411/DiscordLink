package fr.mathilde411.discordlink.util;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MCMDFormat {
    private record FormatCode(Modifiers modifier, Colors color, int pos) {

    }

    public record MarkdownMessage(String message, SortedMap<Integer, Colors> colors) {
        public Colors firstColor() {
            for(Colors color : colors.values()) {
                if(color != Colors.NONE)
                    return color;
            }
            return Colors.NONE;
        }
    }

    public static MarkdownMessage toMarkdown(String message) {
        List<FormatCode> formats = new ArrayList<>();
        Pattern pattern = Pattern.compile("§(?<modifier>[0-9a-fk-or])", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(message);
        Random rand = new Random();

        while (matcher.find()) {
            switch (matcher.group("modifier").toLowerCase()) {
                case "0" -> formats.add(new FormatCode(Modifiers.COLOR, Colors.BLACK, matcher.start()));
                case "1" -> formats.add(new FormatCode(Modifiers.COLOR, Colors.DARK_BLUE, matcher.start()));
                case "2" -> formats.add(new FormatCode(Modifiers.COLOR, Colors.DARK_GREEN, matcher.start()));
                case "3" -> formats.add(new FormatCode(Modifiers.COLOR, Colors.DARK_AQUA, matcher.start()));
                case "4" -> formats.add(new FormatCode(Modifiers.COLOR, Colors.DARK_RED, matcher.start()));
                case "5" -> formats.add(new FormatCode(Modifiers.COLOR, Colors.DARK_PURPLE, matcher.start()));
                case "6" -> formats.add(new FormatCode(Modifiers.COLOR, Colors.GOLD, matcher.start()));
                case "7" -> formats.add(new FormatCode(Modifiers.COLOR, Colors.GRAY, matcher.start()));
                case "8" -> formats.add(new FormatCode(Modifiers.COLOR, Colors.DARK_GRAY, matcher.start()));
                case "9" -> formats.add(new FormatCode(Modifiers.COLOR, Colors.BLUE, matcher.start()));
                case "a" -> formats.add(new FormatCode(Modifiers.COLOR, Colors.GREEN, matcher.start()));
                case "b" -> formats.add(new FormatCode(Modifiers.COLOR, Colors.AQUA, matcher.start()));
                case "c" -> formats.add(new FormatCode(Modifiers.COLOR, Colors.RED, matcher.start()));
                case "d" -> formats.add(new FormatCode(Modifiers.COLOR, Colors.LIGHT_PURPLE, matcher.start()));
                case "e" -> formats.add(new FormatCode(Modifiers.COLOR, Colors.YELLOW, matcher.start()));
                case "f" -> formats.add(new FormatCode(Modifiers.COLOR, Colors.WHITE, matcher.start()));
                case "k" -> formats.add(new FormatCode(Modifiers.OBFUSCATED, Colors.NONE, matcher.start()));
                case "l" -> formats.add(new FormatCode(Modifiers.BOLD, Colors.NONE, matcher.start()));
                case "m" -> formats.add(new FormatCode(Modifiers.STRIKETHROUGH, Colors.NONE, matcher.start()));
                case "n" -> formats.add(new FormatCode(Modifiers.UNDERLINE, Colors.NONE, matcher.start()));
                case "o" -> formats.add(new FormatCode(Modifiers.ITALIC, Colors.NONE, matcher.start()));
                case "r" -> formats.add(new FormatCode(Modifiers.RESET, Colors.NONE, matcher.start()));
            }
        }
        formats.add(new FormatCode(Modifiers.NONE, null, message.length()));

        Function<Modifiers, String> markdown = modifier -> switch (modifier) {
            case BOLD -> "**";
            case ITALIC -> "*";
            case STRIKETHROUGH -> "~~";
            case UNDERLINE -> "__";
            default -> "";
        };

        Function<Integer, String> randomString = size -> {
            String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!@#$%^&()-=_+{}[]";
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < size; i++) {
                builder.append(chars.charAt(rand.nextInt(chars.length())));
            }
            return builder.toString();
        };

        StringBuilder sb = new StringBuilder();
        Stack<Modifiers> currentFormatStack = new Stack<>();
        Stack<Modifiers> unwindFormatStack = new Stack<>();
        Set<Modifiers> toClose = new HashSet<>();
        Queue<Modifiers> toOpenQueue = new LinkedList<>();
        Colors currentColor = Colors.NONE;
        Colors lastColor = Colors.NONE;
        SortedMap<Integer, Colors> colors = new TreeMap<>();
        int pos = 0;
        boolean obfuscated = false;
        for (FormatCode format : formats) {
            if (format.pos > pos) {
                if (colors.isEmpty() || currentColor != lastColor) {
                    colors.put(sb.length(), currentColor);
                    lastColor = currentColor;
                }

                while (!toClose.isEmpty()) {
                    Modifiers mod = currentFormatStack.pop();
                    sb.append(markdown.apply(mod));
                    if (toClose.contains(mod))
                        toClose.remove(mod);
                    else
                        unwindFormatStack.push(mod);
                }

                while (!unwindFormatStack.isEmpty()) {
                    Modifiers mod = unwindFormatStack.pop();
                    sb.append(markdown.apply(mod));
                    currentFormatStack.push(mod);
                }

                while (!toOpenQueue.isEmpty()) {
                    Modifiers mod = toOpenQueue.poll();
                    sb.append(markdown.apply(mod));
                    currentFormatStack.push(mod);
                }


                if (!obfuscated)
                    sb.append(message, pos, format.pos);
                else
                    sb.append(randomString.apply(format.pos - pos));
            }

            pos = format.pos + 2;
            switch (format.modifier) {
                case RESET, COLOR -> {
                    toClose.addAll(currentFormatStack);
                    toOpenQueue.clear();
                    obfuscated = false;
                    currentColor = format.color;
                }
                case OBFUSCATED -> obfuscated = true;
                case BOLD, ITALIC, STRIKETHROUGH, UNDERLINE -> {
                    if (toClose.contains(format.modifier))
                        toClose.remove(format.modifier);
                    else if (!currentFormatStack.contains(format.modifier))
                        toOpenQueue.add(format.modifier);
                }
            }
        }

        while (!currentFormatStack.isEmpty()) {
            sb.append(markdown.apply(currentFormatStack.pop()));
        }

        return new MarkdownMessage(sb.toString(), colors);
    }
}
