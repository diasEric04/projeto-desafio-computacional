package com.example.projeto_desafio_computacional;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Silabador {

    private static final Set<String> DIPHTHONGS = new HashSet<>(Arrays.asList(
            "ai","au","ei","eu","oi","ou","ui","iu","ia","ie","io","ua","ue","uo"
    ));

    private static boolean isStrong(char base) {
        return base == 'a' || base == 'e' || base == 'o';
    }

    private static boolean isWeak(char base) {
        return base == 'i' || base == 'u' || base == 'y';
    }

    private static String removeDiacritics(String s) {
        String n = Normalizer.normalize(s, Normalizer.Form.NFD);
        return n.replaceAll("\\p{M}", "");
    }

    private static char baseChar(char c) {
        String n = Normalizer.normalize(String.valueOf(c), Normalizer.Form.NFD);
        String without = n.replaceAll("\\p{M}", "");
        return without.toLowerCase().charAt(0);
    }

    private static boolean isVowelChar(char c) {
        char b = baseChar(c);
        return b == 'a' || b == 'e' || b == 'i' || b == 'o' || b == 'u' || b == 'y';
    }

    private static boolean weakVowelHasAccent(char c) {
        char b = baseChar(c);
        if (!(b == 'i' || b == 'u')) return false;
        String n = Normalizer.normalize(String.valueOf(c), Normalizer.Form.NFD);
        return n.length() > 1; // tem marca diacrítica
    }

    private static String preprocessQuGu(String s) {
        StringBuilder sb = new StringBuilder();
        char[] cs = s.toCharArray();
        for (int i = 0; i < cs.length; i++) {
            char c = cs[i];
            if (i > 0) {
                char prev = cs[i - 1];
                if ((prev == 'q' || prev == 'Q' || prev == 'g' || prev == 'G')
                        && (Character.toLowerCase(c) == 'u')
                        && i + 1 < cs.length) {
                    char next = cs[i + 1];
                    char baseNext = baseChar(next);
                    if ((baseNext == 'e' || baseNext == 'i') && c != 'ü' && c != 'Ü') {
                        continue; // pula o 'u' mudo
                    }
                }
            }
            sb.append(c);
        }
        return sb.toString();
    }

    private static boolean isDiphthong(String seq) {
        if (seq == null || seq.length() != 2) return false;
        char c1 = seq.charAt(0), c2 = seq.charAt(1);
        if (weakVowelHasAccent(c1) || weakVowelHasAccent(c2)) return false;
        String base = removeDiacritics(seq).toLowerCase();
        if (DIPHTHONGS.contains(base)) return true;
        char b1 = baseChar(c1), b2 = baseChar(c2);
        return (isWeak(b1) && isStrong(b2)) || (isStrong(b1) && isWeak(b2)) || (isWeak(b1) && isWeak(b2));
    }

    private static boolean isTriphthong(String seq) {
        if (seq == null || seq.length() != 3) return false;
        char c1 = seq.charAt(0), c2 = seq.charAt(1), c3 = seq.charAt(2);
        if (weakVowelHasAccent(c1) || weakVowelHasAccent(c3)) return false;
        char b1 = baseChar(c1), b2 = baseChar(c2), b3 = baseChar(c3);
        return isWeak(b1) && isStrong(b2) && isWeak(b3);
    }

    private static int countNucleiInVowelSequence(String seq) {
        int len = seq.length();
        if (len == 0) return 0;
        if (len == 1) return 1;
        if (len == 2) return isDiphthong(seq) ? 1 : 2;
        if (len == 3) {
            if (isTriphthong(seq)) return 1;
            if (isDiphthong(seq.substring(0, 2))) return 2;
            if (isDiphthong(seq.substring(1, 3))) return 2;
            return 3;
        }
        int i = 0, count = 0;
        while (i < len) {
            if (i + 2 < len && isTriphthong(seq.substring(i, i + 3))) {
                count++; i += 3;
            } else if (i + 1 < len && isDiphthong(seq.substring(i, i + 2))) {
                count++; i += 2;
            } else {
                count++; i++;
            }
        }
        return count;
    }

    /**
     * Conta o número de sílabas em uma palavra do português brasileiro.
     * @param palavra A palavra a ser analisada
     * @return número estimado de sílabas (mínimo 1)
     */
    public static int contarSilabas(String palavra) {
        if (palavra == null || palavra.isEmpty()) return 0;
        String s = palavra.trim().replaceAll("[^\\p{L}]", "");
        if (s.isEmpty()) return 0;

        s = preprocessQuGu(s);
        int n = s.length(), i = 0, syllables = 0;

        while (i < n) {
            char c = s.charAt(i);
            if (isVowelChar(c)) {
                int j = i + 1;
                while (j < n && isVowelChar(s.charAt(j))) j++;
                String seq = s.substring(i, j);
                syllables += countNucleiInVowelSequence(seq);
                i = j;
            } else i++;
        }
        return Math.max(1, syllables);
    }
}
