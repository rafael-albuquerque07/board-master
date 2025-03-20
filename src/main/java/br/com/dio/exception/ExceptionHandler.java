package br.com.dio.exception;

import br.com.dio.ui.util.TerminalColors;

import java.sql.SQLException;

/**
 * Classe responsável por centralizar o tratamento de exceções no sistema.
 * Formata e exibe mensagens de erro apropriadas para o usuário.
 */
public class ExceptionHandler {

    /**
     * Trata a exceção e exibe uma mensagem formatada para o usuário
     *
     * @param ex Exceção a ser tratada
     */
    public static void handle(Exception ex) {
        if (ex instanceof SQLException) {
            handleSQLException((SQLException) ex);
        } else if (ex instanceof EntityNotFoundException) {
            handleEntityNotFoundException((EntityNotFoundException) ex);
        } else if (ex instanceof CardBlockedException) {
            handleCardBlockedException((CardBlockedException) ex);
        } else if (ex instanceof CardFinishedException) {
            handleCardFinishedException((CardFinishedException) ex);
        } else if (ex instanceof IllegalArgumentException) {
            handleIllegalArgumentException((IllegalArgumentException) ex);
        } else {
            handleGenericException(ex);
        }
    }

    /**
     * Trata exceções de SQL
     *
     * @param ex Exceção SQL
     */
    private static void handleSQLException(SQLException ex) {
        System.out.println(TerminalColors.RED_BOLD + "Erro de banco de dados: " + TerminalColors.RESET +
                TerminalColors.RED + ex.getMessage() + TerminalColors.RESET);

        // Em ambiente de desenvolvimento, pode ser útil ter mais detalhes
        if (isDevMode()) {
            System.out.println(TerminalColors.RED + "Código de erro: " + ex.getErrorCode() + TerminalColors.RESET);
            System.out.println(TerminalColors.RED + "Estado SQL: " + ex.getSQLState() + TerminalColors.RESET);
        }
    }

    /**
     * Trata exceções de entidade não encontrada
     *
     * @param ex Exceção de entidade não encontrada
     */
    private static void handleEntityNotFoundException(EntityNotFoundException ex) {
        System.out.println(TerminalColors.YELLOW + "Entidade não encontrada: " +
                ex.getMessage() + TerminalColors.RESET);
    }

    /**
     * Trata exceções de card bloqueado
     *
     * @param ex Exceção de card bloqueado
     */
    private static void handleCardBlockedException(CardBlockedException ex) {
        System.out.println(TerminalColors.YELLOW + "Operação bloqueada: " +
                ex.getMessage() + TerminalColors.RESET);
    }

    /**
     * Trata exceções de card finalizado
     *
     * @param ex Exceção de card finalizado
     */
    private static void handleCardFinishedException(CardFinishedException ex) {
        System.out.println(TerminalColors.YELLOW + "Operação não permitida: " +
                ex.getMessage() + TerminalColors.RESET);
    }

    /**
     * Trata exceções de argumento ilegal
     *
     * @param ex Exceção de argumento ilegal
     */
    private static void handleIllegalArgumentException(IllegalArgumentException ex) {
        System.out.println(TerminalColors.YELLOW + "Entrada inválida: " +
                ex.getMessage() + TerminalColors.RESET);
    }

    /**
     * Trata exceções genéricas
     *
     * @param ex Exceção genérica
     */
    private static void handleGenericException(Exception ex) {
        System.out.println(TerminalColors.RED_BOLD + "Erro: " + TerminalColors.RESET +
                TerminalColors.RED + ex.getMessage() + TerminalColors.RESET);

        // Em ambiente de desenvolvimento, pode ser útil mostrar a stack trace
        if (isDevMode()) {
            ex.printStackTrace();
        }
    }

    /**
     * Verifica se o sistema está em modo de desenvolvimento
     *
     * @return true se estiver em modo de desenvolvimento
     */
    private static boolean isDevMode() {
        // Pode ser configurado via variável de ambiente ou propriedade do sistema
        String mode = System.getProperty("app.mode", "dev");
        return "dev".equalsIgnoreCase(mode);
    }

    /**
     * Trata a exceção e aguarda o usuário pressionar ENTER para continuar
     *
     * @param ex Exceção a ser tratada
     */
    public static void handleAndWait(Exception ex) {
        handle(ex);

        System.out.println("\n" + TerminalColors.YELLOW +
                "Pressione ENTER para continuar..." + TerminalColors.RESET);
        try {
            System.in.read();
        } catch (Exception e) {
            // Ignorar exceção
        }
    }
}