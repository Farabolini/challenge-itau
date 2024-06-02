package br.com.farabolini.challenge.adapters.customers;

import br.com.farabolini.challenge.application.dtos.CustomerInfoResponse;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@AllArgsConstructor
public class CustomersAdapterImpl implements CustomersAdapter {

    private final CustomersCommand customersCommand;

    @Override
    public CustomerInfoResponse getCustomerInfo(UUID customerId) {
        return customersCommand.getCustomerInfo(customerId);
    }
}
