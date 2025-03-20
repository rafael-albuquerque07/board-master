package br.com.dio.ui.util;

/**
 * Classe utilitária para formatar texto colorido no terminal
 */
public class TerminalColors {
    // Reset
    public static final String RESET = "\033[0m";  // Text Reset

    // Regular Colors
    public static final String BLACK = "\033[0;30m";   // BLACK
    public static final String RED = "\033[0;31m";     // RED
    public static final String GREEN = "\033[0;32m";   // GREEN
    public static final String YELLOW = "\033[0;33m";  // YELLOW
    public static final String BLUE = "\033[0;34m";    // BLUE
    public static final String PURPLE = "\033[0;35m";  // PURPLE
    public static final String CYAN = "\033[0;36m";    // CYAN
    public static final String WHITE = "\033[0;37m";   // WHITE

    // Bold
    public static final String BLACK_BOLD = "\033[1;30m";  // BLACK
    public static final String RED_BOLD = "\033[1;31m";    // RED
    public static final String GREEN_BOLD = "\033[1;32m";  // GREEN
    public static final String YELLOW_BOLD = "\033[1;33m"; // YELLOW
    public static final String BLUE_BOLD = "\033[1;34m";   // BLUE
    public static final String PURPLE_BOLD = "\033[1;35m"; // PURPLE
    public static final String CYAN_BOLD = "\033[1;36m";   // CYAN
    public static final String WHITE_BOLD = "\033[1;37m";  // WHITE

    // Background
    public static final String BLACK_BACKGROUND = "\033[40m";  // BLACK
    public static final String RED_BACKGROUND = "\033[41m";    // RED
    public static final String GREEN_BACKGROUND = "\033[42m";  // GREEN
    public static final String YELLOW_BACKGROUND = "\033[43m"; // YELLOW
    public static final String BLUE_BACKGROUND = "\033[44m";   // BLUE
    public static final String PURPLE_BACKGROUND = "\033[45m"; // PURPLE
    public static final String CYAN_BACKGROUND = "\033[46m";   // CYAN
    public static final String WHITE_BACKGROUND = "\033[47m";  // WHITE

    /**
     * Formata um texto com cor
     * @param text Texto a ser formatado
     * @param color Código de cor ANSI
     * @return Texto formatado
     */
    public static String colorize(String text, String color) {
        return color + text + RESET;
    }

    /**
     * Retorna cabeçalho formatado com cor de destaque
     * @param text Texto do cabeçalho
     * @return Cabeçalho formatado
     */
    public static String header(String text) {
        return "\n" + BLUE_BOLD + text + RESET + "\n";
    }

    /**
     * Retorna mensagem de erro formatada
     * @param text Texto de erro
     * @return Mensagem de erro formatada
     */
    public static String error(String text) {
        return RED_BOLD + text + RESET;
    }

    /**
     * Retorna mensagem de sucesso formatada
     * @param text Texto de sucesso
     * @return Mensagem de sucesso formatada
     */
    public static String success(String text) {
        return GREEN_BOLD + text + RESET;
    }

    /**
     * Retorna mensagem de aviso formatada
     * @param text Texto de aviso
     * @return Mensagem de aviso formatada
     */
    public static String warning(String text) {
        return YELLOW_BOLD + text + RESET;
    }
}