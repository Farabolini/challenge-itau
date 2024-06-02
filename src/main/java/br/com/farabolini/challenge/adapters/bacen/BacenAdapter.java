package br.com.farabolini.challenge.adapters.bacen;

import br.com.farabolini.challenge.application.dtos.BankTransfer;

public interface BacenAdapter {

    void notify(BankTransfer bankTransfer);

}
