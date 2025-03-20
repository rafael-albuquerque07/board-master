package br.com.dio.ui;
import br.com.dio.ui.util.TerminalColors;

import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.service.BoardQueryService;

import java.sql.SQLException;

import static br.com.dio.persistence.config.ConnectionConfig.getConnection;

public class BoardViewMenu {
    private final BoardEntity entity;

    public BoardViewMenu(BoardEntity entity) {
        this.entity = entity;
    }

    public void execute() throws SQLException {
        System.out.println("\n" + TerminalColors.BLUE_BOLD + "===== VISUALIZAÇÃO DO BOARD =====" + TerminalColors.RESET);
        try(var connection = getConnection()){
            var optional = new BoardQueryService(connection).showBoardDetails(entity.getId());
            optional.ifPresent(b -> {
                System.out.println(TerminalColors.CYAN + "Board: " + b.name() + " (ID: " + b.id() + ")" + TerminalColors.RESET);
                System.out.println("\nColunas:");
                b.columns().forEach(c ->
                        System.out.printf("  %s%-20s%s [%s] - %s cards\n",
                                TerminalColors.GREEN_BOLD,
                                c.name(),
                                TerminalColors.RESET,
                                c.kind(),
                                c.cardsAmount())
                );
            });
        }
    }
}