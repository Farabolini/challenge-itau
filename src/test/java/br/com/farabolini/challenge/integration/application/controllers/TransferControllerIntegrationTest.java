package br.com.farabolini.challenge.integration.application.controllers;

import br.com.farabolini.challenge.application.dtos.AccountInfoResponse;
import br.com.farabolini.challenge.application.dtos.ApplicationErrorResponse;
import br.com.farabolini.challenge.application.dtos.BankTransfer;
import br.com.farabolini.challenge.application.dtos.CustomerInfoResponse;
import br.com.farabolini.challenge.application.dtos.TransferInfo;
import br.com.farabolini.challenge.application.dtos.TransferResponse;
import br.com.farabolini.challenge.domain.entities.Transfer;
import br.com.farabolini.challenge.infrastructure.configurations.WiremockConfig;
import br.com.farabolini.challenge.infrastructure.repositories.TransferRepository;
import br.com.farabolini.challenge.integration.TestContainersTest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(
        scripts = {"/scripts/drop_database.sql"}
)
@AutoConfigureMockMvc
public class TransferControllerIntegrationTest extends TestContainersTest {

    private static final String GET_CUSTOMER_ENDPOINT = "clientes/%s";
    private static final String GET_ACCOUNT_ENDPOINT = "contas/%s";
    private static final String UPDATE_BALANCE_ENDPOINT = "contas/saldos";
    private static final String NOTIFY_BACEN_ENDPOINT = "notificacoes";

    @Autowired
    private WiremockConfig wiremockConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TransferRepository transferRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockRestServiceServer mockServer;

    @BeforeEach
    public void setup() {
        mockServer = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(true).build();
        mockServer.reset();
    }

    @Test
    public void shouldCompleteTransfer() throws Exception {
        Instant before = Instant.now();
        UUID senderAccountId = UUID.randomUUID();
        UUID recipientAccountId = UUID.randomUUID();
        Double amount = 100.0;
        AccountInfoResponse senderAccount = new AccountInfoResponse(senderAccountId, UUID.randomUUID(), 1000.0, true, 100.0);
        CustomerInfoResponse senderInfo = new CustomerInfoResponse(senderAccount.customerId(), "Sender", "Phone", "PF");
        AccountInfoResponse recipientAccount = new AccountInfoResponse(recipientAccountId, UUID.randomUUID(), 0.0, true, 10.0);
        CustomerInfoResponse recipientInfo = new CustomerInfoResponse(recipientAccount.customerId(), "Sender", "Phone", "PF");
        BankTransfer request = new BankTransfer(UUID.randomUUID(), amount, new TransferInfo(senderAccountId, recipientAccountId));

        mockSuccessfulGetAccount(senderAccountId, senderAccount);
        mockSuccessfulGetCustomer(senderAccount.customerId(), senderInfo);
        mockSuccessfulGetAccount(recipientAccountId, recipientAccount);
        mockSuccessfulGetCustomer(recipientAccount.customerId(), recipientInfo);
        mockSuccessfulUpdateBalance(request);
        mockSuccessfulNotifyBACEN(request);

        String response = mockMvc.perform(post("/transferencia")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        TransferResponse transferResponse = objectMapper.readValue(response, TransferResponse.class);

        await().atMost(10, TimeUnit.SECONDS).until(() -> isBacenNotified(transferResponse.transferId()));

        Optional<Transfer> transfer = transferRepository.findById(transferResponse.transferId());
        assertFalse(transfer.isEmpty());
        assertEquals(senderAccountId, transfer.get().getSenderAccountId());
        assertEquals(recipientAccountId, transfer.get().getRecipientAccountId());
        assertEquals(senderAccount.customerId(), transfer.get().getSenderId());
        assertEquals(recipientAccount.customerId(), transfer.get().getRecipientId());
        assertEquals(amount, transfer.get().getAmount());
        assertTrue(transfer.get().getCreatedAt().isAfter(before));
        assertTrue(transfer.get().isBacenNotified());
    }

    @Test
    public void shouldReturnErrorResponseWhenSenderAccountIsNotFound() throws Exception {
        UUID senderAccountId = UUID.randomUUID();
        UUID recipientAccountId = UUID.randomUUID();
        Double amount = 100.0;
        AccountInfoResponse senderAccount = new AccountInfoResponse(senderAccountId, UUID.randomUUID(), 1000.0, true, 100.0);
        BankTransfer request = new BankTransfer(UUID.randomUUID(), amount, new TransferInfo(senderAccountId, recipientAccountId));

        mockErrorGetAccount(senderAccountId);

        String response = mockMvc.perform(post("/transferencia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()).andReturn().getResponse().getContentAsString();

        ApplicationErrorResponse errorResponse = objectMapper.readValue(response, ApplicationErrorResponse.class);
        assertEquals(404, errorResponse.code());
        assertEquals("Unable to retrieve account data", errorResponse.message());
    }

    @Test
    public void shouldReturnErrorResponseWhenSenderDataIsNotFound() throws Exception {
        Instant before = Instant.now();
        UUID senderAccountId = UUID.randomUUID();
        UUID recipientAccountId = UUID.randomUUID();
        Double amount = 100.0;
        AccountInfoResponse senderAccount = new AccountInfoResponse(senderAccountId, UUID.randomUUID(), 1000.0, true, 100.0);
        BankTransfer request = new BankTransfer(UUID.randomUUID(), amount, new TransferInfo(senderAccountId, recipientAccountId));

        mockSuccessfulGetAccount(senderAccountId, senderAccount);
        mockErrorGetCustomer(senderAccount.customerId());

        String response = mockMvc.perform(post("/transferencia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()).andReturn().getResponse().getContentAsString();

        ApplicationErrorResponse errorResponse = objectMapper.readValue(response, ApplicationErrorResponse.class);
        assertEquals(404, errorResponse.code());
        assertEquals("Unable to retrieve customer data", errorResponse.message());
        assertTrue(transferRepository.findAll().isEmpty());
    }

    @Test
    public void shouldReturnErrorResponseWhenRecipientAccountIsNotFound() throws Exception {
        UUID senderAccountId = UUID.randomUUID();
        UUID recipientAccountId = UUID.randomUUID();
        Double amount = 100.0;
        AccountInfoResponse senderAccount = new AccountInfoResponse(senderAccountId, UUID.randomUUID(), 1000.0, true, 100.0);
        CustomerInfoResponse senderInfo = new CustomerInfoResponse(senderAccount.customerId(), "Sender", "Phone", "PF");
        BankTransfer request = new BankTransfer(UUID.randomUUID(), amount, new TransferInfo(senderAccountId, recipientAccountId));

        mockSuccessfulGetAccount(senderAccountId, senderAccount);
        mockSuccessfulGetCustomer(senderAccount.customerId(), senderInfo);
        mockErrorGetAccount(recipientAccountId);

        String response = mockMvc.perform(post("/transferencia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()).andReturn().getResponse().getContentAsString();

        ApplicationErrorResponse errorResponse = objectMapper.readValue(response, ApplicationErrorResponse.class);
        assertEquals(404, errorResponse.code());
        assertEquals("Unable to retrieve account data", errorResponse.message());
        assertTrue(transferRepository.findAll().isEmpty());
    }

    @Test
    public void shouldReturnErrorResponseWhenRecipientDataIsNotFound() throws Exception {
        UUID senderAccountId = UUID.randomUUID();
        UUID recipientAccountId = UUID.randomUUID();
        Double amount = 100.0;
        AccountInfoResponse senderAccount = new AccountInfoResponse(senderAccountId, UUID.randomUUID(), 1000.0, true, 100.0);
        CustomerInfoResponse senderInfo = new CustomerInfoResponse(senderAccount.customerId(), "Sender", "Phone", "PF");
        AccountInfoResponse recipientAccount = new AccountInfoResponse(recipientAccountId, UUID.randomUUID(), 0.0, true, 10.0);
        BankTransfer request = new BankTransfer(UUID.randomUUID(), amount, new TransferInfo(senderAccountId, recipientAccountId));

        mockSuccessfulGetAccount(senderAccountId, senderAccount);
        mockSuccessfulGetCustomer(senderAccount.customerId(), senderInfo);
        mockSuccessfulGetAccount(recipientAccountId, recipientAccount);
        mockErrorGetCustomer(recipientAccount.customerId());

        String response = mockMvc.perform(post("/transferencia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()).andReturn().getResponse().getContentAsString();

        ApplicationErrorResponse errorResponse = objectMapper.readValue(response, ApplicationErrorResponse.class);
        assertEquals(404, errorResponse.code());
        assertEquals("Unable to retrieve customer data", errorResponse.message());
        assertTrue(transferRepository.findAll().isEmpty());
    }

    @Test
    public void shouldReturnErrorResponseWhenCouldNotUpdateBalance() throws Exception {
        UUID senderAccountId = UUID.randomUUID();
        UUID recipientAccountId = UUID.randomUUID();
        Double amount = 100.0;
        AccountInfoResponse senderAccount = new AccountInfoResponse(senderAccountId, UUID.randomUUID(), 1000.0, true, 100.0);
        CustomerInfoResponse senderInfo = new CustomerInfoResponse(senderAccount.customerId(), "Sender", "Phone", "PF");
        AccountInfoResponse recipientAccount = new AccountInfoResponse(recipientAccountId, UUID.randomUUID(), 0.0, true, 10.0);
        CustomerInfoResponse recipientInfo = new CustomerInfoResponse(recipientAccount.customerId(), "Sender", "Phone", "PF");
        BankTransfer request = new BankTransfer(UUID.randomUUID(), amount, new TransferInfo(senderAccountId, recipientAccountId));

        mockSuccessfulGetAccount(senderAccountId, senderAccount);
        mockSuccessfulGetCustomer(senderAccount.customerId(), senderInfo);
        mockSuccessfulGetAccount(recipientAccountId, recipientAccount);
        mockSuccessfulGetCustomer(recipientAccount.customerId(), recipientInfo);
        mockErrorUpdateBalance(request);

        String response = mockMvc.perform(post("/transferencia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError()).andReturn().getResponse().getContentAsString();

        ApplicationErrorResponse errorResponse = objectMapper.readValue(response, ApplicationErrorResponse.class);
        assertEquals(500, errorResponse.code());
        assertEquals("Unable to perform transfer", errorResponse.message());
        assertTrue(transferRepository.findAll().isEmpty());
    }

    @Test
    public void shouldNotReturnErrorResponseWhenBACENFailedOnFirstTryAndRetryAsync() throws Exception{
        Instant before = Instant.now();
        UUID senderAccountId = UUID.randomUUID();
        UUID recipientAccountId = UUID.randomUUID();
        Double amount = 100.0;
        AccountInfoResponse senderAccount = new AccountInfoResponse(senderAccountId, UUID.randomUUID(), 1000.0, true, 100.0);
        CustomerInfoResponse senderInfo = new CustomerInfoResponse(senderAccount.customerId(), "Sender", "Phone", "PF");
        AccountInfoResponse recipientAccount = new AccountInfoResponse(recipientAccountId, UUID.randomUUID(), 0.0, true, 10.0);
        CustomerInfoResponse recipientInfo = new CustomerInfoResponse(recipientAccount.customerId(), "Sender", "Phone", "PF");
        BankTransfer request = new BankTransfer(UUID.randomUUID(), amount, new TransferInfo(senderAccountId, recipientAccountId));

        mockSuccessfulGetAccount(senderAccountId, senderAccount);
        mockSuccessfulGetCustomer(senderAccount.customerId(), senderInfo);
        mockSuccessfulGetAccount(recipientAccountId, recipientAccount);
        mockSuccessfulGetCustomer(recipientAccount.customerId(), recipientInfo);
        mockSuccessfulUpdateBalance(request);
        mockBacenErrorFirstTryThenSuccess(request);

        String response = mockMvc.perform(post("/transferencia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        TransferResponse transferResponse = objectMapper.readValue(response, TransferResponse.class);

        await().atMost(10, TimeUnit.SECONDS).until(() -> isBacenNotified(transferResponse.transferId()));

        Optional<Transfer> transfer = transferRepository.findById(transferResponse.transferId());
        assertFalse(transfer.isEmpty());
        assertEquals(senderAccountId, transfer.get().getSenderAccountId());
        assertEquals(recipientAccountId, transfer.get().getRecipientAccountId());
        assertEquals(senderAccount.customerId(), transfer.get().getSenderId());
        assertEquals(recipientAccount.customerId(), transfer.get().getRecipientId());
        assertEquals(amount, transfer.get().getAmount());
        assertTrue(transfer.get().getCreatedAt().isAfter(before));
        assertTrue(transfer.get().isBacenNotified());
    }

    @Test
    public void shouldNotMarkBacenNotifiedAsTrueWhenNotificationFailed() throws Exception{
        Instant before = Instant.now();
        UUID senderAccountId = UUID.randomUUID();
        UUID recipientAccountId = UUID.randomUUID();
        Double amount = 100.0;
        AccountInfoResponse senderAccount = new AccountInfoResponse(senderAccountId, UUID.randomUUID(), 1000.0, true, 100.0);
        CustomerInfoResponse senderInfo = new CustomerInfoResponse(senderAccount.customerId(), "Sender", "Phone", "PF");
        AccountInfoResponse recipientAccount = new AccountInfoResponse(recipientAccountId, UUID.randomUUID(), 0.0, true, 10.0);
        CustomerInfoResponse recipientInfo = new CustomerInfoResponse(recipientAccount.customerId(), "Sender", "Phone", "PF");
        BankTransfer request = new BankTransfer(UUID.randomUUID(), amount, new TransferInfo(senderAccountId, recipientAccountId));

        mockSuccessfulGetAccount(senderAccountId, senderAccount);
        mockSuccessfulGetCustomer(senderAccount.customerId(), senderInfo);
        mockSuccessfulGetAccount(recipientAccountId, recipientAccount);
        mockSuccessfulGetCustomer(recipientAccount.customerId(), recipientInfo);
        mockSuccessfulUpdateBalance(request);
        mockBacenError(request);

        String response = mockMvc.perform(post("/transferencia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        TransferResponse transferResponse = objectMapper.readValue(response, TransferResponse.class);

        Optional<Transfer> transfer = transferRepository.findById(transferResponse.transferId());
        assertFalse(transfer.isEmpty());
        assertEquals(senderAccountId, transfer.get().getSenderAccountId());
        assertEquals(recipientAccountId, transfer.get().getRecipientAccountId());
        assertEquals(senderAccount.customerId(), transfer.get().getSenderId());
        assertEquals(recipientAccount.customerId(), transfer.get().getRecipientId());
        assertEquals(amount, transfer.get().getAmount());
        assertTrue(transfer.get().getCreatedAt().isAfter(before));
        assertFalse(transfer.get().isBacenNotified());
    }

    @Test
    public void shouldReturnErrorResponseWhenSenderAccountIsNotActive() throws Exception {
        UUID senderAccountId = UUID.randomUUID();
        UUID recipientAccountId = UUID.randomUUID();
        Double amount = 100.0;
        AccountInfoResponse senderAccount = new AccountInfoResponse(senderAccountId, UUID.randomUUID(), 1000.0, false, 100.0);
        CustomerInfoResponse senderInfo = new CustomerInfoResponse(senderAccount.customerId(), "Sender", "Phone", "PF");
        AccountInfoResponse recipientAccount = new AccountInfoResponse(recipientAccountId, UUID.randomUUID(), 0.0, true, 10.0);
        CustomerInfoResponse recipientInfo = new CustomerInfoResponse(recipientAccount.customerId(), "Sender", "Phone", "PF");
        BankTransfer request = new BankTransfer(UUID.randomUUID(), amount, new TransferInfo(senderAccountId, recipientAccountId));

        mockSuccessfulGetAccount(senderAccountId, senderAccount);
        mockSuccessfulGetCustomer(senderAccount.customerId(), senderInfo);
        mockSuccessfulGetAccount(recipientAccountId, recipientAccount);
        mockSuccessfulGetCustomer(recipientAccount.customerId(), recipientInfo);

        String response = mockMvc.perform(post("/transferencia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();

        ApplicationErrorResponse errorResponse = objectMapper.readValue(response, ApplicationErrorResponse.class);
        assertEquals(400, errorResponse.code());
        assertEquals("Sender account is inactive", errorResponse.message());
        assertTrue(transferRepository.findAll().isEmpty());
    }

    @Test
    public void shouldReturnErrorResponseWhenRecipientAccountIsNotActive() throws Exception {
        UUID senderAccountId = UUID.randomUUID();
        UUID recipientAccountId = UUID.randomUUID();
        Double amount = 100.0;
        AccountInfoResponse senderAccount = new AccountInfoResponse(senderAccountId, UUID.randomUUID(), 1000.0, true, 100.0);
        CustomerInfoResponse senderInfo = new CustomerInfoResponse(senderAccount.customerId(), "Sender", "Phone", "PF");
        AccountInfoResponse recipientAccount = new AccountInfoResponse(recipientAccountId, UUID.randomUUID(), 0.0, false, 10.0);
        CustomerInfoResponse recipientInfo = new CustomerInfoResponse(recipientAccount.customerId(), "Sender", "Phone", "PF");
        BankTransfer request = new BankTransfer(UUID.randomUUID(), amount, new TransferInfo(senderAccountId, recipientAccountId));

        mockSuccessfulGetAccount(senderAccountId, senderAccount);
        mockSuccessfulGetCustomer(senderAccount.customerId(), senderInfo);
        mockSuccessfulGetAccount(recipientAccountId, recipientAccount);
        mockSuccessfulGetCustomer(recipientAccount.customerId(), recipientInfo);

        String response = mockMvc.perform(post("/transferencia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();

        ApplicationErrorResponse errorResponse = objectMapper.readValue(response, ApplicationErrorResponse.class);
        assertEquals(400, errorResponse.code());
        assertEquals("Recipient account is inactive", errorResponse.message());
        assertTrue(transferRepository.findAll().isEmpty());
    }

    @Test
    public void shouldReturnErrorResponseWhenSenderExceededDailyLimit() throws Exception {
        UUID senderAccountId = UUID.randomUUID();
        UUID recipientAccountId = UUID.randomUUID();
        Double amount = 100.0;
        AccountInfoResponse senderAccount = new AccountInfoResponse(senderAccountId, UUID.randomUUID(), 1000.0, true, 10.0);
        CustomerInfoResponse senderInfo = new CustomerInfoResponse(senderAccount.customerId(), "Sender", "Phone", "PF");
        AccountInfoResponse recipientAccount = new AccountInfoResponse(recipientAccountId, UUID.randomUUID(), 0.0, true, 10.0);
        CustomerInfoResponse recipientInfo = new CustomerInfoResponse(recipientAccount.customerId(), "Sender", "Phone", "PF");
        BankTransfer request = new BankTransfer(UUID.randomUUID(), amount, new TransferInfo(senderAccountId, recipientAccountId));

        mockSuccessfulGetAccount(senderAccountId, senderAccount);
        mockSuccessfulGetCustomer(senderAccount.customerId(), senderInfo);
        mockSuccessfulGetAccount(recipientAccountId, recipientAccount);
        mockSuccessfulGetCustomer(recipientAccount.customerId(), recipientInfo);

        String response = mockMvc.perform(post("/transferencia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();

        ApplicationErrorResponse errorResponse = objectMapper.readValue(response, ApplicationErrorResponse.class);
        assertEquals(400, errorResponse.code());
        assertEquals("Unable to perform transfer, daily limit exceeded. Current daily limit: 10.00", errorResponse.message());
        assertTrue(transferRepository.findAll().isEmpty());
    }

    @Test
    public void shouldReturnErrorResponseWhenSenderHasNotSufficientBalance() throws Exception {
        UUID senderAccountId = UUID.randomUUID();
        UUID recipientAccountId = UUID.randomUUID();
        Double amount = 100.0;
        AccountInfoResponse senderAccount = new AccountInfoResponse(senderAccountId, UUID.randomUUID(), 1.0, true, 100.0);
        CustomerInfoResponse senderInfo = new CustomerInfoResponse(senderAccount.customerId(), "Sender", "Phone", "PF");
        AccountInfoResponse recipientAccount = new AccountInfoResponse(recipientAccountId, UUID.randomUUID(), 0.0, true, 10.0);
        CustomerInfoResponse recipientInfo = new CustomerInfoResponse(recipientAccount.customerId(), "Sender", "Phone", "PF");
        BankTransfer request = new BankTransfer(UUID.randomUUID(), amount, new TransferInfo(senderAccountId, recipientAccountId));

        mockSuccessfulGetAccount(senderAccountId, senderAccount);
        mockSuccessfulGetCustomer(senderAccount.customerId(), senderInfo);
        mockSuccessfulGetAccount(recipientAccountId, recipientAccount);
        mockSuccessfulGetCustomer(recipientAccount.customerId(), recipientInfo);

        String response = mockMvc.perform(post("/transferencia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest()).andReturn().getResponse().getContentAsString();

        ApplicationErrorResponse errorResponse = objectMapper.readValue(response, ApplicationErrorResponse.class);
        assertEquals(400, errorResponse.code());
        assertEquals("Insufficient balance to perform transfer, current balance: 1.00", errorResponse.message());
        assertTrue(transferRepository.findAll().isEmpty());
    }

    @Test
    public void shouldNotAllowDuplicateTransfers() throws Exception {
        UUID senderAccountId = UUID.randomUUID();
        UUID recipientAccountId = UUID.randomUUID();
        Double amount = 100.0;
        AccountInfoResponse senderAccount = new AccountInfoResponse(senderAccountId, UUID.randomUUID(), 1000.0, true, 100.0);
        CustomerInfoResponse senderInfo = new CustomerInfoResponse(senderAccount.customerId(), "Sender", "Phone", "PF");
        AccountInfoResponse recipientAccount = new AccountInfoResponse(recipientAccountId, UUID.randomUUID(), 0.0, true, 10.0);
        CustomerInfoResponse recipientInfo = new CustomerInfoResponse(recipientAccount.customerId(), "Sender", "Phone", "PF");
        BankTransfer request = new BankTransfer(UUID.randomUUID(), amount, new TransferInfo(senderAccountId, recipientAccountId));

        mockSuccessfulGetAccount(senderAccountId, senderAccount);
        mockSuccessfulGetCustomer(senderAccount.customerId(), senderInfo);
        mockSuccessfulGetAccount(recipientAccountId, recipientAccount);
        mockSuccessfulGetCustomer(recipientAccount.customerId(), recipientInfo);
        mockSuccessfulUpdateBalance(request);
        mockSuccessfulNotifyBACEN(request);

        Thread virtualThread = Thread.ofVirtual().start(() -> {
            try {
                mockMvc.perform(post("/transferencia")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            } catch (Exception e) {
                // Do nothing
            }
        });

        Thread otherVirtualThread = Thread.ofVirtual().start(() -> {
            try {
                Thread.sleep(10);
                String response = mockMvc.perform(post("/transferencia")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isConflict()).andReturn().getResponse().getContentAsString();

                ApplicationErrorResponse errorResponse = objectMapper.readValue(response, ApplicationErrorResponse.class);
                assertEquals(409, errorResponse.code());
                assertEquals("Possible duplicity on transfer, do you really want to continue?", errorResponse.message());
            } catch (Exception e) {
                // Do nothing
            }
        });

        await().atMost(10, TimeUnit.SECONDS).until(() -> transferRepository.findAll().size() == 1);
        virtualThread.join();
        otherVirtualThread.join();

        assertEquals(1, transferRepository.findAll().size());
    }

    private boolean isBacenNotified(UUID transferId) {
        Optional<Transfer> transfer = transferRepository.findById(transferId);
        return transfer.isPresent() && transfer.get().isBacenNotified();
    }

    private void mockBacenError(BankTransfer body) throws JsonProcessingException {
        mockServer.expect(ExpectedCount.manyTimes(), requestTo(getUrl(NOTIFY_BACEN_ENDPOINT)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(objectMapper.writeValueAsString(body)))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));
    }

    private void mockBacenErrorFirstTryThenSuccess(BankTransfer body) throws JsonProcessingException {
        mockServer.expect(requestTo(getUrl(NOTIFY_BACEN_ENDPOINT)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(objectMapper.writeValueAsString(body)))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS));

        mockServer.expect(requestTo(getUrl(NOTIFY_BACEN_ENDPOINT)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(objectMapper.writeValueAsString(body)))
                .andRespond(withStatus(HttpStatus.OK));
    }

    private void mockErrorUpdateBalance(BankTransfer body) throws JsonProcessingException {
        mockServer.expect(requestTo(getUrl(UPDATE_BALANCE_ENDPOINT)))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(content().json(objectMapper.writeValueAsString(body)))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));
    }

    private void mockErrorGetCustomer(UUID customerId) throws JsonProcessingException {
        mockServer.expect(requestTo(getUrl(GET_CUSTOMER_ENDPOINT.formatted(customerId))))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));
    }

    private void mockErrorGetAccount(UUID accountId) {
        mockServer.expect(requestTo(getUrl(GET_ACCOUNT_ENDPOINT.formatted(accountId))))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));
    }

    private void mockSuccessfulNotifyBACEN(BankTransfer body) throws JsonProcessingException {
        mockServer.expect(requestTo(getUrl(NOTIFY_BACEN_ENDPOINT)))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().json(objectMapper.writeValueAsString(body)))
                .andRespond(withStatus(HttpStatus.OK));
    }

    private void mockSuccessfulUpdateBalance(BankTransfer body) throws JsonProcessingException {
        mockServer.expect(requestTo(getUrl(UPDATE_BALANCE_ENDPOINT)))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(content().json(objectMapper.writeValueAsString(body)))
                .andRespond(withStatus(HttpStatus.OK));
    }

    private void mockSuccessfulGetCustomer(UUID customerId, CustomerInfoResponse response) throws JsonProcessingException {
        mockServer.expect(requestTo(getUrl(GET_CUSTOMER_ENDPOINT.formatted(customerId))))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON).body(objectMapper.writeValueAsString(response)));
    }

    private void mockSuccessfulGetAccount(UUID accountId, AccountInfoResponse response) throws JsonProcessingException {
        mockServer.expect(requestTo(getUrl(GET_ACCOUNT_ENDPOINT.formatted(accountId))))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON).body(objectMapper.writeValueAsString(response)));
    }

    private String getUrl(String endpoint) {
        return "%s/%s".formatted(wiremockConfig.getHost(), endpoint);
    }

}
