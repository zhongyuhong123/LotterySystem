package org.example.lotterysystem.service.dto;

import lombok.Data;
import org.example.lotterysystem.service.enums.ActivityPrizeTierEnum;

import java.util.Date;

@Data
public class WinningRecordDTO {
    private Long winnerId;
    private String winnerName;
    private String prizeName;
    private ActivityPrizeTierEnum prizeTier;
    private Date winningTime;
}
