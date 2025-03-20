package br.com.dio.ui;
import br.com.dio.ui.util.TerminalColors;

import br.com.dio.dto.BoardColumnInfoDTO;
import br.com.dio.persistence.entity.BoardEntity;
import br.com.dio.service.CardService;

import java.sql.SQLException;
import java.util.Scanner;

import static br.com.dio.persistence.config.ConnectionConfig.getConnection;

public class CardMovementMenu {
    private final BoardEntity entity;
    private final Scanner scanner;

    public CardMovementMenu(BoardEntity entity, Scanner scanner) {
        this.entity = entity;
        this.scanner = scanner;
    }

    public void execute() throws SQLException {
        System.out.println("\n" + TerminalColors.BLUE_BOLD + "===== MOVER CARD PARA PRÓXIMA COLUNA =====" + TerminalColors.RESET);
        System.out.println("Informe o id do card que deseja mover:");
        var cardId = scanner.nextLong();
        var boardColumnsInfo = entity.getBoardColumns().stream()
                .map(bc -> new BoardColumnInfoDTO(bc.getId(), bc.getOrder(), bc.getKind()))
                .toList();
        try(var connection = getConnection()){
            new CardService(connection).moveToNextColumn(cardId, boardColumnsInfo);
            System.out.println(TerminalColors.success("Card movido com sucesso!"));
        } catch (RuntimeException ex){
            System.out.println(TerminalColors.error(ex.getMessage()));
        }
    }
}