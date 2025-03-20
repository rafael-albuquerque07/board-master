package br.com.dio.ui;
import br.com.dio.ui.util.TerminalColors;

import br.com.dio.dto.BoardColumnInfoDTO;
import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.service.CardService;

import java.sql.SQLException;
import java.util.Scanner;

import static br.com.dio.persistence.config.ConnectionConfig.getConnection;

public class CardBlockingMenu {
    private final BoardEntity entity;
    private final Scanner scanner;

    public CardBlockingMenu(BoardEntity entity, Scanner scanner) {
        this.entity = entity;
        this.scanner = scanner;
    }

    public void execute() throws SQLException {
        System.out.println("\n" + TerminalColors.BLUE_BOLD + "===== BLOQUEAR CARD =====" + TerminalColors.RESET);
        System.out.println("Informe o id do card que será bloqueado:");
        var cardId = scanner.nextLong();
        System.out.println("Informe o motivo do bloqueio:");
        var reason = scanner.next();
        var boardColumnsInfo = entity.getBoardColumns().stream()
                .map(bc -> new BoardColumnInfoDTO(bc.getId(), bc.getOrder(), bc.getKind()))
                .toList();
        try(var connection = getConnection()){
            new CardService(connection).block(cardId, reason, boardColumnsInfo);
            System.out.println(TerminalColors.success("Card bloqueado com sucesso!"));
        } catch (RuntimeException ex){
            System.out.println(TerminalColors.error(ex.getMessage()));
        }
    }
}