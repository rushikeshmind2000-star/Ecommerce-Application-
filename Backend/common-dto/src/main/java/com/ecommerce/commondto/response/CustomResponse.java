package com.ecommerce.commondto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Simple custom message response used for plain text confirmations.
 * Example: delete confirmation, email sent, etc.
 */
@Data
@AllArgsConstructor
public class CustomResponse {

    private String customMsg;
}
