package com.june.tools;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * NurCodeRepeater – einfacher CLI-Code-Formatter (heuristisch für C/Java/JS-ähnliche Syntax).
 *
 * Nutzung:
 * 1) Starten (Java main). Prompts konfigurieren Einrückung/Leerzeilen/Spaces usw.
 * 2) Quelltext eingeben und mit einer Zeile "::END" abschließen.
 * 3) Ausgabe wird formatiert auf stdout gedruckt; optional als Datei gespeichert.
 */
public class NurCodeRepeater {
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        println("NurCodeRepeater bereit");
        println("Eingabe bis ::END, dann wird automatisch formatiert");
        Config cfg = new Config();
        cfg.indentWidth = askInt(br, "Indent Breite (Standard 4): ", 4);
        cfg.collapseBlankLines = askBool(br, "Mehrere Leerzeilen auf eine reduzieren (y/n, Standard y): ", true);
        cfg.removeTrailingSpaces = askBool(br, "Nachlaufende Leerzeichen entfernen (y/n, Standard y): ", true);
        cfg.spaceAroundOps = askBool(br, "Abstaende um Operatoren einfuegen (y/n, Standard y): ", true);

        println("Code einfuellen, Abschluss mit ::END in eigener Zeile");
        String src = readUntilEnd(br);

        String formatted = new FormatterEngine(cfg).format(src);

        println("----- FORMATIERT START -----");
        System.out.print(formatted);
        if (!formatted.endsWith("\n")) System.out.println();
        println("----- FORMATIERT ENDE -----");

        if (askBool(br, "Als Datei speichern (y/n, Standard n): ", false)) {
            print("Dateiname: ");
            String name = br.readLine().trim();
            try (FileOutputStream fos = new FileOutputStream(name)) {
                fos.write(formatted.getBytes(StandardCharsets.UTF_8));
            }
            println("Gespeichert: " + name);
        }
        println("Fertig");
    }

    // Konfiguration
    static class Config {
        int indentWidth = 4;
        boolean collapseBlankLines = true;
        boolean removeTrailingSpaces = true;
        boolean spaceAroundOps = true;
    }

    // Formatter Engine, heuristikbasiert fuer C/Java/JS aehnliche Syntax
    static class FormatterEngine {
        private final Config cfg;
        FormatterEngine(Config cfg){ this.cfg = cfg; }

        String format(String src) {
            // Normalisierung Zeilenenden
            src = src.replace("\r\n", "\n").replace("\r", "\n");

            // Tokenisierung mit Zustandsautomat
            StringBuilder out = new StringBuilder();
            int indent = 0;
            int parenLevel = 0;
            boolean inString = false;
            boolean inChar = false;
            boolean inLineComment = false;
            boolean inBlockComment = false;
            boolean lineStart = true;
            boolean escape = false;
            int i = 0;

            // Arbeitszeile zum spaeteren Trimmen
            StringBuilder line = new StringBuilder();

            while (i < src.length()) {
                char c = src.charAt(i);
                char n = (i + 1 < src.length()) ? src.charAt(i + 1) : '\0';

                // Kommentarstatus pruefen
                if (!inString && !inChar) {
                    if (!inBlockComment && !inLineComment && c == '/' && n == '/') {
                        inLineComment = true;
                    } else if (!inBlockComment && !inLineComment && c == '/' && n == '*') {
                        inBlockComment = true;
                    } else if (inLineComment && c == '\n') {
                        inLineComment = false;
                    } else if (inBlockComment && c == '*' && n == '/') {
                        // Blockkommentarende, beide Zeichen aufnehmen
                        appendLine(out, line, cfg.removeTrailingSpaces);
                        ensureIndent(out, indent, lineStart, cfg.indentWidth);
                        out.append("/*");
                        out.append(readBlockCommentBody(src, i + 2));
                        // readBlockCommentBody endet bei */
                        int end = findBlockCommentEnd(src, i + 2);
                        i = end + 1; // springe hinter */
                        lineStart = true;
                        out.append('\n');
                        continue;
                    }
                }

                // String und Char Statuswechsel
                if (!inLineComment && !inBlockComment) {
                    if (!inChar && c == '"' && !inString) {
                        inString = true;
                        escape = false;
                    } else if (inString) {
                        if (c == '\\' && !escape) {
                            escape = true;
                        } else {
                            if (c == '"' && !escape) {
                                inString = false;
                            }
                            escape = false;
                        }
                    }

                    if (!inString && c == '\'' && !inChar) {
                        inChar = true;
                        escape = false;
                    } else if (inChar) {
                        if (c == '\\' && !escape) {
                            escape = true;
                        } else {
                            if (c == '\'' && !escape) {
                                inChar = false;
                            }
                            escape = false;
                        }
                    }
                }

                // Zeilenumbruchbehandlung
                if (c == '\n') {
                    appendLine(out, line, cfg.removeTrailingSpaces);
                    out.append('\n');
                    lineStart = true;
                    i++;
                    continue;
                }

                // Innerhalb Zeilenkommentar: direkt kopieren
                if (inLineComment) {
                    if (lineStart) {
                        ensureIndent(out, indent, true, cfg.indentWidth);
                        lineStart = false;
                    }
                    out.append(c);
                    i++;
                    continue;
                }

                // Blockkommentare direkt uebernommen, bis verarbeitet
                if (inBlockComment) {
                    if (lineStart) {
                        ensureIndent(out, indent, true, cfg.indentWidth);
                        lineStart = false;
                    }
                    out.append(c);
                    i++;
                    continue;
                }

                // Formatlogik ausserhalb String/Char
                if (!inString && !inChar) {
                    // Tabs -> Spaces
                    if (c == '\t') {
                        int spaces = cfg.indentWidth;
                        for (int s = 0; s < spaces; s++) out.append(' ');
                        i++;
                        lineStart = false;
                        continue;
                    }

                    // Klammernzaehler
                    if (c == '(') parenLevel++;
                    if (c == ')') parenLevel = Math.max(0, parenLevel - 1);

                    // Oeffnende Klammer
                    if (c == '{') {
                        flushLineAsStmt(out, line, indent, lineStart, cfg);
                        ensureIndent(out, indent, true, cfg.indentWidth);
                        out.append('{').append('\n');
                        indent++;
                        lineStart = true;
                        i++;
                        continue;
                    }

                    // Schliessende Klammer
                    if (c == '}') {
                        if (line.length() > 0) {
                            appendLine(out, line, cfg.removeTrailingSpaces);
                            out.append('\n');
                            line.setLength(0);
                        }
                        indent = Math.max(0, indent - 1);
                        ensureIndent(out, indent, true, cfg.indentWidth);
                        out.append('}');
                        // Falls naechstes Zeichen Semikolon, nicht doppelt umbrechen
                        i++;
                        if (i < src.length() && src.charAt(i) == ';') {
                            out.append(';');
                            i++;
                        }
                        out.append('\n');
                        lineStart = true;
                        continue;
                    }

                    // Semikolon: Satzabschluss ausserhalb runder Klammern
                    if (c == ';' && parenLevel == 0) {
                        line.append(';');
                        appendLine(out, line, cfg.removeTrailingSpaces);
                        out.append('\n');
                        lineStart = true;
                        i++;
                        continue;
                    }

                    // Operatorabstaende einfach halten
                    if (cfg.spaceAroundOps && isOp(c)) {
                        // Vermeide Doppelspaces und Strings
                        if (line.length() > 0 && line.charAt(line.length() - 1) != ' ') {
                            line.append(' ');
                        }
                        line.append(c);
                        if (n != ' ' && n != '\n' && n != '\t') {
                            line.append(' ');
                        }
                        lineStart = false;
                        i++;
                        continue;
                    }

                    // Leerzeichen Normalisierung
                    if (c == ' ') {
                        if (line.length() == 0) {
                            // Zeilenanfangspaces ignorieren, Indent kommt separat
                            i++;
                            continue;
                        }
                        if (line.charAt(line.length() - 1) != ' ') {
                            line.append(' ');
                        }
                        i++;
                        lineStart = false;
                        continue;
                    }

                    // Default Zeichen
                    if (lineStart) {
                        ensureIndent(out, indent, true, cfg.indentWidth);
                        lineStart = false;
                    }
                    line.append(c);
                    i++;
                    continue;
                } else {
                    // In String oder Char: roh uebernehmen
                    if (lineStart) {
                        ensureIndent(out, indent, true, cfg.indentWidth);
                        lineStart = false;
                    }
                    line.append(c);
                    i++;
                }
            }

            if (line.length() > 0) {
                appendLine(out, line, cfg.removeTrailingSpaces);
            }

            String result = out.toString();

            if (cfg.collapseBlankLines) {
                result = collapseBlankLines(result);
            }

            return result;
        }

        private static boolean isOp(char c) {
            return "+-*/%=&|^!<>?:".indexOf(c) >= 0;
        }

        private static void flushLineAsStmt(StringBuilder out, StringBuilder line, int indent, boolean lineStart, Config cfg) {
            String s = rtrim(line.toString());
            if (s.isEmpty()) return;
            ensureIndent(out, indent, lineStart, cfg.indentWidth);
            out.append(s);
            out.append('\n');
            line.setLength(0);
        }

        private static void ensureIndent(StringBuilder out, int indent, boolean lineStart, int indentWidth) {
            if (!lineStart) return;
            // einfache, schnelle Indent-Erzeugung anhand Konfiguration
            for (int s = 0; s < indent * indentWidth; s++) {
                out.append(' ');
            }
        }

        private static void appendLine(StringBuilder out, StringBuilder line, boolean trim) {
            String s = trim ? rtrim(line.toString()) : line.toString();
            out.append(s);
            line.setLength(0);
        }

        private static String rtrim(String s) {
            int i = s.length() - 1;
            while (i >= 0 && Character.isWhitespace(s.charAt(i))) i--;
            return s.substring(0, i + 1);
        }

        private static String collapseBlankLines(String s) {
            String[] lines = s.split("\n", -1);
            StringBuilder b = new StringBuilder();
            boolean blank = false;
            for (String L : lines) {
                boolean isBlank = L.trim().isEmpty();
                if (isBlank) {
                    if (!blank) {
                        b.append('\n');
                        blank = true;
                    }
                } else {
                    b.append(L).append('\n');
                    blank = false;
                }
            }
            return b.toString();
        }

        private static String readBlockCommentBody(String src, int from) {
            int end = findBlockCommentEnd(src, from);
            if (end < 0) end = src.length() - 2;
            return src.substring(from, end);
        }

        private static int findBlockCommentEnd(String src, int from) {
            for (int i = from; i + 1 < src.length(); i++) {
                if (src.charAt(i) == '*' && src.charAt(i + 1) == '/') return i;
            }
            return -1;
        }
    }

    private static String readUntilEnd(BufferedReader br) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            if ("::END".equals(line)) break;
            sb.append(line).append('\n');
        }
        return sb.toString();
    }

    private static int askInt(BufferedReader br, String prompt, int def) throws IOException {
        print(prompt);
        String s = br.readLine();
        if (s == null || s.trim().isEmpty()) return def;
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    private static boolean askBool(BufferedReader br, String prompt, boolean def) throws IOException {
        print(prompt);
        String s = br.readLine();
        if (s == null || s.trim().isEmpty()) return def;
        s = s.trim().toLowerCase(Locale.ROOT);
        if (s.startsWith("y") || s.equals("ja")) return true;
        if (s.startsWith("n") || s.equals("nein")) return false;
        return def;
    }

    private static void println(String s){ System.out.println(s); }
    private static void print(String s){ System.out.print(s); }
}
