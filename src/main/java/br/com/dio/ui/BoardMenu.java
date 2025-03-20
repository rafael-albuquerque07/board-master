package br.com.dio.ui;
import br.com.dio.ui.util.TerminalColors;

import br.com.dio.persistence.entity.BoardEntity;
import lombok.AllArgsConstructor;

import java.sql.SQLException;
import java.util.Scanner;

@AllArgsConstructor
public class BoardMenu {

    private final Scanner scanner = new Scanner(System.in).useDelimiter("\n");
    private final BoardEntity entity;

    public void execute() {
        try {
            System.out.println(TerminalColors.header("Bem vindo ao board " + entity.getName() + " (ID: " + entity.getId() + ")"));
            var option = -1;
            while (option != 9) {
                printMenu();
                option = scanner.nextInt();
                switch (option) {
                    case 1 -> new CardCreationMenu(entity, scanner).execute();
                    case 2 -> new CardMovementMenu(entity, scanner).execute();
                    case 3 -> new CardBlockingMenu(entity, scanner).execute();
                    case 4 -> new CardUnblockingMenu(entity, scanner).execute();
                    case 5 -> new CardCancelMenu(entity, scanner).execute();
                    case 6 -> new BoardViewMenu(entity).execute();
                    case 7 -> new ColumnViewMenu(entity, scanner).execute();
                    case 8 -> new CardViewMenu(scanner).execute();
                    case 9 -> System.out.println(TerminalColors.warning("Voltando para o menu anterior"));
                    case 10 -> System.exit(0);
                    case 11 -> new CompletionReportMenu(entity).execute();
                    case 12 -> new BlockingReportMenu(entity).execute();
                    case 13 -> new CardTimelineMenu(entity).execute();
                    default -> System.out.println(TerminalColors.error("Opção inválida, informe uma opção do menu"));
                }
            }
        } catch (SQLException ex) {
            System.out.println(TerminalColors.error("Erro ao acessar o banco de dados: " + ex.getMessage()));
            ex.printStackTrace();
            System.exit(0);
        }
    }

    private void printMenu() {
        System.out.println("\n" + TerminalColors.CYAN_BOLD + "===== MENU DO BOARD =====" + TerminalColors.RESET);
        System.out.println(TerminalColors.YELLOW + "[ Gerenciamento de Cards ]" + TerminalColors.RESET);
        System.out.println("  1 - Criar um card");
        System.out.println("  2 - Mover um card para próxima coluna");
        System.out.println("  3 - Bloquear um card");
        System.out.println("  4 - Desbloquear um card");
        System.out.println("  5 - Cancelar um card");

        System.out.println(TerminalColors.YELLOW + "\n[ Visualização ]" + TerminalColors.RESET);
        System.out.println("  6 - Ver board");
        System.out.println("  7 - Ver coluna com cards");
        System.out.println("  8 - Ver card");

        System.out.println(TerminalColors.YELLOW + "\n[ Relatórios ]" + TerminalColors.RESET);
        System.out.println("  11 - Relatório de tempo de conclusão");
        System.out.println("  12 - Relatório de bloqueios");
        System.out.println("  13 - Timeline detalhada de um card");

        System.out.println(TerminalColors.YELLOW + "\n[ Navegação ]" + TerminalColors.RESET);
        System.out.println("  9 - Voltar para o menu anterior");
        System.out.println("  10 - Sair");

        System.out.print("\nEscolha uma opção: ");
    }
}