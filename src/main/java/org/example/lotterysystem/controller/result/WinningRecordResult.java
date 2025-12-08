package org.example.lotterysystem.controller.result;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class WinningRecordResult implements Serializable {
    private Long winnerId;
    private String winnerName;
    private String prizeName;
    private String prizeTier;
    private Date winningTime;
}
