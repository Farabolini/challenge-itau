package br.com.farabolini.challenge.adapters.customers;

import br.com.farabolini.challenge.application.dtos.CustomerInfoResponse;

import java.util.UUID;

public interface CustomersAdapter {

    CustomerInfoResponse getCustomerInfo(UUID customerId);

}
