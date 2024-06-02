package br.com.farabolini.challenge.unit.adapters.customers;

import br.com.farabolini.challenge.adapters.accounts.AccountsAdapterImpl;
import br.com.farabolini.challenge.adapters.accounts.AccountsCommand;
import br.com.farabolini.challenge.adapters.customers.CustomersAdapterImpl;
import br.com.farabolini.challenge.adapters.customers.CustomersCommand;
import br.com.farabolini.challenge.application.dtos.AccountInfoResponse;
import br.com.farabolini.challenge.application.dtos.BankTransfer;
import br.com.farabolini.challenge.application.dtos.CustomerInfoResponse;
import br.com.farabolini.challenge.application.dtos.TransferInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class CustomerAdapterImplTest {

    @Mock
    private CustomersCommand customersCommand;

    @InjectMocks
    private CustomersAdapterImpl customersAdapter;

    @BeforeEach
    public void setup() {
        openMocks(this);
    }

    @Test
    public void shouldReturnCustomerInfo() {
        UUID customerId = UUID.randomUUID();
        CustomerInfoResponse customerInfo = new CustomerInfoResponse(customerId, "Name", "Phone", "Type");

        when(customersCommand.getCustomerInfo(customerId)).thenReturn(customerInfo);

        CustomerInfoResponse customerInfoResponse = customersAdapter.getCustomerInfo(customerId);
        assertEquals(customerInfo, customerInfoResponse);
    }

}
