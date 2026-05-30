package in.shivam.retaillite.common.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class ErrorResponse {
    private String error;
    private String message;
    private int status;
    private long timestamp;
}
